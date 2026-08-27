package arcade.pacman;

import arcade.engine.Draw;
import arcade.engine.GameScreen;
import arcade.engine.GridPos;
import arcade.engine.Input;
import arcade.engine.InputAware;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Pac-Man game loop, ghost AI, and rendering.
 */
public final class PacManGame implements GameScreen {
    private static final String[] GHOST_NAMES = {"blinky", "pinky", "inky", "clyde"};
    private static final Color[] GHOST_COLORS = {
            new Color(255, 0, 0),
            new Color(255, 184, 255),
            new Color(0, 255, 255),
            new Color(255, 184, 81)
    };
    private static final Color MAZE_BLUE = new Color(33, 33, 255);
    private static final Color PELLET = new Color(255, 183, 174);
    private static final int FRIGHTENED_TICKS = 8 * 60;

    private final PacManAgent agent;
    private final boolean headless;
    private Maze maze;
    private Actor pac;
    private final Actor[] ghosts = new Actor[4];
    private int tick;
    private int score;
    private int lives = 3;
    private int level = 1;
    private int frightenedTicks;
    private int ghostCombo = 1;
    private int readyTicks;
    private int deathTicks;
    private boolean gameOver;
    private boolean done;
    private PacManAction lastDir = PacManAction.LEFT;

    public PacManGame(PacManAgent agent) {
        this(agent, false);
    }

    public PacManGame(PacManAgent agent, boolean headless) {
        this.agent = agent;
        this.headless = headless;
        startLevel(true);
    }

    @Override
    public String title() {
        return "PAC-MAN";
    }

