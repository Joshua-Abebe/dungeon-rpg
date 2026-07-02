package com.dungeonrpg.entity;

import com.dungeonrpg.ability.Pickpocket;

/**
 * Rogue: fast, moderate stats, uses Pickpocket. Same pattern again — this
 * is the third hero built from the exact same Hero skeleton, which is the
 * clearest demonstration that the abstraction was drawn in the right
 * place: three very different playstyles (tank, glass-cannon mage, gold
 * thief) needed zero changes to Character or Hero to exist.
 */
public class Rogue extends Hero {

    public Rogue(String name) {
        super(name, 90, 12, 6, new Pickpocket());
    }
}
