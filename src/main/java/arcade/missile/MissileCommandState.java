package arcade.missile;

import java.util.List;

/**
 * A frozen snapshot of Missile Command at one instant.
 */
public final class MissileCommandState {
    private final int tick;
    private final int score;
    private final int wave;
    private final int width;
    private final int height;
    private final List<City> cities;
    private final List<Base> bases;
    private final List<IncomingMissile> incoming;
    private final List<Explosion> explosions;

    MissileCommandState(
            int tick,
            int score,
            int wave,
            int width,
            int height,
            List<City> cities,
            List<Base> bases,
            List<IncomingMissile> incoming,
            List<Explosion> explosions
    ) {
        this.tick = tick;
        this.score = score;
        this.wave = wave;
        this.width = width;
        this.height = height;
        this.cities = cities;
        this.bases = bases;
        this.incoming = incoming;
        this.explosions = explosions;
    }

    public int tick() {
        return tick;
    }

    public int score() {
        return score;
    }

    public int wave() {
        return wave;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public List<City> cities() {
        return cities;
    }

    public List<Base> bases() {
        return bases;
    }

    public List<IncomingMissile> incoming() {
        return incoming;
    }

    public List<Explosion> explosions() {
        return explosions;
    }

    public long citiesAlive() {
        return cities.stream().filter(City::alive).count();
    }

    /**
     * Incoming missile that will hit soonest, or {@code null} if the sky is clear.
     */
    public IncomingMissile mostUrgent() {
        IncomingMissile best = null;
        double bestEta = Double.MAX_VALUE;
        for (IncomingMissile missile : incoming) {
            double eta = eta(missile);
            if (eta < bestEta) {
                bestEta = eta;
                best = missile;
            }
        }
        return best;
    }

    /**
     * Seconds until this missile reaches its target, at current speed.
     */
    public double eta(IncomingMissile missile) {
        double dist = Math.hypot(missile.targetX() - missile.x(), missile.targetY() - missile.y());
        double speed = Math.max(1e-3, missile.speed());
        return dist / speed;
    }

    /**
     * Predicted position of a missile after {@code seconds}.
     */
    public double[] predict(IncomingMissile missile, double seconds) {
        return new double[] {
                missile.x() + missile.vx() * seconds,
                missile.y() + missile.vy() * seconds
        };
    }

    /**
     * Fire at a lead point so the explosion meets this missile.
     */
    public MissileCommandAction intercept(IncomingMissile missile) {
        Base base = closestArmedBase(missile.x(), missile.y());
        if (base == null) {
            return MissileCommandAction.NONE;
        }
        double interceptor = 280;
        double dist = Math.hypot(missile.x() - base.x(), missile.y() - base.y());
        double seconds = dist / interceptor;
        double[] point = predict(missile, seconds);
        return MissileCommandAction.fireAt(point[0], point[1]);
    }

    public Base closestArmedBase(double x, double y) {
        Base best = null;
        double bestDist = Double.MAX_VALUE;
        for (Base base : bases) {
            if (!base.hasAmmo()) {
                continue;
            }
            double dist = Math.hypot(base.x() - x, base.y() - y);
            if (dist < bestDist) {
                bestDist = dist;
                best = base;
            }
        }
        return best;
    }

    /**
     * An explosion currently blooming in the sky.
     */
    public record Explosion(double x, double y, double radius) {
    }
}
