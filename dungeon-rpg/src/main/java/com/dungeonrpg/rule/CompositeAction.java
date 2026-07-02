package com.dungeonrpg.rule;

import java.util.List;

/**
 * Runs several Actions in sequence as if they were one. Same COMPOSITE
 * PATTERN as CompositeCondition and CompositeStrategy — this class IS an
 * Action, so a Rule that needs "deal damage AND apply poison AND weaken
 * defense" doesn't need a new hand-written class; it's built by handing
 * a CompositeAction three existing Action objects.
 */
public class CompositeAction implements Action {

    private static final long serialVersionUID = 1L;

    private final List<Action> actions;

    public CompositeAction(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public void execute(GameContext ctx) {
        for (Action action : actions) {
            action.execute(ctx);
        }
    }
}
