package com.dungeonrpg;

import com.dungeonrpg.entity.Enemy;
import com.dungeonrpg.entity.Goblin;
import com.dungeonrpg.entity.Skeleton;
import com.dungeonrpg.exception.InvalidMoveException;
import com.dungeonrpg.map.Direction;
import com.dungeonrpg.map.GameMap;
import com.dungeonrpg.map.TileType;

import java.util.List;

/**
 * TEMPORARY: verifies GameMap generation and movement logic in plain
 * console output, with no Swing/AWT involved. This sandbox has no
 * display, so this is how I can actually confirm the map data model is
 * correct before building the Swing window on top of it. Will be deleted
 * once the real game is wired together.
 */
public class MapSmokeTest {
    public static void main(String[] args) throws InvalidMoveException {
        List<Enemy> enemies = List.of(new Goblin("G1"), new Skeleton("S1"));
        GameMap map = new GameMap(1, 12, 8, enemies);

        System.out.println("Floor " + map.getFloorNumber() + " generated, "
            + map.getEnemyPlacements().size() + " enemies placed.\n");
        printMap(map);

        System.out.println("\nMoving hero right 3 times, down 2 times...");
        map.moveHero(Direction.RIGHT);
        map.moveHero(Direction.RIGHT);
        map.moveHero(Direction.RIGHT);
        map.moveHero(Direction.DOWN);
        map.moveHero(Direction.DOWN);
        System.out.println("Hero now at (" + map.getHeroRow() + ", " + map.getHeroCol() + ")");
        printMap(map);

        System.out.println("\nAttempting illegal move into top wall...");
        try {
            for (int i = 0; i < 5; i++) map.moveHero(Direction.UP);
        } catch (InvalidMoveException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }

        System.out.println("\nFloor cleared? " + map.isFloorCleared());
        for (var placement : map.getEnemyPlacements()) {
            map.removeEnemy(placement.getEnemy());
        }
        System.out.println("Floor cleared after removing all enemies? " + map.isFloorCleared());
    }

    private static void printMap(GameMap map) {
        for (int row = 0; row < map.getHeight(); row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < map.getWidth(); col++) {
                if (row == map.getHeroRow() && col == map.getHeroCol()) {
                    line.append('@');
                } else if (map.getEnemyAt(row, col).isPresent()) {
                    line.append('E');
                } else {
                    TileType type = map.getTile(row, col).getType();
                    line.append(switch (type) {
                        case WALL -> '#';
                        case FLOOR -> '.';
                        case STAIRS -> '>';
                        case CHEST -> '$';
                    });
                }
            }
            System.out.println(line);
        }
    }
}
