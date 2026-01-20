package io.github.techtastic.realtimesync.systems;

import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.modules.time.*;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.techtastic.realtimesync.RealTimeSyncPlugin;
import io.github.techtastic.realtimesync.config.RealTimeSyncConfig;
import io.github.techtastic.realtimesync.event.ecs.RealTimeMoonPhaseChangeEvent;
import io.github.techtastic.realtimesync.util.MoonUtil;
import org.jspecify.annotations.NonNull;
import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPhase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * This class holds a series of systems used to synchronize game time with real time.
 * It also contains a system to prevent skipping the night.
 */
public class RealTimeSystems {
    /**
     * This method creates an {@link Instant} based off the provided timezone string which should be parsible by {@link ZoneId}.
     *
     * @param timezone
     * @return The matching {@link Instant}
     */
    protected static Instant getRealTime(@Nullable String timezone) {
        ZonedDateTime zonedDateTime;
        if (timezone == null) {
            zonedDateTime = ZonedDateTime.now();
        } else {
            ZoneId zone = ZoneId.of(timezone);
            zonedDateTime = ZonedDateTime.now(zone);
        }
        return zonedDateTime.toInstant();
    }

    /**
     * This system is used to set both the game time to real time as well as change the given moon phase to the real life moon phase.
     * This system also invokes the {@link RealTimeMoonPhaseChangeEvent} to notify other systems of the new values and phase.
     */
    public static class Init extends StoreSystem<EntityStore> {
        public void onSystemAddedToStore(@Nonnull Store<EntityStore> store) {
            World world = store.getExternalData().getWorld();
            WorldTimeResource worldTimeResource = store.getResource(TimeModule.get().getWorldTimeResourceType());
            Instant realTime = getRealTime(RealTimeSyncConfig.getConfig(world).getTimezone());
            worldTimeResource.setGameTime0(realTime);
            world.execute(() -> {
                MoonIllumination moonIllumination = MoonUtil.getMoonIllumination(realTime);
                MoonPhase.Phase moonPhase = moonIllumination.getClosestPhase();
                int gameMoonPhase = MoonUtil.getGameMoonPhase(moonPhase);
                worldTimeResource.setMoonPhase(gameMoonPhase, store);
                store.invoke(RealTimeSyncPlugin.get().realTimeMoonPhaseChangeEventType, new RealTimeMoonPhaseChangeEvent(realTime));
            });
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

    /**
     * This system is used to set the current game time to the real time per tick using the {@link WorldTimeResource}.
     */
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

    /**
     * This system is used to set the current game time to the real time per tick using the {@link TimeResource}.
     */
    public static class Time extends TickingSystem<EntityStore> {
        public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
            World world = store.getExternalData().getWorld();
            TimeResource timeResource = store.getResource(TimeModule.get().getTimeResourceType());
            timeResource.setNow(getRealTime(RealTimeSyncConfig.getConfig(world).getTimezone()));
        }

        @Override
        public @NonNull Set<Dependency<EntityStore>> getDependencies() {
            return Set.of(new SystemDependency<>(Order.AFTER, TimeSystem.class));
        }
    }

    /**
     * This system is used to prevent players from falling asleep and skipping the night.
     * This is necessary as it will, infact, skip the night and break our real time updates.
     */
    public static class NoSleep extends RefChangeSystem<EntityStore, PlayerSomnolence> {
        @Override
        public @NonNull ComponentType<EntityStore, PlayerSomnolence> componentType() {
            return PlayerSomnolence.getComponentType();
        }

        @Override
        public void onComponentAdded(@NonNull Ref<EntityStore> ref, @NonNull PlayerSomnolence playerSomnolence, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {}

        @Override
        public void onComponentSet(@NonNull Ref<EntityStore> ref, @Nullable PlayerSomnolence playerSomnolence, @NonNull PlayerSomnolence t1, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {
            PlayerSomnolence somnolence = store.getComponent(ref, PlayerSomnolence.getComponentType());
            World world = store.getExternalData().getWorld();
            if (somnolence != null && !(somnolence.getSleepState() instanceof PlayerSleep.FullyAwake))
                world.execute(() -> world.getEntityStore().getStore().putComponent(ref, PlayerSomnolence.getComponentType(), new PlayerSomnolence()));
        }

        @Override
        public void onComponentRemoved(@NonNull Ref<EntityStore> ref, @NonNull PlayerSomnolence playerSomnolence, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {}

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return PlayerSomnolence.getComponentType();
        }
    }
}
