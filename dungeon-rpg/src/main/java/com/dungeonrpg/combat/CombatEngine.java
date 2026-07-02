package com.dungeonrpg.combat;

import com.dungeonrpg.entity.Enemy;
import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.exception.EmptyInventoryException;
import com.dungeonrpg.item.Item;
import com.dungeonrpg.rule.GameContext;
import com.dungeonrpg.rule.Trigger;
import com.dungeonrpg.status.Freeze;
import com.dungeonrpg.status.Stun;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CombatEngine runs one fight between one Hero and one Enemy. It is
 * deliberately Swing-free — CombatPanel calls into this class but this
 * class never imports javax.swing.anything. Same Model/View split as
 * GameMap and MapPanel: the rules live here, where they can be tested and
 * reasoned about without a display, and the rendering lives entirely in
 * CombatPanel.
 *
 * This is also where the Layer 1 rule system plugs into live gameplay:
 * hero.getRuleEngine().fireTrigger() gets called at the exact moments a
 * player-composed Rule would care about (a hit landing, a turn ending,
 * an enemy dying) — CombatEngine doesn't know or care what rules exist,
 * it just announces "this happened" and lets RuleEngine decide what (if
 * anything) responds.
 */
public class CombatEngine {

    private static final double FLEE_CHANCE = 0.7;

    private final Hero hero;
    private final Enemy enemy;
    private final Random random = new Random();
    private final List<String> log = new ArrayList<>();
    private int turnNumber = 0;

    public CombatEngine(Hero hero, Enemy enemy) {
        this.hero = hero;
        this.enemy = enemy;
        log.add(hero.getName() + " encounters " + enemy.getClass().getSimpleName() + " " + enemy.getName() + "!");
        log.add(hero.getName() + ": \"" + BattleBanter.heroOpening(hero.getClass().getSimpleName()) + "\"");
        log.add(enemy.getName() + ": \"" + BattleBanter.enemyOpening(enemy.getClass().getSimpleName()) + "\"");
    }

    public CombatOutcome attack() {
        return runHeroTurn(() -> hero.basicAttack(enemy), hero.getName() + " attacks!");
    }

    public CombatOutcome useAbility() {
        return runHeroTurn(() -> hero.useAbility(enemy), hero.getName() + " uses an ability!");
    }

    public CombatOutcome useItem(Item item) {
        return runHeroTurn(() -> {
            try {
                hero.useItem(item);
                log.add(hero.getName() + " uses " + item.getName() + ".");
            } catch (EmptyInventoryException e) {
                // Shouldn't happen in practice since the UI only offers
                // items actually in the inventory, but if it does, the
                // turn is simply wasted rather than crashing the game.
                log.add("Couldn't use " + item.getName() + ": " + e.getMessage());
            }
        }, null);
    }

    /**
     * Fleeing has a chance to fail. On success, combat ends immediately
     * with no enemy counterattack. On failure, the attempt itself wastes
     * the hero's turn and the enemy still gets to act — running away isn't
     * free, which is what makes Flee a genuine tactical choice rather than
     * a risk-free escape hatch.
     */
    public CombatOutcome flee() {
        if (random.nextDouble() < FLEE_CHANCE) {
            log.add(hero.getName() + " flees the battle!");
            return CombatOutcome.HERO_FLED;
        }
        turnNumber++;
        log.add(hero.getName() + " tries to flee but can't get away!");
        return runEnemyTurnAndTick();
    }

    private static final double QUIP_CHANCE = 0.45;

    private CombatOutcome runHeroTurn(Runnable heroAction, String announcement) {
        turnNumber++;
        if (announcement != null) {
            log.add(announcement);
        }
        if (hero.hasEffect(Stun.class) || hero.hasEffect(Freeze.class)) {
            log.add(hero.getName() + " is unable to act!");
        } else {
            int enemyHpBefore = enemy.getHp();
            heroAction.run();
            int damageDealt = Math.max(0, enemyHpBefore - enemy.getHp());

            if (enemy.isAlive() && random.nextDouble() < QUIP_CHANCE) {
                log.add(hero.getName() + ": \"" + BattleBanter.heroQuip(hero.getClass().getSimpleName()) + "\"");
            }

            if (damageDealt > 0) {
                GameContext hitCtx = new GameContext(hero, enemy, turnNumber, damageDealt, log);
                hero.getRuleEngine().fireTrigger(Trigger.ON_HIT, hitCtx);
            }
        }

        if (!enemy.isAlive()) {
            GameContext deathCtx = new GameContext(hero, enemy, turnNumber, 0, log);
            hero.getRuleEngine().fireTrigger(Trigger.ON_DEATH, deathCtx);

            log.add(enemy.getName() + ": \"" + BattleBanter.enemyDefeatCry(enemy.getClass().getSimpleName()) + "\"");
            log.add(enemy.getName() + " is defeated!");
            log.add(hero.getName() + ": \"" + BattleBanter.heroVictoryLine() + "\"");
            return CombatOutcome.HERO_WON;
        }
        return runEnemyTurnAndTick();
    }

    private CombatOutcome runEnemyTurnAndTick() {
        if (enemy.hasEffect(Stun.class) || enemy.hasEffect(Freeze.class)) {
            log.add(enemy.getName() + " is unable to act!");
        } else {
            enemy.act(hero);
            if (hero.isAlive() && random.nextDouble() < QUIP_CHANCE) {
                log.add(enemy.getName() + ": \"" + BattleBanter.enemyQuip(enemy.getClass().getSimpleName()) + "\"");
            }
        }

        hero.tickStatusEffects();
        enemy.tickStatusEffects();

        // Fired before the death check on purpose: a rule like "heal
        // myself when critically low" should get a genuine chance to
        // save the hero, not just report on a death that already
        // happened. This is what makes ON_TURN_END rules a real
        // defensive tool instead of a purely cosmetic notification.
        GameContext turnEndCtx = new GameContext(hero, enemy, turnNumber, 0, log);
        hero.getRuleEngine().fireTrigger(Trigger.ON_TURN_END, turnEndCtx);

        if (!hero.isAlive()) {
            log.add(hero.getName() + " has fallen...");
            log.add(enemy.getName() + ": \"" + BattleBanter.enemyGloatLine(enemy.getClass().getSimpleName()) + "\"");
            return CombatOutcome.HERO_DIED;
        }
        return CombatOutcome.ONGOING;
    }

    public List<String> getLog() {
        return List.copyOf(log);
    }

    public Hero getHero() {
        return hero;
    }

    public Enemy getEnemy() {
        return enemy;
    }
}
