package com.dungeonrpg.rule;

import com.dungeonrpg.entity.Character;
import com.dungeonrpg.status.StatusEffect;

/** True when the watched character currently has a specific StatusEffect active. */
public class HasStatusCondition implements Condition {

    private static final long serialVersionUID = 1L;

    private final Class<? extends StatusEffect> statusType;
    private final boolean watchSelf;

    public HasStatusCondition(Class<? extends StatusEffect> statusType, boolean watchSelf) {
        this.statusType = statusType;
        this.watchSelf = watchSelf;
    }

    @Override
    public boolean evaluate(GameContext ctx) {
        Character target = watchSelf ? ctx.getSelf() : ctx.getOpponent();
        return target.hasEffect(statusType);
    }
}