    @Override
    public int score() {
        return score;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public void update(double dt, Input input) {
        if (input.back()) {
            done = true;
            return;
        }
        if (gameOver) {
            if (headless || input.confirm()) {
                done = true;
            }
            return;
        }
        if (readyTicks > 0) {
            readyTicks--;
            return;
        }
        if (deathTicks > 0) {
            deathTicks--;
            if (deathTicks == 0) {
                if (lives <= 0) {
                    gameOver = true;
                    if (headless) {
                        done = true;
                    }
                } else {
                    resetActors();
                    readyTicks = headless ? 0 : 90;
                }
            }
            return;
        }

        tick++;
        if (frightenedTicks > 0) {
            frightenedTicks--;
            if (frightenedTicks == 0) {
                ghostCombo = 1;
            }
        }

        PacManAction action = readAction(input);
        if (action.isMove()) {
            pac.desired = action;
            lastDir = action;
        }
        moveActor(pac, true);
        eat();

        for (int i = 0; i < ghosts.length; i++) {
            steerGhost(i);
            moveActor(ghosts[i], false);
        }
        resolveCollisions();

        if (maze.pelletCount() == 0) {
            level++;
            startLevel(false);
        }
    }

    @Override
    public void render(Graphics2D g, int width, int height) {
        Draw.quality(g);
        Draw.fillScreen(g, width, height);

        int hud = 64;
        double tile = Math.min(width / (double) maze.width(), (height - hud) / (double) maze.height());
        double ox = (width - tile * maze.width()) / 2.0;
        double oy = hud + (height - hud - tile * maze.height()) / 2.0;

        drawMaze(g, ox, oy, tile);
        if (deathTicks == 0) {
            for (int i = 0; i < ghosts.length; i++) {
                drawGhost(g, ghosts[i], GHOST_COLORS[i], ox, oy, tile);
            }
        }
        if (deathTicks == 0 || (deathTicks / 8) % 2 == 0) {
            drawPac(g, ox, oy, tile);
        }

        Draw.text(g, "1UP  " + score, 32, 36, Draw.HUD, 22, true);
        Draw.text(g, "LEVEL " + level, width / 2 - 50, 36, Draw.MUTED, 20, true);
        Draw.text(g, "LIVES " + Math.max(lives, 0), width - 170, 36, Draw.HUD, 22, true);
        String who = agentLabel();
        Draw.text(g, who, width - 170, 58, Draw.ACCENT, 14, true);

        if (readyTicks > 0 && !gameOver) {
            Draw.outlined(g, "READY!", width / 2, height / 2, Color.YELLOW, Color.BLACK, 42);
        }
        if (gameOver) {
            Draw.outlined(g, "GAME OVER", width / 2, height / 2 - 10, Color.RED, Color.BLACK, 48);
            Draw.centered(g, headless ? "" : "Enter or Esc to return", width / 2, height / 2 + 36, Draw.MUTED, 18, false);
        }
    }

    public PacManState snapshot() {
        List<GhostInfo> ghostInfos = new ArrayList<>();
        for (int i = 0; i < ghosts.length; i++) {
            Actor ghost = ghosts[i];
            ghostInfos.add(new GhostInfo(
                    GHOST_NAMES[i],
                    ghost.tileX(),
                    ghost.tileY(),
                    frightenedTicks > 0 && !ghost.eaten,
                    ghost.eaten
            ));
        }
        return new PacManState(
                tick,
                score,
                lives,
                level,
                frightenedTicks,
                maze.copyTiles(),
                pac.tileX(),
                pac.tileY(),
                lastDir,
                List.copyOf(ghostInfos),
                maze.pellets(),
                maze.powerPellets()
        );
    }

    private PacManAction readAction(Input input) {
        if (agent == null) {
            return PacManAction.NONE;
        }
        InputAware.feed(agent, input);
        PacManAction action = agent.tick(snapshot());
        return action == null ? PacManAction.NONE : action;
    }

    private String agentLabel() {
        if (agent == null) {
            return "NONE";
        }
        return switch (agent.getClass().getSimpleName()) {
            case "KeyboardAgent" -> "KEYBOARD";
            case "MyAgent" -> "MY AGENT";
            default -> "AGENT";
        };
    }

    private void startLevel(boolean fullReset) {
        maze = new Maze();
        if (fullReset) {
            lives = 3;
            score = 0;
            level = Math.max(level, 1);
            tick = 0;
            gameOver = false;
        }
        resetActors();
        frightenedTicks = 0;
        ghostCombo = 1;
        readyTicks = headless ? 0 : 120;
        deathTicks = 0;
    }

    private void resetActors() {
        pac = Actor.spawn(maze.pacSpawn(), PacManAction.LEFT, pacSpeed());
        lastDir = PacManAction.LEFT;
        for (int i = 0; i < ghosts.length; i++) {
            ghosts[i] = Actor.spawn(maze.ghostSpawn(i), PacManAction.UP, ghostSpeed(false));
            ghosts[i].eaten = false;
        }
    }

    private double pacSpeed() {
        return 6.2 + Math.min(level - 1, 6) * 0.25;
    }

    private double ghostSpeed(boolean frightened) {
        double base = 4.7 + Math.min(level - 1, 6) * 0.22;
        if (frightened) {
            return base * 0.55;
        }
        return base;
    }

    private void eat() {
        int x = pac.tileX();
        int y = pac.tileY();
        Tile tile = maze.tile(x, y);
        if (tile == Tile.PELLET) {
            maze.setTile(x, y, Tile.EMPTY);
            score += 10;
        } else if (tile == Tile.POWER) {
            maze.setTile(x, y, Tile.EMPTY);
            score += 50;
            frightenedTicks = Math.max(180, FRIGHTENED_TICKS - (level - 1) * 40);
            ghostCombo = 1;
            for (Actor ghost : ghosts) {
                if (!ghost.eaten) {
                    ghost.desired = ghost.dir.opposite();
                }
            }
        }
    }

    private void resolveCollisions() {
        for (Actor ghost : ghosts) {
            if (ghost.tileX() != pac.tileX() || ghost.tileY() != pac.tileY()) {
                continue;
            }
            if (ghost.eaten) {
                continue;
            }
            if (frightenedTicks > 0) {
                ghost.eaten = true;
                score += 200 * ghostCombo;
                ghostCombo *= 2;
            } else {
                lives--;
                deathTicks = headless ? 1 : 90;
                return;
            }
        }
    }

    private void steerGhost(int index) {
        Actor ghost = ghosts[index];
        ghost.speed = ghostSpeed(frightenedTicks > 0 && !ghost.eaten);
        GridPos target;
        if (ghost.eaten) {
            target = maze.ghostSpawn(index);
            if (ghost.tileX() == target.x() && ghost.tileY() == target.y()) {
                ghost.eaten = false;
            }
        } else if (maze.inHouse(ghost.tileX(), ghost.tileY())) {
            if (tick < 90 * index) {
                ghost.dir = PacManAction.NONE;
                ghost.desired = PacManAction.NONE;
                return;
            }
            target = new GridPos(maze.door().x(), maze.door().y() - 1);
        } else if (frightenedTicks > 0) {
            ghost.desired = randomOpen(ghost);
            return;
        } else {
            target = chaseTarget(index);
        }
        ghost.desired = greedy(ghost, target);
    }

    private GridPos chaseTarget(int index) {
        int px = pac.tileX();
        int py = pac.tileY();
        return switch (index) {
            case 0 -> new GridPos(px, py);
            case 1 -> new GridPos(px + lastDir.dx * 4, py + lastDir.dy * 4);
            case 2 -> {
                Actor blinky = ghosts[0];
                int tx = px + lastDir.dx * 2;
                int ty = py + lastDir.dy * 2;
                yield new GridPos(tx * 2 - blinky.tileX(), ty * 2 - blinky.tileY());
            }
            default -> {
                int dist = Math.abs(px - ghosts[3].tileX()) + Math.abs(py - ghosts[3].tileY());
                yield dist > 8 ? new GridPos(px, py) : new GridPos(1, maze.height() - 2);
            }
        };
    }

    private PacManAction greedy(Actor actor, GridPos target) {
        PacManAction best = actor.dir;
        int bestDist = Integer.MAX_VALUE;
        for (PacManAction action : PacManAction.moves()) {
            if (action == actor.dir.opposite() && hasSideOption(actor)) {
                continue;
            }
            int nx = maze.wrapX(actor.tileX() + action.dx);
            int ny = actor.tileY() + action.dy;
            if (!maze.walkableForGhost(nx, ny)) {
                continue;
            }
            int dist = Math.abs(nx - target.x()) + Math.abs(ny - target.y());
            if (dist < bestDist) {
                bestDist = dist;
                best = action;
            }
        }
        return best;
    }

    private boolean hasSideOption(Actor actor) {
        int options = 0;
        for (PacManAction action : PacManAction.moves()) {
            if (maze.walkableForGhost(actor.tileX() + action.dx, actor.tileY() + action.dy)) {
                options++;
            }
        }
        return options > 1;
    }

    private PacManAction randomOpen(Actor actor) {
        List<PacManAction> options = new ArrayList<>();
        for (PacManAction action : PacManAction.moves()) {
            if (action == actor.dir.opposite()) {
                continue;
            }
            if (maze.walkableForGhost(actor.tileX() + action.dx, actor.tileY() + action.dy)) {
                options.add(action);
            }
        }
        if (options.isEmpty()) {
            return actor.dir.opposite();
        }
        return options.get((tick + actor.tileX() * 13 + actor.tileY()) % options.size());
    }

    private void moveActor(Actor actor, boolean pacMan) {
        if (actor.dir == PacManAction.NONE && actor.desired == PacManAction.NONE) {
            return;
        }
        double step = actor.speed / 60.0;
        boolean atCenter = Math.abs(actor.x - (actor.tileX() + 0.5)) <= step + 1e-6
                && Math.abs(actor.y - (actor.tileY() + 0.5)) <= step + 1e-6;
        if (atCenter && actor.desired.isMove() && actor.desired != actor.dir) {
            int nx = actor.tileX() + actor.desired.dx;
            int ny = actor.tileY() + actor.desired.dy;
            boolean open = pacMan ? maze.walkableForPac(nx, ny) : maze.walkableForGhost(nx, ny);
            if (open) {
                actor.dir = actor.desired;
                actor.snap();
            }
        }
        if (actor.dir == PacManAction.NONE) {
            return;
        }
        int nx = actor.tileX() + actor.dir.dx;
        int ny = actor.tileY() + actor.dir.dy;
        boolean open = pacMan ? maze.walkableForPac(nx, ny) : maze.walkableForGhost(nx, ny);
        if (atCenter && !open) {
            actor.snap();
            return;
        }
        actor.x += actor.dir.dx * step;
        actor.y += actor.dir.dy * step;
        if (actor.dir.dx != 0) {
            actor.y = actor.tileY() + 0.5;
        }
        if (actor.dir.dy != 0) {
            actor.x = actor.tileX() + 0.5;
        }
        if (actor.x < 0) {
            actor.x += maze.width();
        }
        if (actor.x >= maze.width()) {
            actor.x -= maze.width();
        }
    }

    private void drawMaze(Graphics2D g, double ox, double oy, double tile) {
        g.setStroke(new BasicStroke(Math.max(2f, (float) (tile * 0.12f))));
        for (int y = 0; y < maze.height(); y++) {
            for (int x = 0; x < maze.width(); x++) {
                double px = ox + x * tile;
                double py = oy + y * tile;
                Tile cell = maze.tile(x, y);
                if (cell == Tile.WALL) {
                    g.setColor(MAZE_BLUE);
                    g.drawRoundRect((int) px + 2, (int) py + 2, (int) tile - 4, (int) tile - 4, 8, 8);
                } else if (cell == Tile.PELLET) {
                    g.setColor(PELLET);
                    int s = Math.max(3, (int) (tile * 0.16));
                    g.fillOval((int) (px + tile / 2 - s / 2.0), (int) (py + tile / 2 - s / 2.0), s, s);
                } else if (cell == Tile.POWER) {
                    g.setColor(PELLET);
                    int s = Math.max(8, (int) (tile * 0.42));
                    if ((tick / 8) % 2 == 0) {
                        g.fillOval((int) (px + tile / 2 - s / 2.0), (int) (py + tile / 2 - s / 2.0), s, s);
                    }
                } else if (cell == Tile.DOOR) {
                    g.setColor(new Color(255, 184, 255));
                    g.fillRect((int) px, (int) (py + tile * 0.4), (int) tile, Math.max(3, (int) (tile * 0.12)));
                }
            }
        }
    }

    private void drawPac(Graphics2D g, double ox, double oy, double tile) {
        double cx = ox + pac.x * tile;
        double cy = oy + pac.y * tile;
        double r = tile * 0.45;
        int start = switch (pac.dir) {
            case RIGHT -> 30;
            case UP -> 120;
            case LEFT -> 210;
            case DOWN -> 300;
            case NONE -> 30;
        };
        if ((tick / 5) % 2 == 0) {
            start -= 25;
        }
        g.setColor(Color.YELLOW);
        g.fill(new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, start, 300, Arc2D.PIE));
    }

