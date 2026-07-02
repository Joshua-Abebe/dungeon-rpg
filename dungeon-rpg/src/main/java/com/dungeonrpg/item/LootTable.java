package com.dungeonrpg.item;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * LootTable is a small factory: given a floor number, hand back a
 * reasonable random Item. Kept separate from Weapon/Armor/Potion
 * themselves — those classes describe what an item IS and does, this
 * class decides WHICH one and with what stats to generate for a chest.
 * Mixing "what am I" with "how am I randomly generated" into the same
 * class would make Item harder to reason about for no real benefit.
 */
public final class LootTable {

    private LootTable() {
    }

    private static final Random RANDOM = new Random();

    private static final List<Function<Integer, Item>> GENERATORS = List.of(
        floor -> new Weapon("Reinforced Blade", Item.Rarity.UNCOMMON, 3 + floor, 25),
        floor -> new Armor("Traveler's Plate", Item.Rarity.UNCOMMON, 3 + floor, 25),
        floor -> new Potion("Health Potion", Item.Rarity.COMMON, 25 + floor * 5)
    );

    public static Item randomItem(int floorNumber) {
        Function<Integer, Item> generator = GENERATORS.get(RANDOM.nextInt(GENERATORS.size()));
        return generator.apply(floorNumber);
    }
}
