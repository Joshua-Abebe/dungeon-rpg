package com.dungeonrpg.entity;

import com.dungeonrpg.ai.RandomStrategy;

public class Skeleton extends Enemy {

    public Skeleton(String name) {
        super(name, 60, 13, 4, new RandomStrategy(), 15);
    }
}
