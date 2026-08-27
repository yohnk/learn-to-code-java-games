package arcade.pacman;

import arcade.engine.GridPos;
import java.util.ArrayDeque;
import java.util.List;

/**
 * A frozen snapshot of Pac-Man at one instant.
 *
 * <p>You cannot change the game by mutating this object. Read it, decide, and
 * return a {@link PacManAction}.
 */
public final class PacManState {
    private final int tick;
    private final int score;
    private final int lives;
    private final int level;
    private final int frightenedTicks;
    private final int width;
    private final int height;
    private final Tile[][] tiles;
    private final int pacX;
    private final int pacY;
    private final PacManAction pacDirection;
    private final List<GhostInfo> ghosts;
    private final List<GridPos> pellets;
    private final List<GridPos> powerPellets;

    PacManState(
            int tick,
            int score,
            int lives,
            int level,
            int frightenedTicks,
            Tile[][] tiles,
            int pacX,
            int pacY,
            PacManAction pacDirection,
            List<GhostInfo> ghosts,
            List<GridPos> pellets,
            List<GridPos> powerPellets
    ) {
        this.tick = tick;
        this.score = score;
        this.lives = lives;
        this.level = level;
        this.frightenedTicks = frightenedTicks;
        this.width = tiles[0].length;
        this.height = tiles.length;
        this.tiles = tiles;
        this.pacX = pacX;
        this.pacY = pacY;
        this.pacDirection = pacDirection;
        this.ghosts = ghosts;
        this.pellets = pellets;
        this.powerPellets = powerPellets;
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

    public int level() {
        return level;
    }

    /** How many frames of frightened mode remain (0 if none). */
    public int frightenedTicks() {
        return frightenedTicks;
    }

    public boolean frightened() {
        return frightenedTicks > 0;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int pacX() {
        return pacX;
    }

    public int pacY() {
        return pacY;
    }

    public PacManAction pacDirection() {
        return pacDirection;
    }

    public List<GhostInfo> ghosts() {
        return ghosts;
    }

    public List<GridPos> pellets() {
        return pellets;
    }

    public List<GridPos> powerPellets() {
        return powerPellets;
    }

    public Tile tileAt(int x, int y) {
        if (!inBounds(x, y)) {
            return Tile.WALL;
        }
        return tiles[y][x];
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public boolean isWall(int x, int y) {
        Tile tile = tileAt(x, y);
        return tile == Tile.WALL || tile == Tile.DOOR;
    }

    /**
     * True if Pac-Man could step into this tile (tunnels wrap horizontally).
     */
    public boolean isWalkable(int x, int y) {
        int wrappedX = wrapX(x);
        if (y < 0 || y >= height) {
            return false;
        }
        Tile tile = tiles[y][wrappedX];
        return tile != Tile.WALL && tile != Tile.DOOR;
    }

    public boolean canMove(PacManAction action) {
        if (!action.isMove()) {
            return false;
        }
        return isWalkable(pacX + action.dx, pacY + action.dy);
    }

    public GridPos nextTile(PacManAction action) {
        return new GridPos(wrapX(pacX + action.dx), pacY + action.dy);
    }

    /**
     * One step that reduces Manhattan distance to the target. Returns
     * {@link PacManAction#NONE} if Pac-Man is boxed in. This can get stuck
     * behind walls — try {@link #pathToward(int, int)} once greedy motion
     * is not enough.
     */
    public PacManAction toward(int targetX, int targetY) {
        return bestMove(targetX, targetY, false);
    }

    /**
     * One step that increases Manhattan distance from the target.
     */
    public PacManAction awayFrom(int targetX, int targetY) {
        return bestMove(targetX, targetY, true);
    }

    public int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public int distanceTo(int x, int y) {
        return manhattan(pacX, pacY, x, y);
    }

    public GridPos nearestPellet() {
        return nearest(pellets);
    }

    public GridPos nearestPowerPellet() {
        return nearest(powerPellets);
    }

    public GhostInfo nearestDangerousGhost() {
        GhostInfo best = null;
        int bestDist = Integer.MAX_VALUE;
        for (GhostInfo ghost : ghosts) {
            if (!ghost.dangerous()) {
                continue;
            }
            int dist = distanceTo(ghost.tileX(), ghost.tileY());
            if (dist < bestDist) {
                bestDist = dist;
                best = ghost;
            }
        }
        return best;
    }

    public GhostInfo nearestEdibleGhost() {
        GhostInfo best = null;
        int bestDist = Integer.MAX_VALUE;
        for (GhostInfo ghost : ghosts) {
            if (!ghost.frightened() || ghost.eaten()) {
                continue;
            }
            int dist = distanceTo(ghost.tileX(), ghost.tileY());
            if (dist < bestDist) {
                bestDist = dist;
                best = ghost;
            }
        }
        return best;
    }

    private GridPos nearest(List<GridPos> points) {
        GridPos best = null;
        int bestDist = Integer.MAX_VALUE;
        for (GridPos point : points) {
            int dist = distanceTo(point.x(), point.y());
            if (dist < bestDist) {
                bestDist = dist;
                best = point;
            }
        }
        return best;
    }

    private PacManAction bestMove(int targetX, int targetY, boolean maximize) {
        PacManAction best = PacManAction.NONE;
        int bestScore = maximize ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (PacManAction action : PacManAction.moves()) {
            if (!canMove(action)) {
                continue;
            }
            GridPos next = nextTile(action);
            int dist = manhattan(next.x(), next.y(), targetX, targetY);
            boolean better = maximize ? dist > bestScore : dist < bestScore;
            if (better) {
                bestScore = dist;
                best = action;
            }
        }
        return best;
    }

    /**
     * First step of a shortest walkable path to the target. Uses the tunnels.
     */
    public PacManAction pathToward(int targetX, int targetY) {
        int goalX = wrapX(targetX);
        int goalY = Math.max(0, Math.min(height - 1, targetY));
        if (pacX == goalX && pacY == goalY) {
            return PacManAction.NONE;
        }
        boolean[][] seen = new boolean[height][width];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        seen[pacY][pacX] = true;
        for (PacManAction action : PacManAction.moves()) {
            if (!canMove(action)) {
                continue;
            }
            GridPos next = nextTile(action);
            if (next.x() == goalX && next.y() == goalY) {
                return action;
            }
            seen[next.y()][next.x()] = true;
            queue.add(new int[] {next.x(), next.y(), action.ordinal()});
        }
        while (!queue.isEmpty()) {
            int[] cur = queue.removeFirst();
            int x = cur[0];
            int y = cur[1];
            int first = cur[2];
            for (PacManAction action : PacManAction.moves()) {
                int nx = wrapX(x + action.dx);
                int ny = y + action.dy;
                if (ny < 0 || ny >= height || seen[ny][nx] || !isWalkable(nx, ny)) {
                    continue;
                }
                if (nx == goalX && ny == goalY) {
                    return PacManAction.moves()[first];
                }
                seen[ny][nx] = true;
                queue.add(new int[] {nx, ny, first});
            }
        }
        return toward(goalX, goalY);
    }

    private int wrapX(int x) {
        if (x < 0) {
            return width - 1;
        }
        if (x >= width) {
            return 0;
        }
        return x;
    }
}
