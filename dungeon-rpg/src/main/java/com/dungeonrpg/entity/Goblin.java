package com.dungeonrpg.entity;

import com.dungeonrpg.ai.AggressiveStrategy;

/**
 * Goblin: fast, low HP, always aggressive. Same story as Warrior — this
 * class is almost entirely just numbers, because the actual behavior
 * (act() -> strategy.decideAction()) already lives in Enemy.
 */
public class Goblin extends Enemy {

    public Goblin(String name) {
        super(name, 40, 10, 2, new AggressiveStrategy(), 8);
    }
}
