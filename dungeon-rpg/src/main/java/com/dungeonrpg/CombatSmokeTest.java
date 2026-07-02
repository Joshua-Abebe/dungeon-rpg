package com.dungeonrpg;

import com.dungeonrpg.combat.CombatEngine;
import com.dungeonrpg.combat.CombatOutcome;
import com.dungeonrpg.entity.Goblin;
import com.dungeonrpg.entity.Warrior;

/**
 * TEMPORARY: verifies CombatEngine logic in plain console output, no Swing
 * involved. Confirms the damage-floor fix, ability usage, and win
 * condition all work correctly before building CombatPanel on top.
 */
public class CombatSmokeTest {
    public static void main(String[] args) {
        Warrior warrior = new Warrior("Kael");
        Goblin goblin = new Goblin("Grubnak");
        CombatEngine engine = new CombatEngine(warrior, goblin);

        CombatOutcome outcome = CombatOutcome.ONGOING;
        int turn = 1;
        while (outcome == CombatOutcome.ONGOING) {
            outcome = (turn % 3 == 0) ? engine.useAbility() : engine.attack();
            turn++;
            if (turn > 20) {
                System.out.println("Combat ran too long, something is wrong.");
                break;
            }
        }

        engine.getLog().forEach(System.out::println);
        System.out.println("\nFinal outcome: " + outcome);
        System.out.println("Kael HP: " + warrior.getHp() + " / " + warrior.getMaxHp());
        System.out.println("Grubnak HP: " + goblin.getHp() + " / " + goblin.getMaxHp());
    }
}
