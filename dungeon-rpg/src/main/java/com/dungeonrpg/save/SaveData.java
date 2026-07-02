package com.dungeonrpg.save;

import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.map.GameMap;

import java.io.Serializable;

/**
 * SaveData is a plain bundle of "everything needed to resume exactly
 * where the player left off" — the Hero (with its inventory, gold, HP)
 * and the current floor's GameMap (with its tile state and remaining
 * enemies). Deliberately just data, no behavior: SaveManager does the
 * actual reading/writing, this class only describes the shape of what
 * gets saved.
 */
public class SaveData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Hero hero;
    private final GameMap map;

    public SaveData(Hero hero, GameMap map) {
        this.hero = hero;
        this.map = map;
    }

    public Hero getHero() {
        return hero;
    }

    public GameMap getMap() {
        return map;
    }
}
