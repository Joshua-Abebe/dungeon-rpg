package com.dungeonrpg.rule;

import java.util.List;

/**
 * Combines several Conditions with AND/OR logic. This is the COMPOSITE
 * PATTERN again — same shape as CompositeStrategy in the ai package —
 * CompositeCondition implements Condition just like its children do, so
 * from a Rule's point of view a composite condition IS just a Condition,
 * even though internally it's coordinating several. This is also the
 * mechanism that lets the player build genuinely new logic at runtime:
 * "low HP AND fighting the Dragon" is a combination that doesn't exist
 * as a named class anywhere in this codebase — it's assembled from two
 * existing Condition objects the moment the player builds it.
 */
public class CompositeCondition implements Condition {

    private static final long serialVersionUID = 1L;

    private final List<Condition> conditions;
    private final LogicOperator operator;

    public CompositeCondition(List<Condition> conditions, LogicOperator operator) {
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("CompositeCondition needs at least one condition");
        }
        this.conditions = conditions;
        this.operator = operator;
    }

    @Override
    public boolean evaluate(GameContext ctx) {
        return switch (operator) {
            case AND -> conditions.stream().allMatch(c -> c.evaluate(ctx));
            case OR -> conditions.stream().anyMatch(c -> c.evaluate(ctx));
        };
    }
}
