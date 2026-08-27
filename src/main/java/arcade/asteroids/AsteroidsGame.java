package arcade.asteroids;

import arcade.engine.Draw;
import arcade.engine.GameScreen;
import arcade.engine.Input;
import arcade.engine.InputAware;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Vector-style Asteroids: wraparound space, spin, thrust, fire.
 */
public final class AsteroidsGame implements GameScreen {
    static final int WORLD_W = 960;
    static final int WORLD_H = 720;

    private final AsteroidsAgent agent;
    private final boolean headless;
    private final Random random = new Random(42);
    private final List<Rock> rocks = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private Ship ship;
    private int tick;
    private int score;
    private int lives = 3;
    private int wave = 1;
    private int fireCooldown;
    private int respawnTicks;
    private boolean gameOver;
    private boolean done;
    private boolean thrusting;

    public AsteroidsGame(AsteroidsAgent agent) {
        this(agent, false);
    }

    public AsteroidsGame(AsteroidsAgent agent, boolean headless) {
        this.agent = agent;
        this.headless = headless;
        spawnShip();
        spawnWave();
    }

    @Override
    public String title() {
        return "ASTEROIDS";
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
        tick++;
        AsteroidsControls controls = readControls(input);
        if (ship.alive) {
            if (controls.rotateLeft) {
                ship.angle += 3.6 * dt;
            }
            if (controls.rotateRight) {
                ship.angle -= 3.6 * dt;
            }
            thrusting = controls.thrust;
            if (controls.thrust) {
                ship.vx += Math.cos(ship.angle) * 210 * dt;
                ship.vy += Math.sin(ship.angle) * 210 * dt;
                double speed = Math.hypot(ship.vx, ship.vy);
                if (speed > 340) {
                    ship.vx *= 340 / speed;
                    ship.vy *= 340 / speed;
                }
            }
            if (controls.fire) {
                tryFire();
            }
        }
        if (fireCooldown > 0) {
            fireCooldown--;
        }
        if (respawnTicks > 0) {
            respawnTicks--;
            if (respawnTicks == 0 && !ship.alive && lives > 0) {
                spawnShip();
            }
        }

        if (ship.alive) {
            ship.x = wrapX(ship.x + ship.vx * dt);
            ship.y = wrapY(ship.y + ship.vy * dt);
        }
        if (ship.invulnerable > 0) {
            ship.invulnerable--;
        }

        for (Rock rock : rocks) {
            rock.x = wrapX(rock.x + rock.vx * dt);
            rock.y = wrapY(rock.y + rock.vy * dt);
            rock.spin += rock.spinSpeed * dt;
        }
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            bullet.x = wrapX(bullet.x + bullet.vx * dt);
            bullet.y = wrapY(bullet.y + bullet.vy * dt);
            bullet.life--;
            if (bullet.life <= 0) {
                bulletIt.remove();
            }
        }

        collideBullets();
        collideShip();

