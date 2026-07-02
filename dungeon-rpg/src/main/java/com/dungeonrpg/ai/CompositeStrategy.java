package com.dungeonrpg.ai;

import com.dungeonrpg.entity.Character;

import java.util.List;

/**
 * The boss strategy: holds a LIST of other strategies and rotates through
 * them one per turn (aggressive, then defensive, then random, repeat).
 * This is the COMPOSITE PATTERN — CompositeStrategy implements the exact
 * same AIStrategy interface as its children, so from Enemy's point of view
 * it's just "an AIStrategy," even though internally it's coordinating
 * several. Dragon is built with this so its behavior actually shifts
 * mid-fight instead of staying static like every other enemy.
 */
public class CompositeStrategy implements AIStrategy {

    private final List<AIStrategy> strategies;
    private int turnIndex = 0;

    public CompositeStrategy(List<AIStrategy> strategies) {
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("CompositeStrategy needs at least one strategy");
        }
        this.strategies = strategies;
    }

    @Override
    public void decideAction(Character self, Character target) {
        AIStrategy current = strategies.get(turnIndex % strategies.size());
        current.decideAction(self, target);
        turnIndex++;
    }
}
