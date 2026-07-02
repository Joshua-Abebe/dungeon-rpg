package com.dungeonrpg.rule;

/**
 * A Condition that is always true — used for rules with no additional
 * gating beyond their Trigger firing. This exists as its own named
 * class rather than an anonymous "ctx -> true" lambda specifically so
 * it can be compared for equality: Java lambdas don't override
 * equals() to compare based on behavior, so two separately-written
 * "ctx -> true" lambdas are never equal to each other even though they
 * do the exact same thing — which would silently defeat duplicate-rule
 * detection in the Rule Builder for the single most common condition
 * choice ("Always").
 */
public class AlwaysCondition implements Condition {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean evaluate(GameContext ctx) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AlwaysCondition;
    }

    @Override
    public int hashCode() {
        return AlwaysCondition.class.hashCode();
    }
}
