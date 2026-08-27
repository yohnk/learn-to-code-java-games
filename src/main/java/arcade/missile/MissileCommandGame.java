package arcade.missile;

import arcade.agents.missile.KeyboardAgent;
import arcade.engine.Draw;
import arcade.engine.GameScreen;
import arcade.engine.Input;
import arcade.engine.InputAware;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Defend six cities from inbound ballistic missiles.
 */
public final class MissileCommandGame implements GameScreen {
    static final int WORLD_W = 960;
    static final int WORLD_H = 720;
    private static final double GROUND = 670;
    private static final double INTERCEPTOR_SPEED = 280;

    private final MissileCommandAgent agent;
    private final boolean headless;
    private final Random random = new Random(7);
    private final CityUnit[] cities;
    private final Battery[] bases;
    private final List<Warhead> inbound = new ArrayList<>();
    private final List<Interceptor> outbound = new ArrayList<>();
    private final List<Blast> blasts = new ArrayList<>();
    private int tick;
    private int score;
    private int wave = 1;
    private int toSpawn;
    private int spawnCooldown;
    private int fireCooldown;
    private int wavePause;
    private boolean gameOver;
    private boolean done;

    public MissileCommandGame(MissileCommandAgent agent) {
        this(agent, false);
    }

    public MissileCommandGame(MissileCommandAgent agent, boolean headless) {
        this.agent = agent;
        this.headless = headless;
        cities = new CityUnit[] {
                new CityUnit(90),
                new CityUnit(200),
                new CityUnit(310),
                new CityUnit(650),
                new CityUnit(760),
                new CityUnit(870)
        };
        bases = new Battery[] {
                new Battery(140),
                new Battery(480),
                new Battery(820)
        };
        startWave();
    }

    @Override
    public String title() {
        return "MISSILE COMMAND";
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
        MissileCommandAction action = readAction(input);
        if (action.fire()) {
            tryFire(action.x(), action.y());
        }
        if (fireCooldown > 0) {
            fireCooldown--;
        }

        if (wavePause > 0) {
            wavePause--;
            if (wavePause == 0) {
                startWave();
            }
        } else if (toSpawn > 0) {
            spawnCooldown--;
            if (spawnCooldown <= 0) {
                spawnInbound();
                toSpawn--;
                spawnCooldown = Math.max(20, 70 - wave * 4);
            }
        }

        for (Warhead warhead : inbound) {
            warhead.x += warhead.vx * dt;
            warhead.y += warhead.vy * dt;
        }
        Iterator<Interceptor> shotIt = outbound.iterator();
        while (shotIt.hasNext()) {
            Interceptor shot = shotIt.next();
            double dx = shot.tx - shot.x;
            double dy = shot.ty - shot.y;
            double dist = Math.hypot(dx, dy);
            double step = INTERCEPTOR_SPEED * dt;
            if (dist <= step) {
                blasts.add(Blast.at(shot.tx, shot.ty));
                shotIt.remove();
            } else {
                shot.x += dx / dist * step;
                shot.y += dy / dist * step;
            }
        }

        Iterator<Blast> blastIt = blasts.iterator();
        while (blastIt.hasNext()) {
            Blast blast = blastIt.next();
            blast.age += dt;
            if (blast.age > 1.15) {
                blastIt.remove();
            }
        }

        hitWarheads();
        impactGround();

        if (!anyCityAlive()) {
            gameOver = true;
            if (headless) {
                done = true;
            }
        } else if (toSpawn == 0 && inbound.isEmpty() && outbound.isEmpty() && blasts.isEmpty() && wavePause == 0) {
            score += (int) (citiesAlive() * 100);
            for (Battery base : bases) {
                score += base.ammo * 5;
            }
            wave++;
            wavePause = headless ? 1 : 120;
        }
    }

