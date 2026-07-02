package com.dungeonrpg.ability;

import com.dungeonrpg.entity.Character;
import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.status.Stun;

/**
 * Warrior's signature move: a harder hit than a basic attack, plus a
 * stun. Every Ability in the game deals ABILITY_DAMAGE_MULTIPLIER times
 * the hero's attack stat as its base damage — abilities are meant to be
 * the stronger, more deliberate choice compared to a plain Attack, with
 * each class's flavor effect (stun, burn, gold theft) layered on top of
 * that shared baseline rather than replacing it.
 */
public class ShieldBash implements Ability {

    private static final double ABILITY_DAMAGE_MULTIPLIER = 1.2;

    @Override
    public void use(Hero user, Character target) {
        int damage = (int) Math.round(user.getAttack() * ABILITY_DAMAGE_MULTIPLIER);
        target.takeDamage(damage);
        target.applyEffect(new Stun());
    }
}
