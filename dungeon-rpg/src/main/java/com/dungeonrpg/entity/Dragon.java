package com.dungeonrpg.entity;

import com.dungeonrpg.ai.AggressiveStrategy;
import com.dungeonrpg.ai.CompositeStrategy;
import com.dungeonrpg.ai.DefensiveStrategy;
import com.dungeonrpg.ai.RandomStrategy;

import java.util.List;

/**
 * Dragon: the final boss. The only enemy built with a CompositeStrategy
 * instead of a single strategy — it cycles between aggressive, defensive,
 * and random behavior across the fight, which is what makes it feel
 * meaningfully different from every other enemy despite reusing the exact
 * same Enemy/Character machinery.
 */
public class Dragon extends Enemy {

    public Dragon(String name) {
        super(name, 300, 25, 15,
            new CompositeStrategy(List.of(
                new AggressiveStrategy(),
                new DefensiveStrategy(),
                new RandomStrategy()
            )), 100);
    }
}