    @Override
    public void render(Graphics2D g, int width, int height) {
        Draw.quality(g);
        Draw.fillScreen(g, width, height);
        double sx = width / (double) WORLD_W;
        double sy = height / (double) WORLD_H;
        g.scale(sx, sy);

        g.setColor(new Color(16, 18, 40));
        g.fillRect(0, 0, WORLD_W, WORLD_H);
        g.setColor(new Color(90, 70, 40));
        g.fillRect(0, (int) GROUND, WORLD_W, WORLD_H - (int) GROUND);

        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(255, 90, 70));
        for (Warhead warhead : inbound) {
            g.drawLine((int) warhead.originX, (int) warhead.originY, (int) warhead.x, (int) warhead.y);
            g.fillOval((int) warhead.x - 3, (int) warhead.y - 3, 6, 6);
        }
        g.setColor(new Color(120, 220, 255));
        for (Interceptor shot : outbound) {
            g.drawLine((int) shot.originX, (int) shot.originY, (int) shot.x, (int) shot.y);
            g.fillOval((int) shot.x - 2, (int) shot.y - 2, 4, 4);
        }
        for (Blast blast : blasts) {
            double r = blast.radius();
            float alpha = (float) Math.max(0.15, 1.0 - blast.age);
            g.setColor(new Color(1f, 0.85f, 0.3f, alpha));
            g.fillOval((int) (blast.x - r), (int) (blast.y - r), (int) (r * 2), (int) (r * 2));
        }

        for (CityUnit city : cities) {
            drawCity(g, city);
        }
        for (Battery base : bases) {
            drawBase(g, base);
        }

        if (agent instanceof KeyboardAgent keyboard) {
            g.setColor(new Color(255, 255, 255, 200));
            int cx = (int) keyboard.aimX();
            int cy = (int) keyboard.aimY();
            g.drawLine(cx - 10, cy, cx + 10, cy);
            g.drawLine(cx, cy - 10, cx, cy + 10);
        }
        g.scale(1 / sx, 1 / sy);

