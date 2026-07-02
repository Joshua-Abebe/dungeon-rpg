package com.dungeonrpg.status;

import com.dungeonrpg.entity.Character;

import java.io.Serializable;

/**
 * StatusEffect is the abstract parent of Poison, Freeze, and Stun.
 *
 * WHY THIS SHAPE: every status effect needs to (a) do something each turn
 * (tick) and (b) eventually wear off (isExpired). By putting the "how many
 * turns are left" bookkeeping here in the parent, every subclass gets
 * expiry tracking for free (reuse) and only has to implement the one thing
 * that's actually unique to it: what tick() does.
 */
public abstract class StatusEffect implements Serializable {

    private static final long serialVersionUID = 1L;

    private int remainingTurns;

    protected StatusEffect(int durationTurns) {
        this.remainingTurns = durationTurns;
    }

    /**
     * Applies this effect's behavior to the affected character for one
     * turn (e.g. Poison deals damage, Freeze skips a turn) and decrements
     * the remaining duration. Each subclass overrides this differently —
     * that's polymorphism again, same shape as Character.act().
     */
    public void tick(Character affected) {
        applyEffect(affected);
        remainingTurns--;
    }

    protected abstract void applyEffect(Character affected);

    public boolean isExpired() {
        return remainingTurns <= 0;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }
}
