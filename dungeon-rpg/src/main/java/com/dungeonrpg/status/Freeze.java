package com.dungeonrpg.status;

import com.dungeonrpg.entity.Character;

/**
 * Freeze causes the affected character to lose their next turn. Same shape
 * as Stun (empty tick, the actual "skip this turn" check happens in the
 * combat loop via hasEffect(Freeze.class)) — the difference between them is
 * conceptual (frost vs a physical stagger) and in how they're applied, not
 * in the mechanics. Having both still demonstrates that multiple sibling
 * subclasses can share near-identical shape while remaining distinct types.
 */
public class Freeze extends StatusEffect {

    public Freeze() {
        super(1);
    }

    @Override
    protected void applyEffect(Character affected) {
        // No per-tick effect beyond existing — the turn-skip is enforced
        // by combat code checking hasEffect(Freeze.class), same as Stun.
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Freeze;
    }

    @Override
    public int hashCode() {
        return Freeze.class.hashCode();
    }
}