        Draw.text(g, "SCORE  " + score, 28, 36, Draw.HUD, 22, true);
        Draw.text(g, "WAVE  " + wave, width / 2 - 50, 36, Draw.MUTED, 20, true);
        Draw.text(g, agentLabel(), width - 180, 36, Draw.ACCENT, 18, true);
        if (gameOver) {
            Draw.outlined(g, "THE CITIES FELL", width / 2, height / 2, new Color(255, 80, 80), Color.BLACK, 40);
        } else if (wavePause > 0) {
            Draw.outlined(g, "WAVE " + wave, width / 2, height / 2, Draw.HUD, Color.BLACK, 42);
        }
    }

    private MissileCommandAction readAction(Input input) {
        if (agent == null) {
            return MissileCommandAction.NONE;
        }
        InputAware.feed(agent, input);
        MissileCommandAction action = agent.tick(snapshot());
        return action == null ? MissileCommandAction.NONE : action;
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

    MissileCommandState snapshot() {
        List<City> cityViews = new ArrayList<>();
        for (CityUnit city : cities) {
            cityViews.add(new City(city.x, GROUND, city.alive));
        }
        List<Base> baseViews = new ArrayList<>();
        for (Battery base : bases) {
            baseViews.add(new Base(base.x, GROUND - 8, base.ammo));
        }
        List<IncomingMissile> inboundViews = new ArrayList<>();
        for (Warhead warhead : inbound) {
            inboundViews.add(new IncomingMissile(
                    warhead.x, warhead.y, warhead.vx, warhead.vy, warhead.tx, warhead.ty));
        }
        List<MissileCommandState.Explosion> blastViews = new ArrayList<>();
        for (Blast blast : blasts) {
            blastViews.add(new MissileCommandState.Explosion(blast.x, blast.y, blast.radius()));
        }
        return new MissileCommandState(
                tick,
                score,
                wave,
                WORLD_W,
                WORLD_H,
                List.copyOf(cityViews),
                List.copyOf(baseViews),
                List.copyOf(inboundViews),
                List.copyOf(blastViews)
        );
    }

    private void tryFire(double x, double y) {
        if (fireCooldown > 0) {
            return;
        }
        Battery best = null;
        double bestDist = Double.MAX_VALUE;
        for (Battery base : bases) {
            if (base.ammo <= 0) {
                continue;
            }
            double dist = Math.hypot(base.x - x, GROUND - y);
            if (dist < bestDist) {
                bestDist = dist;
                best = base;
            }
        }
        if (best == null) {
            return;
        }
        best.ammo--;
        fireCooldown = 10;
        Interceptor shot = new Interceptor();
        shot.originX = best.x;
        shot.originY = GROUND - 12;
        shot.x = best.x;
        shot.y = GROUND - 12;
        shot.tx = clamp(x, 10, WORLD_W - 10);
        shot.ty = clamp(y, 20, GROUND - 20);
        outbound.add(shot);
    }

    private void startWave() {
        toSpawn = 6 + wave * 2;
        spawnCooldown = 20;
        for (Battery base : bases) {
            base.ammo = 10;
        }
    }

    private void spawnInbound() {
        List<CityUnit> living = new ArrayList<>();
        for (CityUnit city : cities) {
            if (city.alive) {
                living.add(city);
            }
        }
        if (living.isEmpty()) {
            return;
        }
        CityUnit target = living.get(random.nextInt(living.size()));
        Warhead warhead = new Warhead();
        warhead.originX = 40 + random.nextDouble() * (WORLD_W - 80);
        warhead.originY = 0;
        warhead.x = warhead.originX;
        warhead.y = 0;
        warhead.tx = target.x;
        warhead.ty = GROUND;
        double speed = 55 + wave * 8 + random.nextDouble() * 20;
        double dx = warhead.tx - warhead.x;
        double dy = warhead.ty - warhead.y;
        double dist = Math.hypot(dx, dy);
        warhead.vx = dx / dist * speed;
        warhead.vy = dy / dist * speed;
        inbound.add(warhead);
    }

    private void hitWarheads() {
        Iterator<Warhead> it = inbound.iterator();
        while (it.hasNext()) {
            Warhead warhead = it.next();
            boolean hit = false;
            for (Blast blast : blasts) {
                if (Math.hypot(warhead.x - blast.x, warhead.y - blast.y) < blast.radius()) {
                    hit = true;
                    break;
                }
            }
            if (hit) {
                it.remove();
                score += 25;
                blasts.add(Blast.at(warhead.x, warhead.y));
            }
        }
    }

    private void impactGround() {
        Iterator<Warhead> it = inbound.iterator();
        while (it.hasNext()) {
            Warhead warhead = it.next();
            if (warhead.y < GROUND) {
                continue;
            }
            it.remove();
            blasts.add(Blast.at(warhead.x, GROUND - 4));
            for (CityUnit city : cities) {
                if (city.alive && Math.abs(city.x - warhead.x) < 28) {
                    city.alive = false;
                }
            }
        }
    }

    private boolean anyCityAlive() {
        return citiesAlive() > 0;
    }

    private int citiesAlive() {
        int count = 0;
        for (CityUnit city : cities) {
            if (city.alive) {
                count++;
            }
        }
        return count;
    }

    private void drawCity(Graphics2D g, CityUnit city) {
        if (!city.alive) {
            g.setColor(new Color(40, 30, 30));
            g.fillRect((int) city.x - 16, (int) GROUND - 8, 32, 8);
            return;
        }
        g.setColor(new Color(90, 200, 130));
        g.fillRect((int) city.x - 18, (int) GROUND - 22, 12, 22);
        g.fillRect((int) city.x - 4, (int) GROUND - 34, 10, 34);
        g.fillRect((int) city.x + 8, (int) GROUND - 18, 10, 18);
        g.setColor(new Color(255, 220, 90));
        g.fillRect((int) city.x - 14, (int) GROUND - 16, 4, 4);
        g.fillRect((int) city.x, (int) GROUND - 24, 4, 4);
    }

    private void drawBase(Graphics2D g, Battery base) {
        g.setColor(new Color(180, 180, 200));
        Path2D path = new Path2D.Double();
        path.moveTo(base.x - 22, GROUND);
        path.lineTo(base.x, GROUND - 28);
        path.lineTo(base.x + 22, GROUND);
        path.closePath();
        g.fill(path);
        g.setColor(Draw.HUD);
        g.setFont(Draw.font(12, true));
        g.drawString(String.valueOf(base.ammo), (int) base.x - 6, (int) GROUND - 32);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class CityUnit {
        final double x;
        boolean alive = true;

        CityUnit(double x) {
            this.x = x;
        }
    }

    private static final class Battery {
        final double x;
        int ammo = 10;

        Battery(double x) {
            this.x = x;
        }
    }

    private static final class Warhead {
        double originX;
        double originY;
        double x;
        double y;
        double vx;
        double vy;
        double tx;
        double ty;
    }

    private static final class Interceptor {
        double originX;
        double originY;
        double x;
        double y;
        double tx;
        double ty;
    }

    private static final class Blast {
        double x;
        double y;
        double age;

        static Blast at(double x, double y) {
            Blast blast = new Blast();
            blast.x = x;
            blast.y = y;
            return blast;
        }

        double radius() {
            double t = age / 1.15;
            if (t < 0.55) {
                return 8 + t / 0.55 * 52;
            }
            return 60 * (1.0 - (t - 0.55) / 0.45);
        }
    }
}
