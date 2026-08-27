package arcade.asteroids;

/**
 * Buttons held this frame. Create a new one each tick, set the flags you want,
 * and return it.
 */
public final class AsteroidsControls {
    public boolean rotateLeft;
    public boolean rotateRight;
    public boolean thrust;
    public boolean fire;

    public static AsteroidsControls none() {
        return new AsteroidsControls();
    }

    public static AsteroidsControls of(boolean rotateLeft, boolean rotateRight, boolean thrust, boolean fire) {
        AsteroidsControls controls = new AsteroidsControls();
        controls.rotateLeft = rotateLeft;
        controls.rotateRight = rotateRight;
        controls.thrust = thrust;
        controls.fire = fire;
        return controls;
    }
}
