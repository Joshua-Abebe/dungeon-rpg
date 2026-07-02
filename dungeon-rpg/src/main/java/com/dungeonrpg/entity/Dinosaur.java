package com.dungeonrpg.entity;

import com.dungeonrpg.ai.AggressiveStrategy;
import com.dungeonrpg.ai.CompositeStrategy;
import com.dungeonrpg.ai.DefensiveStrategy;
import com.dungeonrpg.ai.RandomStrategy;

import java.util.List;

/**
 * Dinosaur is the final boss: bigger and tougher than every other
 * enemy, guarding the princess at the end of the dungeon. Same
 * CompositeStrategy AI pattern as Dragon (cycling aggressive/defensive/
 * random behavior across the fight so it doesn't feel like a static
 * damage sponge), but its own distinct class and sprite — a separate
 * subclass rather than reskinning Dragon, since "what this enemy IS"
 * (a dinosaur, not a dragon) is a genuine identity difference worth its
 * own type, not just a cosmetic label change.
 */
public class Dinosaur extends Enemy {

    public Dinosaur(String name) {
        super(name, 200, 12, 10,
            new CompositeStrategy(List.of(
                new AggressiveStrategy(),
                new AggressiveStrategy(),
                new DefensiveStrategy(),
                new RandomStrategy()
            )), 150);
    }
}
