package com.dungeonrpg.entity;

import com.dungeonrpg.ai.AIStrategy;

/**
 * Enemy is the abstract parent of Goblin, Skeleton, Troll, and Dragon.
 *
 * Mirrors Hero's shape exactly (extends Character, holds one composed
 * collaborator) but the collaborator is an AIStrategy instead of an
 * Inventory — because what varies between enemy types isn't "what they
 * carry," it's "how they decide what to do." That decision is INFORMATION
 * HIDING: nothing outside Enemy ever needs to know which AIStrategy a
 * given enemy is running — it's a private implementation detail.
 */
public abstract class Enemy extends Character {

    private final AIStrategy strategy;
    private int lootGold;

    protected Enemy(String name, int maxHp, int attack, int defense, AIStrategy strategy, int lootGold) {
        super(name, maxHp, attack, defense);
        this.strategy = strategy;
        this.lootGold = lootGold;
    }

    /**
     * Removes and returns whatever gold this enemy is still holding.
     * Used by Pickpocket (steals mid-combat) and by loot drops on death
     * (full amount, at the end of the fight). Either caller just gets
     * "however much is left" — if Pickpocket already took some, a death
     * drop naturally yields less. No special-casing needed between the
     * two call sites.
     */
    public int takeLootGold() {
        int taken = lootGold;
        lootGold = 0;
        return taken;
    }

    /**
     * act() is inherited as abstract from Character, and here we give
     * every Enemy subtype a common implementation: just defer to whatever
     * strategy this enemy was built with. Concrete subclasses (Goblin,
     * Dragon) don't need to override act() at all unless they want
     * genuinely special turn behavior beyond "run my AI strategy" —
     * that's reuse in action.
     */
    @Override
    public void act(Character target) {
        strategy.decideAction(this, target);
    }
}
