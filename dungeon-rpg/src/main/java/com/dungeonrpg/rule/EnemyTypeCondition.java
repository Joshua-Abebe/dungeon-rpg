package com.dungeonrpg.rule;

/** True when the opponent is a specific enemy type (matched by simple class name, e.g. "Dragon"). */
public class EnemyTypeCondition implements Condition {

    private static final long serialVersionUID = 1L;

    private final String enemyClassName;

    public EnemyTypeCondition(String enemyClassName) {
        this.enemyClassName = enemyClassName;
    }

    @Override
    public boolean evaluate(GameContext ctx) {
        return ctx.getOpponent().getClass().getSimpleName().equals(enemyClassName);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EnemyTypeCondition other)) return false;
        return enemyClassName.equals(other.enemyClassName);
    }

    @Override
    public int hashCode() {
        return enemyClassName.hashCode();
    }
}
