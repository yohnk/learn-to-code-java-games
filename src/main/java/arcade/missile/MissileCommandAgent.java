package arcade.missile;

/**
 * Student-facing Missile Command controller.
 *
 * <p>The game constructs your class once per round, then calls {@link #tick}
 * every frame. Put long-lived objects in fields on your class.
 */
public interface MissileCommandAgent {
    MissileCommandAction tick(MissileCommandState state);
}
