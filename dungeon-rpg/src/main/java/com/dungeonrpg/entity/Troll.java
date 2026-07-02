package com.dungeonrpg.entity;

import com.dungeonrpg.ai.DefensiveStrategy;

public class Troll extends Enemy {

    public Troll(String name) {
        super(name, 150, 17, 12, new DefensiveStrategy(), 30);
    }
}
