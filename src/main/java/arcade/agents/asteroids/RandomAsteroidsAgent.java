package arcade.agents.asteroids;

import arcade.asteroids.AsteroidsAgent;
import arcade.asteroids.AsteroidsControls;
import arcade.asteroids.AsteroidsState;
import java.util.Random;

/** Randomly mashes the controls. Useful as a "this is not enough" demo. */
public final class RandomAsteroidsAgent implements AsteroidsAgent {
    private final Random random = new Random();

    @Override
    public AsteroidsControls tick(AsteroidsState state) {
        return AsteroidsControls.of(
                random.nextBoolean(),
                random.nextBoolean(),
                random.nextDouble() < 0.3,
                random.nextDouble() < 0.08
        );
    }
}
