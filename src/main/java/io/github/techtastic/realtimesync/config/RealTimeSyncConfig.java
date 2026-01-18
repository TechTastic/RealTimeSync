package io.github.techtastic.realtimesync.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nullable;
import java.time.ZoneId;

public class RealTimeSyncConfig {
    private String timezone;
    private String openMeteoAPI;

    public RealTimeSyncConfig(@Nullable String timezone, @Nullable String openMeteoAPI) {
        if (timezone != null && timezone.isEmpty())
            this.timezone = null;
        else
            this.timezone = timezone;

        if (openMeteoAPI != null && openMeteoAPI.isEmpty())
            this.openMeteoAPI = null;
        else
            this.openMeteoAPI = openMeteoAPI;
    }

    private RealTimeSyncConfig() {
        this(null, null);
    }

    @Nullable
    public String getTimezone() {
        return this.timezone;
    }

    @Nullable
    public String getOpenMeteoAPIKey() {
        return this.openMeteoAPI;
    }

    public void setTimezone(@Nullable String timezone) {
        if (timezone != null && timezone.isEmpty())
            this.timezone = null;
        else
            this.timezone = timezone;
    }

    public void setOpenMeteoAPIKey(@Nullable String openMeteoAPI) {
        if (openMeteoAPI != null && openMeteoAPI.isEmpty())
            this.openMeteoAPI = null;
        else
            this.openMeteoAPI = openMeteoAPI;
    }

    public static RealTimeSyncConfig getConfig(World world) {
        return world.getWorldConfig().getPluginConfig().computeIfAbsent(RealTimeSyncConfig.class, c ->
                new RealTimeSyncConfig(null, null));
    }

    public static final BuilderCodec<RealTimeSyncConfig> CODEC =
            BuilderCodec.builder(RealTimeSyncConfig.class, RealTimeSyncConfig::new)
                    .addField(
                            new KeyedCodec<>("Timezone", Codec.STRING),
                            RealTimeSyncConfig::setTimezone,
                            RealTimeSyncConfig::getTimezone
                    ).addField(
                            new KeyedCodec<>("OpenMeteoAPI", Codec.STRING),
                            RealTimeSyncConfig::setOpenMeteoAPIKey,
                            RealTimeSyncConfig::getOpenMeteoAPIKey
                    ).validator( (c, r) -> {
                        if (c.getTimezone() == null) return;
                        try {
                            ZoneId.of(c.getTimezone());
                        } catch (Exception e) {
                            r.warn(e.getLocalizedMessage());
                        }
                    } )
                    .build();
}
