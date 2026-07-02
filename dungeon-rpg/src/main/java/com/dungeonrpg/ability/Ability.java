package com.dungeonrpg.ability;

import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.entity.Character;

import java.io.Serializable;

/**
 * Ability is an INTERFACE, not an abstract class, because ShieldBash,
 * Fireball, and Pickpocket share no state and no behavior at all — they
 * only share a contract: "given a hero and a target, do your one special
 * thing." When there's nothing to reuse, only a shape to guarantee,
 * an interface is the right tool (vs. an abstract class, which is for
 * sharing implementation).
 *
 * Extends Serializable so a Hero (which holds one of these) can be
 * written to a save file. Because ShieldBash/Fireball/Pickpocket hold no
 * fields at all, this costs them nothing — they become serializable
 * automatically just by implementing this interface.
 */
public interface Ability extends Serializable {
    void use(Hero user, Character target);
}

