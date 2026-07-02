package com.dungeonrpg.map;

/**
 * DifficultyScaler owns the "how hard should this floor be" math. The
 * player picks a difficulty (1-10) once, at the start of a run, in
 * HeroSelectFrame; this class turns that single number plus the current
 * floor into a stat multiplier applied to every enemy on that floor via
 * Character.scaleStats(). Runtime scaling is exponential in the floor
 * number (Math.pow(GROWTH_RATE, floorNumber - 1)) — the brief explicitly
 * asks for the curve to start accessible but ramp up drastically at
 * higher floors/difficulties, which a linear formula wouldn't deliver
 * (a linear curve stays "fair," an exponential one eventually
 * overwhelms even a skilled player, which is the actual goal here).
 */
public final class DifficultyScaler {

    private DifficultyScaler() {
    }

    public static final int MIN_DIFFICULTY = 1;
    public static final int MAX_DIFFICULTY = 10;
    public static final int DEFAULT_DIFFICULTY = 3;

    private static final double GROWTH_RATE = 1.3;

    public static double multiplierFor(int floorNumber, int difficulty) {
        int clamped = Math.max(MIN_DIFFICULTY, Math.min(MAX_DIFFICULTY, difficulty));
        double difficultyFactor = 0.5 + clamped * 0.15;
        double floorGrowth = Math.pow(GROWTH_RATE, floorNumber - 1);
        return difficultyFactor * floorGrowth;
    }
}
