package com.wildraces;

import com.wildraces.race.Race;

/** Injected onto PlayerEntity via mixin to expose the player's chosen race. */
public interface PlayerRaceAccess {
    Race wildraces$getRace();
    void wildraces$setRace(Race race);
}
