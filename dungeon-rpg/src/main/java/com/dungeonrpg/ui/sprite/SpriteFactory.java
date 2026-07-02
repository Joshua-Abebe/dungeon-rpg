package com.dungeonrpg.ui.sprite;

import com.dungeonrpg.entity.Character;

import java.util.Map;

/**
 * Looks up the right CombatantSprite for any Character. Sprites are
 * stateless (all animation state lives in CombatantPortrait, not here),
 * so one shared instance per class is enough — no need to build a new
 * sprite object for every Goblin that spawns.
 *
 * Keyed by simple class name rather than an instanceof chain: adding an
 * eighth combatant type later means adding one line here, not editing a
 * chain of if/else checks.
 */
public final class SpriteFactory {

    private SpriteFactory() {
    }

    private static final Map<String, CombatantSprite> SPRITES = Map.of(
        "Warrior", new WarriorSprite(),
        "Mage", new MageSprite(),
        "Rogue", new RogueSprite(),
        "Goblin", new GoblinSprite(),
        "Skeleton", new SkeletonSprite(),
        "Troll", new TrollSprite(),
        "Dragon", new DragonSprite(),
        "Dinosaur", new DinosaurSprite()
    );

    public static CombatantSprite forCharacter(Character character) {
        CombatantSprite sprite = SPRITES.get(character.getClass().getSimpleName());
        if (sprite == null) {
            throw new IllegalArgumentException(
                "No sprite registered for " + character.getClass().getSimpleName());
        }
        return sprite;
    }
}
