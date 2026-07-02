package com.dungeonrpg.map;

import com.dungeonrpg.entity.Enemy;

import java.util.List;
import java.util.Optional;

/**
 * The outcome of one GameMap.wanderEnemies() tick: which enemy (if any)
 * caught the hero, and which enemies just gave up an active chase this
 * tick (the hero successfully put enough distance between them). Both
 * pieces of information come out of the same movement pass, so bundling
 * them in one small result object avoids GameMap needing two separate
 * methods that would otherwise have to duplicate the same iteration.
 */
public class WanderResult {

    private final Enemy caughtHero;
    private final List<Enemy> justEvaded;

    public WanderResult(Enemy caughtHero, List<Enemy> justEvaded) {
        this.caughtHero = caughtHero;
        this.justEvaded = justEvaded;
    }

    public Optional<Enemy> getCaughtHero() {
        return Optional.ofNullable(caughtHero);
    }

    public List<Enemy> getJustEvaded() {
        return justEvaded;
    }
}
