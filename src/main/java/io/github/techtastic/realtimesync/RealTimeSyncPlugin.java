package io.github.techtastic.realtimesync;

import com.hypixel.hytale.component.event.WorldEventType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.techtastic.realtimesync.config.RealTimeSyncConfig;
import io.github.techtastic.realtimesync.event.ecs.RealTimeMoonPhaseChangeEvent;
import io.github.techtastic.realtimesync.systems.RealTimeSystems;

/**
 * This class is the main class form the Real Time Sync plugin.
 */
public class RealTimeSyncPlugin extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static RealTimeSyncPlugin instance;

    /**
     * The {@link WorldEventType} used for {@link RealTimeMoonPhaseChangeEvent}
     */
    public final WorldEventType<EntityStore, RealTimeMoonPhaseChangeEvent> realTimeMoonPhaseChangeEventType;

    public RealTimeSyncPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());

        instance = this;
        this.getCodecRegistry(WorldConfig.PLUGIN_CODEC).register(RealTimeSyncConfig.class, "RealTimeSync", RealTimeSyncConfig.CODEC);
        this.realTimeMoonPhaseChangeEventType = this.getEntityStoreRegistry().registerWorldEventType(RealTimeMoonPhaseChangeEvent.class);
    }

    @Override
    protected void setup() {
        this.getEntityStoreRegistry().registerSystem(new RealTimeSystems.Init());
        this.getEntityStoreRegistry().registerSystem(new RealTimeSystems.Ticking());
        this.getEntityStoreRegistry().registerSystem(new RealTimeSystems.Time());
        this.getEntityStoreRegistry().registerSystem(new RealTimeSystems.NoSleep());
    }

    /**
     * This method gets the instance of the loaded {@link RealTimeSyncPlugin}.
     *
     * @return The loaded {@link RealTimeSyncPlugin}
     */
    public static RealTimeSyncPlugin get() {
        return instance;
    }
}