    private void drawGhost(Graphics2D g, Actor ghost, Color color, double ox, double oy, double tile) {
        double cx = ox + ghost.x * tile;
        double cy = oy + ghost.y * tile;
        double r = tile * 0.42;
        if (ghost.eaten) {
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(cx - r * 0.35, cy - r * 0.25, r * 0.28, r * 0.32));
            g.fill(new Ellipse2D.Double(cx + r * 0.05, cy - r * 0.25, r * 0.28, r * 0.32));
            g.setColor(new Color(50, 80, 255));
            g.fill(new Ellipse2D.Double(cx - r * 0.25, cy - r * 0.12, r * 0.14, r * 0.14));
            g.fill(new Ellipse2D.Double(cx + r * 0.15, cy - r * 0.12, r * 0.14, r * 0.14));
            return;
        }
        boolean scared = frightenedTicks > 0;
        boolean flash = scared && frightenedTicks < 120 && (tick / 8) % 2 == 0;
        g.setColor(scared ? (flash ? Color.WHITE : new Color(33, 33, 255)) : color);
        Path2D body = new Path2D.Double();
        body.moveTo(cx - r, cy);
        body.quadTo(cx - r, cy - r, cx, cy - r);
        body.quadTo(cx + r, cy - r, cx + r, cy);
        body.lineTo(cx + r, cy + r * 0.7);
        int waves = 4;
        for (int i = waves; i >= 0; i--) {
            double wx = cx - r + (2 * r * i) / (double) waves;
            double wy = cy + r * (i % 2 == 0 ? 0.85 : 0.55);
            body.lineTo(wx, wy);
        }
        body.closePath();
        g.fill(body);
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(cx - r * 0.42, cy - r * 0.28, r * 0.32, r * 0.36));
        g.fill(new Ellipse2D.Double(cx + r * 0.08, cy - r * 0.28, r * 0.32, r * 0.36));
        g.setColor(scared ? Color.PINK : new Color(50, 80, 255));
        double px = ghost.dir.dx * r * 0.08;
        double py = ghost.dir.dy * r * 0.08;
        g.fill(new Ellipse2D.Double(cx - r * 0.32 + px, cy - r * 0.16 + py, r * 0.16, r * 0.16));
        g.fill(new Ellipse2D.Double(cx + r * 0.18 + px, cy - r * 0.16 + py, r * 0.16, r * 0.16));
    }

    private static final class Actor {
        double x;
        double y;
        PacManAction dir;
        PacManAction desired;
        double speed;
        boolean eaten;

        static Actor spawn(GridPos tile, PacManAction dir, double speed) {
            Actor actor = new Actor();
            actor.x = tile.x() + 0.5;
            actor.y = tile.y() + 0.5;
            actor.dir = dir;
            actor.desired = dir;
            actor.speed = speed;
            return actor;
        }

        int tileX() {
            return (int) Math.floor(x);
        }

        int tileY() {
            return (int) Math.floor(y);
        }

        void snap() {
            x = tileX() + 0.5;
            y = tileY() + 0.5;
        }
    }
}
