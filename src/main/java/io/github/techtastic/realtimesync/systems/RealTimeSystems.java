package io.github.techtastic.realtimesync.systems;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

public class RealTimeSystems {
    protected static RealTimeSyncConfig getConfig(World world) {
        return world.getWorldConfig().getPluginConfig().computeIfAbsent(RealTimeSyncConfig.class, c -> new RealTimeSyncConfig(null));
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
            worldTimeResource.setGameTime0(getRealTime(getConfig(world).getTimezone()));
            world.execute(() -> worldTimeResource.updateMoonPhase(world, store));
        }

        public void onSystemRemovedFromStore(@Nonnull Store<EntityStore> store) {
            World world = store.getExternalData().getWorld();
            WorldConfig worldConfig = world.getWorldConfig();
            worldConfig.setGameTime(getRealTime(getConfig(world).getTimezone()));
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
            worldTimeResource.setGameTime0(getRealTime(getConfig(world).getTimezone()));
        }
    }
}
