package com.dungeonrpg.item;

import com.dungeonrpg.entity.Hero;

import java.io.Serializable;

/**
 * Item is the abstract parent of Weapon, Armor, and Potion.
 *
 * All three share a name and a rarity tier, but each does something
 * completely different when used — a weapon gets equipped, a potion gets
 * drunk and destroyed. applyTo() is the polymorphic hook: Inventory can
 * call applyTo() on ANY item without an if/else chain checking "is this a
 * Weapon? Is this a Potion?" (that if/else chain is exactly what
 * polymorphism exists to eliminate).
 */
public abstract class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Rarity { COMMON, UNCOMMON, RARE, LEGENDARY }

    private final String name;
    private final Rarity rarity;

    protected Item(String name, Rarity rarity) {
        this.name = name;
        this.rarity = rarity;
    }

    public abstract void applyTo(Hero hero);

    /** True if using this item consumes it (Potions), false if it's equipped (Weapon/Armor). */
    public abstract boolean isConsumable();

    /**
     * A short, player-facing description of what this item actually
     * does — shown when the player picks it up from a chest, and again
     * when choosing which item to use in combat. Same polymorphic
     * pattern as applyTo(): callers never need to check "is this a
     * Weapon or a Potion" to know what text to show, they just call
     * getDescription() and each subclass answers for itself.
     */
    public abstract String getDescription();

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }
}
