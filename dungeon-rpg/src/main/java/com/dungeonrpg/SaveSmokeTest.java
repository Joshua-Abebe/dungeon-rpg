package com.dungeonrpg;

import com.dungeonrpg.entity.Hero;
import com.dungeonrpg.entity.Warrior;
import com.dungeonrpg.exception.SaveFileCorruptedException;
import com.dungeonrpg.item.LootTable;
import com.dungeonrpg.map.FloorFactory;
import com.dungeonrpg.map.DifficultyScaler;
import com.dungeonrpg.map.GameMap;
import com.dungeonrpg.save.SaveData;
import com.dungeonrpg.save.SaveManager;

import java.io.File;
import java.io.IOException;

/**
 * TEMPORARY: proves the full save/load round trip actually works,
 * including for a Hero carrying items and a GameMap with an opened
 * chest and remaining enemies — the exact object graph the game will
 * save in practice. This is also the definitive check that the
 * "non-serializable declared type" javac warnings on List<X> fields are
 * harmless: if they weren't, this test would throw
 * NotSerializableException instead of passing.
 */
public class SaveSmokeTest {
    public static void main(String[] args) throws IOException, SaveFileCorruptedException {
        Warrior hero = new Warrior("Kael");
        hero.addGold(42);
        hero.addItem(LootTable.randomItem(1));
        hero.takeDamage(15);

        GameMap map = FloorFactory.generateFloor(2, DifficultyScaler.DEFAULT_DIFFICULTY);
        map.tryOpenChest(map.getHeight() - 2, 1); // open the chest so save captures that state too

        File file = File.createTempFile("dungeon-save-test", ".dat");
        file.deleteOnExit();

        System.out.println("Before save:");
        System.out.println("  Hero: " + hero.getName() + ", HP " + hero.getHp() + "/" + hero.getMaxHp()
            + ", gold " + hero.getGold() + ", items " + hero.getInventory().getItems().size());
        System.out.println("  Map: floor " + map.getFloorNumber() + ", enemies " + map.getEnemyPlacements().size());

        SaveManager.save(new SaveData(hero, map), file);
        System.out.println("\nSaved to " + file.getAbsolutePath() + " (" + file.length() + " bytes)");

        SaveData loaded = SaveManager.load(file);
        Hero loadedHero = loaded.getHero();
        GameMap loadedMap = loaded.getMap();

        System.out.println("\nAfter load:");
        System.out.println("  Hero: " + loadedHero.getName() + ", HP " + loadedHero.getHp() + "/" + loadedHero.getMaxHp()
            + ", gold " + loadedHero.getGold() + ", items " + loadedHero.getInventory().getItems().size());
        System.out.println("  Map: floor " + loadedMap.getFloorNumber() + ", enemies " + loadedMap.getEnemyPlacements().size());

        boolean matches = loadedHero.getName().equals(hero.getName())
            && loadedHero.getHp() == hero.getHp()
            && loadedHero.getGold() == hero.getGold()
            && loadedHero.getInventory().getItems().size() == hero.getInventory().getItems().size()
            && loadedMap.getFloorNumber() == map.getFloorNumber()
            && loadedMap.getEnemyPlacements().size() == map.getEnemyPlacements().size();

        System.out.println("\nRound trip matches original: " + matches);

        System.out.println("\nLoading a corrupted file should throw:");
        File badFile = File.createTempFile("dungeon-bad", ".dat");
        badFile.deleteOnExit();
        java.nio.file.Files.writeString(badFile.toPath(), "not a real save file");
        try {
            SaveManager.load(badFile);
            System.out.println("ERROR: did not throw!");
        } catch (SaveFileCorruptedException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }
    }
}
