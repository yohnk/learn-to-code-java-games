package arcade.engine;

import java.awt.Graphics2D;

/**
 * One full-screen view: the menu, or a running game.
 */
public interface GameScreen {
    void update(double dt, Input input);

    void render(Graphics2D g, int width, int height);

    /**
     * True when the player (or headless runner) should leave this screen.
     */
    boolean isDone();

    int score();

    default String title() {
        return "";
    }
}
