package com.dungeonrpg.map;

import com.dungeonrpg.entity.Enemy;

import java.io.Serializable;

/**
 * Pairs an Enemy with the row/col it currently stands on. A dedicated
 * class instead of a raw Map<Enemy, int[]> or two parallel lists — the
 * explicit type makes GameMap's code read as "here is a placement" rather
 * than "here is some array whose meaning you have to remember," which
 * matters for the "even a novice can follow this" goal.
 */
public class EnemyPlacement implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Enemy enemy;
    private int row;
    private int col;

    public EnemyPlacement(Enemy enemy, int row, int col) {
        this.enemy = enemy;
        this.row = row;
        this.col = col;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isAt(int row, int col) {
        return this.row == row && this.col == col;
    }

    // Tracks whether this enemy was aggroed (chasing) as of the last
    // wanderEnemies() tick, purely so GameMap can detect the MOMENT an
    // enemy gives up the chase (aggro -> not aggro) and report that as
    // an "evaded" event — not gameplay state, just a one-tick memory
    // for the risk/reward evasion notice.
    private boolean aggro = false;

    boolean isAggro() {
        return aggro;
    }

    void setAggro(boolean aggro) {
        this.aggro = aggro;
    }

    /**
     * Updates this placement's position. Only called from
     * GameMap.wanderEnemies() (itself synchronized), which is what keeps
     * this mutation safe even though it happens on a background thread
     * while the EDT might simultaneously be reading positions to render
     * the map.
     */
    void moveTo(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
    }
}
