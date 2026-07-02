package com.dungeonrpg.map;

import com.dungeonrpg.entity.Enemy;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * A background thread that moves enemies every tick: wandering
 * randomly by default, or aggressively chasing the hero once within
 * range (see GameMap.wanderEnemies()). This is the "enemy AI thread"
 * called out in the project brief — the actual decision-and-move work
 * runs entirely off the Swing Event Dispatch Thread, and results get
 * marshaled back onto the EDT via SwingUtilities.invokeLater(), which
 * is the correct, standard way to touch Swing components (or trigger
 * Swing-facing callbacks like starting combat) from a non-UI thread.
 */
public class EnemyWanderThread extends Thread {

    private static final int MIN_INTERVAL_MS = 400;
    private static final int MAX_INTERVAL_MS = 700;

    private final GameMap map;
    private final Runnable onMapChanged;
    private final Consumer<Enemy> onEnemyCaughtHero;
    private final Consumer<List<Enemy>> onEnemiesEvaded;
    private final Random random = new Random();
    private volatile boolean running = true;

    public EnemyWanderThread(GameMap map, Runnable onMapChanged, Consumer<Enemy> onEnemyCaughtHero,
                              Consumer<List<Enemy>> onEnemiesEvaded) {
        super("EnemyWanderThread-floor" + map.getFloorNumber());
        this.map = map;
        this.onMapChanged = onMapChanged;
        this.onEnemyCaughtHero = onEnemyCaughtHero;
        this.onEnemiesEvaded = onEnemiesEvaded;
        setDaemon(true); // never block the JVM from exiting on its own
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(MIN_INTERVAL_MS + random.nextInt(MAX_INTERVAL_MS - MIN_INTERVAL_MS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (!running) {
                return;
            }

            WanderResult result = map.wanderEnemies();
            SwingUtilities.invokeLater(onMapChanged);
            result.getCaughtHero().ifPresent(enemy -> SwingUtilities.invokeLater(() -> onEnemyCaughtHero.accept(enemy)));
            if (!result.getJustEvaded().isEmpty()) {
                SwingUtilities.invokeLater(() -> onEnemiesEvaded.accept(result.getJustEvaded()));
            }
        }
    }

    /** Stops the loop cleanly. Must be called whenever the floor changes or the window closes. */
    public void stopWandering() {
        running = false;
        interrupt();
    }
}
