package com.dungeonrpg.ability;

import com.dungeonrpg.entity.Character;
import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.status.Poison;

/**
 * Mage's signature move: a hit stronger than a basic attack, plus
 * lingering burn damage. The "burn" is implemented as a Poison instance
 * (damage-over-time) — the game only has one damage-over-time status,
 * so Fireball reuses it rather than inventing a near-duplicate
 * BurnEffect class. This is REUSE working at the design level, not just
 * the code level: recognizing that "burn" and "poison" are the same
 * mechanic wearing different narrative labels. Impact damage follows
 * the same ABILITY_DAMAGE_MULTIPLIER every other ability uses, so
 * Fireball is the stronger choice compared to a plain Attack, same as
 * every hero's ability.
 */
public class Fireball implements Ability {

    private static final double ABILITY_DAMAGE_MULTIPLIER = 1.2;
    private static final int BURN_DAMAGE_PER_TURN = 3;
    private static final int BURN_DURATION = 2;

    @Override
    public void use(Hero user, Character target) {
        int impactDamage = (int) Math.round(user.getAttack() * ABILITY_DAMAGE_MULTIPLIER);
        target.takeDamage(impactDamage);
        target.applyEffect(new Poison(BURN_DAMAGE_PER_TURN, BURN_DURATION));
    }
}
