package com.dungeonrpg.rule;

import com.dungeonrpg.status.StatusEffect;

import java.io.Serializable;

/**
 * A tiny factory for producing a brand-new StatusEffect each time a Rule
 * fires. Deliberately NOT just java.util.function.Supplier<StatusEffect>:
 * a lambda assigned to a functional interface only becomes serializable
 * if that interface itself extends Serializable, and this whole rule
 * system needs to survive save/load. Defining this one-method interface
 * ourselves (extending Serializable) means a lambda like
 * "() -> new Poison(5, 3)" passed to ApplyStatusAction serializes safely
 * — using the stock Supplier here would silently break saving a Hero
 * with rules attached.
 */
public interface StatusEffectFactory extends Serializable {
    StatusEffect create();
}
