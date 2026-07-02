package com.dungeonrpg.rule;

import java.io.Serializable;

/**
 * Condition is the "when" half of a Rule: given the current GameContext,
 * should this rule's Action fire? Same interface-as-contract reasoning
 * as Ability and AIStrategy elsewhere in this codebase — LowHPCondition,
 * HasStatusCondition, TurnNumberCondition, EnemyTypeCondition, and
 * CompositeCondition share nothing but this one method, so an interface
 * is the right tool.
 *
 * This is also where the project's PARAMETRIC / RUNTIME COMPOSITION
 * story lives: the player picks a Condition implementation, an Action
 * implementation, and a Trigger, and that specific combination becomes a
 * brand-new Rule object at runtime — one that never existed in the
 * source code as a named class. Condition never combines itself with an
 * Action; that pairing happens one level up, in Rule.
 */
public interface Condition extends Serializable {
    boolean evaluate(GameContext ctx);
}
