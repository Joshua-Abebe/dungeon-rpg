package com.dungeonrpg.item;

import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.exception.EmptyInventoryException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory holds a Hero's items. It's a small, focused class whose only
 * job is storage and validated access — this is deliberately kept separate
 * from Hero itself (single responsibility), which is what makes the
 * composition relationship (Hero has-a Inventory) worth having instead of
 * just putting a List<Item> field directly on Hero.
 *
 * PARAMETRIC POLYMORPHISM will show up here too later — this becomes
 * Inventory<T extends Item> once we need type-specific inventories
 * (e.g. a weapons-only inventory). Keeping it simple for now per rule #1:
 * plain Inventory works for the full game, generics are a stretch upgrade.
 */
public class Inventory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Item> items = new ArrayList<>();

    public void add(Item item) {
        items.add(item);
    }

    public void consume(Item item, Hero owner) throws EmptyInventoryException {
        if (!items.contains(item)) {
            throw new EmptyInventoryException(
                "Cannot use " + item.getName() + ": not in inventory");
        }
        item.applyTo(owner);
        if (item.isConsumable()) {
            items.remove(item);
        }
    }

    public List<Item> getItems() {
        return List.copyOf(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
