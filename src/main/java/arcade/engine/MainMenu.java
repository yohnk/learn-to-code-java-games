package arcade.engine;

import arcade.catalog.Agents;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Title screen: pick a game and who is playing it.
 */
public final class MainMenu implements GameScreen {
    private static final String[] TIPS = {
            "Pac-Man: arrows / WASD. Eat dots, avoid ghosts.",
            "Asteroids: left/right spin, up thrust, space fire.",
            "Missile Command: arrows move the crosshair, space fires."
    };

    private final GameStarter starter;
    private int gameIndex;
    private int agentIndex;
    private boolean started;
    private int pulse;

    public MainMenu(GameStarter starter) {
        this.starter = starter;
    }

    @Override
    public void update(double dt, Input input) {
        pulse++;
        GameId[] games = GameId.values();
        if (input.keyPressed(KeyEvent.VK_LEFT) || input.keyPressed(KeyEvent.VK_A)) {
            gameIndex = (gameIndex + games.length - 1) % games.length;
            agentIndex = 0;
        }
        if (input.keyPressed(KeyEvent.VK_RIGHT) || input.keyPressed(KeyEvent.VK_D)) {
            gameIndex = (gameIndex + 1) % games.length;
            agentIndex = 0;
        }
        List<Agents.Option> agents = Agents.options(games[gameIndex]);
        if (input.keyPressed(KeyEvent.VK_UP) || input.keyPressed(KeyEvent.VK_W)) {
            agentIndex = (agentIndex + agents.size() - 1) % agents.size();
        }
        if (input.keyPressed(KeyEvent.VK_DOWN) || input.keyPressed(KeyEvent.VK_S)) {
            agentIndex = (agentIndex + 1) % agents.size();
        }
        if (input.confirm() || input.keyPressed(KeyEvent.VK_1) || input.keyPressed(KeyEvent.VK_2) || input.keyPressed(KeyEvent.VK_3)) {
            if (input.keyPressed(KeyEvent.VK_1)) {
                gameIndex = 0;
            } else if (input.keyPressed(KeyEvent.VK_2)) {
                gameIndex = 1;
            } else if (input.keyPressed(KeyEvent.VK_3)) {
                gameIndex = 2;
            }
            starter.start(games[gameIndex], Agents.options(games[gameIndex]).get(agentIndex));
            started = true;
        }
    }

    @Override
    public void render(Graphics2D g, int width, int height) {
        Draw.quality(g);
        Draw.fillScreen(g, width, height);
        Draw.outlined(g, "ARCADE AGENTS", width / 2, 92, Draw.HUD, new Color(80, 40, 0), 48);
        Draw.centered(g, "Write Java. Play the classics.", width / 2, 128, Draw.MUTED, 18, false);

        GameId[] games = GameId.values();
        int cardW = 260;
        int gap = 24;
        int total = games.length * cardW + (games.length - 1) * gap;
        int x0 = (width - total) / 2;
        for (int i = 0; i < games.length; i++) {
            int x = x0 + i * (cardW + gap);
            int y = 180;
            Draw.panel(g, x, y, cardW, 150, i == gameIndex);
            Draw.centered(g, games[i].title(), x + cardW / 2, y + 70, i == gameIndex ? Draw.ACCENT : Color.WHITE, 20, true);
            Draw.centered(g, "[" + (i + 1) + "]", x + cardW / 2, y + 108, Draw.MUTED, 16, false);
        }

        Draw.centered(g, "AGENT", width / 2, 370, Draw.MUTED, 16, true);
        List<Agents.Option> agents = Agents.options(games[gameIndex]);
        int ay = 400;
        for (int i = 0; i < agents.size(); i++) {
            boolean selected = i == agentIndex;
            String mark = selected ? "> " : "  ";
            Color color = selected ? Draw.HUD : Draw.MUTED;
            Draw.centered(g, mark + agents.get(i).label(), width / 2, ay, color, 20, selected);
            ay += 32;
        }

        Draw.centered(g, TIPS[gameIndex], width / 2, height - 90, Draw.MUTED, 16, false);
        String prompt = (pulse / 40) % 2 == 0 ? "Press Enter to play" : "";
        Draw.centered(g, prompt, width / 2, height - 50, Draw.ACCENT, 18, true);
        Draw.centered(g, "Left/Right: game    Up/Down: agent    Esc in-game: menu", width / 2, height - 24, new Color(90, 90, 120), 13, false);
    }

    @Override
    public boolean isDone() {
        return started;
    }

    @Override
    public int score() {
        return 0;
    }

    @FunctionalInterface
    public interface GameStarter {
        void start(GameId game, Agents.Option agent);
    }
}
