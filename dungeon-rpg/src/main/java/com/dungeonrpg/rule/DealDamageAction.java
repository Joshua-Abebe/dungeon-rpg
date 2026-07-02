package com.dungeonrpg.rule;

import com.dungeonrpg.entity.Character;

/** Deals a fixed amount of damage to self or opponent. */
public class DealDamageAction implements Action {

    private static final long serialVersionUID = 1L;

    private final int amount;
    private final boolean targetSelf;

    public DealDamageAction(int amount, boolean targetSelf) {
        this.amount = amount;
        this.targetSelf = targetSelf;
    }

    @Override
    public void execute(GameContext ctx) {
        Character target = targetSelf ? ctx.getSelf() : ctx.getOpponent();
        target.takeDamage(amount);
        ctx.logMessage("[Rule] " + target.getName() + " takes " + amount + " bonus damage!");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DealDamageAction other)) return false;
        return amount == other.amount && targetSelf == other.targetSelf;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(amount, targetSelf);
    }
}
