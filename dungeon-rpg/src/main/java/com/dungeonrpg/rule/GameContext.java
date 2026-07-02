package com.dungeonrpg.rule;

import com.dungeonrpg.entity.Character;

import java.io.Serializable;
import java.util.List;

/**
 * GameContext is the one object every Condition and Action gets handed
 * when a Rule is evaluated. It bundles "everything a rule might need to
 * know or affect" — who owns the rule (self), who they're fighting
 * (opponent), what turn it is, and how much damage was just dealt — so
 * Condition/Action implementations only ever need one parameter, not a
 * growing list of them as the rule system gets more capable.
 *
 * Deliberately just a data carrier (no game logic of its own) — same
 * role CombatOutcome and SaveData play elsewhere in this codebase.
 */
public class GameContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Character self;
    private final Character opponent;
    private final int turnNumber;
    private final int lastDamageDealt;
    private final List<String> log;

    public GameContext(Character self, Character opponent, int turnNumber, int lastDamageDealt, List<String> log) {
        this.self = self;
        this.opponent = opponent;
        this.turnNumber = turnNumber;
        this.lastDamageDealt = lastDamageDealt;
        this.log = log;
    }

    public Character getSelf() {
        return self;
    }

    public Character getOpponent() {
        return opponent;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public int getLastDamageDealt() {
        return lastDamageDealt;
    }

    /** Rules can narrate themselves into the same combat log the player already sees. */
    public void logMessage(String message) {
        log.add(message);
    }
}
