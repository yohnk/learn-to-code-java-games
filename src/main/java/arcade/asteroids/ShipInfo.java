package arcade.asteroids;

/**
 * The player's ship as the agent sees it.
 *
 * @param angle radians, 0 faces right, increases counter-clockwise
 */
public record ShipInfo(
        double x,
        double y,
        double angle,
        double vx,
        double vy,
        boolean alive,
        boolean invulnerable
) {
}
