package com.dungeonrpg.entity;

import com.dungeonrpg.status.StatusEffect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Character is the root of every living thing in the dungeon: heroes and
 * enemies alike. It owns the state every Character needs (hp, attack,
 * defense) and the behavior every Character must define for itself (act()).
 *
 * WHY ABSTRACT: there is no such thing as a bare "Character" in the game —
 * you only ever have a Warrior, a Goblin, etc. Making this class abstract
 * stops anyone from accidentally instantiating a Character directly, and
 * forces every subclass to supply its own act() logic (abstraction +
 * information hiding).
 *
 * IMPLEMENTS SERIALIZABLE so save/load can write a Hero (and everything
 * it's carrying — Inventory, Ability, StatusEffects) straight to disk.
 * Because Hero, Enemy, and every concrete subclass extend Character,
 * they all become serializable automatically without touching them
 * individually — Java's Serializable, once present on a superclass,
 * applies to the whole subtree.
 */
public abstract class Character implements Serializable {

    private static final long serialVersionUID = 1L;

    // ENCAPSULATION: all fields are private. Nothing outside this class
    // (not even subclasses) can reach in and set hp = -50 directly. Every
    // change to state goes through a validated method below.
    private final String name;
    private int hp;
    private int maxHp;
    private int attack;
    private int defense;

    private final List<StatusEffect> activeEffects = new ArrayList<>();

    protected Character(String name, int maxHp, int attack, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
    }

    /**
     * Every concrete subclass (Warrior, Goblin, ...) must decide what
     * happens when it's this Character's turn. This is the hook that makes
     * INCLUSION POLYMORPHISM possible: a List<Character> can hold Warriors
     * and Goblins side by side, and calling act() on each one runs whatever
     * that specific subclass implemented, without the caller needing to
     * know which subtype it's dealing with.
     */
    public abstract void act(Character target);

    // --- Validated state changes (encapsulation in action) ---

    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage cannot be negative: " + amount);
        }
        // Minimum 1 damage always gets through, even against very high
        // defense. Without this floor, a high-DEF enemy like Dragon could
        // fully no-sell attacks whose raw damage happened to be <= its
        // defense stat (this is exactly the bug found during the Phase 1
        // smoke test, where Ashclaw's HP never moved against Kael).
        int mitigated = amount <= 0 ? 0 : Math.max(1, amount - defense);
        hp = Math.max(0, hp - mitigated);
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal amount cannot be negative: " + amount);
        }
        hp = Math.min(maxHp, hp + amount);
    }

    public void applyEffect(StatusEffect effect) {
        activeEffects.add(effect);
    }

    /**
     * Runs all active status effects (Poison, Freeze, Stun) at the end of
     * this Character's turn, and removes any that have expired. Every
     * StatusEffect subclass defines its own tick() behavior, so this loop
     * doesn't need to know or care which effects are active — more
     * polymorphism, and it's the shared "reuse" logic mentioned in the
     * project brief (StatusEffect tick logic shared across all effects).
     */
    public void tickStatusEffects() {
        List<StatusEffect> expired = new ArrayList<>();
        for (StatusEffect effect : activeEffects) {
            effect.tick(this);
            if (effect.isExpired()) {
                expired.add(effect);
            }
        }
        activeEffects.removeAll(expired);
    }

    public boolean hasEffect(Class<? extends StatusEffect> effectType) {
        for (StatusEffect effect : activeEffects) {
            if (effectType.isInstance(effect)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    // --- Read-only access (getters, no setters exposed for hp/name) ---

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    // Subclasses ARE allowed to buff/debuff stats (e.g. a status effect
    // lowering defense) but only through this protected method, not by
    // touching the field directly.
    protected void modifyAttack(int delta) {
        this.attack = Math.max(0, this.attack + delta);
    }

    protected void modifyDefense(int delta) {
        this.defense = Math.max(0, this.defense + delta);
    }

    /**
     * Public entry points for external systems that legitimately need to
     * change combat stats but aren't part of the Character hierarchy
     * itself — most notably ModifyStatAction in the rule engine, where a
     * player-composed Rule might read "on hit, permanently weaken the
     * enemy's defense." Kept as separate public methods (rather than
     * just making modifyAttack/modifyDefense public outright) so it's
     * clear in the code which callers are "part of the character" vs.
     * "an external system acting on the character."
     */
    public void boostAttack(int delta) {
        modifyAttack(delta);
    }

    public void boostDefense(int delta) {
        modifyDefense(delta);
    }

    /**
     * Multiplies max HP, attack, and defense by the given factor, and
     * fully heals to the new max — used by DifficultyScaler to make
     * enemies tougher on later floors / higher difficulty settings
     * without needing a different constructor or subclass per
     * difficulty tier. One method, reused for every enemy type.
     */
    public void scaleStats(double multiplier) {
        this.maxHp = (int) Math.round(this.maxHp * multiplier);
        this.hp = this.maxHp;
        this.attack = (int) Math.round(this.attack * multiplier);
        this.defense = (int) Math.round(this.defense * multiplier);
    }
}
