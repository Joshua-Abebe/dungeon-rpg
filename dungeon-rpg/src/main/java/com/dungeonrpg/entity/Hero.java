package com.dungeonrpg.entity;

import com.dungeonrpg.item.Inventory;
import com.dungeonrpg.item.Item;
import com.dungeonrpg.ability.Ability;
import com.dungeonrpg.exception.EmptyInventoryException;
import com.dungeonrpg.rule.RuleEngine;
import com.dungeonrpg.rule.RuleSet;

/**
 * Hero is the abstract parent of Warrior, Mage, and Rogue.
 *
 * WHY A SEPARATE LAYER BETWEEN Character AND Warrior: Hero and Enemy share
 * the "is a Character" relationship, but heroes specifically need an
 * inventory and gold, and use an Ability object, while enemies specifically
 * need an AIStrategy. Putting hero-only concerns here (not in Character)
 * keeps Character lean and keeps Enemy from inheriting fields it would
 * never use — that's information hiding working in both directions.
 *
 * COMPOSITION: Hero *has-a* Inventory. The Hero doesn't extend Inventory or
 * reimplement item storage — it holds a reference to one and delegates to
 * it. This is the classic "has-a vs is-a" distinction in action.
 *
 * Hero also *has-a* RuleEngine: whatever Rules the player composed before
 * this run travel with the Hero exactly the same way their Inventory
 * does — one more collaborator object, not a redesign of Hero itself.
 */
public abstract class Hero extends Character {

    private final Inventory inventory;
    private int gold;
    private final Ability ability;
    private final RuleEngine ruleEngine = new RuleEngine();
    // The player's rule "workspace" for this playthrough: every active
    // rule they've added via the Rule Builder. Owned by Hero (not by
    // the Rule Builder screen) specifically so it survives every time
    // the Rule Builder is opened and closed — a screen you open and
    // close should never be where persistent state actually lives.
    private final RuleSet ruleSet = new RuleSet();

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    protected Hero(String name, int maxHp, int attack, int defense, Ability ability) {
        super(name, maxHp, attack, defense);
        this.inventory = new Inventory();
        this.gold = 0;
        this.ability = ability;
    }

    public RuleEngine getRuleEngine() {
        return ruleEngine;
    }

    /**
     * Satisfies Character's abstract act(). In the finished game, the
     * player chooses Attack / Ability / Item / Flee each turn through the
     * combat UI, and that UI calls basicAttack() or useAbility() directly
     * — so act() is rarely called on a Hero in practice. It still needs a
     * real implementation (not left abstract) so that Hero itself isn't
     * abstract-by-accident in places like AI-controlled test fights or a
     * "auto-battle" convenience feature. Default behavior: a basic attack.
     */
    @Override
    public void act(Character target) {
        basicAttack(target);
    }

    /**
     * The bread-and-butter attack every hero shares, regardless of class.
     * Subclasses don't override this — they override useAbility() for
     * what's actually unique to them (reuse: this logic is written once,
     * here, and every Hero subtype gets it for free).
     */
    public void basicAttack(Character target) {
        target.takeDamage(getAttack());
    }

    /**
     * Overloaded variant of basicAttack: same method name, different
     * parameter list (OVERLOADING POLYMORPHISM). It also demonstrates
     * COERCION: getAttack() returns an int, but multiplying it against
     * a double criticalMultiplier automatically widens it to double —
     * no explicit cast needed at the call site. Available for any
     * future critical-hit mechanic; not currently wired into a specific
     * ability, since none of the existing three abilities call for one.
     */
    public void basicAttack(Character target, double criticalMultiplier) {
        double scaledDamage = getAttack() * criticalMultiplier; // int -> double coercion
        target.takeDamage((int) Math.round(scaledDamage));
    }

    /**
     * Delegates to whichever Ability this hero was built with (Shield Bash,
     * Fireball, Pickpocket). The Hero doesn't know or care which one it
     * is — it just calls use(). That's the Ability interface doing its job.
     */
    public void useAbility(Character target) {
        ability.use(this, target);
    }

    public void addItem(Item item) {
        inventory.add(item);
    }

    public void useItem(Item item) throws EmptyInventoryException {
        inventory.consume(item, this);
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    /**
     * Equipment applies its bonus through Character's public
     * boostAttack()/boostDefense() — the same sanctioned entry point the
     * rule engine's ModifyStatAction uses. Both "wearing better armor"
     * and "a rule that weakens an enemy on hit" are really the same
     * operation (change a stat from outside the Character itself), so
     * they share one path in rather than two separate ones.
     */
    public void equipAttackBonus(int amount) {
        boostAttack(amount);
    }

    public void equipDefenseBonus(int amount) {
        boostDefense(amount);
    }

    public int getGold() {
        return gold;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
