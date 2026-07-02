package com.dungeonrpg;

import com.dungeonrpg.entity.Goblin;
import com.dungeonrpg.entity.Warrior;
import com.dungeonrpg.rule.*;

import java.util.ArrayList;
import java.util.List;

/**
 * TEMPORARY: proves the cycle guard added to GameEventBus actually
 * prevents a crash. Builds a rule that is subscribed to EventType.HIT
 * and whose own Action publishes EventType.HIT again — exactly the
 * configuration an ordinary player could build in the Rule Builder
 * without knowing it was dangerous. Before the fix, this test would
 * throw a StackOverflowError; after the fix, it should complete
 * normally with the self-referencing rule firing exactly once per
 * publish, not infinitely.
 */
public class EventCycleSmokeTest {
    public static void main(String[] args) {
        Warrior hero = new Warrior("Kael");
        Goblin enemy = new Goblin("Grubnak");
        List<String> log = new ArrayList<>();
        RuleEngine engine = hero.getRuleEngine();

        // A rule subscribed to HIT, whose OWN action re-publishes HIT.
        // If this recurses, the JVM throws StackOverflowError and this
        // test crashes instead of printing "PASSED".
        Rule selfReferencing = new Rule(
            "Self-Referencing Rule",
            Trigger.ON_HIT,
            ctx -> true,
            new CompositeAction(List.of(
                new DealDamageAction(2, false),
                new PublishEventAction(EventType.HIT, engine.getEventBus())
            )));
        engine.subscribeToEvent(EventType.HIT, selfReferencing);

        GameContext ctx = new GameContext(hero, enemy, 1, 0, log);

        try {
            engine.getEventBus().publish(new GameEvent<>(EventType.HIT, 0, ctx));
            System.out.println("PASSED: cycle guard prevented infinite recursion.");
            System.out.println("Enemy HP after one publish (should have dropped by exactly 2, "
                + "not more, since the cycle should fire only once): " + enemy.getHp() + " / " + enemy.getMaxHp());
        } catch (StackOverflowError e) {
            System.out.println("FAILED: StackOverflowError - the cycle guard did not work.");
        }
    }
}
