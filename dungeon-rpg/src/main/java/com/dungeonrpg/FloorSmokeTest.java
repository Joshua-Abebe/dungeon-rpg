package com.dungeonrpg;

import com.dungeonrpg.item.Item;
import com.dungeonrpg.map.FloorFactory;
import com.dungeonrpg.map.DifficultyScaler;
import com.dungeonrpg.map.GameMap;

import java.util.Optional;

/**
 * TEMPORARY: verifies FloorFactory generates all levels correctly (1
 * enemy on level 1, scaling up to the level 5 boss) and that chest
 * opening yields loot exactly once, before wiring floor progression
 * into GameWindow.
 */
public class FloorSmokeTest {
    public static void main(String[] args) {
        for (int floor = 1; floor <= FloorFactory.TOTAL_FLOORS; floor++) {
            GameMap map = FloorFactory.generateFloor(floor, DifficultyScaler.DEFAULT_DIFFICULTY);
            System.out.println("Floor " + floor + ": " + map.getEnemyPlacements().size()
                + " enemies -> " + map.getEnemyPlacements().stream()
                    .map(p -> p.getEnemy().getClass().getSimpleName())
                    .toList());
        }

        System.out.println("\nInvalid floor number should throw:");
        try {
            FloorFactory.generateFloor(99, DifficultyScaler.DEFAULT_DIFFICULTY);
            System.out.println("ERROR: did not throw!");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }

        System.out.println("\nChest test:");
        GameMap map = FloorFactory.generateFloor(1, DifficultyScaler.DEFAULT_DIFFICULTY);
        // The chest is always placed at (height-2, 1) by GameMap's layout.
        int chestRow = map.getHeight() - 2;
        int chestCol = 1;

        Optional<Item> first = map.tryOpenChest(chestRow, chestCol);
        System.out.println("First open: " + (first.isPresent() ? first.get().getName() : "nothing"));

        Optional<Item> second = map.tryOpenChest(chestRow, chestCol);
        System.out.println("Second open (should be empty): " + second.isPresent());
    }
}
