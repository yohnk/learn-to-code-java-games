package arcade.engine;

import arcade.asteroids.AsteroidsGame;
import arcade.catalog.Agents;
import arcade.missile.MissileCommandGame;
import arcade.pacman.PacManGame;

/**
 * Builds a playable screen from a game id and agent choice.
 */
public final class Games {
    private Games() {
    }

    public static GameScreen create(GameId game, Agents.Option agent, boolean headless) {
        return switch (game) {
            case PACMAN -> new PacManGame(Agents.pacman(agent), headless);
            case ASTEROIDS -> new AsteroidsGame(Agents.asteroids(agent), headless);
            case MISSILE -> new MissileCommandGame(Agents.missile(agent), headless);
        };
    }
}
