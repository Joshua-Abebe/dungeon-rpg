package com.dungeonrpg.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * RuleEngine holds every Rule a player has built for one Hero and knows
 * how to run "just the ones that matter right now" — fireTrigger() only
 * evaluates Rules registered for the exact Trigger that just happened,
 * so a combat with 10 active rules doesn't mean 10 evaluations every
 * single time anything happens; ON_HIT rules only run on hits, ON_DEATH
 * rules only run on a death, etc.
 *
 * RuleEngine covers both layers of the rule system: direct composition
 * (this class + Condition + Action + Trigger, evaluated via
 * fireTrigger()) and rule chaining (the GameEventBus below, where one
 * rule's Action can publish an event that wakes up a second,
 * independently-registered rule without either one knowing the other
 * exists).
 */
public class RuleEngine implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Rule> rules = new ArrayList<>();

    // Layer 2: RuleEngine *has-a* GameEventBus, same composition pattern
    // as everywhere else in this codebase (Hero has-a Inventory, GameMap
    // has-a Tile[][]). Rules added via addRule() are evaluated directly
    // by fireTrigger() whenever CombatEngine announces a Trigger; rules
    // added via subscribeToEvent() are NOT touched by fireTrigger() at
    // all — they only run when some Action (typically PublishEventAction,
    // possibly from a Layer-1 rule's CompositeAction) explicitly
    // publishes a matching event onto this bus. That's the actual
    // mechanical difference between "a rule" and "a chained rule."
    private final GameEventBus eventBus = new GameEventBus();

    public void addRule(Rule rule) {
        rules.add(rule);
    }

    public void removeRule(Rule rule) {
        rules.remove(rule);
    }

    /**
     * Removes a rule regardless of HOW it was registered. A Rule object
     * carries no flag saying "I was added via addRule()" or "I was
     * subscribed via subscribeToEvent()" — that distinction only exists
     * in which list/map happens to reference it. Since a rule can only
     * ever have been registered one way in practice, trying both removal
     * paths is safe: removing something that was never there is simply
     * a no-op on both List.remove() and GameEventBus.unsubscribe().
     */
    public void removeRuleEverywhere(Rule rule) {
        removeRule(rule);
        for (EventType type : EventType.values()) {
            eventBus.unsubscribe(type, rule);
        }
    }

    public void subscribeToEvent(EventType type, Rule rule) {
        eventBus.subscribe(type, rule);
    }

    public GameEventBus getEventBus() {
        return eventBus;
    }

    public void fireTrigger(Trigger trigger, GameContext ctx) {
        for (Rule rule : rules) {
            if (rule.getTrigger() == trigger) {
                rule.apply(ctx);
            }
        }
    }

    public List<Rule> getRules() {
        return List.copyOf(rules);
    }
}
