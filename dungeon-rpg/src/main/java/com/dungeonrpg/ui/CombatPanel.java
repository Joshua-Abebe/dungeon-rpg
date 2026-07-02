package com.dungeonrpg.ui;

import com.dungeonrpg.combat.CombatEngine;
import com.dungeonrpg.combat.CombatOutcome;
import com.dungeonrpg.entity.Enemy;
import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.item.Item;
import com.dungeonrpg.ui.sprite.SpriteFactory;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * CombatPanel is the view half of the combat screen. Same responsibility
 * as before (render whatever CombatEngine reports, forward clicks to it),
 * now also driving two CombatantPortrait components: a lunge animation
 * on the attacker whenever an action resolves, and a flash + floating
 * damage number on whichever side's HP actually dropped. The "who got
 * hit and by how much" is worked out here by comparing HP before/after
 * each CombatEngine call — CombatEngine itself stays animation-agnostic,
 * it just reports outcomes and a text log, same as always.
 */
public class CombatPanel extends JPanel {

    private final CombatEngine engine;
    private final Consumer<CombatOutcome> onCombatEnd;

    private final CombatantPortrait heroPortrait;
    private final CombatantPortrait enemyPortrait;
    private final HealthBar heroHpBar = new HealthBar();
    private final HealthBar enemyHpBar = new HealthBar();
    private final JLabel heroLabel = new JLabel();
    private final JLabel enemyLabel = new JLabel();
    private final JTextArea logArea = new JTextArea();

    private final RoundedButton attackButton = new RoundedButton("Attack");
    private final RoundedButton abilityButton = new RoundedButton("Ability");
    private final RoundedButton itemButton = new RoundedButton("Item");
    private final RoundedButton fleeButton = new RoundedButton("Flee");

    public CombatPanel(Hero hero, Enemy enemy, Consumer<CombatOutcome> onCombatEnd, int floorNumber) {
        this.engine = new CombatEngine(hero, enemy);
        this.onCombatEnd = onCombatEnd;
        this.heroPortrait = new CombatantPortrait(SpriteFactory.forCharacter(hero), false);
        this.enemyPortrait = new CombatantPortrait(SpriteFactory.forCharacter(enemy), true);

        setLayout(new BorderLayout(14, 14));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(tintForFloor(floorNumber));

        add(buildStatusPanel(), BorderLayout.NORTH);
        add(buildLogPanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);

        wireButtons();
        refresh();
    }

    /**
     * Same floor-environment cue as FightFlashPanel, but subtler — this
     * background is visible for the whole fight, not just a 650ms
     * flash, so it stays close to the normal theme rather than a bold
     * tint that would get tiring to look at.
     */
    private static Color tintForFloor(int floorNumber) {
        return switch (floorNumber) {
            case 2 -> new Color(18, 22, 32);
            case 3 -> new Color(34, 20, 18);
            default -> UiTheme.BG_DARKEST;
        };
    }

