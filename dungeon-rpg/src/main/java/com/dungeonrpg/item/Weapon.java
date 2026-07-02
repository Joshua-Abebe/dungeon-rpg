package com.dungeonrpg.item;

import com.dungeonrpg.entity.Hero;

/**
 * Weapon: boosts attack when equipped, and has durability that wears down
 * with use (wired into combat later — for now, the field exists and can
 * be decremented, demonstrating the state is real, not just decorative).
 */
public class Weapon extends Item {

    private final int attackBonus;
    private int durability;

    public Weapon(String name, Rarity rarity, int attackBonus, int durability) {
        super(name, rarity);
        this.attackBonus = attackBonus;
        this.durability = durability;
    }

    @Override
    public void applyTo(Hero hero) {
        hero.equipAttackBonus(attackBonus);
    }

    @Override
    public boolean isConsumable() {
        return false;
    }

    @Override
    public String getDescription() {
        return "+" + attackBonus + " Attack when equipped (durability " + durability + ")";
    }

    public void reduceDurability(int amount) {
        durability = Math.max(0, durability - amount);
    }

    public boolean isBroken() {
        return durability <= 0;
    }

    public int getDurability() {
        return durability;
    }
}
