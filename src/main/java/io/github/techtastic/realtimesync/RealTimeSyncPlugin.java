package io.github.techtastic.realtimesync;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import io.github.techtastic.realtimesync.config.RealTimeSyncConfig;
import io.github.techtastic.realtimesync.systems.RealTimeSystems;

public class RealTimeSyncPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public RealTimeSyncPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
        this.getCodecRegistry(WorldConfig.PLUGIN_CODEC).register(RealTimeSyncConfig.class, "RealTimeSync", RealTimeSyncConfig.CODEC);
    }

    @Override
    protected void setup() {
        this.getEntityStoreRegistry().registerSystem(new RealTimeSystems.Init());
        this.getEntityStoreRegistry().registerSystem(new RealTimeSystems.Ticking());
    }
}
