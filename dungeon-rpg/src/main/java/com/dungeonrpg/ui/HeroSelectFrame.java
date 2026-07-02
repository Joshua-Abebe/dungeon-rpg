package com.dungeonrpg.ui;

import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.entity.Mage;
import com.dungeonrpg.entity.Rogue;
import com.dungeonrpg.entity.Warrior;
import com.dungeonrpg.map.DifficultyScaler;
import com.dungeonrpg.map.FloorFactory;
import com.dungeonrpg.map.GameMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Supplier;

/**
 * HeroSelectFrame is the pre-game screen: pick a class, name your hero,
 * set a difficulty, optionally compose rules in the Rule Builder before
 * the run even starts, then begin. Class selection uses radio buttons
 * (not per-card "Play" buttons like before) specifically so there's a
 * single, stable notion of "which class is currently chosen" that both
 * "Open Rule Builder" and "Begin Adventure" can share — once you open
 * the Rule Builder, a real Hero of that class exists and owns the
 * RuleEngine those rules attach to, so class selection locks after that
 * point rather than risking rules built for a Mage silently landing on
 * a freshly-created Rogue.
 */
public class HeroSelectFrame extends JFrame {

    private final JTextField nameField;
    private final JRadioButton warriorRadio;
    private final JRadioButton mageRadio;
    private final JRadioButton rogueRadio;
    private final JLabel lockNotice;

    private Hero draftHero;
    private String draftHeroClass;

    public HeroSelectFrame() {
        super("Dungeon RPG — Choose Your Hero");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UiTheme.BG_DARKEST);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Choose Your Hero");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(UiTheme.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(16));

        JLabel nameLabel = new JLabel("Hero name:");
        nameLabel.setForeground(UiTheme.TEXT_MUTED);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        content.add(nameLabel);

        nameField = new JTextField("Kael");
        nameField.setMaximumSize(new Dimension(200, 30));
        nameField.setAlignmentX(CENTER_ALIGNMENT);
        content.add(nameField);
        content.add(Box.createVerticalStrut(12));

        JLabel difficultyLabel = new JLabel("Difficulty (1 easy — 10 brutal):");
        difficultyLabel.setForeground(UiTheme.TEXT_MUTED);
        difficultyLabel.setAlignmentX(CENTER_ALIGNMENT);
        content.add(difficultyLabel);

        JSpinner difficultySpinner = new JSpinner(new SpinnerNumberModel(
            DifficultyScaler.DEFAULT_DIFFICULTY, DifficultyScaler.MIN_DIFFICULTY, DifficultyScaler.MAX_DIFFICULTY, 1));
        difficultySpinner.setMaximumSize(new Dimension(80, 30));
        difficultySpinner.setAlignmentX(CENTER_ALIGNMENT);
        content.add(difficultySpinner);
        content.add(Box.createVerticalStrut(16));

        ButtonGroup classGroup = new ButtonGroup();
        warriorRadio = new JRadioButton("Warrior", true);
        mageRadio = new JRadioButton("Mage");
        rogueRadio = new JRadioButton("Rogue");
        for (JRadioButton radio : new JRadioButton[] { warriorRadio, mageRadio, rogueRadio }) {
            radio.setOpaque(false);
            radio.setForeground(UiTheme.TEXT_PRIMARY);
            radio.setAlignmentX(CENTER_ALIGNMENT);
            classGroup.add(radio);
        }

        content.add(classCard("Warrior", "Tanky melee fighter. Shield Bash stuns enemies.", warriorRadio));
        content.add(Box.createVerticalStrut(8));
        content.add(classCard("Mage", "High burst damage. Fireball burns enemies over time.", mageRadio));
        content.add(Box.createVerticalStrut(8));
        content.add(classCard("Rogue", "Swift and cunning. Pickpocket steals enemy gold mid-fight.", rogueRadio));
        content.add(Box.createVerticalStrut(6));

        lockNotice = new JLabel(" ");
        lockNotice.setForeground(UiTheme.ACCENT);
        lockNotice.setFont(UiTheme.FONT_BODY);
        lockNotice.setAlignmentX(CENTER_ALIGNMENT);
        content.add(lockNotice);
        content.add(Box.createVerticalStrut(6));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(CENTER_ALIGNMENT);

        RoundedButton ruleBuilderButton = new RoundedButton("Open Rule Builder");
        ruleBuilderButton.setPreferredSize(new Dimension(170, 38));
        ruleBuilderButton.addActionListener(e -> openRuleBuilder());

        RoundedButton beginButton = new RoundedButton("Begin Adventure");
        beginButton.setPreferredSize(new Dimension(160, 38));
        beginButton.addActionListener(e -> beginAdventure((Integer) difficultySpinner.getValue()));

        buttonRow.add(ruleBuilderButton);
        buttonRow.add(beginButton);
        content.add(buttonRow);
        content.add(Box.createVerticalStrut(16));

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scrollPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(440, 640);
        setLocationRelativeTo(null);
    }

    private JPanel classCard(String className, String description, JRadioButton radio) {
        RoundedPanel card = new RoundedPanel(UiTheme.BG_PANEL_RAISED, 12);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        card.setMaximumSize(new Dimension(320, 74));
        card.setAlignmentX(CENTER_ALIGNMENT);

        radio.setFont(UiTheme.FONT_HEADING);
        card.add(radio);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setForeground(UiTheme.TEXT_MUTED);
        descLabel.setFont(UiTheme.FONT_BODY);
        card.add(descLabel);

        return card;
    }

    private String selectedClassName() {
        if (mageRadio.isSelected()) return "Mage";
        if (rogueRadio.isSelected()) return "Rogue";
        return "Warrior";
    }

    private Supplier<Hero> factoryFor(String className, String name) {
        return switch (className) {
            case "Mage" -> () -> new Mage(name);
            case "Rogue" -> () -> new Rogue(name);
            default -> () -> new Warrior(name);
        };
    }

    private String safeName() {
        String text = nameField.getText().trim();
        return text.isEmpty() ? "Hero" : text;
    }

    /**
     * Opens the Rule Builder against a real Hero, creating one on first
     * use (locking class selection from that point on, since the rules
     * being composed are attached to that specific Hero's RuleEngine —
     * switching classes afterward would silently strand them).
     */
    private void openRuleBuilder() {
        if (draftHero == null) {
            draftHeroClass = selectedClassName();
            draftHero = factoryFor(draftHeroClass, safeName()).get();
            warriorRadio.setEnabled(false);
            mageRadio.setEnabled(false);
            rogueRadio.setEnabled(false);
            lockNotice.setText("Class locked to " + draftHeroClass + " (rules were composed for this class)");
        }

        JDialog dialog = new JDialog(this, "Rule Builder", true);
        RuleBuilderPanel panel = new RuleBuilderPanel(draftHero, dialog::dispose);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.setContentPane(scrollPane);
        dialog.setSize(680, 900);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void beginAdventure(int difficulty) {
        Hero hero = draftHero != null ? draftHero : factoryFor(selectedClassName(), safeName()).get();
        Supplier<Hero> heroFactory = factoryFor(draftHero != null ? draftHeroClass : selectedClassName(), safeName());

        GameMap map = FloorFactory.generateFloor(1, difficulty);
        GameWindow window = new GameWindow(map, hero, heroFactory, difficulty);
        window.setVisible(true);
        dispose();
    }
}
