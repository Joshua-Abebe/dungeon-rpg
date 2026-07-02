package com.dungeonrpg.rule;

/**
 * The event categories that flow through GameEventBus. Deliberately a
 * separate enum from Trigger, even though the names overlap conceptually
 * (HIT vs ON_HIT) — Trigger describes moments CombatEngine itself
 * announces directly to a RuleEngine (Layer 1), while EventType
 * describes events that flow through the bus, which can originate from
 * a Rule's own Action (Layer 2 chaining), not just from CombatEngine.
 * Keeping them distinct types means Layer 2 can evolve independently of
 * Layer 1 (e.g. gaining new event categories that have no corresponding
 * combat Trigger) without the two systems tangling together.
 */
public enum EventType {
    HIT,
    DEATH,
    STATUS_APPLIED,
    TURN_END
}
