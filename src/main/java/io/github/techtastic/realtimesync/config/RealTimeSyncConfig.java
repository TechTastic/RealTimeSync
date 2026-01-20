package io.github.techtastic.realtimesync.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nullable;
import java.time.ZoneId;

/**
 * The configuration class used by the {@link io.github.techtastic.realtimesync.systems.RealTimeSystems} and gotten from {@link com.hypixel.hytale.server.core.universe.world.WorldConfig} to determine the timezone to be used.
 */
public class RealTimeSyncConfig {
    private String timezone;

    public RealTimeSyncConfig(@Nullable String timezone) {
        if (timezone != null && timezone.isEmpty())
            this.timezone = null;
        else
            this.timezone = timezone;
    }

    private RealTimeSyncConfig() {
        this(null);
    }

    /**
     * This method gets either the {@link ZoneId} formatted timezone string or null.
     *
     * @return Either the {@link ZoneId} formatted timezone string or null
     */
    @Nullable
    public String getTimezone() {
        return this.timezone;
    }

    /**
     * This method sets the timezone string.
     *
     * @param timezone Either the {@link ZoneId} formatted timezone string or null
     */
    public void setTimezone(@Nullable String timezone) {
        if (timezone != null && timezone.isEmpty())
            this.timezone = null;
        else
            this.timezone = timezone;
    }

    /**
     * This is a helper method for extracting the configuration class instance from the {@link World}
     *
     * @param world The {@link World}
     * @return The configuration class instance from the {@link World}
     */
    public static RealTimeSyncConfig getConfig(World world) {
        return world.getWorldConfig().getPluginConfig().computeIfAbsent(RealTimeSyncConfig.class, c ->
                new RealTimeSyncConfig(null));
    }

    public static final BuilderCodec<RealTimeSyncConfig> CODEC =
            BuilderCodec.builder(RealTimeSyncConfig.class, RealTimeSyncConfig::new)
                    .addField(
                            new KeyedCodec<>("Timezone", Codec.STRING),
                            RealTimeSyncConfig::setTimezone,
                            RealTimeSyncConfig::getTimezone
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
