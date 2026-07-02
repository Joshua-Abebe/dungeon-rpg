package com.dungeonrpg.rule;

import java.io.Serializable;

/**
 * A Rule is exactly the object the project brief describes: a
 * Condition, an Action, and a Trigger bound together. It has no
 * intelligence of its own beyond "if my condition says yes, run my
 * action" — all the actual decision-making and effects live in whatever
 * Condition and Action objects it was built with. This is what makes
 * Rule genuinely reusable: the same Rule class represents every possible
 * rule a player could ever build, because Condition and Action are the
 * parts that vary, not Rule itself.
 */
public class Rule implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Trigger trigger;
    private final Condition condition;
    private final Action action;

    public Rule(String name, Trigger trigger, Condition condition, Action action) {
        this.name = name;
        this.trigger = trigger;
        this.condition = condition;
        this.action = action;
    }

    public void apply(GameContext ctx) {
        if (condition.evaluate(ctx)) {
            action.execute(ctx);
        }
    }

    public String getName() {
        return name;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * True if this rule and another would behave identically — same
     * Trigger, same Condition, same Action — regardless of what either
     * one is named. Used by the Rule Builder to block adding a rule
     * that would silently duplicate an existing one's effect under a
     * different name (which would otherwise double that effect every
     * time it fires, with nothing indicating why). Compares this
     * Rule's own private fields directly against another Rule's,
     * rather than exposing getCondition()/getAction() publicly just
     * for this one purpose.
     */
    public boolean hasSameBehaviorAs(Rule other) {
        return trigger == other.trigger && condition.equals(other.condition) && action.equals(other.action);
    }

    @Override
    public String toString() {
        return name + " (" + trigger + ")";
    }
}
