package com.dungeonrpg.exception;

/**
 * Thrown when the player tries to use or drop an item that isn't in their
 * inventory. This is a CHECKED exception on purpose: using an item is a
 * routine, expected user action (unlike a corrupted save file, which is
 * closer to catastrophic) — so callers are required to handle the "you
 * don't have that" case explicitly rather than letting it crash the game.
 */
public class EmptyInventoryException extends Exception {
    public EmptyInventoryException(String message) {
        super(message);
    }
}