        if (rocks.isEmpty()) {
            wave++;
            spawnWave();
        }
    }

    @Override
    public void render(Graphics2D g, int width, int height) {
        Draw.quality(g);
        Draw.fillScreen(g, width, height);
        AffineTransform original = g.getTransform();
        double sx = width / (double) WORLD_W;
        double sy = height / (double) WORLD_H;
        // World Y increases upward so facingError math matches what students expect.
        g.translate(0, height);
        g.scale(sx, -sy);
        g.setStroke(new BasicStroke(1.6f));
        g.setColor(new Color(40, 40, 70));
        for (int i = 0; i < 40; i++) {
            int x = (i * 97 + tick / 2) % WORLD_W;
            int y = (i * 53) % WORLD_H;
            g.fillRect(x, y, 2, 2);
        }
        g.setColor(Color.WHITE);
        for (Rock rock : rocks) {
            drawRock(g, rock);
        }
        g.setColor(new Color(255, 220, 80));
        for (Bullet bullet : bullets) {
            g.fillOval((int) bullet.x - 2, (int) bullet.y - 2, 4, 4);
        }
        if (ship.alive && (ship.invulnerable == 0 || (tick / 4) % 2 == 0)) {
            drawShip(g);
        }
        g.setTransform(original);
        Draw.text(g, "SCORE  " + score, 28, 36, Draw.HUD, 22, true);
        Draw.text(g, "LIVES  " + Math.max(lives, 0), width - 180, 36, Draw.HUD, 22, true);
        Draw.text(g, agentLabel(), width - 180, 58, Draw.ACCENT, 14, true);
        if (gameOver) {
            Draw.outlined(g, "GAME OVER", width / 2, height / 2, Color.WHITE, Color.BLACK, 48);
        }
    }

    private AsteroidsControls readControls(Input input) {
        if (agent == null || !ship.alive) {
            return AsteroidsControls.none();
        }
        InputAware.feed(agent, input);
        AsteroidsControls controls = agent.tick(snapshot());
        return controls == null ? AsteroidsControls.none() : controls;
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

    AsteroidsState snapshot() {
        List<SpaceBody> rockViews = new ArrayList<>();
        for (Rock rock : rocks) {
            rockViews.add(new SpaceBody(rock.x, rock.y, rock.vx, rock.vy, rock.radius, rock.size));
        }
        List<SpaceBody> bulletViews = new ArrayList<>();
        for (Bullet bullet : bullets) {
            bulletViews.add(new SpaceBody(bullet.x, bullet.y, bullet.vx, bullet.vy, 2, 0));
        }
        return new AsteroidsState(
                tick,
                score,
                lives,
                WORLD_W,
                WORLD_H,
                new ShipInfo(ship.x, ship.y, ship.angle, ship.vx, ship.vy, ship.alive, ship.invulnerable > 0),
                List.copyOf(rockViews),
                List.copyOf(bulletViews)
        );
    }

    private void tryFire() {
        if (fireCooldown > 0 || bullets.size() >= 5 || !ship.alive) {
            return;
        }
        fireCooldown = 12;
        double nose = 16;
        Bullet bullet = new Bullet();
        bullet.x = wrapX(ship.x + Math.cos(ship.angle) * nose);
        bullet.y = wrapY(ship.y + Math.sin(ship.angle) * nose);
        bullet.vx = Math.cos(ship.angle) * 520 + ship.vx * 0.3;
        bullet.vy = Math.sin(ship.angle) * 520 + ship.vy * 0.3;
        bullet.life = 55;
        bullets.add(bullet);
    }

    private void collideBullets() {
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            Rock hit = firstHit(bullet.x, bullet.y, 3);
            if (hit != null) {
                bulletIt.remove();
                smash(hit);
            }
        }
    }

    private void collideShip() {
        if (!ship.alive || ship.invulnerable > 0) {
            return;
        }
        Rock hit = firstHit(ship.x, ship.y, 12);
        if (hit != null) {
            smash(hit);
            ship.alive = false;
            lives--;
            thrusting = false;
            if (lives <= 0) {
                gameOver = true;
                if (headless) {
                    done = true;
                }
            } else {
                respawnTicks = headless ? 1 : 90;
            }
        }
    }

    private Rock firstHit(double x, double y, double radius) {
        for (Rock rock : rocks) {
            if (distance(x, y, rock.x, rock.y) < radius + rock.radius) {
                return rock;
            }
        }
        return null;
    }

    private void smash(Rock rock) {
        rocks.remove(rock);
        score += switch (rock.size) {
            case 3 -> 20;
            case 2 -> 50;
            default -> 100;
        };
        if (rock.size > 1) {
            rocks.add(child(rock, 0.6));
            rocks.add(child(rock, -0.6));
        }
    }

    private Rock child(Rock parent, double kick) {
        Rock rock = new Rock();
        rock.size = parent.size - 1;
        rock.radius = parent.radius * 0.62;
        rock.x = parent.x;
        rock.y = parent.y;
        rock.vx = parent.vy * kick + rand(-40, 40);
        rock.vy = -parent.vx * kick + rand(-40, 40);
        rock.spinSpeed = rand(-2, 2);
        rock.vertices = jagged(rock.radius);
        return rock;
    }

    private void spawnShip() {
        ship = new Ship();
        ship.x = WORLD_W / 2.0;
        ship.y = WORLD_H / 2.0;
        ship.alive = true;
        ship.invulnerable = 120;
    }

    private void spawnWave() {
        int count = 3 + wave;
        for (int i = 0; i < count; i++) {
            Rock rock = new Rock();
            rock.size = 3;
            rock.radius = 42;
            double angle = random.nextDouble() * Math.PI * 2;
            rock.x = wrapX(WORLD_W / 2.0 + Math.cos(angle) * 280);
            rock.y = wrapY(WORLD_H / 2.0 + Math.sin(angle) * 220);
            rock.vx = rand(-70, 70);
            rock.vy = rand(-70, 70);
            rock.spinSpeed = rand(-1.2, 1.2);
            rock.vertices = jagged(rock.radius);
            rocks.add(rock);
        }
    }

    private double[] jagged(double radius) {
        int n = 9;
        double[] pts = new double[n];
        for (int i = 0; i < n; i++) {
            pts[i] = radius * rand(0.7, 1.15);
        }
        return pts;
    }

    private void drawRock(Graphics2D g, Rock rock) {
        Path2D path = new Path2D.Double();
        int n = rock.vertices.length;
        for (int i = 0; i < n; i++) {
            double a = rock.spin + (Math.PI * 2 * i) / n;
            double px = rock.x + Math.cos(a) * rock.vertices[i];
            double py = rock.y + Math.sin(a) * rock.vertices[i];
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.closePath();
        g.draw(path);
    }

    private void drawShip(Graphics2D g) {
        double c = Math.cos(ship.angle);
        double s = Math.sin(ship.angle);
        Path2D path = new Path2D.Double();
        path.moveTo(ship.x + c * 16, ship.y + s * 16);
        path.lineTo(ship.x - c * 12 + s * 10, ship.y - s * 12 - c * 10);
        path.lineTo(ship.x - c * 6, ship.y - s * 6);
        path.lineTo(ship.x - c * 12 - s * 10, ship.y - s * 12 + c * 10);
        path.closePath();
        g.setColor(Color.WHITE);
        g.draw(path);
        if (thrusting && (tick / 2) % 2 == 0) {
            g.setColor(new Color(255, 140, 60));
            g.drawLine(
                    (int) (ship.x - c * 8),
                    (int) (ship.y - s * 8),
                    (int) (ship.x - c * 18),
                    (int) (ship.y - s * 18)
            );
        }
    }

    private double distance(double x1, double y1, double x2, double y2) {
        double dx = Math.abs(x1 - x2);
        double dy = Math.abs(y1 - y2);
        if (dx > WORLD_W / 2.0) {
            dx = WORLD_W - dx;
        }
        if (dy > WORLD_H / 2.0) {
            dy = WORLD_H - dy;
        }
        return Math.hypot(dx, dy);
    }

    private double wrapX(double x) {
        double w = WORLD_W;
        return (x % w + w) % w;
    }

    private double wrapY(double y) {
        double h = WORLD_H;
        return (y % h + h) % h;
    }

    private double rand(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private static final class Ship {
        double x;
        double y;
        double vx;
        double vy;
        double angle = Math.PI / 2;
        boolean alive;
        int invulnerable;
    }

    private static final class Rock {
        double x;
        double y;
        double vx;
        double vy;
        double radius;
        int size;
        double spin;
        double spinSpeed;
        double[] vertices;
    }

    private static final class Bullet {
        double x;
        double y;
        double vx;
        double vy;
        int life;
    }
}
