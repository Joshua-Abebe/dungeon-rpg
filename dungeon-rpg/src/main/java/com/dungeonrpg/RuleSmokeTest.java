package com.dungeonrpg;

import com.dungeonrpg.entity.Goblin;
import com.dungeonrpg.entity.Warrior;
import com.dungeonrpg.rule.*;
import com.dungeonrpg.status.Poison;

import java.util.ArrayList;
import java.util.List;

/**
 * TEMPORARY: verifies the Layer 1 rule engine (Condition, Action,
 * Trigger, Rule, RuleEngine, and both Composite variants) in isolation,
 * with no CombatEngine or Swing involved. Builds several rules
 * programmatically — standing in for what a future Rule Builder GUI
 * would let the player construct interactively — and fires triggers by
 * hand to confirm each one does exactly what it should.
 */
public class RuleSmokeTest {
    public static void main(String[] args) {
        Warrior hero = new Warrior("Kael");
        Goblin enemy = new Goblin("Grubnak");
        List<String> log = new ArrayList<>();

        RuleEngine engine = new RuleEngine();

        // Rule 1: a simple single condition/action pair.
        Rule healWhenLow = new Rule(
            "Emergency Heal",
            Trigger.ON_TURN_END,
            new LowHPCondition(0.3, true),
            new HealAction(20, true));
        engine.addRule(healWhenLow);

        // Rule 2: CompositeCondition (AND) + CompositeAction - proves
        // runtime composition of conditions AND actions in one rule.
        Rule finisherCombo = new Rule(
            "Dragon Finisher",
            Trigger.ON_HIT,
            new CompositeCondition(List.of(
                new LowHPCondition(0.25, false),
                new EnemyTypeCondition("Goblin")
            ), LogicOperator.AND),
            new CompositeAction(List.of(
                new DealDamageAction(999, false),
                new ApplyStatusAction(() -> new Poison(3, 2), false)
            )));
        engine.addRule(finisherCombo);

        // Rule 3: every-3rd-turn periodic rule.
        Rule everyThirdTurnBoost = new Rule(
            "Battle Fury",
            Trigger.ON_TURN_END,
            new TurnNumberCondition(3, true),
            new ModifyStatAction(StatType.ATTACK, 2, true));
        engine.addRule(everyThirdTurnBoost);

        System.out.println("Rules registered: " + engine.getRules().size());
        for (Rule r : engine.getRules()) {
            System.out.println("  - " + r.getName() + " (" + r.getTrigger() + ")");
        }

        System.out.println("\n-- Simulating a fight --");
        int startingAttack = hero.getAttack();
        enemy.takeDamage(35); // bring Grubnak under 25% HP after defense mitigation (2 DEF)

        System.out.println("Turn 1: firing ON_HIT (enemy at " + enemy.getHp() + "/" + enemy.getMaxHp() + " HP)");
        GameContext hitCtx = new GameContext(hero, enemy, 1, 5, log);
        engine.fireTrigger(Trigger.ON_HIT, hitCtx);
        System.out.println("  Enemy alive? " + enemy.isAlive() + " (finisher rule should have killed it)");
        System.out.println("  Enemy has Poison? " + enemy.hasEffect(Poison.class));

        hero.takeDamage(110); // bring Kael to 10/120, under 30% heal threshold
        System.out.println("\nTurn 3: firing ON_TURN_END (hero at " + hero.getHp() + "/" + hero.getMaxHp() + " HP)");
        GameContext turnCtx = new GameContext(hero, enemy, 3, 0, log);
        engine.fireTrigger(Trigger.ON_TURN_END, turnCtx);
        System.out.println("  Hero HP after heal rule: " + hero.getHp() + " (should be +20)");
        System.out.println("  Hero attack after fury rule: " + hero.getAttack()
            + " (should be +2 from " + startingAttack + ")");

        System.out.println("\n-- Rule log --");
        log.forEach(System.out::println);
    }
}
