package com.dungeonrpg.map;

import java.io.Serializable;

/**
 * One cell of the dungeon grid. Deliberately dumb: a Tile knows its own
 * position and type, and nothing else — it doesn't know about the hero,
 * enemies, or rendering. Keeping it this simple is what makes GameMap's
 * "Tile[][]" composition clean: the grid is just data, and all the
 * interesting logic (movement rules, encounters) lives one level up.
 */
public class Tile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;
    private final TileType type;

    public Tile(int row, int col, TileType type) {
        this.row = row;
        this.col = col;
        this.type = type;
    }

    public boolean isWalkable() {
        return type != TileType.WALL;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public TileType getType() {
        return type;
    }
}
