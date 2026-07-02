package com.dungeonrpg.rule;

import com.dungeonrpg.entity.Character;

/**
 * Permanently raises or lowers a stat on self or opponent. Uses
 * Character's public boostAttack()/boostDefense() — the same sanctioned
 * entry point Hero's equipment system uses — so this action can't reach
 * past validation and set a stat to something invalid; it goes through
 * exactly the same door as everything else that legitimately modifies
 * combat stats from outside the Character hierarchy.
 */
public class ModifyStatAction implements Action {

    private static final long serialVersionUID = 1L;

    private final StatType stat;
    private final int delta;
    private final boolean targetSelf;

    public ModifyStatAction(StatType stat, int delta, boolean targetSelf) {
        this.stat = stat;
        this.delta = delta;
        this.targetSelf = targetSelf;
    }

    @Override
    public void execute(GameContext ctx) {
        Character target = targetSelf ? ctx.getSelf() : ctx.getOpponent();
        switch (stat) {
            case ATTACK -> target.boostAttack(delta);
            case DEFENSE -> target.boostDefense(delta);
        }
        ctx.logMessage("[Rule] " + target.getName() + "'s " + stat.name().toLowerCase()
            + " changes by " + delta + "!");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ModifyStatAction other)) return false;
        return stat == other.stat && delta == other.delta && targetSelf == other.targetSelf;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(stat, delta, targetSelf);
    }
}
