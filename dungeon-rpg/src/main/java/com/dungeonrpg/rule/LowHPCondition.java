package com.dungeonrpg.rule;

import com.dungeonrpg.entity.Character;

/**
 * True when the watched character's HP has dropped below a percentage
 * of their max HP. "watchSelf" decides whether "the watched character"
 * means the rule owner or their opponent — the same Condition class
 * covers both "heal myself when I'm low" and "finish them off when
 * they're low" depending on how the player configures it, rather than
 * needing two near-identical classes.
 */
public class LowHPCondition implements Condition {

    private static final long serialVersionUID = 1L;

    private final double thresholdPercent;
    private final boolean watchSelf;

    public LowHPCondition(double thresholdPercent, boolean watchSelf) {
        this.thresholdPercent = thresholdPercent;
        this.watchSelf = watchSelf;
    }

    @Override
    public boolean evaluate(GameContext ctx) {
        Character target = watchSelf ? ctx.getSelf() : ctx.getOpponent();
        double ratio = (double) target.getHp() / target.getMaxHp();
        return ratio < thresholdPercent;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LowHPCondition other)) return false;
        return Double.compare(thresholdPercent, other.thresholdPercent) == 0 && watchSelf == other.watchSelf;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(thresholdPercent, watchSelf);
    }
}
