package io.github.techtastic.realtimesync.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import javax.annotation.Nullable;
import java.time.ZoneId;

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

    @Nullable
    public String getTimezone() {
        return this.timezone;
    }

    public void setTimezone(@Nullable String timezone) {
        if (timezone != null && timezone.isEmpty())
            this.timezone = null;
        else
            this.timezone = timezone;
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
