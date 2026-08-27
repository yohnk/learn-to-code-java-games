package arcade.asteroids;

/**
 * Student-facing Asteroids controller.
 *
 * <p>The game constructs your class once per round, then calls {@link #tick}
 * every frame. Put long-lived objects in fields on your class.
 */
public interface AsteroidsAgent {
    AsteroidsControls tick(AsteroidsState state);
}
