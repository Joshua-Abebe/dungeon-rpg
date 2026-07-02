package com.dungeonrpg.map;

/**
 * The four kinds of ground a Tile can be. An enum is the right tool here
 * (not a class hierarchy) because these are a small, fixed, closed set of
 * values with no behavior differences worth modeling as separate classes —
 * unlike Item or Character, where each subtype genuinely does different
 * things.
 */
public enum TileType {
    WALL,
    FLOOR,
    STAIRS,
    CHEST
}
