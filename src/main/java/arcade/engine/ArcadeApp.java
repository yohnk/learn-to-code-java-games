package arcade.engine;

import arcade.catalog.Agents;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Swing window and 60 FPS loop. Games only see {@link GameScreen} and {@link Input}.
 */
public final class ArcadeApp {
    public static final int WIDTH = 960;
    public static final int HEIGHT = 720;
    public static final double DT = 1.0 / 60.0;

    private final Input input = new Input();
    private GameScreen screen;
    private GameId pendingGame;
    private Agents.Option pendingAgent;

    public ArcadeApp() {
        this.screen = new MainMenu(this::queueGame);
    }

    public ArcadeApp(GameId game, Agents.Option agent) {
        this.screen = Games.create(game, agent, false);
    }

    public void show() {
        JFrame frame = new JFrame("Arcade Agents");
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics;
                screen.render(g, getWidth(), getHeight());
            }
        };
        canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        canvas.setFocusable(true);
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                input.keyPressedEvent(event.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent event) {
                input.keyReleasedEvent(event.getKeyCode());
            }
        });
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                input.mousePressed(scaleX(event.getX(), canvas.getWidth()), scaleY(event.getY(), canvas.getHeight()));
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                input.mouseReleased();
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                input.mouseMoved(scaleX(event.getX(), canvas.getWidth()), scaleY(event.getY(), canvas.getHeight()));
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                mouseMoved(event);
            }
        };
        canvas.addMouseListener(mouse);
        canvas.addMouseMotionListener(mouse);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.add(canvas);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        canvas.requestFocusInWindow();

        Timer timer = new Timer(16, event -> {
            screen.update(DT, input);
            if (screen.isDone()) {
                if (pendingGame != null) {
                    screen = Games.create(pendingGame, pendingAgent, false);
                    pendingGame = null;
                    pendingAgent = null;
                } else if (!(screen instanceof MainMenu)) {
                    screen = new MainMenu(this::queueGame);
                }
            }
            input.endFrame();
            canvas.repaint();
        });
        timer.start();
    }

    /**
     * Runs a game without opening a window. Used by tests and classroom scripts.
     */
    public static int playHeadless(GameScreen game, int maxTicks) {
        Input input = new Input();
        int ticks = 0;
        while (ticks < maxTicks && !game.isDone()) {
            game.update(DT, input);
            ticks++;
        }
        return game.score();
    }

    /**
     * Renders one frame off-screen so render paths cannot silently explode.
     */
    public static void renderOnce(GameScreen game) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            game.render(g, WIDTH, HEIGHT);
        } finally {
            g.dispose();
        }
    }

    public static void launchMenu() {
        SwingUtilities.invokeLater(() -> new ArcadeApp().show());
    }

    public static void launch(GameId game, Agents.Option agent) {
        SwingUtilities.invokeLater(() -> new ArcadeApp(game, agent).show());
    }

    private void queueGame(GameId game, Agents.Option agent) {
        pendingGame = game;
        pendingAgent = agent;
    }

    private static int scaleX(int x, int width) {
        return (int) Math.round(x * (WIDTH / (double) Math.max(width, 1)));
    }

    private static int scaleY(int y, int height) {
        return (int) Math.round(y * (HEIGHT / (double) Math.max(height, 1)));
    }
}
