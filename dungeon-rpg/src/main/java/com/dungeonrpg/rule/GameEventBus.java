package com.dungeonrpg.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GameEventBus is the OBSERVER PATTERN: Rules subscribe() to an
 * EventType they care about, and whenever anything publish()es an event
 * of that type, every subscribed Rule gets evaluated against the
 * event's context. Nothing that publishes an event needs to know who
 * (if anyone) is listening — that's the whole point of Observer, and
 * it's what makes RULE CHAINING possible: Rule A's Action can publish
 * a HIT event without knowing Rule B exists, and Rule B reacts purely
 * because it subscribed to that event type, with no direct reference
 * between the two Rule objects at all.
 */
public class GameEventBus implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<EventType, List<Rule>> subscribers = new HashMap<>();

    // Tracks which EventTypes are currently being dispatched, purely to
    // detect cycles. Nothing stops a player from building a rule via the
    // Rule Builder that is subscribed to EventType.HIT and whose own
    // Action publishes EventType.HIT again (directly, or indirectly
    // through a longer chain of rules) — without this guard, that
    // configuration would recurse until the JVM throws a
    // StackOverflowError and the game crashes. This is not a
    // theoretical concern; the Rule Builder UI makes that exact
    // configuration trivially constructible by an ordinary player.
    private final Set<EventType> inProgress = new HashSet<>();

    public void subscribe(EventType type, Rule rule) {
        subscribers.computeIfAbsent(type, key -> new ArrayList<>()).add(rule);
    }

    public void unsubscribe(EventType type, Rule rule) {
        List<Rule> list = subscribers.get(type);
        if (list != null) {
            list.remove(rule);
        }
    }

    /**
     * The dispatch loop: finds every Rule subscribed to this event's
     * type and runs it. Each subscribed Rule still checks its own
     * Condition first (via Rule.apply) — being subscribed to an event
     * only means "I get asked," not "I automatically fire."
     *
     * If this EventType is already being dispatched somewhere higher up
     * the current call stack (a cycle), this call is a no-op instead of
     * recursing — the second-order chain reaction is deliberately
     * dropped rather than looping forever.
     */
    public void publish(GameEvent<?> event) {
        EventType type = event.getType();
        if (!inProgress.add(type)) {
            return; // cycle detected: this event type is already mid-dispatch
        }
        try {
            List<Rule> subscribed = subscribers.getOrDefault(type, List.of());
            for (Rule rule : subscribed) {
                rule.apply(event.getContext());
            }
        } finally {
            inProgress.remove(type);
        }
    }

    public List<Rule> getSubscribers(EventType type) {
        return List.copyOf(subscribers.getOrDefault(type, List.of()));
    }
}
