package com.dungeonrpg.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * RuleSet is the player-facing manifest of every rule currently active
 * on a Hero. It's kept deliberately separate from RuleEngine: RuleSet
 * exists purely so the Rule Builder UI has something to display and
 * track (including chained rules registered via subscribeToEvent(),
 * which RuleEngine.getRules() alone wouldn't show, since that only
 * returns directly-addRule()-registered rules) — RuleEngine remains the
 * thing that actually runs rules during combat. pushToEngine() is the
 * bridge between them: copying a RuleSet's active rules into a Hero's
 * real RuleEngine.
 */
public class RuleSet implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Rule> activeRules = new ArrayList<>();

    public void addActiveRule(Rule rule) {
        activeRules.add(rule);
    }

    public void removeActiveRule(Rule rule) {
        activeRules.remove(rule);
    }

    public List<Rule> getActiveRules() {
        return List.copyOf(activeRules);
    }

    /** Copies every active rule into a live RuleEngine — the bridge from "composed loadout" to "actually running in combat." */
    public void pushToEngine(RuleEngine engine) {
        for (Rule rule : activeRules) {
            engine.addRule(rule);
        }
    }
}
