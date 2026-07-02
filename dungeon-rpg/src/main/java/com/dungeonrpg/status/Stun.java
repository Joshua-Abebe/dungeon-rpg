package com.dungeonrpg.status;

import com.dungeonrpg.entity.Character;

/**
 * Stun skips the affected character's next action entirely. In this simple
 * version, the "skip the turn" check is done by combat code calling
 * hasEffect(Stun.class) before letting a character act — applyEffect()
 * itself has nothing to do each tick except expire after one turn, since
 * the skipping behavior lives where turns are actually taken.
 */
public class Stun extends StatusEffect {

    public Stun() {
        super(1);
    }

    @Override
    protected void applyEffect(Character affected) {
        // Intentionally empty: the effect of being stunned is enforced by
        // the combat loop checking hasEffect(Stun.class) before a turn,
        // not by anything this tick() needs to do to the character's stats.
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Stun;
    }

    @Override
    public int hashCode() {
        return Stun.class.hashCode();
    }
}
