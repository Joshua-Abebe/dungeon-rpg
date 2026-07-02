package com.dungeonrpg;

import com.dungeonrpg.entity.Goblin;
import com.dungeonrpg.entity.Warrior;
import com.dungeonrpg.rule.*;

import java.util.ArrayList;
import java.util.List;

/**
 * TEMPORARY: proves Layer 2 chaining actually works — specifically, that
 * a Rule can fire WITHOUT ever being registered in RuleEngine.addRule()
 * or matched against a Trigger, purely because a different rule's
 * Action published an event it was subscribed to. This is the key
 * distinction from Layer 1: Rule B here is only reachable through the
 * GameEventBus, never through fireTrigger().
 */
public class RuleChainSmokeTest {
    public static void main(String[] args) {
        Warrior hero = new Warrior("Kael");
        Goblin enemy = new Goblin("Grubnak");
        List<String> log = new ArrayList<>();
        RuleEngine engine = hero.getRuleEngine();

        // Rule B: the "chained" rule. Subscribed to the bus, NEVER
        // added via addRule() - RuleEngine.fireTrigger() has no way to
        // reach this rule directly.
        Rule echoStrike = new Rule(
            "Echo Strike",
            Trigger.ON_HIT, // trigger is irrelevant here since this rule is never fired via fireTrigger()
            ctx -> true,
            new DealDamageAction(3, false));
        engine.subscribeToEvent(EventType.HIT, echoStrike);

        // Rule A: the "primary" rule, fired normally through
        // fireTrigger(ON_HIT). Its action deals damage AND publishes a
        // HIT event - that publish is what wakes up Rule B.
        Rule openingStrike = new Rule(
            "Opening Strike",
            Trigger.ON_HIT,
            ctx -> true,
            new CompositeAction(List.of(
                new DealDamageAction(5, false),
                new PublishEventAction(EventType.HIT, engine.getEventBus())
            )));
        engine.addRule(openingStrike);

        System.out.println("Rules directly registered with fireTrigger: " + engine.getRules().size()
            + " (" + engine.getRules().get(0).getName() + ")");
        System.out.println("Rules subscribed to HIT event on the bus: "
            + engine.getEventBus().getSubscribers(EventType.HIT).size()
            + " (" + engine.getEventBus().getSubscribers(EventType.HIT).get(0).getName() + ")");

        int hpBefore = enemy.getHp();
        System.out.println("\nGrubnak HP before: " + hpBefore);

        GameContext ctx = new GameContext(hero, enemy, 1, 0, log);
        engine.fireTrigger(Trigger.ON_HIT, ctx);

        int hpAfter = enemy.getHp();
        int totalDamage = hpBefore - hpAfter;
        System.out.println("Grubnak HP after: " + hpAfter + " (lost " + totalDamage + ")");
        // Each takeDamage() call is mitigated by Goblin's defense (2)
        // separately: 5 raw -> 3 actual, then 3 raw -> 1 actual
        // (minimum-1 floor). So the two chained hits total 4, not 8 -
        // defense applies per-hit, not to the combined total.
        System.out.println("Expected: 3 (Opening Strike, 5-2 DEF) + 1 (Echo Strike, 3-2 DEF, min 1) = 4");
        System.out.println("\nChaining worked correctly: " + (totalDamage == 4));

        System.out.println("\n-- Log --");
        log.forEach(System.out::println);
    }
}
