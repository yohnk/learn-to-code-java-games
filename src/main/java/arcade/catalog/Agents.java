package arcade.catalog;

import arcade.agents.asteroids.PointAndShootAgent;
import arcade.agents.asteroids.RandomAsteroidsAgent;
import arcade.agents.missile.NearestThreatAgent;
import arcade.agents.missile.RandomMissileAgent;
import arcade.agents.pacman.HungryPacManAgent;
import arcade.agents.pacman.RandomPacManAgent;
import arcade.asteroids.AsteroidsAgent;
import arcade.engine.GameId;
import arcade.missile.MissileCommandAgent;
import arcade.pacman.PacManAgent;
import java.util.List;
import java.util.function.Supplier;
import student.asteroids.MyAgent;

/**
 * How students (and demos) get plugged into a game.
 *
 * <p>{@code my} is the default (keyboard until students uncomment {@code tick}).
 * Add a new sample by appending to the list for that game. The student entry
 * is always {@code my}.
 */
public final class Agents {
    public static final String DEFAULT = "my";

    private Agents() {
    }

    public record Option(String id, String label, Supplier<?> factory) {
    }

    public static List<Option> options(GameId game) {
        return switch (game) {
            case PACMAN -> List.of(
                    new Option("my", "My Agent", student.pacman.MyAgent::new),
                    new Option("keyboard", "KeyboardAgent", arcade.agents.pacman.KeyboardAgent::new),
                    new Option("random", "Random (sample)", RandomPacManAgent::new),
                    new Option("sample", "Hungry (sample)", HungryPacManAgent::new)
            );
            case ASTEROIDS -> List.of(
                    new Option("my", "My Agent", MyAgent::new),
                    new Option("keyboard", "KeyboardAgent", arcade.agents.asteroids.KeyboardAgent::new),
                    new Option("random", "Random (sample)", RandomAsteroidsAgent::new),
                    new Option("sample", "Point and Shoot (sample)", PointAndShootAgent::new)
            );
            case MISSILE -> List.of(
                    new Option("my", "My Agent", student.missile.MyAgent::new),
                    new Option("keyboard", "KeyboardAgent", arcade.agents.missile.KeyboardAgent::new),
                    new Option("random", "Random (sample)", RandomMissileAgent::new),
                    new Option("sample", "Nearest Threat (sample)", NearestThreatAgent::new)
            );
        };
    }

    public static Option find(GameId game, String id) {
        String key = alias(id.trim().toLowerCase());
        return options(game).stream()
                .filter(option -> option.id.equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown agent '" + id + "' for " + game.id() + ". Try: my, keyboard, random, sample"));
    }

    public static PacManAgent pacman(Option option) {
        return (PacManAgent) option.factory.get();
    }

    public static AsteroidsAgent asteroids(Option option) {
        return (AsteroidsAgent) option.factory.get();
    }

    public static MissileCommandAgent missile(Option option) {
        return (MissileCommandAgent) option.factory.get();
    }

    private static String alias(String id) {
        return id.equals("human") ? "keyboard" : id;
    }
}
