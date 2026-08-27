package arcade;

import arcade.agents.asteroids.PointAndShootAgent;
import arcade.agents.missile.NearestThreatAgent;
import arcade.agents.pacman.HungryPacManAgent;
import arcade.agents.pacman.RandomPacManAgent;
import arcade.asteroids.AsteroidsGame;
import arcade.catalog.Agents;
import arcade.engine.ArcadeApp;
import arcade.engine.GameId;
import arcade.engine.Games;
import arcade.missile.MissileCommandGame;
import arcade.pacman.Maze;
import arcade.pacman.PacManAction;
import arcade.pacman.PacManGame;
import arcade.pacman.PacManState;
import arcade.pacman.Tile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArcadeAgentsTest {
    @Test
    void mazeRowsAreRectangular() {
        int width = Maze.ROWS[0].length();
        assertTrue(width > 10);
        for (int i = 0; i < Maze.ROWS.length; i++) {
            assertEquals(width, Maze.ROWS[i].length(), "row " + i);
        }
    }

    @Test
    void mazeHasPelletsAndAPacSpawn() {
        Maze maze = new Maze();
        assertTrue(maze.pelletCount() > 50);
        assertEquals(Tile.EMPTY, maze.tile(maze.pacSpawn().x(), maze.pacSpawn().y()));
    }

    @Test
    void ghostsCanLeaveTheHouse() {
        Maze maze = new Maze();
        int doorX = maze.door().x();
        int doorY = maze.door().y();
        assertEquals(Tile.DOOR, maze.tile(doorX, doorY));
        assertTrue(maze.tile(doorX, doorY - 1) != Tile.WALL, "tile above door must be open");
    }

    @Test
    void pacManSnapshotHasHelperMoves() {
        PacManGame game = new PacManGame(state -> PacManAction.NONE, true);
        PacManState state = game.snapshot();
        assertNotNull(state.nearestPellet());
        assertTrue(state.width() > 0);
        assertFalse(state.ghosts().isEmpty());
        boolean anyMove = false;
        for (PacManAction action : PacManAction.moves()) {
            anyMove |= state.canMove(action);
        }
        assertTrue(anyMove);
    }

    @Test
    void hungryPacManScoresByEatingPellets() {
        PacManGame game = new PacManGame(new HungryPacManAgent(), true);
        int score = ArcadeApp.playHeadless(game, 30 * 60);
        assertTrue(score >= 80, "expected pellet eating, score was " + score);
    }

    @Test
    void randomPacManSurvivesAFewSeconds() {
        PacManGame game = new PacManGame(new RandomPacManAgent(), true);
        ArcadeApp.playHeadless(game, 180);
        ArcadeApp.renderOnce(new PacManGame(new RandomPacManAgent(), true));
        assertTrue(game.score() >= 0);
    }

    @Test
    void asteroidsAndMissileRunHeadless() {
        AsteroidsGame asteroids = new AsteroidsGame(new PointAndShootAgent(), true);
        ArcadeApp.playHeadless(asteroids, 240);
        ArcadeApp.renderOnce(new AsteroidsGame(new PointAndShootAgent(), true));

        MissileCommandGame missile = new MissileCommandGame(new NearestThreatAgent(), true);
        ArcadeApp.playHeadless(missile, 240);
        ArcadeApp.renderOnce(new MissileCommandGame(new NearestThreatAgent(), true));
        assertTrue(asteroids.score() >= 0);
        assertTrue(missile.score() >= 0);
    }

    @Test
    void catalogWiresStudentAgents() {
        assertEquals("my", Agents.find(GameId.PACMAN, "my").id());
        assertEquals("keyboard", Agents.find(GameId.ASTEROIDS, "human").id());
        assertEquals(Agents.DEFAULT, Agents.options(GameId.PACMAN).getFirst().id());
        assertEquals("my", Agents.DEFAULT);
        ArcadeApp.playHeadless(Games.create(GameId.PACMAN, Agents.find(GameId.PACMAN, "my"), true), 60);
    }
}
