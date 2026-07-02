package com.dungeonrpg.ui;

import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.rule.Action;
import com.dungeonrpg.rule.*;
import com.dungeonrpg.status.Freeze;
import com.dungeonrpg.status.Poison;
import com.dungeonrpg.status.Stun;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * RuleBuilderPanel is the live, in-game version of what GameLauncher's
 * hardcoded demo rules only simulate: the player choosing a Condition,
 * an Action, and a Trigger from dropdowns, naming the result, and
 * having a brand-new Rule object added to their Hero's RuleEngine right
 * then — exactly the "player composes rules at runtime, not selecting
 * from pre-built options" idea the whole project is built around.
 *
 * This panel owns its own RuleSet (hero.getRuleSet() — the player's
 * composition workspace, persisted on the Hero so it survives every
 * time this panel is closed and reopened) and pushes into the Hero's
 * live RuleEngine directly whenever a rule is added.
 */
public class RuleBuilderPanel extends JPanel {

    private final Hero hero;
    private final RuleSet ruleSet; // hero.getRuleSet() - persists across every time this panel is opened
    private final Runnable onBack;

    // Only these three triggers are ever actually fired by CombatEngine
    // (see CombatEngine.runHeroTurn/runEnemyTurnAndTick). ON_STATUS_APPLIED
    // and ON_FLOOR_ENTER exist in the Trigger enum as part of the
    // engine's architecture, but nothing in the game currently calls
    // fireTrigger() with either of them. Offering them here would let a
    // player build a rule that silently never fires — a worse outcome
    // than not offering the option at all, so this list is deliberately
    // narrower than Trigger.values().
    private static final Trigger[] FUNCTIONAL_TRIGGERS = { Trigger.ON_HIT, Trigger.ON_TURN_END, Trigger.ON_DEATH };

    private final JComboBox<Trigger> triggerCombo = new JComboBox<>(FUNCTIONAL_TRIGGERS);
    private final JCheckBox chainCheckbox = new JCheckBox("React to another rule instead of a Trigger");
    private final JComboBox<EventType> chainEventCombo = new JComboBox<>(EventType.values());
    private final JComboBox<EventType> publishEventCombo = new JComboBox<>(EventType.values());

