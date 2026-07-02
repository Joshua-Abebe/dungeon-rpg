package com.dungeonrpg.ai;

import com.dungeonrpg.entity.Character;

import java.util.Random;

/**
 * Unpredictable behavior: a coin flip each turn between attacking and
 * holding back. Skeletons use this to represent "balanced" — neither
 * reliably aggressive nor reliably defensive, which in practice reads as
 * erratic, bony chaos.
 */
public class RandomStrategy implements AIStrategy {

    private final Random random = new Random();

    @Override
    public void decideAction(Character self, Character target) {
        boolean attacks = random.nextBoolean();
        if (attacks) {
            target.takeDamage(self.getAttack());
        }
    }
}
