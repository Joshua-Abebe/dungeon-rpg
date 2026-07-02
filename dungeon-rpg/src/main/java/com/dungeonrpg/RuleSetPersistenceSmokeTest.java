package com.dungeonrpg;

import com.dungeonrpg.entity.Warrior;
import com.dungeonrpg.rule.DealDamageAction;
import com.dungeonrpg.rule.Rule;
import com.dungeonrpg.rule.Trigger;

/**
 * TEMPORARY: proves Hero.getRuleSet() genuinely persists across
 * multiple simulated Rule Builder opens, rather than being recreated
 * empty every time the screen is opened and closed. (This test
 * previously also covered template persistence; the template feature
 * was removed from the project, so this test now covers active-rule
 * persistence only, which is still real, load-bearing behavior.)
 */
public class RuleSetPersistenceSmokeTest {
    public static void main(String[] args) {
        Warrior hero = new Warrior("Kael");

        // Simulates opening the Rule Builder the first time and adding a rule.
        Rule rule = new Rule("Test Rule", Trigger.ON_HIT, ctx -> true, new DealDamageAction(5, false));
        hero.getRuleSet().addActiveRule(rule);

        System.out.println("Same RuleSet instance returned on every call to getRuleSet(): "
            + (hero.getRuleSet() == hero.getRuleSet()));

        System.out.println("\nBefore 'reopening': " + hero.getRuleSet().getActiveRules().size() + " active rule(s)");

        // Simulates reopening the Rule Builder - it would ask the SAME
        // hero for its RuleSet again, exactly like RuleBuilderPanel's
        // constructor now does (this.ruleSet = hero.getRuleSet();).
        var reopenedRuleSet = hero.getRuleSet();
        System.out.println("After 'reopening': " + reopenedRuleSet.getActiveRules().size() + " active rule(s)");

        boolean ruleSurvived = reopenedRuleSet.getActiveRules().size() == 1
            && reopenedRuleSet.getActiveRules().get(0).getName().equals("Test Rule");
        System.out.println("\nActive rule survived reopening: " + ruleSurvived);
        System.out.println("PASSED: " + ruleSurvived);
    }
}
