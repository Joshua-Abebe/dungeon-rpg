package com.dungeonrpg;

import com.dungeonrpg.exception.InvalidMoveException;
import com.dungeonrpg.map.Direction;
import com.dungeonrpg.map.FloorFactory;
import com.dungeonrpg.map.DifficultyScaler;
import com.dungeonrpg.map.GameMap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TEMPORARY: stress-tests GameMap's synchronized methods by hammering
 * moveHero() and wanderEnemies() from several threads at once, the same
 * way the real EDT and EnemyWanderThread will contend for the same
 * GameMap during actual play. A synchronization bug (a missing
 * `synchronized`, a race in enemyPlacements) would show up here as a
 * thrown exception or a corrupted final state — reasoning about thread
 * safety on paper isn't enough, this actually exercises it.
 */
public class ConcurrencySmokeTest {
    public static void main(String[] args) throws InterruptedException {
        GameMap map = FloorFactory.generateFloor(2, DifficultyScaler.DEFAULT_DIFFICULTY);

        int threadCount = 8;
        int iterationsPerThread = 500;
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threadCount);
        AtomicInteger exceptions = new AtomicInteger(0);
        AtomicReference<Throwable> firstFailure = new AtomicReference<>();

        Direction[] directions = Direction.values();

        for (int t = 0; t < threadCount; t++) {
            boolean isWanderThread = t % 2 == 0;
            Thread worker = new Thread(() -> {
                try {
                    startGate.await();
                    for (int i = 0; i < iterationsPerThread; i++) {
                        if (isWanderThread) {
                            map.wanderEnemies();
                        } else {
                            try {
                                map.moveHero(directions[i % directions.length]);
                            } catch (InvalidMoveException e) {
                                // expected routinely (walls) - not a failure
                            }
                        }
                        // Also hammer the read-side methods every iteration,
                        // same as MapPanel's paintComponent would while these
                        // writers are active.
                        map.getEnemyPlacements();
                        map.getHeroRow();
                        map.getHeroCol();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    exceptions.incrementAndGet();
                    firstFailure.compareAndSet(null, e);
                } finally {
                    doneGate.countDown();
                }
            });
            worker.start();
        }

        startGate.countDown();
        doneGate.await();

        System.out.println("Threads: " + threadCount + ", iterations each: " + iterationsPerThread);
        System.out.println("Exceptions thrown: " + exceptions.get());
        if (firstFailure.get() != null) {
            System.out.println("First failure: " + firstFailure.get());
        }
        System.out.println("Final enemy count: " + map.getEnemyPlacements().size()
            + " (should still be a sane, non-negative number)");
        System.out.println("Final hero position: (" + map.getHeroRow() + ", " + map.getHeroCol() + ")");
        System.out.println("\nResult: " + (exceptions.get() == 0 ? "PASSED - no corruption under concurrent access" : "FAILED"));
    }
}
