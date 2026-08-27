package arcade.pacman;

/**
 * Student-facing Pac-Man controller.
 *
 * <p>The game constructs your class once per round, then calls {@link #tick}
 * every frame. Put long-lived objects in fields on your class (or use
 * {@link arcade.api.Memory}).
 */
public interface PacManAgent {
    PacManAction tick(PacManState state);
}