    private JPanel buildStatusPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 16, 0));
        panel.setOpaque(false);
        panel.add(buildCombatantCard(heroPortrait, heroLabel, heroHpBar, false));
        panel.add(buildCombatantCard(enemyPortrait, enemyLabel, enemyHpBar, true));
        return panel;
    }

    private RoundedPanel buildCombatantCard(CombatantPortrait portrait, JLabel nameLabel,
                                             HealthBar bar, boolean alignRight) {
        RoundedPanel card = new RoundedPanel(UiTheme.BG_PANEL_RAISED, 14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(6, 14, 12, 14));

        nameLabel.setForeground(UiTheme.TEXT_PRIMARY);
        nameLabel.setFont(UiTheme.FONT_HEADING);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        portrait.setAlignmentX(CENTER_ALIGNMENT);
        bar.setAlignmentX(CENTER_ALIGNMENT);

        card.add(portrait);
        card.add(nameLabel);
        card.add(javax.swing.Box.createVerticalStrut(6));
        card.add(bar);
        return card;
    }

    private JScrollPane buildLogPanel() {
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(UiTheme.BG_PANEL);
        logArea.setForeground(UiTheme.TEXT_PRIMARY);
        logArea.setFont(UiTheme.FONT_LOG);
        logArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(400, 180));
        scrollPane.setBorder(BorderFactory.createLineBorder(UiTheme.BG_PANEL_RAISED, 1, true));
        scrollPane.getViewport().setBackground(UiTheme.BG_PANEL);
        return scrollPane;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        panel.setOpaque(false);
        for (RoundedButton button : List.of(attackButton, abilityButton, itemButton, fleeButton)) {
            button.setPreferredSize(new Dimension(96, 38));
            panel.add(button);
        }
        return panel;
    }

    private static final int ENEMY_REACT_DELAY_MS = 220;
    private static final int HERO_REACT_DELAY_MS = 560;
    private static final int END_TRANSITION_DELAY_MS = 950;

    private void wireButtons() {
        attackButton.addActionListener(e -> runHeroAction(engine::attack, true));
        abilityButton.addActionListener(e -> runHeroAction(engine::useAbility, true));
        fleeButton.addActionListener(e -> runHeroAction(engine::flee, false));
        itemButton.addActionListener(e -> handleItemButton());
    }

    /**
     * Items are chosen through a simple selection dialog rather than a
     * custom submenu component. This keeps the combat screen itself
     * uncluttered and was a deliberate scope call: a full drag-and-drop
     * item panel would look nicer but risks the deadline for a feature
     * that isn't core to demonstrating the OOP hierarchy underneath it.
     */
    private void handleItemButton() {
        List<Item> items = engine.getHero().getInventory().getItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items in inventory.");
            return;
        }
        // Each option shows both the name AND what it actually does
        // ("Health Potion — Restores 30 HP, then is consumed") so the
        // player can make an informed choice without needing to already
        // know the item, rather than picking a bare name blind.
        String[] labels = items.stream()
            .map(item -> item.getName() + " — " + item.getDescription())
            .toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(this, "Choose an item to use:",
            "Inventory", JOptionPane.PLAIN_MESSAGE, null, labels, labels[0]);
        if (chosen == null) {
            return;
        }
        int chosenIndex = java.util.Arrays.asList(labels).indexOf(chosen);
        Item selected = items.get(chosenIndex);
        runHeroAction(() -> engine.useItem(selected), false);
    }

    /**
     * Runs one hero action end to end: captures HP before, calls into
     * CombatEngine (the actual rules), then compares HP after to decide
     * which animations to play and when. The engine call itself already
     * resolved the ENTIRE turn (hero acts, then enemy counters) before
     * this method sees the result — the delays below are purely visual
     * pacing so the lunge/flash/counter-lunge play out as a believable
     * back-and-forth instead of every effect firing at once.
     */
    private void runHeroAction(Supplier<CombatOutcome> action, boolean showLunge) {
        int enemyHpBefore = engine.getEnemy().getHp();
        int heroHpBefore = engine.getHero().getHp();

        CombatOutcome outcome = action.get();

        if (showLunge) {
            heroPortrait.playAttackAnimation();
        }

        int enemyDamage = enemyHpBefore - engine.getEnemy().getHp();
        if (enemyDamage > 0) {
            runAfterDelay(ENEMY_REACT_DELAY_MS, () -> enemyPortrait.flashHit(enemyDamage));
        }

        int heroDamage = heroHpBefore - engine.getHero().getHp();
        if (heroDamage > 0) {
            runAfterDelay(HERO_REACT_DELAY_MS, () -> {
                enemyPortrait.playAttackAnimation();
                heroPortrait.flashHit(heroDamage);
            });
        }

        refresh();

        if (outcome != CombatOutcome.ONGOING) {
            setActionsEnabled(false);
            runAfterDelay(END_TRANSITION_DELAY_MS, () -> {
                heroPortrait.dispose();
                enemyPortrait.dispose();
                onCombatEnd.accept(outcome);
            });
        }
    }

    /**
     * Runs the given action once, after a delay, on the Swing event
     * thread. A thin, clearly-named wrapper around javax.swing.Timer's
     * one-shot mode (setRepeats(false)) — used here purely to pace out
     * animation effects so a lunge, a flash, and a counter-lunge read as
     * a sequence rather than all firing in the same instant.
     */
    private void runAfterDelay(int delayMs, Runnable action) {
        Timer timer = new Timer(delayMs, e -> action.run());
        timer.setRepeats(false);
        timer.start();
    }

    private void setActionsEnabled(boolean enabled) {
        attackButton.setEnabled(enabled);
        abilityButton.setEnabled(enabled);
        itemButton.setEnabled(enabled);
        fleeButton.setEnabled(enabled);
    }

    private void refresh() {
        Hero hero = engine.getHero();
        Enemy enemy = engine.getEnemy();

        heroLabel.setText(hero.getClass().getSimpleName() + " " + hero.getName());
        heroHpBar.setValue(hero.getHp(), hero.getMaxHp());

        enemyLabel.setText(enemy.getClass().getSimpleName() + " " + enemy.getName());
        enemyHpBar.setValue(enemy.getHp(), enemy.getMaxHp());

        logArea.setText(String.join("\n", engine.getLog()));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}

