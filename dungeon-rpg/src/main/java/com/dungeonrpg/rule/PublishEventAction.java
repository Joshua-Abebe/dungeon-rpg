package com.dungeonrpg.rule;

/**
 * PublishEventAction is what makes Layer 2 chaining possible: it's an
 * ordinary Action (usable anywhere any other Action is, including
 * inside a CompositeAction alongside DealDamageAction, ApplyStatusAction,
 * etc.) whose only effect is announcing "this event just happened" on a
 * GameEventBus. A Rule built with this action doesn't know or care
 * whether anything is listening — it just publishes, and whatever Rules
 * are subscribed to that EventType react on their own.
 */
public class PublishEventAction implements Action {

    private static final long serialVersionUID = 1L;

    private final EventType eventType;
    private final GameEventBus bus;

    public PublishEventAction(EventType eventType, GameEventBus bus) {
        this.eventType = eventType;
        this.bus = bus;
    }

    @Override
    public void execute(GameContext ctx) {
        // Autoboxes to Integer, which satisfies GameEvent's
        // <T extends Serializable> bound automatically.
        bus.publish(new GameEvent<>(eventType, ctx.getLastDamageDealt(), ctx));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PublishEventAction other)) return false;
        return eventType == other.eventType;
    }

    @Override
    public int hashCode() {
        return eventType.hashCode();
    }
}
