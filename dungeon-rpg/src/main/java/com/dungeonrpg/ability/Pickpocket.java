package com.dungeonrpg.ability;

import com.dungeonrpg.entity.Character;
import com.dungeonrpg.entity.Enemy;
import com.dungeonrpg.entity.Hero;

/**
 * Rogue's signature move: a strike with the same ABILITY_DAMAGE_MULTIPLIER
 * baseline every ability uses, plus stolen gold as the bonus effect on
 * top — the same shape as ShieldBash (damage + stun) and Fireball
 * (damage + burn). An earlier version of this class dealt no damage at
 * all, stealing gold instead of hitting; that made Pickpocket the one
 * ability weaker than a plain Attack, breaking the rule that every
 * Ability should out-damage a basic Attack. Notice the instanceof check
 * guarding the gold theft specifically — that part only makes sense
 * against an Enemy, since Heroes don't carry stealable loot gold, but
 * the damage itself applies to any Character like every other ability.
 */
public class Pickpocket implements Ability {

    private static final double ABILITY_DAMAGE_MULTIPLIER = 1.2;

    @Override
    public void use(Hero user, Character target) {
        int damage = (int) Math.round(user.getAttack() * ABILITY_DAMAGE_MULTIPLIER);
        target.takeDamage(damage);
        if (target instanceof Enemy enemy) {
            int stolen = enemy.takeLootGold();
            user.addGold(stolen);
        }
    }
}
