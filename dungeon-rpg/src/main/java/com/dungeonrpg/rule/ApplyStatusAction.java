package com.dungeonrpg.rule;

import com.dungeonrpg.entity.Character;
import com.dungeonrpg.status.StatusEffect;

/** Applies a status effect to self or opponent. */
public class ApplyStatusAction implements Action {

    private static final long serialVersionUID = 1L;

    private final StatusEffectFactory factory;
    private final boolean targetSelf;

    // Sampled ONCE at construction, purely for equals()/hashCode().
    // StatusEffectFactory is a lambda, and Java lambdas don't override
    // equals() to compare based on what they'd produce — two separately
    // built factories that would always create equal StatusEffects are
    // otherwise indistinguishable from two that never would. This
    // sample is never applied to a Character and never mutated; the
    // real factory field is still what execute() actually calls each
    // time, since a fresh instance per application matters (status
    // effects carry their own remaining-duration state).
    private final StatusEffect comparisonSample;

    public ApplyStatusAction(StatusEffectFactory factory, boolean targetSelf) {
        this.factory = factory;
        this.targetSelf = targetSelf;
        this.comparisonSample = factory.create();
    }

    @Override
    public void execute(GameContext ctx) {
        Character target = targetSelf ? ctx.getSelf() : ctx.getOpponent();
        StatusEffect effect = factory.create();
        target.applyEffect(effect);
        ctx.logMessage("[Rule] " + target.getName() + " is afflicted by "
            + effect.getClass().getSimpleName() + "!");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ApplyStatusAction other)) return false;
        return targetSelf == other.targetSelf && comparisonSample.equals(other.comparisonSample);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(targetSelf, comparisonSample);
    }
}
