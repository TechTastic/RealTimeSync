package io.github.techtastic.realtimesync.util;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.server.core.asset.AssetModule;
import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPhase;

import java.time.Instant;

/**
 * This is a utility class for moon-related information.
 */
public class MoonUtil {
    /**
     * This method determines if the More Moon Phases asset pack is loaded for integration.
     *
     * @return Whether the More Moon Phases asset pack is loaded
     */
    public static boolean hasMoreMoonPhases() {
        AssetPack pack = AssetModule.get().getAssetPack("TechTastic:MoreMoonPhases");
        return pack != null;
    }

    /**
     * This method gets the {@link MoonPhase} data from the given {@link Instant}.
     *
     * @param time The {@link Instant} used to calculate the {@link MoonPhase} data
     * @return The calculated {@link MoonPhase} data
     */
    public static MoonPhase getMoonPhaseInstance(Instant time) {
        return MoonPhase.compute().on(time).execute();
    }

    /**
     * This method gets the {@link MoonIllumination} data from the given {@link Instant}.
     *
     * @param time The {@link Instant} used to calculate the {@link MoonIllumination} data
     * @return The calculated {@link MoonIllumination} data
     */
    public static MoonIllumination getMoonIllumination(Instant time) {
        return MoonIllumination.compute().on(time).execute();
    }

    /**
     * This method gets the game moon phase from the given {@link MoonPhase.Phase}.
     *
     * Note: If More Moon Phases is present, then the waning variants of the moons are used. Otherwise, all waning variants are mapped to their vanilla counterparts.
     *
     * @param phase The {@link MoonPhase.Phase} enum value to be mapped
     * @return The mapped game moon phase integer
     */
    public static int getGameMoonPhase(MoonPhase.Phase phase) {
        return switch (phase) {
            case MoonPhase.Phase.FULL_MOON -> 0;
            case MoonPhase.Phase.WAXING_GIBBOUS  -> 1;
            case MoonPhase.Phase.FIRST_QUARTER -> 2;
            case MoonPhase.Phase.WAXING_CRESCENT -> 3;
            case MoonPhase.Phase.NEW_MOON -> 4;
            default -> switch (phase) { // TODO: Special Handlers Later
                case MoonPhase.Phase.WANING_GIBBOUS -> hasMoreMoonPhases() ? 5 : 1;
                case MoonPhase.Phase.LAST_QUARTER -> hasMoreMoonPhases() ? 6 : 2;
                case MoonPhase.Phase.WANING_CRESCENT -> hasMoreMoonPhases() ? 7 : 3;
                default -> 0;
            };
        };
    }
}
