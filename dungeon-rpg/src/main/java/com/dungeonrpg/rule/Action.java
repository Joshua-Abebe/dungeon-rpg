package com.dungeonrpg.rule;

import java.io.Serializable;

/**
 * Action is the "then" half of a Rule: what actually happens once a
 * Condition says yes. DealDamageAction, ApplyStatusAction, HealAction,
 * ModifyStatAction, and CompositeAction all implement this and nothing
 * more — no shared state, only a shared contract, same as Condition.
 */
public interface Action extends Serializable {
    void execute(GameContext ctx);
}
