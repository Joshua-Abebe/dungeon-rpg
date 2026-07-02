package com.dungeonrpg.map;

import com.dungeonrpg.entity.Dinosaur;
import com.dungeonrpg.entity.Enemy;
import com.dungeonrpg.entity.Goblin;
import com.dungeonrpg.entity.Skeleton;
import com.dungeonrpg.entity.Troll;

import java.util.ArrayList;
import java.util.List;

/**
 * FloorFactory owns the "what does level N look like" decision.
 * Enemy COUNT is driven directly by the difficulty the player chose:
 * level 1 spawns exactly as many enemies as the chosen difficulty
 * (difficulty 4 -> 4 enemies), and each level after that adds exactly
 * one more, all the way to level 4. Level 5 is always a single,
 * fixed-stat boss encounter (Dinosaur) that does NOT scale with
 * difficulty at all — a deliberate exception, since the boss stats
 * were given as exact numbers rather than a scaling rule, and a stable
 * final gate is more coherent than one that becomes trivial at low
 * difficulty or unbeatable at high difficulty.
 */
public final class FloorFactory {

    private FloorFactory() {
    }

    public static final int TOTAL_FLOORS = 5;
    public static final int BOSS_FLOOR = 5;
    private static final int MAP_WIDTH = 14;
    private static final int MAP_HEIGHT = 9;

    public static GameMap generateFloor(int floorNumber, int difficulty) {
        if (floorNumber < 1 || floorNumber > TOTAL_FLOORS) {
            throw new IllegalArgumentException("No floor definition for floor " + floorNumber);
        }
        List<Enemy> enemies = enemiesForFloor(floorNumber, difficulty);
        if (floorNumber != BOSS_FLOOR) {
            double multiplier = DifficultyScaler.multiplierFor(floorNumber, difficulty);
            enemies.forEach(enemy -> enemy.scaleStats(multiplier));
        }
        return new GameMap(floorNumber, MAP_WIDTH, MAP_HEIGHT, enemies);
    }

    private static List<Enemy> enemiesForFloor(int floorNumber, int difficulty) {
        if (floorNumber == BOSS_FLOOR) {
            return List.of(new Dinosaur("Vorak"));
        }
        int clampedDifficulty = Math.max(DifficultyScaler.MIN_DIFFICULTY,
            Math.min(DifficultyScaler.MAX_DIFFICULTY, difficulty));
        int enemyCount = clampedDifficulty + (floorNumber - 1);
        return generateEnemies(enemyCount);
    }

    /**
     * Builds a list of exactly `count` enemies by cycling through a
     * fixed three-type rotation (Goblin, Skeleton, Troll) in order.
     * Kept deliberately simple, per the actual request that drove this
     * design: a predictable, easy-to-explain pattern the player can
     * learn at a glance, rather than a weighted or randomized mix that
     * would make the enemy count/type relationship harder to reason
     * about.
     */
    private static List<Enemy> generateEnemies(int count) {
        List<Enemy> enemies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int cycle = i % 3;
            Enemy enemy = switch (cycle) {
                case 0 -> new Goblin("Goblin " + (i + 1));
                case 1 -> new Skeleton("Skeleton " + (i + 1));
                default -> new Troll("Troll " + (i + 1));
            };
            enemies.add(enemy);
        }
        return enemies;
    }
}
