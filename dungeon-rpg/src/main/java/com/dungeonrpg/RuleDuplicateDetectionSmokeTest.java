package com.dungeonrpg;

import com.dungeonrpg.rule.*;
import com.dungeonrpg.status.Poison;

/**
 * TEMPORARY: proves Rule.hasSameBehaviorAs() correctly identifies two
 * differently-NAMED rules as duplicates when their Trigger, Condition,
 * and Action are all identical — and correctly does NOT flag rules that
 * merely look similar but actually differ in one of those three parts.
 * Specifically exercises the two hardest cases: AlwaysCondition (which
 * used to be a bare lambda with no meaningful equals()) and
 * ApplyStatusAction wrapping a Poison factory (which used to be
 * completely opaque for comparison purposes).
 */
public class RuleDuplicateDetectionSmokeTest {
    public static void main(String[] args) {
        // Case 1: identical Trigger + Condition + Action, different names -> duplicate.
        Rule ruleA = new Rule("Extra Damage", Trigger.ON_HIT, new AlwaysCondition(), new DealDamageAction(5, false));
        Rule ruleB = new Rule("Bonus Damage", Trigger.ON_HIT, new AlwaysCondition(), new DealDamageAction(5, false));
        System.out.println("Case 1 - same behavior, different names -> hasSameBehaviorAs: "
            + ruleA.hasSameBehaviorAs(ruleB) + " (expected true)");

        // Case 2: same Trigger + Action, but DIFFERENT Condition params -> NOT a duplicate.
        Rule ruleC = new Rule("Early Strike", Trigger.ON_HIT, new TurnNumberCondition(1, false), new DealDamageAction(5, false));
        Rule ruleD = new Rule("Late Strike", Trigger.ON_HIT, new TurnNumberCondition(2, false), new DealDamageAction(5, false));
        System.out.println("Case 2 - different Condition params -> hasSameBehaviorAs: "
            + ruleC.hasSameBehaviorAs(ruleD) + " (expected false)");

        // Case 3: same Trigger + Condition, but DIFFERENT Action amount -> NOT a duplicate.
        Rule ruleE = new Rule("Small Hit", Trigger.ON_HIT, new AlwaysCondition(), new DealDamageAction(5, false));
        Rule ruleF = new Rule("Big Hit", Trigger.ON_HIT, new AlwaysCondition(), new DealDamageAction(50, false));
        System.out.println("Case 3 - different Action amount -> hasSameBehaviorAs: "
            + ruleE.hasSameBehaviorAs(ruleF) + " (expected false)");

        // Case 4: different Trigger entirely -> NOT a duplicate, even with identical Condition+Action.
        Rule ruleG = new Rule("On Hit Heal", Trigger.ON_HIT, new AlwaysCondition(), new HealAction(10, true));
        Rule ruleH = new Rule("On Death Heal", Trigger.ON_DEATH, new AlwaysCondition(), new HealAction(10, true));
        System.out.println("Case 4 - different Trigger -> hasSameBehaviorAs: "
            + ruleG.hasSameBehaviorAs(ruleH) + " (expected false)");

        // Case 5: the hard case - two ApplyStatusAction(Poison) rules with
        // identical params, built as two SEPARATE lambda factories (so
        // reference equality alone would wrongly say "different").
        Rule ruleI = new Rule("Poison A", Trigger.ON_HIT, new AlwaysCondition(),
            new ApplyStatusAction(() -> new Poison(3, 2), false));
        Rule ruleJ = new Rule("Poison B", Trigger.ON_HIT, new AlwaysCondition(),
            new ApplyStatusAction(() -> new Poison(3, 2), false));
        System.out.println("Case 5 - separately-built Poison factories, same params -> hasSameBehaviorAs: "
            + ruleI.hasSameBehaviorAs(ruleJ) + " (expected true)");

        // Case 6: same as Case 5 but different Poison duration -> NOT a duplicate.
        Rule ruleK = new Rule("Poison C", Trigger.ON_HIT, new AlwaysCondition(),
            new ApplyStatusAction(() -> new Poison(3, 2), false));
        Rule ruleL = new Rule("Poison D", Trigger.ON_HIT, new AlwaysCondition(),
            new ApplyStatusAction(() -> new Poison(3, 5), false));
        System.out.println("Case 6 - different Poison duration -> hasSameBehaviorAs: "
            + ruleK.hasSameBehaviorAs(ruleL) + " (expected false)");

        boolean allCorrect = ruleA.hasSameBehaviorAs(ruleB)
            && !ruleC.hasSameBehaviorAs(ruleD)
            && !ruleE.hasSameBehaviorAs(ruleF)
            && !ruleG.hasSameBehaviorAs(ruleH)
            && ruleI.hasSameBehaviorAs(ruleJ)
            && !ruleK.hasSameBehaviorAs(ruleL);
        System.out.println("\nPASSED: " + allCorrect);
    }
}
