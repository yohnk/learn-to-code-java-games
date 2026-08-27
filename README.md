# Arcade Agents

A Java + Gradle classroom kit: students write **agents** that play Pac-Man, Asteroids, and Missile Command.

Each agent is a class the game constructs **once**, then calls a `tick` method every frame. Students put all of their decision logic in `tick`, and keep long-lived data in **fields** on that class (a counter, a target tile, a `Memory` map, …).

The games themselves are original-inspired clones for teaching. They are not arcade-perfect recreations and do not use original ROM assets.

## Requirements

- **JDK 21 or newer** (the project is developed against JDK 21 bytecode; JDK 25 works)
- No other installs. Gradle Wrapper is included.

## Run

```bash
./gradlew run
```

That opens a menu. Left/right pick a game, up/down pick who is playing, Enter starts.

Skip the menu:

```bash
./gradlew run --args="--game pacman --agent my"
./gradlew run --args="--game asteroids --agent sample"
./gradlew run --args="--game missile --agent sample"
```

Agents: `my` (default — keyboard until you uncomment `tick`), `keyboard`, `random`, `sample`.

Headless (no window — useful in tests or on a projector machine that is acting as a server):

```bash
./gradlew run --args="--game pacman --agent sample --headless --ticks 4000"
./gradlew test
```

## Where students write code

Only these three files:

| Game | File |
|---|---|
| Pac-Man | [`src/main/java/student/pacman/MyAgent.java`](src/main/java/student/pacman/MyAgent.java) |
| Asteroids | [`src/main/java/student/asteroids/MyAgent.java`](src/main/java/student/asteroids/MyAgent.java) |
| Missile Command | [`src/main/java/student/missile/MyAgent.java`](src/main/java/student/missile/MyAgent.java) |

Everything under `arcade/` is the engine. Students should read the `*State` classes; they do not need to edit them.

`MyAgent` extends `KeyboardAgent`. With `tick` commented out (the default), they play with the keyboard. Uncomment `tick` to run their code; comment it out again to play themselves.

### The agent contract

```java
public final class MyAgent extends KeyboardAgent {
    private int framesAlive;          // lives across ticks

    // Uncomment this method to take over from the keyboard.
    //
    // @Override
    // public PacManAction tick(PacManState state) {
    //     framesAlive++;
    //     return state.toward(state.nearestPellet().x(), state.nearestPellet().y());
    // }
}
```

- **Input:** an immutable snapshot (`PacManState`, `AsteroidsState`, or `MissileCommandState`)
- **Output:** a controller command for this frame
- **Memory:** fields on `MyAgent`, or [`arcade.api.Memory`](src/main/java/arcade/api/Memory.java)

Sample agents (for demos and for students who get stuck) live in `src/main/java/arcade/agents/`.

## Suggested lessons

1. **Keyboard play.** Default is My Agent with `tick` commented out, so arrows / WASD work immediately. Play each game yourselves first so you know the rules. Uncomment `tick` when you are ready to program.
2. **Pac-Man pellets.** The starter agent already chases dots with greedy `toward()` and dies to ghosts. Add “if a ghost is close, run.” When walls get in the way, switch to `pathToward()`.
3. **Pac-Man power pellets.** When frightened, chase ghosts instead.
4. **Asteroids aiming.** `state.facingError(x, y)` is the angle you still need to turn. Fire when it is near zero.
5. **Asteroids survival.** If `nearestAsteroid` is too close, turn away and thrust.
6. **Missile Command leading.** The starter shoots at the missile’s current position. `state.intercept(missile)` leads the target. Try writing the lead math yourselves first.

Keep observations **symbolic** (tiles, positions, velocities). Students should not need pixels or neural nets.

## Controls

| Game | KeyboardAgent |
|---|---|
| Pac-Man | Arrow keys or WASD |
| Asteroids | Left/right rotate, up/W thrust, space fire |
| Missile Command | Arrows move the crosshair, space (or click) fires |
| All | Esc returns to the menu |

## Project layout

```
src/main/java/
  student/          ← students edit these three MyAgent classes
  arcade/
    Launcher.java   ← main
    api/Memory.java
    catalog/Agents.java   ← plug-in list (my / keyboard / random / sample)
    engine/         ← window, loop, menu
    pacman/
    asteroids/
    missile/
    agents/         ← sample brains
```

To add another student agent, create a class that implements the game’s `*Agent` interface and register it in `arcade.catalog.Agents`.

## Design notes

This is intentionally **not** OpenAI Gymnasium. Gymnasium is a Python RL loop over pixel or vector observations. Arcade Agents is a Java teaching loop over readable game state:

`Agent` constructed once → `tick(state) → action` every frame → the game draws what happened.

That is the same shape as university kits such as Ms. Pac-Man vs Ghosts and GVGAI, with a single Gradle project and three games behind one launcher.
