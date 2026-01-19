package io.github.techtastic.realtimesync.systems;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.builtin.asseteditor.util.AssetStoreUtil;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.systems.player.EnterBedSystem;
import com.hypixel.hytale.builtin.beds.sleep.systems.world.StartSlumberSystem;
import com.hypixel.hytale.builtin.beds.sleep.systems.world.UpdateWorldSlumberSystem;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.modules.time.*;
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
    protected static boolean hasMoreMoonPhases() {
        AssetPack pack = AssetModule.get().getAssetPack("TechTastic:MoreMoonPhases");
        return pack != null;
    }

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
                case Phase.WANING_GIBBOUS -> hasMoreMoonPhases() ? 5 : 1;
                case Phase.LAST_QUARTER -> hasMoreMoonPhases() ? 6 : 2;
                case Phase.WANING_CRESCENT -> hasMoreMoonPhases() ? 7 : 3;
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
            long worldMillis = worldTimeResource.getGameTime().toEpochMilli();
            long realMillis = getRealTime(RealTimeSyncConfig.getConfig(world).getTimezone()).toEpochMilli();
            long lerpedMillis = (long) (worldMillis + ((realMillis - worldMillis) * dt));
            Instant lerpedTime = Instant.ofEpochMilli(lerpedMillis);
            worldTimeResource.setGameTime0(lerpedTime);
        }

        @Override
        public @NonNull Set<Dependency<EntityStore>> getDependencies() {
            return Set.of(new SystemDependency<>(Order.AFTER, WorldTimeSystems.Ticking.class));
        }
    }

    public static class Time extends TickingSystem<EntityStore> {
        public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
            World world = store.getExternalData().getWorld();
            TimeResource timeResource = store.getResource(TimeModule.get().getTimeResourceType());
            long worldMillis = timeResource.getNow().toEpochMilli();
            long realMillis = getRealTime(RealTimeSyncConfig.getConfig(world).getTimezone()).toEpochMilli();
            long lerpedMillis = (long) (worldMillis + ((realMillis - worldMillis) * dt));
            Instant lerpedTime = Instant.ofEpochMilli(lerpedMillis);
            store.getResource(TimeModule.get().getTimeResourceType()).setNow(lerpedTime);
        }

        @Override
        public @NonNull Set<Dependency<EntityStore>> getDependencies() {
            return Set.of(new SystemDependency<>(Order.AFTER, TimeSystem.class));
        }
    }

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
