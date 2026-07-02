package com.dungeonrpg;

import com.dungeonrpg.entity.Character;
import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.entity.Enemy;
import com.dungeonrpg.entity.Warrior;
import com.dungeonrpg.entity.Mage;
import com.dungeonrpg.entity.Rogue;
import com.dungeonrpg.entity.Goblin;
import com.dungeonrpg.entity.Skeleton;
import com.dungeonrpg.entity.Troll;
import com.dungeonrpg.entity.Dragon;

/**
 * TEMPORARY smoke test, not the real game entry point. Instantiates every
 * concrete Hero and Enemy subclass and runs one real fight, to prove the
 * full hierarchy (all 3 heroes, all 4 enemies, every ability and AI
 * strategy) actually compiles and behaves, not just the original pair.
 * Will be deleted once the real combat screen exists.
 */
public class Main {
    public static void main(String[] args) {
        // Prove every concrete class actually instantiates without error.
        Hero[] heroes = { new Warrior("Kael"), new Mage("Lyra"), new Rogue("Finn") };
        Enemy[] enemies = { new Goblin("Grubnak"), new Skeleton("Rattlebone"),
                             new Troll("Boulder"), new Dragon("Ashclaw") };

        System.out.println("Instantiated " + heroes.length + " hero types and "
            + enemies.length + " enemy types successfully.");
        for (Hero h : heroes) {
            System.out.println("  " + h.getClass().getSimpleName() + ": "
                + h.getHp() + " HP, " + h.getAttack() + " ATK");
        }
        for (Enemy e : enemies) {
            System.out.println("  " + e.getClass().getSimpleName() + ": "
                + e.getHp() + " HP, " + e.getAttack() + " ATK");
        }

        // Run one real fight: Warrior vs Dragon, using abilities this time.
        System.out.println("\n-- Kael vs Ashclaw --");
        Hero warrior = heroes[0];
        Character dragon = enemies[3];
        int turn = 1;
        while (warrior.isAlive() && dragon.isAlive() && turn <= 15) {
            System.out.println("Turn " + turn + ":");

            if (turn % 3 == 0) {
                warrior.useAbility(dragon);
                System.out.println("  Kael uses Shield Bash -> Ashclaw HP: " + dragon.getHp());
            } else {
                warrior.act(dragon);
                System.out.println("  Kael attacks -> Ashclaw HP: " + dragon.getHp());
            }
            if (!dragon.isAlive()) break;

            dragon.act(warrior);
            System.out.println("  Ashclaw acts -> Kael HP: " + warrior.getHp());

            warrior.tickStatusEffects();
            dragon.tickStatusEffects();
            turn++;
        }

        Character winner = warrior.isAlive() ? warrior : dragon;
        System.out.println("\n" + winner.getClass().getSimpleName() + " wins!");
    }
}

