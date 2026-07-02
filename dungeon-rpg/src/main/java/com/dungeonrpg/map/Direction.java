package com.dungeonrpg.map;

/**
 * The four movement directions, each carrying its own row/col delta. This
 * is a small but genuine example of an enum with per-constant fields
 * (not just names) — each Direction knows how to apply itself to a
 * position, which keeps that arithmetic out of GameMap and out of the
 * key-listener code in the UI.
 */
public enum Direction {
    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1);

    private final int rowDelta;
    private final int colDelta;

    Direction(int rowDelta, int colDelta) {
        this.rowDelta = rowDelta;
        this.colDelta = colDelta;
    }

    public int getRowDelta() {
        return rowDelta;
    }

    public int getColDelta() {
        return colDelta;
    }

    /** Used for wall-bounce: reverse direction when the hero's auto-patrol hits a wall. */
    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