    private final JComboBox<String> conditionTypeCombo = new JComboBox<>(new String[] {
        "Always", "My HP Below %", "Enemy HP Below %", "Every N Turns", "Exact Turn Number", "Enemy Is Type"
    });
    private final JSpinner conditionPercentSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 99, 1));
    private final JSpinner conditionTurnSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JComboBox<String> conditionEnemyTypeCombo =
        new JComboBox<>(new String[] { "Goblin", "Skeleton", "Troll", "Dinosaur" });
    private final JPanel conditionParamCards = new JPanel(new CardLayout());

    private final JComboBox<String> actionTypeCombo = new JComboBox<>(new String[] {
        "Deal Damage to Enemy", "Heal Self", "Poison Enemy", "Weaken Enemy Defense",
        "Boost My Attack", "Stun Enemy", "Freeze Enemy", "Publish Event (wakes up chained rules)"
    });
    private final JSpinner actionAmountSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 999, 1));
    private final JSpinner actionDurationSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
    private final JPanel actionParamCards = new JPanel(new CardLayout());

    private final JTextField nameField = new JTextField("My Rule");

    private final DefaultListModel<Rule> activeRulesModel = new DefaultListModel<>();
    private final JList<Rule> activeRulesList = new JList<>(activeRulesModel);

    public RuleBuilderPanel(Hero hero, Runnable onBack) {
        this.hero = hero;
        this.ruleSet = hero.getRuleSet();
        this.onBack = onBack;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        setBackground(UiTheme.BG_DARKEST);

        JComponent titleBlock = buildTitle();
        titleBlock.setAlignmentX(LEFT_ALIGNMENT);
        titleBlock.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleBlock.getPreferredSize().height));
        add(titleBlock);
        add(Box.createVerticalStrut(12));

        JPanel formAndLists = buildFormAndLists();
        formAndLists.setAlignmentX(LEFT_ALIGNMENT);
        // Without this cap, GridLayout (used inside formAndLists) has no
        // natural maximum size, so BoxLayout stretches it to consume
        // EVERY extra pixel of vertical space the dialog happens to
        // have - which was pushing the footer below arbitrarily far
        // down regardless of how tall the dialog was made. Capping the
        // maximum height to the panel's own preferred height is what
        // lets the footer sit directly after it, at its natural size.
        formAndLists.setMaximumSize(new Dimension(Integer.MAX_VALUE, formAndLists.getPreferredSize().height));
        add(formAndLists);
        add(Box.createVerticalStrut(12));

        JPanel footer = buildFooter();
        footer.setAlignmentX(LEFT_ALIGNMENT);
        add(footer);

        wireBehavior();
        refreshActiveRulesList();
    }

    private JPanel buildTitle() {
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Rule Builder");
        title.setForeground(UiTheme.TEXT_PRIMARY);
        title.setFont(UiTheme.FONT_HEADING);
        title.setAlignmentX(LEFT_ALIGNMENT);
        titleBlock.add(title);

        JLabel explainer = new JLabel("<html><div style='width:460px'>"
            + "Every rule you build here belongs to <b>" + hero.getName() + "</b> only — enemies never use this "
            + "system, there is no way to build a rule for them. A rule fires when its <b>Trigger</b> "
            + "(a moment from " + hero.getName() + "'s side of the fight) happens AND its <b>Condition</b> "
            + "is also true; when both hold, its <b>Action</b> runs. Rules exist only for your <b>current "
            + "playthrough</b> — they are never saved to disk, and are lost if you restart or return to the "
            + "Main Menu."
            + "</div></html>");
        explainer.setForeground(UiTheme.TEXT_MUTED);
        explainer.setFont(UiTheme.FONT_BODY);
        explainer.setAlignmentX(LEFT_ALIGNMENT);
        titleBlock.add(Box.createVerticalStrut(6));
        titleBlock.add(explainer);

        return titleBlock;
    }

    private JPanel buildFormAndLists() {
        JPanel container = new JPanel(new GridLayout(1, 2, 12, 0));
        container.setOpaque(false);
        container.add(buildComposePanel());
        RoundedPanel listsPanel = buildListsPanel();
        container.add(listsPanel);
        return container;
    }

    private RoundedPanel buildComposePanel() {
        RoundedPanel panel = new RoundedPanel(UiTheme.BG_PANEL_RAISED, 12);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        panel.add(themedLabel("Trigger — WHEN this rule is checked (always from your side of the fight)"));
        capWidth(triggerCombo);
        panel.add(triggerCombo);
        chainCheckbox.setOpaque(false);
        chainCheckbox.setForeground(UiTheme.TEXT_PRIMARY);
        panel.add(chainCheckbox);
        panel.add(captionLabel("Instead of a Trigger, fire only when another rule below publishes this event:"));
        capWidth(chainEventCombo);
        panel.add(chainEventCombo);
        panel.add(Box.createVerticalStrut(8));

        panel.add(themedLabel("Condition — an extra requirement, checked in addition to the Trigger"));
        capWidth(conditionTypeCombo);
        panel.add(conditionTypeCombo);
        buildConditionParamCards();
        panel.add(conditionParamCards);
        panel.add(Box.createVerticalStrut(8));

        panel.add(themedLabel("Action — what actually happens once Trigger + Condition both succeed"));
        capWidth(actionTypeCombo);
        panel.add(actionTypeCombo);
        buildActionParamCards();
        panel.add(actionParamCards);
        panel.add(Box.createVerticalStrut(8));

        panel.add(themedLabel("Rule name"));
        capWidth(nameField);
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(10));

        RoundedButton addButton = new RoundedButton("Add Rule");
        addButton.addActionListener(e -> handleAddRule());
        panel.add(addButton);
        panel.add(Box.createVerticalStrut(8));

        RoundedButton starterRulesButton = new RoundedButton("Load Starter Rules");
        starterRulesButton.addActionListener(e -> handleLoadStarterRules());
        panel.add(starterRulesButton);

        // Caps the WHOLE compose panel's width directly, rather than
        // relying on every individual child (spinners, checkboxes, the
        // condition/action param sub-panels) to be separately capped.
        // Several of those children turned out NOT to be capped
        // (JSpinners and JComboBoxes inside the CardLayout param
        // panels), and CardLayout's preferred width is the max of ALL
        // its cards even when only one is visible — a single wide
        // uncapped child was enough to blow out the entire dialog's
        // layout. Capping the container itself is the robust fix: it
        // catches any such case, present or future, in one place.
        int neededHeight = panel.getPreferredSize().height;
        Dimension capped = new Dimension(260, neededHeight);
        panel.setMaximumSize(capped);
        panel.setPreferredSize(capped);

        return panel;
    }

    private JLabel themedLabel(String text) {
        JLabel label = new JLabel("<html><div style='width:220px'>" + text + "</div></html>");
        label.setForeground(UiTheme.TEXT_MUTED);
        label.setFont(UiTheme.FONT_BODY);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    /** Smaller, italicized explanatory text - distinct from themedLabel's section headers, used for inline "why this exists" notes. */
    private JLabel captionLabel(String text) {
        JLabel label = new JLabel("<html><i><div style='width:210px'>" + text + "</div></i></html>");
        label.setForeground(UiTheme.TEXT_MUTED);
        label.setFont(UiTheme.FONT_BODY.deriveFont(11f));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Caps a form control's width so it doesn't auto-expand to fit its
     * longest possible dropdown item (e.g. "Weaken Enemy Defense"). The
     * game window has a fixed, non-resizable size, so components that
     * size themselves freely can silently push the whole form wider
     * than the window and force ugly horizontal scrolling.
     */
    private void capWidth(JComponent component) {
        Dimension size = new Dimension(230, component.getPreferredSize().height);
        component.setMaximumSize(size);
        component.setPreferredSize(size);
        component.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void buildConditionParamCards() {
        conditionParamCards.setOpaque(false);
        conditionParamCards.add(new JPanel(), "Always");

        JPanel percentPanel = new JPanel();
        percentPanel.setOpaque(false);
        percentPanel.add(conditionPercentSpinner);
        percentPanel.add(new JLabel("%"));
        conditionParamCards.add(percentPanel, "My HP Below %");
        conditionParamCards.add(percentPanel, "Enemy HP Below %");

        JPanel turnPanel = new JPanel();
        turnPanel.setOpaque(false);
        turnPanel.add(conditionTurnSpinner);
        conditionParamCards.add(turnPanel, "Every N Turns");
        conditionParamCards.add(turnPanel, "Exact Turn Number");

        JPanel enemyTypePanel = new JPanel();
        enemyTypePanel.setOpaque(false);
        enemyTypePanel.add(conditionEnemyTypeCombo);
        conditionParamCards.add(enemyTypePanel, "Enemy Is Type");
    }

    private void buildActionParamCards() {
        actionParamCards.setOpaque(false);

        JPanel amountPanel = new JPanel();
        amountPanel.setOpaque(false);
        amountPanel.add(actionAmountSpinner);
        actionParamCards.add(amountPanel, "Deal Damage to Enemy");
        actionParamCards.add(amountPanel, "Heal Self");
        actionParamCards.add(amountPanel, "Weaken Enemy Defense");
        actionParamCards.add(amountPanel, "Boost My Attack");

        JPanel poisonPanel = new JPanel();
        poisonPanel.setOpaque(false);
        poisonPanel.add(new JLabel("dmg/turn:"));
        poisonPanel.add(actionAmountSpinner);
        poisonPanel.add(new JLabel("turns:"));
        poisonPanel.add(actionDurationSpinner);
        actionParamCards.add(poisonPanel, "Poison Enemy");

        actionParamCards.add(new JPanel(), "Stun Enemy");
        actionParamCards.add(new JPanel(), "Freeze Enemy");

        JPanel publishPanel = new JPanel();
        publishPanel.setOpaque(false);
        publishPanel.add(publishEventCombo);
        actionParamCards.add(publishPanel, "Publish Event (wakes up chained rules)");
    }

    private RoundedPanel buildListsPanel() {
        RoundedPanel panel = new RoundedPanel(UiTheme.BG_PANEL_RAISED, 12);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        panel.add(themedLabel("Active rules on " + hero.getName()));
        activeRulesList.setBackground(UiTheme.BG_PANEL);
        activeRulesList.setForeground(UiTheme.TEXT_PRIMARY);
        JScrollPane activeScroll = new JScrollPane(activeRulesList);
        activeScroll.setPreferredSize(new Dimension(210, 260));
        panel.add(activeScroll);
        panel.add(Box.createVerticalStrut(8));

        RoundedButton removeButton = new RoundedButton("Remove Selected Rule");
        removeButton.addActionListener(e -> handleRemoveRule());
        panel.add(removeButton);

        int neededHeight = panel.getPreferredSize().height;
        Dimension capped = new Dimension(260, neededHeight);
        panel.setMaximumSize(capped);
        panel.setPreferredSize(capped);

        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.setOpaque(false);
        RoundedButton backButton = new RoundedButton("Done");
        backButton.setPreferredSize(new Dimension(190, 36));
        backButton.addActionListener(e -> onBack.run());
        footer.add(backButton);
        return footer;
    }

    private void wireBehavior() {
        conditionTypeCombo.addActionListener(e ->
            ((CardLayout) conditionParamCards.getLayout()).show(conditionParamCards, (String) conditionTypeCombo.getSelectedItem()));
        actionTypeCombo.addActionListener(e ->
            ((CardLayout) actionParamCards.getLayout()).show(actionParamCards, (String) actionTypeCombo.getSelectedItem()));
        chainCheckbox.addActionListener(e -> {
            boolean chained = chainCheckbox.isSelected();
            triggerCombo.setEnabled(!chained);
            chainEventCombo.setEnabled(chained);
        });
        chainEventCombo.setEnabled(false);
    }

    private Condition buildConditionFromForm() {
        String type = (String) conditionTypeCombo.getSelectedItem();
        double percent = (Integer) conditionPercentSpinner.getValue() / 100.0;
        int turns = (Integer) conditionTurnSpinner.getValue();
        String enemyType = (String) conditionEnemyTypeCombo.getSelectedItem();

        return switch (type) {
            case "My HP Below %" -> new LowHPCondition(percent, true);
            case "Enemy HP Below %" -> new LowHPCondition(percent, false);
            case "Every N Turns" -> new TurnNumberCondition(turns, true);
            case "Exact Turn Number" -> new TurnNumberCondition(turns, false);
            case "Enemy Is Type" -> new EnemyTypeCondition(enemyType);
            default -> new AlwaysCondition(); // "Always"
        };
    }

    private Action buildActionFromForm() {
        String type = (String) actionTypeCombo.getSelectedItem();
        int amount = (Integer) actionAmountSpinner.getValue();
        int duration = (Integer) actionDurationSpinner.getValue();
        EventType publishType = (EventType) publishEventCombo.getSelectedItem();

        return switch (type) {
            case "Heal Self" -> new HealAction(amount, true);
            case "Poison Enemy" -> new ApplyStatusAction(() -> new Poison(amount, duration), false);
            case "Weaken Enemy Defense" -> new ModifyStatAction(StatType.DEFENSE, -amount, false);
            case "Boost My Attack" -> new ModifyStatAction(StatType.ATTACK, amount, true);
            case "Stun Enemy" -> new ApplyStatusAction(Stun::new, false);
            case "Freeze Enemy" -> new ApplyStatusAction(Freeze::new, false);
            case "Publish Event (wakes up chained rules)" -> new PublishEventAction(publishType, hero.getRuleEngine().getEventBus());
            default -> new DealDamageAction(amount, false); // "Deal Damage to Enemy"
        };
    }

    private void handleAddRule() {
        String name = nameField.getText().isBlank() ? "Unnamed Rule" : nameField.getText().trim();

        boolean nameTaken = ruleSet.getActiveRules().stream().anyMatch(r -> r.getName().equals(name));
        if (nameTaken) {
            JOptionPane.showMessageDialog(this, "\"" + name + "\" is already an active rule. "
                + "Choose a different name, or remove the existing one first.");
            return;
        }

        Trigger trigger = (Trigger) triggerCombo.getSelectedItem();
        Condition condition = buildConditionFromForm();
        Action action = buildActionFromForm();

        Rule rule = new Rule(name, trigger, condition, action);

        // Two rules with different names but the identical Trigger +
        // Condition + Action would behave identically and silently
        // stack their effects every time they fire (two rules each
        // dealing 5 damage on the same hit adds up to 10, with nothing
        // telling the player why). Checked here, after building the
        // new Rule object but before registering it anywhere, using
        // Rule.hasSameBehaviorAs() rather than comparing raw form
        // state, so this catches duplicates regardless of how the
        // existing rule was originally built (hand-built here, or via
        // "Load Starter Rules").
        Rule behavioralDuplicate = ruleSet.getActiveRules().stream()
            .filter(rule::hasSameBehaviorAs)
            .findFirst()
            .orElse(null);
        if (behavioralDuplicate != null) {
            JOptionPane.showMessageDialog(this, "This rule would behave exactly like \""
                + behavioralDuplicate.getName() + "\" (same Trigger, Condition, and Action). "
                + "Adding it would silently double that effect every time it fires. "
                + "Change the Trigger, Condition, or Action to make this rule genuinely different.");
            return;
        }

        if (chainCheckbox.isSelected()) {
            EventType eventType = (EventType) chainEventCombo.getSelectedItem();
            hero.getRuleEngine().subscribeToEvent(eventType, rule);
        } else {
            hero.getRuleEngine().addRule(rule);
        }
        ruleSet.addActiveRule(rule);
        refreshActiveRulesList();

        JOptionPane.showMessageDialog(this, "\"" + name + "\" added to " + hero.getName()
            + "'s rules for this playthrough. It will affect your next fight, and is lost if you restart.");
    }

    /**
     * Adds a small, known-good starting set of rules — the same ones
     * that used to be silently attached to every hero automatically.
     * Making this an explicit, player-triggered button instead of
     * always-on background behavior matters for the same reason the
     * whole Rule Builder exists: the player is supposed to be the one
     * deciding what rules are active, not finding mystery rules already
     * running that they never chose.
     *
     * Guarded against being clicked twice: without this check, a second
     * click would add a second "Opening Strike," a second "Echo
     * Strike," and so on — silently doubling every one of their
     * effects (two rules each dealing +5 damage on turn 1 adds up to
     * +10, not +5), with no indication to the player of why combat
     * suddenly felt different.
     */
    private void handleLoadStarterRules() {
        boolean alreadyLoaded = ruleSet.getActiveRules().stream().anyMatch(r -> r.getName().equals("Opening Strike"));
        if (alreadyLoaded) {
            JOptionPane.showMessageDialog(this, "Starter rules are already loaded — "
                + "loading them again would duplicate every one of their effects.");
            return;
        }

        List<Rule> starterRules = GameLauncher.attachDemoRules(hero);

        // Same behavioral check as handleAddRule(), applied per starter
        // rule: a player could have hand-built something identical to
        // one of these five before clicking this button. If any
        // conflict, undo the ones already registered on the engine and
        // refuse the whole batch, rather than silently adding four of
        // five and leaving the player to guess which one was skipped.
        for (Rule starterRule : starterRules) {
            Rule conflict = ruleSet.getActiveRules().stream()
                .filter(starterRule::hasSameBehaviorAs)
                .findFirst()
                .orElse(null);
            if (conflict != null) {
                starterRules.forEach(r -> hero.getRuleEngine().removeRuleEverywhere(r));
                JOptionPane.showMessageDialog(this, "Can't load starter rules: \"" + starterRule.getName()
                    + "\" would behave exactly like your existing rule \"" + conflict.getName() + "\".");
                return;
            }
        }

        starterRules.forEach(ruleSet::addActiveRule);
        refreshActiveRulesList();
        JOptionPane.showMessageDialog(this, "Starter rules loaded: Opening Strike, Echo Strike (chained), "
            + "Weaken Strike, Weaken Strike II, and Second Wind.");
    }

    /**
     * Removes the selected rule from both the display list (RuleSet)
     * and the live RuleEngine that actually fires it — removing it from
     * only one of the two would either leave a rule silently still
     * firing in combat while invisible in the list, or vice versa.
     * RuleEngine.removeRuleEverywhere() handles the fact that a rule
     * might have been registered as a direct trigger OR as a chained
     * subscription, without this panel needing to know or track which.
     */
    private void handleRemoveRule() {
        Rule selected = activeRulesList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a rule from the list first.");
            return;
        }
        hero.getRuleEngine().removeRuleEverywhere(selected);
        ruleSet.removeActiveRule(selected);
        refreshActiveRulesList();
        JOptionPane.showMessageDialog(this, "\"" + selected.getName() + "\" removed.");
    }

    private void refreshActiveRulesList() {
        activeRulesModel.clear();
        ruleSet.getActiveRules().forEach(activeRulesModel::addElement);
    }
}
