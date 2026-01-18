package io.github.techtastic.realtimesync.systems;

import com.hypixel.hytale.builtin.weather.systems.WeatherSystem;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.modules.time.TimeModule;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.modules.time.WorldTimeSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.techtastic.realtimesync.config.RealTimeSyncConfig;
import org.jspecify.annotations.NonNull;
import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPhase.Phase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

public class RealTimeSystems {
    protected static Phase getMoonPhase(Instant time) {
        return MoonIllumination.compute().on(time).execute().getClosestPhase();
    }

    protected static int getGameMoonPhase(Phase phase) {
        return switch (phase) {
            case Phase.FULL_MOON -> 0;
            case Phase.WAXING_GIBBOUS  -> 1;
            case Phase.FIRST_QUARTER -> 2;
            case Phase.WAXING_CRESCENT -> 3;
            case Phase.NEW_MOON -> 4;
            default -> switch (phase) { // TODO: Special Handlers Later
                case Phase.WANING_GIBBOUS -> 1;
                case Phase.LAST_QUARTER -> 2;
                case Phase.WANING_CRESCENT -> 3;
                default -> 0;
            };
        };
    }

    protected static Instant getRealTime(@Nullable String timezone) {
        ZonedDateTime zonedDateTime;
        if (timezone == null) {
            zonedDateTime = ZonedDateTime.now();
        } else {
            ZoneId zone = ZoneId.of("America/New_York");
            zonedDateTime = ZonedDateTime.now(zone);
        }
        return zonedDateTime.toInstant();
    }

    public static class Init extends StoreSystem<EntityStore> {
        public void onSystemAddedToStore(@Nonnull Store<EntityStore> store) {
            World world = store.getExternalData().getWorld();
            WorldTimeResource worldTimeResource = store.getResource(TimeModule.get().getWorldTimeResourceType());
            Instant realTime = getRealTime(RealTimeSyncConfig.getConfig(world).getTimezone());
            worldTimeResource.setGameTime0(realTime);
            world.execute(() -> worldTimeResource.setMoonPhase(getGameMoonPhase(getMoonPhase(realTime)), store));
        }

        public void onSystemRemovedFromStore(@Nonnull Store<EntityStore> store) {
            World world = store.getExternalData().getWorld();
            WorldConfig worldConfig = world.getWorldConfig();
            worldConfig.setGameTime(getRealTime(RealTimeSyncConfig.getConfig(world).getTimezone()));
            worldConfig.markChanged();
        }

        @Override
        public @NonNull Set<Dependency<EntityStore>> getDependencies() {
            return Set.of(new SystemDependency<>(Order.AFTER, WorldTimeSystems.Init.class));
        }
    }

    public static class Ticking extends TickingSystem<EntityStore> {
        public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
            World world = store.getExternalData().getWorld();
            WorldTimeResource worldTimeResource = store.getResource(TimeModule.get().getWorldTimeResourceType());
            worldTimeResource.setGameTime0(getRealTime(RealTimeSyncConfig.getConfig(world).getTimezone()));
        }

        @Override
        public @NonNull Set<Dependency<EntityStore>> getDependencies() {
            return Set.of(new SystemDependency<>(Order.AFTER, WorldTimeSystems.Ticking.class));
        }
    }
}
