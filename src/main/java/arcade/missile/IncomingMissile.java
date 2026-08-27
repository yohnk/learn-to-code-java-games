package arcade.missile;

/**
 * An inbound enemy missile.
 */
public record IncomingMissile(
        double x,
        double y,
        double vx,
        double vy,
        double targetX,
        double targetY
) {
    public double speed() {
        return Math.hypot(vx, vy);
    }
}
