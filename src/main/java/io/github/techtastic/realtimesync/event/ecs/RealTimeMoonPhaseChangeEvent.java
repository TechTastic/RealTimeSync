package io.github.techtastic.realtimesync.event.ecs;

import com.hypixel.hytale.component.system.EcsEvent;
import io.github.techtastic.realtimesync.util.MoonUtil;
import org.shredzone.commons.suncalc.MoonIllumination;
import org.shredzone.commons.suncalc.MoonPhase;

import java.time.Instant;

/**
 * This event is invoked by {@link io.github.techtastic.realtimesync.systems.RealTimeSystems.Init} and contains data useful for any other plugins to take advantage of.
 */
public class RealTimeMoonPhaseChangeEvent extends EcsEvent {
    private final Instant realTime;
    private final MoonIllumination moonIllumination;
    private final MoonPhase moonPhaseData;
    private final MoonPhase.Phase closestMoonPhase;
    private final int gameMoonPhase;

    public RealTimeMoonPhaseChangeEvent(Instant realTime) {
        this.realTime = realTime;
        this.moonIllumination = MoonUtil.getMoonIllumination(realTime);
        this.moonPhaseData = MoonUtil.getMoonPhaseInstance(realTime);
        this.closestMoonPhase = this.moonIllumination.getClosestPhase();
        this.gameMoonPhase = MoonUtil.getGameMoonPhase(this.closestMoonPhase);
    }

    /**
     * This method gets the timestamp {@link Instant} at which this event was created.
     *
     * @return The timestamp {@link Instant}
     */
    public Instant getRealTime() {
        return this.realTime;
    }

    /**
     * This method gets the {@link MoonIllumination} data for the given event timestamp.
     *
     * @return The {@link MoonIllumination} data at the given timestamp
     */
    public MoonIllumination getMoonIllumination() {
        return this.moonIllumination;
    }

    /**
     * This method gets the {@link MoonPhase} data for the given event timestamp.
     *
     * @return The {@link MoonPhase} data at the given timestamp
     */
    public MoonPhase getMoonPhaseData() {
        return this.moonPhaseData;
    }

    /**
     * This method gets the closest {@link MoonPhase.Phase} for the given event timestamp.
     *
     * @return The closest {@link MoonPhase.Phase} data at the given timestamp
     */
    public MoonPhase.Phase getClosestMoonPhase() {
        return this.closestMoonPhase;
    }

    /**
     * This method gets the actual moon phase passed to the {@link com.hypixel.hytale.server.core.modules.time.WorldTimeResource}.
     *
     * Note: If More Moon Phases is present, then the waning variants of the moons are used. Otherwise, all waning variants are mapped to their vanilla counterparts.
     * @return The game moon phase integer
     */
    public int getGameMoonPhase() {
        return this.gameMoonPhase;
    }
}
