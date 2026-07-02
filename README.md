# Dungeon RPG

A 2D tile-based dungeon crawler built in Java with a Swing GUI, centered on a
two-layer, **player-composable combat rule engine**, auto-patrolling enemy
AI, and a five-level, difficulty-scaled dungeon culminating in a boss fight.

Rather than choosing from a fixed menu of pre-built abilities, the player
writes their own combat behavior at runtime through an in-game Rule Builder:
pairing a **Trigger** ("when I land a hit"), a **Condition** ("if my HP is
below 30%"), and an **Action** ("heal myself"). Rules can also chain into one
another through an event bus — one rule's action can publish an event that
wakes up a second, otherwise unrelated rule.

## Features

- **Auto-patrol movement** — the hero moves continuously along the grid,
  bouncing off walls; arrow keys queue a buffered turn taken the instant it
  opens up, rather than moving the hero instantly.
- **Aggro-chase enemy AI** — enemies wander randomly until the hero comes
  within range, then lock on and pursue at half the hero's speed, so outrunning
  a chaser is a real, learnable option (at the cost of that fight's loot).
- **Player-defined rule engine** — compose combat rules from Trigger +
  Condition + Action, including rule chaining via an event bus, with
  duplicate-rule and name-collision detection.
- **Three hero classes** (Warrior, Mage, Rogue) and **four enemy types**
  (Goblin, Skeleton, Troll, Dragon) plus a fixed-stat Dinosaur boss on the
  final floor.
- **Difficulty scaling** — enemy count and stats scale with a 1–10
  difficulty setting on floors 1–4; the boss floor is fixed regardless of
  difficulty.
- **Save/load** — single-slot save via Java object serialization, with a
  background autosave thread.
- **No external dependencies** — every visual (sprites, UI theme, icons) is
  drawn with Java2D shapes, and the project builds with nothing but a JDK.

## Tech stack

- **Language:** Java (JDK 17+)
- **GUI:** Java Swing (`JPanel`/`JComponent` subclasses using `Graphics2D`)
- **Persistence:** native Java object serialization
- **Concurrency:** `Thread` subclasses + `javax.swing.Timer`, coordinated via
  `synchronized` methods and `SwingUtilities.invokeLater()`
- **Build:** plain `javac`/`java` — no Maven/Gradle

## Getting started

Requires a JDK (17+) on your `PATH`.

```bash
cd dungeon-rpg
javac -d out $(find src/main/java -name "*.java")
java -cp out com.dungeonrpg.ui.GameLauncher
```

To open in IntelliJ IDEA: open the repo root directly (no build file
needed) and mark `dungeon-rpg/src/main/java` as a Sources Root if it isn't
detected automatically.

## How to play

1. On launch, pick a hero name, class, and difficulty (1–10).
2. Optionally open the **Rule Builder** before starting to compose combat
   rules in advance — a fresh hero starts with zero rules.
3. The hero auto-patrols; use arrow keys to queue turns at intersections.
4. Colliding with an enemy triggers a "FIGHT!" transition into turn-based
   combat (Attack / Ability / Item / Flee).
5. Reach the stairs to advance floors — bypassing enemies entirely is a
   valid strategy, at the cost of their loot and gold.
6. Floor 5 is a fixed-stat boss fight. Defeat it to win.

Use the **Rules** menu to reopen the Rule Builder at any time outside
combat, and **Game → Main Menu** to return to hero select without
relaunching. **Game → Save/Load** manages the single save slot.

## Architecture

The codebase is organized bottom-up by responsibility, and consistently
separates game rules from rendering (e.g. `GameMap` has no Swing imports;
`MapPanel` has no game rules) so core systems are testable headlessly:

| Package | Responsibility |
|---|---|
| `entity` | `Character` → `Hero`/`Enemy` hierarchy and all concrete types |
| `item` | `Item` (`Weapon`, `Armor`, `Potion`), `Inventory`, `LootTable` |
| `status` | `StatusEffect` and its subclasses (`Poison`, `Freeze`, `Stun`) |
| `ability` / `ai` | Strategy-pattern interfaces (`Ability`, `AIStrategy`) and implementations |
| `map` | `GameMap`, `Tile`, `FloorFactory`, `DifficultyScaler`, `EnemyWanderThread` |
| `combat` | `CombatEngine`, `CombatOutcome`, `BattleBanter` — turn resolution, no Swing |
| `rule` | The two-layer rule engine: `Condition`, `Action`, `Trigger`, `Rule`, `RuleEngine`, `GameEventBus` |
| `save` | `SaveData`, `SaveManager`, `AutosaveThread` |
| `ui` | All Swing components: `GameWindow`, `MapPanel`, `CombatPanel`, `RuleBuilderPanel`, `HeroSelectFrame` |

## Testing

There's no JUnit dependency — correctness is verified with headless,
console-based smoke tests that run the same way as the game itself:

```bash
java -cp out com.dungeonrpg.MapSmokeTest
java -cp out com.dungeonrpg.CombatSmokeTest
java -cp out com.dungeonrpg.FloorSmokeTest
java -cp out com.dungeonrpg.SaveSmokeTest
java -cp out com.dungeonrpg.ConcurrencySmokeTest
java -cp out com.dungeonrpg.RuleSmokeTest
java -cp out com.dungeonrpg.RuleChainSmokeTest
java -cp out com.dungeonrpg.CombatRuleIntegrationTest
java -cp out com.dungeonrpg.DifficultySmokeTest
java -cp out com.dungeonrpg.EventCycleSmokeTest
java -cp out com.dungeonrpg.RuleSetPersistenceSmokeTest
java -cp out com.dungeonrpg.RuleRemovalSmokeTest
java -cp out com.dungeonrpg.RuleDuplicateDetectionSmokeTest
```

`ConcurrencySmokeTest` in particular hammers `moveHero()` and
`wanderEnemies()` from eight threads at once to verify the map's shared
state stays consistent under real contention.

## Known limitations

- Single save slot — manual save and autosave both overwrite the same file.
- Rules built in the Rule Builder are per-playthrough only; they are never
  persisted and are lost on restart.
