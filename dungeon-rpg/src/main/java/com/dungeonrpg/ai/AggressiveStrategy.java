package com.dungeonrpg.ai;

import com.dungeonrpg.entity.Character;

/**
 * The simplest possible AI: always attack, no conditions. Goblins use this.
 * Because AIStrategy is an interface, this class owes nothing to Enemy or
 * to any other strategy — it's a completely standalone, swappable unit of
 * behavior.
 */
public class AggressiveStrategy implements AIStrategy {

    @Override
    public void decideAction(Character self, Character target) {
        target.takeDamage(self.getAttack());
    }
}
