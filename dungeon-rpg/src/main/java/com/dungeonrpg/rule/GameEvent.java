package com.dungeonrpg.rule;

import java.io.Serializable;

/**
 * A single event traveling through the GameEventBus. Generic over its
 * payload type T — a HIT event might carry the damage dealt (Integer), a
 * STATUS_APPLIED event might carry the StatusEffect that was applied,
 * and so on, without GameEvent needing a different class per payload
 * kind. This is the project brief's "GameEvent<T> — generic, carries
 * event payload" requirement.
 *
 * T is bounded by Serializable (not left as a raw generic <T>) so that
 * whatever payload a GameEvent carries is guaranteed safe to include if
 * a Hero's rule set is ever saved — a bounded type parameter here
 * catches a serialization mistake at compile time instead of a runtime
 * NotSerializableException deep inside a save file.
 */
public class GameEvent<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final EventType type;
    private final T payload;
    private final GameContext context;

    public GameEvent(EventType type, T payload, GameContext context) {
        this.type = type;
        this.payload = payload;
        this.context = context;
    }

    public EventType getType() {
        return type;
    }

    public T getPayload() {
        return payload;
    }

    public GameContext getContext() {
        return context;
    }
}
