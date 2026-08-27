package arcade.asteroids;

/**
 * A rock or bullet in the playfield.
 *
 * @param size 3 = large asteroid, 2 = medium, 1 = small, 0 = bullet
 */
public record SpaceBody(
        double x,
        double y,
        double vx,
        double vy,
        double radius,
        int size
) {
    public double speed() {
        return Math.hypot(vx, vy);
    }
}
