package com.dungeonrpg.rule;

/**
 * The moments during combat a Rule can attach itself to. RuleEngine
 * fires one of these at each meaningful point in a turn, and only Rules
 * registered for that exact Trigger get evaluated — this is what lets a
 * player build a rule that says "only check this when I land a hit,"
 * rather than every rule being checked constantly regardless of context.
 *
 * Every Trigger is fired from hero.getRuleEngine() by CombatEngine, and
 * every one of them describes something from the HERO's side of the
 * fight specifically — never the enemy's. There is no path anywhere in
 * the codebase for an Enemy to own or fire rules; the rule engine
 * belongs to Hero alone (Hero.getRuleEngine()), and Enemy has no
 * equivalent field. ON_HIT fires when the hero lands a hit (never when
 * the enemy does), and ON_DEATH fires when the enemy dies (never when
 * the hero does) — both directions are fixed by CombatEngine, not
 * configurable per rule. The displayName strings exist specifically so
 * the Rule Builder UI states this unambiguously instead of showing the
 * player a bare internal constant name that doesn't say whose hit or
 * whose death it means.
 */
public enum Trigger {
    ON_HIT("When I Land a Hit"),
    ON_TURN_END("At the End of Every Round"),
    ON_DEATH("When the Enemy Dies"),
    ON_STATUS_APPLIED("Status Applied (not yet wired to fire — unused)"),
    ON_FLOOR_ENTER("Floor Entered (not yet wired to fire — unused)");

    private final String displayName;

    Trigger(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
