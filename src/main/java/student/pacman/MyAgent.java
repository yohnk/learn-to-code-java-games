package student.pacman;

import arcade.agents.pacman.KeyboardAgent;
import arcade.engine.GridPos;
import arcade.pacman.PacManAction;
import arcade.pacman.PacManState;

/**
 * YOUR Pac-Man code lives in this file.
 *
 * <p>Until you uncomment {@code tick}, this agent is a keyboard: arrows / WASD.
 * Uncomment {@code tick} to take over. Comment it out again to play yourself.
 *
 * <p>The game creates this class once. Add fields below to remember information
 * between frames.
 */
public final class MyAgent extends KeyboardAgent {

    /** Example of long-lived memory. Add more fields as you need them. */
    @SuppressWarnings("unused")
    private int framesAlive;

    // Uncomment this method to write your own Pac-Man brain.
    // Comment it out again if you want the keyboard back.
    //
    // @Override
    // public PacManAction tick(PacManState state) {
    //     framesAlive++;
    //
    //     GridPos pellet = state.nearestPellet();
    //     if (pellet == null) {
    //         return PacManAction.NONE;
    //     }
    //     // Starter strategy: always chase pellets. Ghosts will catch you —
    //     // that is your first improvement to make.
    //     return state.toward(pellet.x(), pellet.y());
    // }
}
