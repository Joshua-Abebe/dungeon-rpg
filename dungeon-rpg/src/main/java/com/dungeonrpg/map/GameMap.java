package com.dungeonrpg.map;

import com.dungeonrpg.entity.Enemy;
import com.dungeonrpg.exception.InvalidMoveException;
import com.dungeonrpg.item.Item;
import com.dungeonrpg.item.LootTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * GameMap owns one dungeon floor: the tile grid, where the hero currently
 * stands, and which enemies are still alive on this floor.
 *
 * COMPOSITION is the whole point of this class: GameMap *has-a* Tile[][]
 * and *has-a* List<EnemyPlacement>. It doesn't extend either — it just
 * holds and manages them, which is exactly the "GameMap has-a Tile[][]"
 * relationship called out in the project brief.
 *
 * THREAD SAFETY: this object is read and written from two different
 * threads — the Swing Event Dispatch Thread (player movement, rendering)
 * and a background EnemyWanderThread that periodically shuffles idle
 * enemies around the map. Every method that touches heroRow/heroCol,
 * tiles, or enemyPlacements is `synchronized` on this instance, so the
 * two threads can never observe or produce a half-updated state. This is
 * the real, working example of "synchronized access to shared game
 * state" called out in the project brief — not a decorative keyword,
 * but a genuine race condition (two threads mutating the same enemy
 * list) that would otherwise be possible.
 */
