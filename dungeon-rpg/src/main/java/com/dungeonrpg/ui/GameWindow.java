package com.dungeonrpg.ui;

import com.dungeonrpg.combat.CombatOutcome;
import com.dungeonrpg.entity.Enemy;
import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.exception.SaveFileCorruptedException;
import com.dungeonrpg.item.Item;
import com.dungeonrpg.map.Direction;
import com.dungeonrpg.map.EnemyWanderThread;
import com.dungeonrpg.map.FloorFactory;
import com.dungeonrpg.map.GameMap;
import com.dungeonrpg.save.AutosaveThread;
import com.dungeonrpg.save.SaveData;
import com.dungeonrpg.save.SaveManager;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * GameWindow is the top-level Swing frame. It hosts three views inside a
 * CardLayout — map, combat, and an end screen — and switches between
 * them based on what happens during play. GameWindow itself doesn't
 * contain any game rules (no damage math, no movement validation, no
 * loot tables); it just reacts to what GameMap, CombatEngine, and
 * FloorFactory report, which is what keeps this class from growing into
 * an unmanageable God-object as more screens get added.
 */
public class GameWindow extends JFrame {

    private static final String MAP_CARD = "MAP";
    private static final String COMBAT_CARD = "COMBAT";
    private static final String END_CARD = "END";
    private static final String RULES_CARD = "RULES";
    private static final File SAVE_FILE = new File("dungeon-save.dat");

    private GameMap map;
    private Hero hero;
    private MapPanel mapPanel;
    private final JLabel statusLabel;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardContainer = new JPanel(cardLayout);

    private CombatPanel currentCombatPanel;
    private JPanel currentEndPanel;
    private RuleBuilderPanel rulesPanel;
    private boolean inCombat = false;
    private boolean gameEnded = false;
    private boolean inRuleBuilder = false;

    // Guards every point where `hero` or `map` is REASSIGNED to a new
    // object (floor change, restart) or read as a consistent pair for
    // saving. AutosaveThread reads both fields from a background thread
    // while the EDT can reassign them at almost any time (reaching the
    // stairs, dying) — without this lock, autosave could read a Hero
    // that belongs to floor 2 alongside a GameMap that's already been
    // replaced with floor 3.
    private final Object stateLock = new Object();

    private EnemyWanderThread wanderThread;
    private final AutosaveThread autosaveThread;
    private final Supplier<Hero> heroFactory;
    private final int difficulty;
    private final Timer heroMoveTimer;
    private static final String FIGHT_CARD = "FIGHT";
    private static final int HERO_TICK_MS = 400;

