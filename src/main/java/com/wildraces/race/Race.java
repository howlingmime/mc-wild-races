package com.wildraces.race;

public enum Race {
    NONE(
        "None",
        "No special abilities.",
        0, 0, 0
    ),
    ARACHNID(
        "Arachnid",
        "Climb any wall. Melee hits apply Slowness. Weak to fire (+50% fire damage).",
        4, 0, 0
    ),
    MINOTAUR(
        "Minotaur",
        "Massive health and strength. Sprint attacks send enemies flying. Moves slower.",
        12, 4, -0.05
    ),
    LIONBEAR(
        "Lionbear",
        "Fearsome predator. Permanent night vision. Landing from heights unleashes a shockwave.",
        8, 2, 0
    ),
    TROLL(
        "Troll",
        "Immense constitution with constant regeneration. Burns in direct sunlight.",
        20, 3, -0.075
    ),
    CENTAUR(
        "Centaur",
        "Swift and sure-footed. Deals 50% more damage with bows. Gains Jump Boost.",
        6, 0, 0.05
    );

    public final String displayName;
    public final String description;
    /** Extra health points added (2 pts = 1 heart). */
    public final double bonusHealth;
    /** Extra base attack damage added. */
    public final double bonusDamage;
    /** Flat movement speed modifier (player base is ~0.1). */
    public final double speedModifier;

    Race(String displayName, String description,
         double bonusHealth, double bonusDamage, double speedModifier) {
        this.displayName = displayName;
        this.description = description;
        this.bonusHealth = bonusHealth;
        this.bonusDamage = bonusDamage;
        this.speedModifier = speedModifier;
    }
}
