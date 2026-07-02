package com.dungeonrpg.exception;

/**
 * Thrown when the player tries to move onto a wall or off the edge of the
 * map. Checked on purpose, same reasoning as EmptyInventoryException:
 * walking into a wall is routine and expected (the player mashes an arrow
 * key near an edge constantly), so the caller is required to handle it
 * explicitly — in practice that just means "catch it and do nothing,"
 * but the compiler forcing that decision is the point.
 */
public class InvalidMoveException extends Exception {
    public InvalidMoveException(String message) {
        super(message);
    }
}
