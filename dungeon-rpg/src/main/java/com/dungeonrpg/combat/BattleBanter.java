package com.dungeonrpg.combat;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * BattleBanter holds short flavor lines keyed by class name (Warrior,
 * Mage, Rogue, Goblin, Skeleton, Troll, Dragon) and hands out a random
 * one on request. It's deliberately just data plus lookup — no rendering,
 * no game rules — so CombatEngine can call it directly and stay
 * Swing-free, same as everything else in the combat package.
 *
 * Keying by simple class name (a String) rather than adding a "getQuotes()"
 * method to every Hero/Enemy subclass was a scope decision: it keeps the
 * personality/flavor layer completely separate from the gameplay classes,
 * so adding or editing banter never means touching Warrior.java or
 * Dragon.java at all.
 */
public final class BattleBanter {

    private BattleBanter() {
    }

    private static final Random RANDOM = new Random();

    private static final Map<String, List<String>> OPENING_LINES = Map.of(
        "Warrior", List.of("Stand and face me!", "Let's settle this, blade to claw."),
        "Mage", List.of("You'll regret this, creature.", "Feel the weight of the arcane."),
        "Rogue", List.of("You won't even see me coming.", "Careful — I bite from the shadows.")
    );

    private static final Map<String, List<String>> ENEMY_OPENING_LINES = Map.of(
        "Goblin", List.of("Yip yip! Fresh meat!", "Grubnak smash puny hero!"),
        "Skeleton", List.of("Rattle... rattle... you're already dead.", "Bones remember every fight."),
        "Troll", List.of("Grrrnn. You. Small. I. Big.", "Troll no move. Troll SMASH."),
        "Dragon", List.of("You dare challenge me, mortal?", "Ashes remember your name before I do."),
        "Dinosaur", List.of("RAAAWR! None shall pass!", "The princess is MINE to guard!")
    );

    private static final Map<String, List<String>> HERO_QUIPS = Map.of(
        "Warrior", List.of("Is that all you've got?", "My shield has cracked worse than this.", "Hah! Barely felt it."),
        "Mage", List.of("The flames hunger still.", "Burn a little brighter for me.", "Magic always finds its mark."),
        "Rogue", List.of("Didn't even see that, did you?", "Your coin purse is lighter already.", "Too slow.")
    );

    private static final Map<String, List<String>> ENEMY_QUIPS = Map.of(
        "Goblin", List.of("Ow! Grubnak angry now!", "You fight dirty too!"),
        "Skeleton", List.of("Rattlebone does not fear pain.", "A crack? I've had worse."),
        "Troll", List.of("Troll barely notice.", "Hnnf. Annoying human."),
        "Dragon", List.of("A scratch. Nothing more.", "You test my patience, mortal."),
        "Dinosaur", List.of("RAWR! That tickled!", "You cannot save her!")
    );

    private static final Map<String, List<String>> ENEMY_DEFEAT_CRIES = Map.of(
        "Goblin", List.of("Grubnak... retreat...!"),
        "Skeleton", List.of("Back... to the ground..."),
        "Troll", List.of("Troll... falls..."),
        "Dragon", List.of("Impossible... a mortal... defeats ME...?"),
        "Dinosaur", List.of("Impossible... the princess... is free...")
    );

    private static final List<String> HERO_VICTORY_LINES = List.of(
        "It's over.", "Stay down this time.", "Well fought — but I win."
    );

    private static final Map<String, List<String>> ENEMY_GLOAT_LINES = Map.of(
        "Goblin", List.of("Grubnak win! Yip yip!"),
        "Skeleton", List.of("Another set of bones for the pile."),
        "Troll", List.of("Troll... win."),
        "Dragon", List.of("As it should be. Mortals always fall."),
        "Dinosaur", List.of("She stays with ME. RAAAWR!")
    );

    public static String heroOpening(String heroClassName) {
        return pickOrDefault(OPENING_LINES, heroClassName, "Let's finish this.");
    }

    public static String enemyOpening(String enemyClassName) {
        return pickOrDefault(ENEMY_OPENING_LINES, enemyClassName, "You'll regret this.");
    }

    public static String heroQuip(String heroClassName) {
        return pickOrDefault(HERO_QUIPS, heroClassName, "Take that!");
    }

    public static String enemyQuip(String enemyClassName) {
        return pickOrDefault(ENEMY_QUIPS, enemyClassName, "Grr!");
    }

    public static String enemyDefeatCry(String enemyClassName) {
        return pickOrDefault(ENEMY_DEFEAT_CRIES, enemyClassName, "No... not like this...");
    }

    public static String heroVictoryLine() {
        return HERO_VICTORY_LINES.get(RANDOM.nextInt(HERO_VICTORY_LINES.size()));
    }

    public static String enemyGloatLine(String enemyClassName) {
        return pickOrDefault(ENEMY_GLOAT_LINES, enemyClassName, "Victory.");
    }

    private static String pickOrDefault(Map<String, List<String>> bank, String key, String fallback) {
        List<String> options = bank.get(key);
        if (options == null || options.isEmpty()) {
            return fallback;
        }
        return options.get(RANDOM.nextInt(options.size()));
    }
}
