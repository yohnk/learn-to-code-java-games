package arcade.missile;

/**
 * Fire a counter-missile at a point, or do nothing this tick.
 *
 * <p>The game rate-limits shots; extra fire commands are ignored.
 */
public final class MissileCommandAction {
    public static final MissileCommandAction NONE = new MissileCommandAction(false, 0, 0);

    private final boolean fire;
    private final double x;
    private final double y;

    private MissileCommandAction(boolean fire, double x, double y) {
        this.fire = fire;
        this.x = x;
        this.y = y;
    }

    public static MissileCommandAction fireAt(double x, double y) {
        return new MissileCommandAction(true, x, y);
    }

    public boolean fire() {
        return fire;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }
}
