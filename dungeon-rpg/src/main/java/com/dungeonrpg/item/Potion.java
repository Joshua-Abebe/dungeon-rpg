package com.dungeonrpg.item;

import com.dungeonrpg.entity.Hero;

/**
 * Potion: the one Item that's consumable. isConsumable() returning true is
 * what tells Inventory.consume() to remove this item after use — Weapon
 * and Armor return false and stay equipped instead of disappearing.
 */
public class Potion extends Item {

    private final int healAmount;

    public Potion(String name, Rarity rarity, int healAmount) {
        super(name, rarity);
        this.healAmount = healAmount;
    }

    @Override
    public void applyTo(Hero hero) {
        hero.heal(healAmount);
    }

    @Override
    public boolean isConsumable() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Restores " + healAmount + " HP, then is consumed";
    }
}
