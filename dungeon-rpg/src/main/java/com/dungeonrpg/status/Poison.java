package com.dungeonrpg.status;

import com.dungeonrpg.entity.Character;

/**
 * Poison deals damage every turn for a fixed number of turns. This is the
 * clearest example of why tick() lives in the parent: Poison, Freeze, and
 * Stun all need "count down and expire," but only Poison needs "and also
 * hurt the character" — that's the one line that actually varies.
 */
public class Poison extends StatusEffect {

    private final int damagePerTurn;

    public Poison(int damagePerTurn, int durationTurns) {
        super(durationTurns);
        this.damagePerTurn = damagePerTurn;
    }

    @Override
    protected void applyEffect(Character affected) {
        affected.takeDamage(damagePerTurn);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Poison other)) return false;
        return damagePerTurn == other.damagePerTurn && getRemainingTurns() == other.getRemainingTurns();
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(damagePerTurn, getRemainingTurns());
    }
}
