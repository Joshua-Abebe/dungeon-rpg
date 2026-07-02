package com.dungeonrpg.item;

import com.dungeonrpg.entity.Hero;

/** Armor: mirrors Weapon exactly but boosts defense instead of attack. */
public class Armor extends Item {

    private final int defenseBonus;
    private int durability;

    public Armor(String name, Rarity rarity, int defenseBonus, int durability) {
        super(name, rarity);
        this.defenseBonus = defenseBonus;
        this.durability = durability;
    }

    @Override
    public void applyTo(Hero hero) {
        hero.equipDefenseBonus(defenseBonus);
    }

    @Override
    public boolean isConsumable() {
        return false;
    }

    @Override
    public String getDescription() {
        return "+" + defenseBonus + " Defense when equipped (durability " + durability + ")";
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