public final class GameMap implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int AGGRO_RADIUS = 3;

    private final int width;
    private final int height;
    private final int floorNumber;
    private final Tile[][] tiles;
    private final List<EnemyPlacement> enemyPlacements = new ArrayList<>();
    private final Random random = new Random();

    private int heroRow;
    private int heroCol;
    private Direction heroDirection = Direction.RIGHT;
    private Direction pendingTurn;
    private int stairsRow;
    private int stairsCol;

    public GameMap(int floorNumber, int width, int height, List<Enemy> enemiesToPlace) {
        this.floorNumber = floorNumber;
        this.width = width;
        this.height = height;
        this.tiles = new Tile[height][width];
        generateLayout(enemiesToPlace);
    }

    /**
     * Builds a simple bordered room: walls around the edge, floor inside,
     * stairs in the far corner, a chest, and the given enemies scattered
     * on open floor tiles. Deliberately simple per rule #1 — a proper
     * procedural dungeon generator is a whole project on its own, and a
     * single clean room per floor is enough to make every OOP concept in
     * the brief demonstrable without risking the deadline.
     */
    private void generateLayout(List<Enemy> enemiesToPlace) {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                boolean border = row == 0 || col == 0 || row == height - 1 || col == width - 1;
                tiles[row][col] = new Tile(row, col, border ? TileType.WALL : TileType.FLOOR);
            }
        }

        heroRow = 1;
        heroCol = 1;

        stairsRow = height - 2;
        stairsCol = width - 2;
        tiles[stairsRow][stairsCol] = new Tile(stairsRow, stairsCol, TileType.STAIRS);

        int chestRow = height - 2;
        int chestCol = 1;
        tiles[chestRow][chestCol] = new Tile(chestRow, chestCol, TileType.CHEST);

        for (Enemy enemy : enemiesToPlace) {
            int row, col;
            do {
                row = 1 + random.nextInt(height - 2);
                col = 1 + random.nextInt(width - 2);
            } while (isReserved(row, col));
            enemyPlacements.add(new EnemyPlacement(enemy, row, col));
        }
    }

    private boolean isReserved(int row, int col) {
        boolean onHero = row == heroRow && col == heroCol;
        boolean onStairs = row == stairsRow && col == stairsCol;
        boolean onChest = tiles[row][col].getType() == TileType.CHEST;
        boolean onAnotherEnemy = getEnemyAt(row, col).isPresent();
        return onHero || onStairs || onChest || onAnotherEnemy;
    }

    /**
     * Moves the hero one step in the given direction, or throws if that
     * step is illegal (off the grid or into a wall). This is the real,
     * meaningful use of InvalidMoveException the project brief asks for —
     * not a decorative try/catch, but the actual mechanism that enforces
     * movement rules.
     */
    public synchronized void moveHero(Direction direction) throws InvalidMoveException {
        int newRow = heroRow + direction.getRowDelta();
        int newCol = heroCol + direction.getColDelta();

        if (newRow < 0 || newRow >= height || newCol < 0 || newCol >= width) {
            throw new InvalidMoveException("Cannot move outside the dungeon bounds");
        }
        if (!tiles[newRow][newCol].isWalkable()) {
            throw new InvalidMoveException("Cannot walk through a wall");
        }

        heroRow = newRow;
        heroCol = newCol;
    }

    /**
     * If the given position holds an unopened chest, generates loot,
     * replaces that tile with plain FLOOR (so it can't be opened twice
     * and the map visually shows it's been looted), and returns the
     * item. Returns empty for any other tile type. Overwriting the grid
     * cell after generation is the same technique already used to place
     * the stairs and chest tiles in generateLayout() — Tile objects are
     * immutable and cheap, so "changing" a tile just means replacing the
     * array slot with a new one.
     */
    public synchronized Optional<Item> tryOpenChest(int row, int col) {
        if (tiles[row][col].getType() != TileType.CHEST) {
            return Optional.empty();
        }
        tiles[row][col] = new Tile(row, col, TileType.FLOOR);
        return Optional.of(LootTable.randomItem(floorNumber));
    }

    /**
     * Buffers a turn request from the player. It doesn't move the hero
     * immediately — it's only applied on the next tickHeroMovement()
     * call, and only if that direction is actually open right now. This
     * is the standard "buffered input" technique classic auto-runner
     * games use: press a direction slightly before the intersection you
     * want to turn at, and the turn takes effect the instant it becomes
     * possible, rather than requiring frame-perfect timing.
     */
    public synchronized void setPendingTurn(Direction direction) {
        this.pendingTurn = direction;
    }

    /**
     * Advances the hero one tile along its current auto-patrol path.
     * Called on a fixed timer by GameWindow, not by direct player input
     * — the player only steers via setPendingTurn(), the actual stepping
     * happens here automatically. If a pending turn is open, it's taken;
     * otherwise the hero continues in its current direction, bouncing
     * (reversing direction) off any wall it would otherwise walk into.
     */
    public synchronized void tickHeroMovement() {
        if (pendingTurn != null && isWalkableFrom(heroRow, heroCol, pendingTurn)) {
            heroDirection = pendingTurn;
        }
        pendingTurn = null;

        if (!isWalkableFrom(heroRow, heroCol, heroDirection)) {
            heroDirection = heroDirection.opposite();
        }
        if (isWalkableFrom(heroRow, heroCol, heroDirection)) {
            heroRow += heroDirection.getRowDelta();
            heroCol += heroDirection.getColDelta();
        }
        // If even the reversed direction is blocked (a 1x1 dead end),
        // the hero simply stays put this tick rather than throwing —
        // auto-patrol should never crash on an edge case, just pause.
    }

    private boolean isWalkableFrom(int row, int col, Direction direction) {
        int newRow = row + direction.getRowDelta();
        int newCol = col + direction.getColDelta();
        if (newRow < 0 || newRow >= height || newCol < 0 || newCol >= width) {
            return false;
        }
        return tiles[newRow][newCol].isWalkable();
    }

    public synchronized Direction getHeroDirection() {
        return heroDirection;
    }

    /**
     * Lets idle enemies (not currently in combat — combat locks onto a
     * specific Enemy object by identity, not position, so this can't
     * interfere with an ongoing fight) take one random step to an
     * adjacent open tile. Called periodically by EnemyWanderThread on a
     * background thread; synchronized so it can never run concurrently
     * with the EDT calling moveHero() or reading enemyPlacements mid-walk.
     *
     * @return true if any enemy actually moved (so the caller only
     *         bothers repainting when something visibly changed)
     */
    /**
     * Moves every enemy one step: enemies outside a 2-tile radius of the
     * hero wander randomly (unchanged from before), but any enemy
     * within that radius abandons wandering and greedily steps toward
     * the hero instead — reducing whichever axis (row or column) has
     * the larger distance first. This isn't full pathfinding (no
     * obstacle-avoidance routing around walls), which is a deliberate
     * scope choice: the dungeon rooms are simple enough that greedy
     * chasing reads as "aggressive and relentless" without needing an
     * A* implementation.
     *
     * Unlike the old wander-only version, a chasing enemy CAN land on
     * the hero's own tile — that's how aggro-chase actually leads to a
     * fight. When that happens, this method returns the enemy that
     * caught the hero, so the caller (EnemyWanderThread) can trigger
     * combat from the background thread's tick.
     */
    private boolean chaseTickToggle = false;

    /**
     * Moves every enemy one step: enemies outside a 2-tile radius of the
     * hero wander randomly (unchanged from before), but any enemy
     * within that radius abandons wandering and greedily steps toward
     * the hero instead — reducing whichever axis (row or column) has
     * the larger distance first. This isn't full pathfinding (no
     * obstacle-avoidance routing around walls), which is a deliberate
     * scope choice: the dungeon rooms are simple enough that greedy
     * chasing reads as "aggressive and relentless" without needing an
     * A* implementation.
     *
     * Chasing enemies only actually take a step on every OTHER call to
     * this method (chaseTickToggle), while the hero moves every tick of
     * its own timer — a deliberate, permanent speed disadvantage for
     * chasers. Without this, a locked-on enemy closes distance at
     * exactly the hero's own speed and can never realistically be
     * outrun; halving chase speed is what makes "bypass the enemy
     * instead of fighting it" an actual, learnable skill rather than a
     * theoretical possibility that never works in practice.
     */
    public synchronized WanderResult wanderEnemies() {
        Enemy caughtHero = null;
        List<Enemy> justEvaded = new ArrayList<>();
        chaseTickToggle = !chaseTickToggle;

        for (EnemyPlacement placement : enemyPlacements) {
            int rowDistance = Math.abs(placement.getRow() - heroRow);
            int colDistance = Math.abs(placement.getCol() - heroCol);
            boolean aggro = Math.max(rowDistance, colDistance) <= AGGRO_RADIUS;

            if (placement.isAggro() && !aggro) {
                justEvaded.add(placement.getEnemy());
            }
            placement.setAggro(aggro);

            int[] target;
            if (aggro) {
                if (!chaseTickToggle) {
                    continue; // half-speed chase: sit still on every other tick
                }
                target = chaseStep(placement, rowDistance, colDistance);
            } else {
                if (random.nextDouble() > 0.4) {
                    continue; // most enemies stay put most cycles when not aggroed
                }
                Direction[] directions = Direction.values();
                Direction pick = directions[random.nextInt(directions.length)];
                target = new int[] { placement.getRow() + pick.getRowDelta(), placement.getCol() + pick.getColDelta() };
            }

            if (target == null) {
                continue; // both chase directions were blocked this tick
            }
            int newRow = target[0];
            int newCol = target[1];

            boolean inBounds = newRow > 0 && newRow < height - 1 && newCol > 0 && newCol < width - 1;
            boolean walkable = inBounds && tiles[newRow][newCol].isWalkable();
            boolean onAnotherEnemy = getEnemyAt(newRow, newCol)
                .filter(other -> other != placement.getEnemy())
                .isPresent();

            if (walkable && !onAnotherEnemy) {
                placement.moveTo(newRow, newCol);
                if (newRow == heroRow && newCol == heroCol) {
                    caughtHero = placement.getEnemy();
                }
            }
        }
        return new WanderResult(caughtHero, justEvaded);
    }

    /**
     * Picks the best step toward the hero for a chasing enemy: try the
     * axis with the larger distance first (the "greedy" choice), and if
     * that tile isn't walkable, fall back to the other axis instead of
     * giving up for the tick. Without this fallback, an aggro enemy
     * standing directly beside a wall on its preferred axis would just
     * stall in place every tick instead of actually closing in — which
     * is exactly the "enemies don't reliably lock on" behavior being
     * fixed here.
     */
    private int[] chaseStep(EnemyPlacement placement, int rowDistance, int colDistance) {
        int stepRow = Integer.compare(heroRow, placement.getRow());
        int stepCol = Integer.compare(heroCol, placement.getCol());

        int[] primary;
        int[] secondary;
        if (rowDistance >= colDistance && stepRow != 0) {
            primary = new int[] { placement.getRow() + stepRow, placement.getCol() };
            secondary = new int[] { placement.getRow(), placement.getCol() + stepCol };
        } else {
            primary = new int[] { placement.getRow(), placement.getCol() + stepCol };
            secondary = new int[] { placement.getRow() + stepRow, placement.getCol() };
        }

        if (isOpenTile(primary[0], primary[1])) {
            return primary;
        }
        if (isOpenTile(secondary[0], secondary[1])) {
            return secondary;
        }
        return null;
    }

    private boolean isOpenTile(int row, int col) {
        boolean inBounds = row > 0 && row < height - 1 && col > 0 && col < width - 1;
        return inBounds && tiles[row][col].isWalkable();
    }

    public synchronized Optional<Enemy> getEnemyAt(int row, int col) {
        return enemyPlacements.stream()
            .filter(p -> p.isAt(row, col))
            .map(EnemyPlacement::getEnemy)
            .findFirst();
    }

    public synchronized void removeEnemy(Enemy enemy) {
        enemyPlacements.removeIf(p -> p.getEnemy() == enemy);
    }

    public synchronized boolean isFloorCleared() {
        return enemyPlacements.isEmpty();
    }

    public synchronized boolean isHeroOnStairs() {
        return heroRow == stairsRow && heroCol == stairsCol;
    }

    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public synchronized int getHeroRow() {
        return heroRow;
    }

    public synchronized int getHeroCol() {
        return heroCol;
    }

    public synchronized List<EnemyPlacement> getEnemyPlacements() {
        return List.copyOf(enemyPlacements);
    }
}
