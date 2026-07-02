package com.dungeonrpg.entity;

import com.dungeonrpg.ability.Fireball;

/**
 * Mage: high damage, low defense, uses Fireball. Same pattern as Warrior —
 * stats plus which Ability object to plug in.
 */
public class Mage extends Hero {

    public Mage(String name) {
        super(name, 80, 18, 4, new Fireball());
    }
}
