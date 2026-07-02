package com.dungeonrpg.exception;

/**
 * Thrown when a save file can't be read back — missing, truncated, from
 * an incompatible version, or otherwise not a valid game save. Checked
 * on purpose: loading a save is exactly the kind of operation that can
 * fail for reasons outside the program's control (a deleted file, a disk
 * error, a save from a different build), so the caller is required to
 * handle that possibility explicitly rather than the game silently
 * crashing on a bad file.
 */
public class SaveFileCorruptedException extends Exception {
    public SaveFileCorruptedException(String message) {
        super(message);
    }
}
