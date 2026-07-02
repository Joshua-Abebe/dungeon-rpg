package com.dungeonrpg.entity;

import com.dungeonrpg.ability.ShieldBash;

/**
 * Warrior is the first concrete leaf of the Hero branch. Notice how little
 * code is actually here: stats and which Ability to use. Everything else
 * (attacking, inventory, taking damage) was already written once in
 * Character and Hero. This is the payoff of the hierarchy — adding a new
 * hero type later means writing a class this small, not rebuilding combat
 * logic from scratch.
 */
public class Warrior extends Hero {

    public Warrior(String name) {
        super(name, 120, 15, 10, new ShieldBash());
    }
}
