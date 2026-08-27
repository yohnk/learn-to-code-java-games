package arcade.pacman;

/**
 * Direction Pac-Man should try to go this tick.
 *
 * <p>{@link #NONE} means "keep doing what you were doing."
 */
public enum PacManAction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0),
    NONE(0, 0);

    public final int dx;
    public final int dy;

    PacManAction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public boolean isMove() {
        return this != NONE;
    }

    public PacManAction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case NONE -> NONE;
        };
    }

    public static PacManAction[] moves() {
        return new PacManAction[] {UP, DOWN, LEFT, RIGHT};
    }
}
