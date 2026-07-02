package com.dungeonrpg.rule;

/**
 * True on specific turn numbers. "everyNTurns" mode (turnNumber acting
 * as the interval) lets a player build a rule like "every 3rd turn,
 * heal myself" without needing a separate RepeatingCondition class —
 * one boolean flag reuses the same field two different ways.
 */
public class TurnNumberCondition implements Condition {

    private static final long serialVersionUID = 1L;

    private final int turnNumber;
    private final boolean everyNTurns;

    public TurnNumberCondition(int turnNumber, boolean everyNTurns) {
        this.turnNumber = turnNumber;
        this.everyNTurns = everyNTurns;
    }

    @Override
    public boolean evaluate(GameContext ctx) {
        if (everyNTurns) {
            return turnNumber > 0 && ctx.getTurnNumber() % turnNumber == 0;
        }
        return ctx.getTurnNumber() == turnNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TurnNumberCondition other)) return false;
        return turnNumber == other.turnNumber && everyNTurns == other.everyNTurns;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(turnNumber, everyNTurns);
    }
}
