package com.dungeonrpg.ai;

import com.dungeonrpg.entity.Character;

/**
 * Cautious behavior: if this enemy is below half health, it guards (skips
 * its attack) instead of pressing forward. Trolls use this — high HP and
 * DEF makes "wait it out" a genuinely viable, thematically fitting choice,
 * unlike a fragile Goblin where guarding would just delay the inevitable.
 */
public class DefensiveStrategy implements AIStrategy {

    @Override
    public void decideAction(Character self, Character target) {
        boolean lowHealth = self.getHp() < self.getMaxHp() / 2;
        if (lowHealth) {
            return; // guarding: no action this turn
        }
        target.takeDamage(self.getAttack());
    }
}
