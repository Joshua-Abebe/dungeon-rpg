package com.dungeonrpg.rule;

import com.dungeonrpg.entity.Character;

/** Restores HP to self or opponent. */
public class HealAction implements Action {

    private static final long serialVersionUID = 1L;

    private final int amount;
    private final boolean targetSelf;

    public HealAction(int amount, boolean targetSelf) {
        this.amount = amount;
        this.targetSelf = targetSelf;
    }

    @Override
    public void execute(GameContext ctx) {
        Character target = targetSelf ? ctx.getSelf() : ctx.getOpponent();
        target.heal(amount);
        ctx.logMessage("[Rule] " + target.getName() + " recovers " + amount + " HP!");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HealAction other)) return false;
        return amount == other.amount && targetSelf == other.targetSelf;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(amount, targetSelf);
    }
}
