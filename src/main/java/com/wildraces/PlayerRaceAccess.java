package com.wildraces;

import com.wildraces.race.Race;

/** Injected onto Player via mixin to expose race data and Sprite wing-meter state. */
public interface PlayerRaceAccess {
    Race  wildraces$getRace();
    void  wildraces$setRace(Race race);

    /** Wing meter: 0.0 = empty, 1.0 = full (15 sec of flight at full). */
    float wildraces$getSpriteMeter();
    void  wildraces$setSpriteMeter(float meter);
}