    public GameWindow(GameMap map, Hero hero, Supplier<Hero> heroFactory, int difficulty) {
        super("Dungeon RPG");
        this.map = map;
        this.hero = hero;
        this.heroFactory = heroFactory;
        this.difficulty = difficulty;
        this.mapPanel = new MapPanel(map, hero);
        this.statusLabel = new JLabel();
        statusLabel.setOpaque(true);
        statusLabel.setBackground(UiTheme.BG_PANEL);
        statusLabel.setForeground(UiTheme.TEXT_PRIMARY);
        statusLabel.setFont(UiTheme.FONT_BODY_BOLD);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        setJMenuBar(buildMenuBar());
        setLayout(new BorderLayout());
        cardContainer.add(mapPanel, MAP_CARD);
        add(cardContainer, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        bindTurnKeys();
        updateStatus();

        this.autosaveThread = new AutosaveThread(this::snapshotForSave, SAVE_FILE);
        autosaveThread.start();
        restartWanderThread();

        // Drives the hero's auto-patrol: fires on the EDT every
        // HERO_TICK_MS, regardless of which GameMap instance `map`
        // currently points to, so floor changes don't need to restart
        // this timer the way the background wander thread does.
        this.heroMoveTimer = new Timer(HERO_TICK_MS, e -> tickHero());
        heroMoveTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                autosaveThread.stopAutosaving();
                heroMoveTimer.stop();
                if (wanderThread != null) {
                    wanderThread.stopWandering();
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");

        JMenuItem saveItem = new JMenuItem("Save Game");
        saveItem.addActionListener(e -> handleSave());
        JMenuItem loadItem = new JMenuItem("Load Game");
        loadItem.addActionListener(e -> handleLoad());
        JMenuItem mainMenuItem = new JMenuItem("Main Menu");
        mainMenuItem.addActionListener(e -> returnToMainMenu());
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(saveItem);
        gameMenu.add(loadItem);
        gameMenu.addSeparator();
        gameMenu.add(mainMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(quitItem);
        menuBar.add(gameMenu);

        JMenu rulesMenu = new JMenu("Rules");
        JMenuItem openBuilderItem = new JMenuItem("Rule Builder");
        openBuilderItem.addActionListener(e -> openRuleBuilder());
        rulesMenu.add(openBuilderItem);
        menuBar.add(rulesMenu);

        return menuBar;
    }

    private void openRuleBuilder() {
        if (inCombat || gameEnded) {
            JOptionPane.showMessageDialog(this, "Can't open the Rule Builder during combat or after the game has ended.");
            return;
        }
        inRuleBuilder = true;
        if (rulesPanel == null) {
            rulesPanel = new RuleBuilderPanel(hero, this::closeRuleBuilder);
            JScrollPane scrollPane = new JScrollPane(rulesPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            cardContainer.add(scrollPane, RULES_CARD);
        }
        cardLayout.show(cardContainer, RULES_CARD);
    }

    private void closeRuleBuilder() {
        inRuleBuilder = false;
        cardLayout.show(cardContainer, MAP_CARD);
    }

    private SaveData snapshotForSave() {
        synchronized (stateLock) {
            return new SaveData(hero, map);
        }
    }

    private void handleSave() {
        if (inCombat || gameEnded) {
            JOptionPane.showMessageDialog(this, "Can't save during combat or after the game has ended.");
            return;
        }
        try {
            SaveManager.save(snapshotForSave(), SAVE_FILE);
            JOptionPane.showMessageDialog(this, "Game saved.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage(),
                "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLoad() {
        try {
            SaveData data = SaveManager.load(SAVE_FILE);
            synchronized (stateLock) {
                hero = data.getHero();
                map = data.getMap();
            }
            inCombat = false;
            gameEnded = false;
            swapMapPanel();
            cardLayout.show(cardContainer, MAP_CARD);
            updateStatus();
            JOptionPane.showMessageDialog(this, "Game loaded.");
        } catch (SaveFileCorruptedException e) {
            JOptionPane.showMessageDialog(this, "Could not load save: " + e.getMessage(),
                "Load error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Replaces mapPanel with a fresh one bound to the current `map`, and (re)starts the wander thread for it. */
    private void swapMapPanel() {
        cardContainer.remove(mapPanel);
        mapPanel = new MapPanel(map, hero);
        cardContainer.add(mapPanel, MAP_CARD);
        restartWanderThread();
    }

    private void restartWanderThread() {
        if (wanderThread != null) {
            wanderThread.stopWandering();
        }
        wanderThread = new EnemyWanderThread(map, mapPanel::repaint, this::onEnemyCaughtHero, this::onEnemiesEvaded);
        wanderThread.start();
    }

    /**
     * Called when the player successfully outruns a chasing enemy. This
     * is the "risk vs reward" reminder from the design brief: bypassing
     * is a legitimate strategy, but it costs the loot and gold that
     * only come from actually winning a fight — the player should know
     * that trade-off happened, not just silently keep moving.
     */
    private void onEnemiesEvaded(java.util.List<Enemy> evaded) {
        if (inCombat || gameEnded || inRuleBuilder) {
            return;
        }
        String names = evaded.stream().map(Enemy::getName).reduce((a, b) -> a + ", " + b).orElse("an enemy");
        statusLabel.setText("Evaded " + names + "! (no loot or gold from a fight you skip)");
    }

    /** Called (on the EDT, via EnemyWanderThread's callback) when an aggro-chasing enemy lands on the hero's tile. */
    private void onEnemyCaughtHero(Enemy enemy) {
        if (!inCombat && !gameEnded && !inRuleBuilder) {
            startCombat(enemy);
        }
    }

    private void bindTurnKeys() {
        bindKey(KeyEvent.VK_UP, Direction.UP);
        bindKey(KeyEvent.VK_DOWN, Direction.DOWN);
        bindKey(KeyEvent.VK_LEFT, Direction.LEFT);
        bindKey(KeyEvent.VK_RIGHT, Direction.RIGHT);
    }

    private void bindKey(int keyCode, Direction direction) {
        String actionName = "turn-" + direction;
        JComponent content = (JComponent) getContentPane();

        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(keyCode, 0), actionName);

        content.getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Arrow keys no longer move the hero directly — they
                // just record which way the player WANTS to turn. The
                // hero keeps auto-patrolling either way; tickHero()
                // decides whether that turn is actually possible yet.
                if (!inCombat && !gameEnded && !inRuleBuilder) {
                    map.setPendingTurn(direction);
                }
            }
        });
    }

    /**
     * Runs on a fixed timer (HERO_TICK_MS) regardless of player input —
     * this is what makes the hero auto-patrol. Every tick: advance the
     * hero one tile (bouncing off walls, taking a buffered turn if one
     * was queued and is open), then run the exact same post-move checks
     * the old direct-input handleMove() used to run inline: chests,
     * landing on an enemy, reaching the stairs.
     */
    private void tickHero() {
        if (inCombat || gameEnded || inRuleBuilder) {
            return;
        }

        map.tickHeroMovement();

        map.tryOpenChest(map.getHeroRow(), map.getHeroCol()).ifPresent(this::onChestOpened);
        map.getEnemyAt(map.getHeroRow(), map.getHeroCol()).ifPresent(this::startCombat);

        if (!inCombat) {
            if (map.isHeroOnStairs()) {
                advanceFloor();
            } else {
                updateStatus();
            }
            mapPanel.repaint();
        }
    }

    private void onChestOpened(Item item) {
        hero.addItem(item);
        JOptionPane.showMessageDialog(this,
            hero.getName() + " found a " + item.getName() + "!\n" + item.getDescription(),
            "Chest opened", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startCombat(Enemy enemy) {
        if (inCombat) {
            return; // already mid-fight (e.g. hero and an aggro enemy collided on the same tick)
        }
        inCombat = true;

        FightFlashPanel flash = new FightFlashPanel(() -> showCombatScreen(enemy), map.getFloorNumber());
        cardContainer.add(flash, FIGHT_CARD);
        cardLayout.show(cardContainer, FIGHT_CARD);
    }

    private void showCombatScreen(Enemy enemy) {
        if (currentCombatPanel != null) {
            cardContainer.remove(currentCombatPanel);
        }
        currentCombatPanel = new CombatPanel(hero, enemy, this::onCombatEnd, map.getFloorNumber());
        cardContainer.add(currentCombatPanel, COMBAT_CARD);
        cardLayout.show(cardContainer, COMBAT_CARD);
    }

    private void onCombatEnd(CombatOutcome outcome) {
        Enemy defeatedEnemy = getEnemyInProgress();

        switch (outcome) {
            case HERO_WON -> {
                if (defeatedEnemy != null) {
                    hero.addGold(defeatedEnemy.takeLootGold());
                    map.removeEnemy(defeatedEnemy);
                }
                statusLabel.setText(hero.getName() + " won the fight!");
            }
            case HERO_FLED -> {
                inCombat = false;
                showFledScreen();
                return;
            }
            case HERO_DIED -> {
                inCombat = false;
                showGameOver();
                return;
            }
            case ONGOING -> throw new IllegalStateException("onCombatEnd called with ONGOING outcome");
        }

        inCombat = false;
        cardLayout.show(cardContainer, MAP_CARD);
        mapPanel.repaint();
        updateStatus();
    }

    private Enemy getEnemyInProgress() {
        return map.getEnemyAt(map.getHeroRow(), map.getHeroCol()).orElse(null);
    }

    /**
     * Called when the hero reaches an unlocked staircase. Either loads
     * the next floor, or — if this was the last floor — ends the game
     * in victory. FloorFactory owns the actual "how many floors are
     * there and what's on them" decision; this method just asks for the
     * next one and reacts to whether it exists.
     */
    private void advanceFloor() {
        int nextFloor = map.getFloorNumber() + 1;
        if (nextFloor > FloorFactory.TOTAL_FLOORS) {
            showVictory();
            return;
        }

        synchronized (stateLock) {
            map = FloorFactory.generateFloor(nextFloor, difficulty);
        }
        swapMapPanel();
        cardLayout.show(cardContainer, MAP_CARD);
        updateStatus();
    }

    private void showVictory() {
        gameEnded = true;
        showEndScreen(new EndScreenPanel(
            "Victory!",
            hero.getName() + " defeats the dinosaur and rescues the princess!",
            UiTheme.HP_HIGH,
            this::restartGame,
            () -> System.exit(0)));
    }

    private void showGameOver() {
        gameEnded = true;
        showEndScreen(new EndScreenPanel(
            "You Died",
            hero.getName() + " has fallen in the dungeon.",
            UiTheme.HP_LOW,
            this::restartGame,
            () -> System.exit(0)));
    }

    /**
     * Fleeing ends the run, same as dying — a deliberate design choice
     * (not a bug): if retreating had no real cost, "Flee" would be a
     * risk-free escape hatch instead of a genuine last-resort decision.
     * Distinct flavor text from the death screen so the player can tell
     * at a glance why their run ended.
     */
    private void showFledScreen() {
        gameEnded = true;
        showEndScreen(new EndScreenPanel(
            "You Fled",
            hero.getName() + " abandoned the fight and the dungeon claims the rest.",
            UiTheme.HP_LOW,
            this::restartGame,
            () -> System.exit(0)));
    }

    private void showEndScreen(JPanel endScreen) {
        if (currentEndPanel != null) {
            cardContainer.remove(currentEndPanel);
        }
        currentEndPanel = endScreen;
        cardContainer.add(currentEndPanel, END_CARD);
        cardLayout.show(cardContainer, END_CARD);
    }

    /**
     * Resets the game to a brand new run: a fresh Hero of whichever
     * class the player originally chose (via heroFactory, supplied by
     * HeroSelectFrame), back on floor 1. Using the factory rather than
     * hardcoding a class here is what makes "Play Again" correctly
     * respect a Mage or Rogue run instead of silently downgrading every
     * restart back to a Warrior.
     */
    /**
     * Returns to the hero selection screen without closing and
     * relaunching the whole application. Every background thread this
     * window owns must be explicitly stopped first — daemon threads
     * would otherwise keep running against a GameMap nobody can see
     * anymore, for as long as the JVM stays alive.
     */
    private void returnToMainMenu() {
        autosaveThread.stopAutosaving();
        heroMoveTimer.stop();
        if (wanderThread != null) {
            wanderThread.stopWandering();
        }
        dispose();
        new HeroSelectFrame().setVisible(true);
    }

    private void restartGame() {
        gameEnded = false;
        synchronized (stateLock) {
            hero = heroFactory.get();
            map = FloorFactory.generateFloor(1, difficulty);
        }
        swapMapPanel();
        cardLayout.show(cardContainer, MAP_CARD);
        updateStatus();
    }

    private void updateStatus() {
        int remaining = map.getEnemyPlacements().size();
        statusLabel.setText("Floor " + map.getFloorNumber() + " / " + FloorFactory.TOTAL_FLOORS
            + "  |  " + hero.getName() + " HP: " + hero.getHp() + "/" + hero.getMaxHp()
            + "  |  Gold: " + hero.getGold()
            + "  |  Enemies remaining: " + remaining);
    }
}


