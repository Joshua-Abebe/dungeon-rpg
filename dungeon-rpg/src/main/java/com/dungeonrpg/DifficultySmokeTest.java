package com.dungeonrpg;

import com.dungeonrpg.entity.Enemy;
import com.dungeonrpg.entity.Goblin;
import com.dungeonrpg.map.DifficultyScaler;
import com.dungeonrpg.map.FloorFactory;
import com.dungeonrpg.map.GameMap;

/**
 * TEMPORARY: verifies difficulty scaling actually changes enemy stats,
 * that the curve is monotonically increasing across both floor number
 * and difficulty setting, and that the level 5 boss is a FIXED-stat
 * encounter that does NOT receive the difficulty multiplier (a
 * deliberate design choice: the final fight should be the same
 * challenge regardless of the difficulty the player chose at the
 * start).
 */
public class DifficultySmokeTest {
    public static void main(String[] args) {
        System.out.println("Multiplier table (difficulty x floor):");
        for (int difficulty = 1; difficulty <= 10; difficulty += 3) {
            StringBuilder row = new StringBuilder("difficulty " + difficulty + ": ");
            for (int floor = 1; floor <= 4; floor++) {
                row.append(String.format("floor%d=%.2f  ", floor, DifficultyScaler.multiplierFor(floor, difficulty)));
            }
            System.out.println(row);
        }

        System.out.println("\n-- Applying to a real Goblin --");
        Goblin baseline = new Goblin("Baseline");
        System.out.println("Unscaled: HP=" + baseline.getMaxHp() + " ATK=" + baseline.getAttack() + " DEF=" + baseline.getDefense());

        Goblin easy = new Goblin("Easy");
        easy.scaleStats(DifficultyScaler.multiplierFor(1, 1));
        System.out.println("Difficulty 1, floor 1: HP=" + easy.getMaxHp() + " ATK=" + easy.getAttack() + " DEF=" + easy.getDefense());

        Goblin brutalLate = new Goblin("BrutalLate");
        brutalLate.scaleStats(DifficultyScaler.multiplierFor(4, 10));
        System.out.println("Difficulty 10, floor 4: HP=" + brutalLate.getMaxHp() + " ATK=" + brutalLate.getAttack() + " DEF=" + brutalLate.getDefense());

        boolean increasesWithFloor = DifficultyScaler.multiplierFor(4, 5) > DifficultyScaler.multiplierFor(1, 5);
        boolean increasesWithDifficulty = DifficultyScaler.multiplierFor(2, 10) > DifficultyScaler.multiplierFor(2, 1);
        boolean brutalIsToughest = brutalLate.getMaxHp() > easy.getMaxHp() && brutalLate.getMaxHp() > baseline.getMaxHp();
        System.out.println("\nMonotonically increases with floor: " + increasesWithFloor);
        System.out.println("Monotonically increases with difficulty: " + increasesWithDifficulty);
        System.out.println("Difficulty-10-floor-4 Goblin is the toughest: " + brutalIsToughest);

        System.out.println("\n-- Level " + FloorFactory.BOSS_FLOOR + " boss check (should be FIXED stats regardless of difficulty) --");
        GameMap bossAtLowDifficulty = FloorFactory.generateFloor(FloorFactory.BOSS_FLOOR, DifficultyScaler.MIN_DIFFICULTY);
        GameMap bossAtHighDifficulty = FloorFactory.generateFloor(FloorFactory.BOSS_FLOOR, DifficultyScaler.MAX_DIFFICULTY);

        Enemy bossLow = bossAtLowDifficulty.getEnemyPlacements().get(0).getEnemy();
        Enemy bossHigh = bossAtHighDifficulty.getEnemyPlacements().get(0).getEnemy();

        System.out.println(bossLow.getClass().getSimpleName() + " at difficulty " + DifficultyScaler.MIN_DIFFICULTY
            + ": HP=" + bossLow.getMaxHp() + " ATK=" + bossLow.getAttack() + " DEF=" + bossLow.getDefense());
        System.out.println(bossHigh.getClass().getSimpleName() + " at difficulty " + DifficultyScaler.MAX_DIFFICULTY
            + ": HP=" + bossHigh.getMaxHp() + " ATK=" + bossHigh.getAttack() + " DEF=" + bossHigh.getDefense());

        boolean bossStatsIdentical = bossLow.getMaxHp() == bossHigh.getMaxHp()
            && bossLow.getAttack() == bossHigh.getAttack()
            && bossLow.getDefense() == bossHigh.getDefense();
        boolean bossHasExpectedStats = bossLow.getMaxHp() == 200 && bossLow.getAttack() == 12;
        System.out.println("\nBoss stats identical regardless of difficulty: " + bossStatsIdentical);
        System.out.println("Boss has the requested 200 HP / 12 ATK: " + bossHasExpectedStats);
    }
}
