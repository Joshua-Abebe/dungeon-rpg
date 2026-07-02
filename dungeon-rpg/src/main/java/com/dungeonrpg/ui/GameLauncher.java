package com.dungeonrpg.ui;

import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.rule.CompositeAction;
import com.dungeonrpg.rule.DealDamageAction;
import com.dungeonrpg.rule.EventType;
import com.dungeonrpg.rule.HealAction;
import com.dungeonrpg.rule.LowHPCondition;
import com.dungeonrpg.rule.ModifyStatAction;
import com.dungeonrpg.rule.PublishEventAction;
import com.dungeonrpg.rule.Rule;
import com.dungeonrpg.rule.RuleEngine;
import com.dungeonrpg.rule.StatType;
import com.dungeonrpg.rule.Trigger;
import com.dungeonrpg.rule.TurnNumberCondition;

import javax.swing.SwingUtilities;
import java.util.List;

/**
 * The real entry point for the visual game. Run THIS class to see the
 * actual window. main() opens HeroSelectFrame first — the player picks
 * a class and names their hero there, and HeroSelectFrame is what
 * actually constructs the Hero and opens GameWindow.
 *
 * A fresh hero starts with ZERO rules attached. attachDemoRules()
 * exists purely as an opt-in example set the player can load via the
 * "Load Starter Rules" button in the Rule Builder — it demonstrates
 * both rule engine layers at once: "Second Wind" (direct composition,
 * a simple heal-when-low rule), "Opening Strike" chained into "Echo
 * Strike" (chaining — Opening Strike's action publishes a HIT event,
 * and Echo Strike, which is NEVER registered against a Trigger, fires
 * purely because it's subscribed to that event on the bus), and
 * "Weaken Strike"/"Weaken Strike II" (two independent rules sharing the
 * same shape with one field changed). This is deliberately no longer
 * automatic: having rules the player never chose silently active on
 * every run undercut the project's actual premise of a player-composed
 * rule system.
 */
public class GameLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HeroSelectFrame selectFrame = new HeroSelectFrame();
            selectFrame.setVisible(true);
        });
    }

    /**
     * Attaches a small set of example rules covering both rule engine
     * layers. This used to run automatically on every hero at game
     * start; it's now only called when the player explicitly clicks
     * "Load Starter Rules" in the Rule Builder. Having rules the player
     * never asked for silently active on every run undercut the
     * project's actual premise — a player-composed rule system — so a
     * fresh hero now starts with zero rules until the player adds some,
     * either from scratch or via this button.
     *
     * Returns every Rule it created, including "Echo Strike" (which is
     * only reachable through RuleEngine's event bus, not its trigger
     * list) — callers that want to mirror these into a RuleSet for UI
     * display need the complete list, not just the ones addRule()
     * would report back.
     */
    static List<Rule> attachDemoRules(Hero hero) {
        RuleEngine engine = hero.getRuleEngine();

        // Chaining: Echo Strike is subscribed to the bus and ONLY
        // reachable that way - it is never added via engine.addRule(),
        // so RuleEngine.fireTrigger() has no direct path to it.
        Rule echoStrike = new Rule(
            "Echo Strike",
            Trigger.ON_HIT, // irrelevant here - this rule only fires via the event bus, never via fireTrigger()
            ctx -> true,
            new DealDamageAction(3, false));
        engine.subscribeToEvent(EventType.HIT, echoStrike);

        // A direct rule whose action ALSO publishes to the bus, which is
        // what wakes Echo Strike up - one rule's action causing a
        // second, otherwise-unrelated rule to fire.
        Rule openingStrike = new Rule(
            "Opening Strike",
            Trigger.ON_HIT,
            new TurnNumberCondition(1, false),
            new CompositeAction(List.of(
                new DealDamageAction(5, false),
                new PublishEventAction(EventType.HIT, engine.getEventBus())
            )));
        engine.addRule(openingStrike);

        Rule secondWind = new Rule(
            "Second Wind",
            Trigger.ON_TURN_END,
            new LowHPCondition(0.3, true),
            new HealAction(15, true));
        engine.addRule(secondWind);

        // Two independent rules with the same shape (weaken the
        // enemy's defense) except for which turn each one fires on -
        // both hand-written directly rather than derived from each
        // other, since there is no template/inheritance mechanism.
        Rule weakenStrike = new Rule(
            "Weaken Strike",
            Trigger.ON_HIT,
            new TurnNumberCondition(1, false),
            new ModifyStatAction(StatType.DEFENSE, -2, false));
        engine.addRule(weakenStrike);

        Rule weakenStrikeTwo = new Rule(
            "Weaken Strike II",
            Trigger.ON_HIT,
            new TurnNumberCondition(2, false),
            new ModifyStatAction(StatType.DEFENSE, -2, false));
        engine.addRule(weakenStrikeTwo);

        return List.of(echoStrike, openingStrike, secondWind, weakenStrike, weakenStrikeTwo);
    }
}
