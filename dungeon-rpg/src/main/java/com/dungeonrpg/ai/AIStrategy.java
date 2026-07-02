package com.dungeonrpg.ai;

import com.dungeonrpg.entity.Character;

import java.io.Serializable;

/**
 * AIStrategy is an interface for the same reason Ability is: Aggressive,
 * Defensive, and Random behavior share no code, only a contract. This is
 * the classic STRATEGY PATTERN — Enemy holds a reference to an AIStrategy
 * and calls decideAction() without knowing which concrete strategy it has.
 * Swapping a Goblin's strategy at runtime is just swapping the object it
 * points to; no changes to Enemy or Goblin needed.
 *
 * Extends Serializable so an Enemy (which holds one of these) can be
 * written to a save file. RandomStrategy's java.util.Random field and
 * CompositeStrategy's List<AIStrategy> field are both already
 * serializable in the JDK, so nothing extra is needed there either.
 */
public interface AIStrategy extends Serializable {
    void decideAction(Character self, Character target);
}

