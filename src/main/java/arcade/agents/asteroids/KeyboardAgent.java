package arcade.agents.asteroids;

import arcade.asteroids.AsteroidsAgent;
import arcade.asteroids.AsteroidsControls;
import arcade.asteroids.AsteroidsState;
import arcade.engine.Input;
import arcade.engine.InputAware;

/**
 * Left/right rotate, up/W thrust, space fire.
 */
public class KeyboardAgent implements AsteroidsAgent, InputAware {
    private Input input;

    @Override
    public void setInput(Input input) {
        this.input = input;
    }

    @Override
    public AsteroidsControls tick(AsteroidsState state) {
        if (input == null) {
            return AsteroidsControls.none();
        }
        return AsteroidsControls.of(input.left(), input.right(), input.up(), input.fire());
    }
}
