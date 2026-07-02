package com.dungeonrpg;

import com.dungeonrpg.combat.CombatEngine;
import com.dungeonrpg.combat.CombatOutcome;
import com.dungeonrpg.entity.Dragon;
import com.dungeonrpg.entity.Warrior;
import com.dungeonrpg.rule.*;
import com.dungeonrpg.status.Poison;

import java.util.List;

/**
 * TEMPORARY: proves the rule engine actually affects a real fight run
 * through CombatEngine (not the isolated hand-fired triggers in
 * RuleSmokeTest). Gives Kael a rule that poisons the Dragon whenever he
 * lands a hit, then fights a real fight and confirms the poison actually
 * appears mid-combat, driven entirely by CombatEngine calling
 * hero.getRuleEngine().fireTrigger() internally.
 */
public class CombatRuleIntegrationTest {
    public static void main(String[] args) {
        Warrior hero = new Warrior("Kael");
        Dragon dragon = new Dragon("Ashclaw");

        // "Always true" condition: a plain lambda works here because
        // Condition is a single-method interface. This rule means
        // "whenever I land a hit, poison the enemy" - no extra
        // condition needed beyond the ON_HIT trigger itself firing.
        hero.getRuleEngine().addRule(new Rule(
            "Venom Strike",
            Trigger.ON_HIT,
            ctx -> true,
            new ApplyStatusAction(() -> new Poison(4, 3), false)));

        CombatEngine engine = new CombatEngine(hero, dragon);

        CombatOutcome outcome = CombatOutcome.ONGOING;
        int guard = 0;
        while (outcome == CombatOutcome.ONGOING && guard < 30) {
            outcome = engine.attack();
            guard++;
        }

        System.out.println("Fight ended after " + guard + " actions: " + outcome);
        System.out.println("\nFull combat log:");
        List<String> log = engine.getLog();
        log.forEach(System.out::println);

        boolean ruleActuallyFired = log.stream().anyMatch(line -> line.contains("[Rule]"));
        System.out.println("\nRule fired during real combat: " + ruleActuallyFired);
    }
}
