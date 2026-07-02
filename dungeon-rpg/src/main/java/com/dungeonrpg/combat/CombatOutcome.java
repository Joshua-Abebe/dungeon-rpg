package com.dungeonrpg.combat;

/**
 * The four ways a combat encounter can resolve. CombatEngine returns one
 * of these after every action, and the UI layer (CombatPanel) uses it to
 * decide whether to keep the fight going or switch back to the map.
 */
public enum CombatOutcome {
    ONGOING,
    HERO_WON,
    HERO_DIED,
    HERO_FLED
}
