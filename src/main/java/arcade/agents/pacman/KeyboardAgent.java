package arcade.agents.pacman;

import arcade.engine.Input;
import arcade.engine.InputAware;
import arcade.pacman.PacManAction;
import arcade.pacman.PacManAgent;
import arcade.pacman.PacManState;

/**
 * Passes arrow keys / WASD straight through as Pac-Man moves.
 */
public class KeyboardAgent implements PacManAgent, InputAware {
    private Input input;

    @Override
    public void setInput(Input input) {
        this.input = input;
    }

    @Override
    public PacManAction tick(PacManState state) {
        if (input == null) {
            return PacManAction.NONE;
        }
        if (input.up()) {
            return PacManAction.UP;
        }
        if (input.down()) {
            return PacManAction.DOWN;
        }
        if (input.left()) {
            return PacManAction.LEFT;
        }
        if (input.right()) {
            return PacManAction.RIGHT;
        }
        return PacManAction.NONE;
    }
}
