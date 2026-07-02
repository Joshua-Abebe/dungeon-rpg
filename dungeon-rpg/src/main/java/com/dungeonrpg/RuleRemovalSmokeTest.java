package com.dungeonrpg;

import com.dungeonrpg.entity.Goblin;
import com.dungeonrpg.entity.Warrior;
import com.dungeonrpg.rule.*;

import java.util.ArrayList;
import java.util.List;

/**
 * TEMPORARY: proves RuleEngine.removeRuleEverywhere() actually stops a
 * rule from firing, for BOTH ways a rule can be registered — a direct
 * addRule() rule and a chained subscribeToEvent() rule. A rule object
 * carries no flag saying which one it is, so this test exists
 * specifically to prove removal works correctly for both cases, not
 * just the more obvious one.
 */
public class RuleRemovalSmokeTest {
    public static void main(String[] args) {
        Warrior hero = new Warrior("Kael");
        Goblin enemy = new Goblin("Grubnak");
        List<String> log = new ArrayList<>();
        RuleEngine engine = hero.getRuleEngine();
        GameContext ctx = new GameContext(hero, enemy, 1, 0, log);

        // --- Case 1: a direct, addRule()-registered rule ---
        Rule directRule = new Rule("Direct Damage", Trigger.ON_HIT, ctx0 -> true, new DealDamageAction(10, false));
        engine.addRule(directRule);

        engine.fireTrigger(Trigger.ON_HIT, ctx);
        boolean directFiredBeforeRemoval = enemy.getHp() < enemy.getMaxHp();
        System.out.println("Direct rule fired before removal: " + directFiredBeforeRemoval
            + " (HP " + enemy.getHp() + "/" + enemy.getMaxHp() + ")");

        engine.removeRuleEverywhere(directRule);
        enemy.heal(999);
        engine.fireTrigger(Trigger.ON_HIT, ctx);
        boolean directFiredAfterRemoval = enemy.getHp() < enemy.getMaxHp();
        System.out.println("Direct rule fired AFTER removal (should be false): " + directFiredAfterRemoval);

        // --- Case 2: a chained, subscribeToEvent()-registered rule ---
        Rule chainedRule = new Rule("Chained Damage", Trigger.ON_HIT, ctx0 -> true, new DealDamageAction(7, false));
        engine.subscribeToEvent(EventType.HIT, chainedRule);

        engine.getEventBus().publish(new GameEvent<>(EventType.HIT, 0, ctx));
        boolean chainedFiredBeforeRemoval = enemy.getHp() < enemy.getMaxHp();
        System.out.println("\nChained rule fired before removal: " + chainedFiredBeforeRemoval
            + " (HP " + enemy.getHp() + "/" + enemy.getMaxHp() + ")");

        engine.removeRuleEverywhere(chainedRule);
        enemy.heal(999);
        engine.getEventBus().publish(new GameEvent<>(EventType.HIT, 0, ctx));
        boolean chainedFiredAfterRemoval = enemy.getHp() < enemy.getMaxHp();
        System.out.println("Chained rule fired AFTER removal (should be false): " + chainedFiredAfterRemoval);

        boolean passed = directFiredBeforeRemoval && !directFiredAfterRemoval
            && chainedFiredBeforeRemoval && !chainedFiredAfterRemoval;
        System.out.println("\nPASSED: " + passed);
    }
}
