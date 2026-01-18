package io.github.techtastic.realtimesync.systems;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.weather.components.WeatherTracker;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.environment.config.WeatherForecast;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.openmeteo.sdk.WeatherApiResponse;
import io.github.techtastic.realtimesync.RealTimeSyncPlugin;
import io.github.techtastic.realtimesync.config.RealTimeSyncConfig;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class RealWeatherSystems {
    protected static String getAPIKey(World world) {
        return RealTimeSyncConfig.getConfig(world).getOpenMeteoAPIKey();
    }

    public static class WorldAddedSystem extends StoreSystem<EntityStore> {
        @Override
        public void onSystemAddedToStore(@NonNull Store<EntityStore> store) {
            AtomicReference<StringBuilder> envs = new AtomicReference<>(new StringBuilder().append("\n"));
            Environment.getAssetMap().getAssetMap().keySet().forEach(w -> envs.set(envs.get().append("- ").append(w).append("\n")));
            RealTimeSyncPlugin.LOGGER.atInfo().log(envs.get().toString());


            Set<String> weather = Weather.getAssetMap().getAssetMap().keySet();
            AtomicReference<StringBuilder> str = new AtomicReference<>(new StringBuilder().append("\n### Zone 1:\n"));
            weather.stream().filter(s -> s.contains("Zone1")).forEach(w -> str.set(str.get().append("- ").append(w).append("\n")));
            str.set(str.get().append("\n### Zone 2:\n"));
            weather.stream().filter(s -> s.contains("Zone2")).forEach(w -> str.set(str.get().append("- ").append(w).append("\n")));
            str.set(str.get().append("\n### Zone 3:\n"));
            weather.stream().filter(s -> s.contains("Zone3")).forEach(w -> str.set(str.get().append("- ").append(w).append("\n")));
            str.set(str.get().append("\n### Zone 4:\n"));
            weather.stream().filter(s -> s.contains("Zone4")).forEach(w -> str.set(str.get().append("- ").append(w).append("\n")));
            RealTimeSyncPlugin.LOGGER.atInfo().log(str.get().toString());
        }

        @Override
        public void onSystemRemovedFromStore(@NonNull Store<EntityStore> store) {}
    }

    public static class TickingSystem extends EntityTickingSystem<EntityStore> {
        private static final ComponentType<EntityStore, PlayerRef> PLAYER_REF_COMPONENT_TYPE;
        private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE;
        private static final ComponentType<EntityStore, WeatherTracker> WEATHER_TRACKER_COMPONENT_TYPE;
        private static final ResourceType<EntityStore, WeatherResource> WEATHER_RESOURCE_TYPE;
        private static final Query<EntityStore> QUERY;

        @Override
        public Query<EntityStore> getQuery() {
            return QUERY;
        }

        @Override
        public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
            String apiKey = getAPIKey(store.getExternalData().getWorld());
            if (apiKey == null) return;

            WeatherResource weatherResource = store.getResource(WEATHER_RESOURCE_TYPE);
            if (weatherResource.consumeForcedWeatherChange()) {
                weatherResource.playerUpdateDelay = 1.0F;
                store.tick(this, dt, systemIndex);
            } else {
                if (weatherResource.getForcedWeatherIndex() == 0) {
                    WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
                    int currentHour = worldTimeResource.getCurrentHour();
                    if (weatherResource.compareAndSwapHour(currentHour)) {
                        Int2IntMap environmentWeather = weatherResource.getEnvironmentWeather();
                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        IndexedLookupTableAssetMap<String, Environment> assetMap = Environment.getAssetMap();

                        for(Map.Entry<String, Environment> entry : assetMap.getAssetMap().entrySet()) {
                            String key = entry.getKey();
                            int index = assetMap.getIndex(key);
                            if (index == Integer.MIN_VALUE) {
                                throw new IllegalArgumentException("Unknown key! " + key);
                            }

                            IWeightedMap<WeatherForecast> weatherForecast = entry.getValue().getWeatherForecast(currentHour);
                            //weatherForecast.get()


                            int selectedWeatherIndex = weatherForecast.get(random).getWeatherIndex();
                            environmentWeather.put(index, selectedWeatherIndex);
                        }
                    }
                }

                weatherResource.playerUpdateDelay -= dt;
                if (weatherResource.playerUpdateDelay <= 0.0F) {
                    weatherResource.playerUpdateDelay = 1.0F;
                    store.tick(this, dt, systemIndex);
                }

            }
        }

        @Override
        public void tick(float v, int i, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {}

        static {
            PLAYER_REF_COMPONENT_TYPE = PlayerRef.getComponentType();
            TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
            WEATHER_TRACKER_COMPONENT_TYPE = WeatherTracker.getComponentType();
            WEATHER_RESOURCE_TYPE = WeatherResource.getResourceType();
            QUERY = Archetype.of(PLAYER_REF_COMPONENT_TYPE, TRANSFORM_COMPONENT_TYPE, WEATHER_TRACKER_COMPONENT_TYPE);
        }
    }
}
