package arcade.asteroids;

import java.util.List;

/**
 * A frozen snapshot of Asteroids at one instant.
 */
public final class AsteroidsState {
    private final int tick;
    private final int score;
    private final int lives;
    private final int width;
    private final int height;
    private final ShipInfo ship;
    private final List<SpaceBody> asteroids;
    private final List<SpaceBody> bullets;

    AsteroidsState(
            int tick,
            int score,
            int lives,
            int width,
            int height,
            ShipInfo ship,
            List<SpaceBody> asteroids,
            List<SpaceBody> bullets
    ) {
        this.tick = tick;
        this.score = score;
        this.lives = lives;
        this.width = width;
        this.height = height;
        this.ship = ship;
        this.asteroids = asteroids;
        this.bullets = bullets;
    }

    public int tick() {
        return tick;
    }

    public int score() {
        return score;
    }

    public int lives() {
        return lives;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public ShipInfo ship() {
        return ship;
    }

    public List<SpaceBody> asteroids() {
        return asteroids;
    }

    public List<SpaceBody> bullets() {
        return bullets;
    }

    /**
     * Shortest wrapped distance between two points.
     */
    public double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(dx(x1, x2), dy(y1, y2));
    }

    /**
     * World angle from the ship to a point, in radians.
     */
    public double angleTo(double x, double y) {
        return Math.atan2(dy(ship.y(), y), dx(ship.x(), x));
    }

    /**
     * How far the ship must rotate (radians) to face a point.
     * Positive means rotate left, negative means rotate right.
     */
    public double facingError(double x, double y) {
        return wrapAngle(angleTo(x, y) - ship.angle());
    }

    public SpaceBody nearestAsteroid() {
        SpaceBody best = null;
        double bestDist = Double.MAX_VALUE;
        for (SpaceBody rock : asteroids) {
            double dist = distance(ship.x(), ship.y(), rock.x(), rock.y());
            if (dist < bestDist) {
                bestDist = dist;
                best = rock;
            }
        }
        return best;
    }

    public double dx(double fromX, double toX) {
        double dx = toX - fromX;
        if (dx > width / 2.0) {
            dx -= width;
        }
        if (dx < -width / 2.0) {
            dx += width;
        }
        return dx;
    }

    public double dy(double fromY, double toY) {
        double dy = toY - fromY;
        if (dy > height / 2.0) {
            dy -= height;
        }
        if (dy < -height / 2.0) {
            dy += height;
        }
        return dy;
    }

    public static double wrapAngle(double angle) {
        while (angle > Math.PI) {
            angle -= Math.PI * 2;
        }
        while (angle < -Math.PI) {
            angle += Math.PI * 2;
        }
        return angle;
    }
}
