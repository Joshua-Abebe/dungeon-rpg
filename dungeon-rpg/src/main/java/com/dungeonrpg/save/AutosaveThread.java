package com.dungeonrpg.save;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * A background thread that periodically writes the current game state
 * to disk, so the player is never more than ~30 seconds of progress from
 * a save without ever having to think about saving manually. The actual
 * file write (SaveManager.save()) can take a moment on a slow disk —
 * running it on a background thread means that moment never freezes the
 * game window, which is the whole reason this needs its own thread
 * rather than just running on a Swing Timer on the EDT.
 *
 * The Supplier<SaveData> is provided by GameWindow rather than this
 * class reaching into GameWindow's fields directly — AutosaveThread
 * doesn't need to know anything about Swing, hero classes, or floors,
 * it just needs "something that can hand me a SaveData when asked."
 */
public class AutosaveThread extends Thread {

    private static final int INTERVAL_MS = 30_000;

    private final Supplier<SaveData> saveDataSupplier;
    private final File file;
    private volatile boolean running = true;

    public AutosaveThread(Supplier<SaveData> saveDataSupplier, File file) {
        super("AutosaveThread");
        this.saveDataSupplier = saveDataSupplier;
        this.file = file;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (!running) {
                return;
            }

            try {
                SaveManager.save(saveDataSupplier.get(), file);
            } catch (IOException e) {
                // Autosave is a convenience, not a critical operation —
                // a failed autosave (disk full, permissions) shouldn't
                // crash the game or interrupt the player. Just note it
                // and try again on the next cycle.
                System.err.println("Autosave failed: " + e.getMessage());
            }
        }
    }

    public void stopAutosaving() {
        running = false;
        interrupt();
    }
}
