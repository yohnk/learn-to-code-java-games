package arcade.engine;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Keyboard and mouse snapshot for the current frame.
 *
 * <p>The engine updates this; games and the menu only read it.
 */
public final class Input {
    private final Set<Integer> down = new HashSet<>();
    private final Set<Integer> pressed = new HashSet<>();
    private int mouseX;
    private int mouseY;
    private boolean mouseDown;
    private boolean mouseClicked;

    public boolean keyDown(int keyCode) {
        return down.contains(keyCode);
    }

    public boolean keyPressed(int keyCode) {
        return pressed.contains(keyCode);
    }

    public boolean up() {
        return keyDown(KeyEvent.VK_UP) || keyDown(KeyEvent.VK_W);
    }

    public boolean down() {
        return keyDown(KeyEvent.VK_DOWN) || keyDown(KeyEvent.VK_S);
    }

    public boolean left() {
        return keyDown(KeyEvent.VK_LEFT) || keyDown(KeyEvent.VK_A);
    }

    public boolean right() {
        return keyDown(KeyEvent.VK_RIGHT) || keyDown(KeyEvent.VK_D);
    }

    public boolean fire() {
        return keyDown(KeyEvent.VK_SPACE) || mouseDown;
    }

    public boolean confirm() {
        return keyPressed(KeyEvent.VK_ENTER) || keyPressed(KeyEvent.VK_SPACE);
    }

    public boolean back() {
        return keyPressed(KeyEvent.VK_ESCAPE);
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public boolean mouseClicked() {
        return mouseClicked;
    }

    public boolean mouseDown() {
        return mouseDown;
    }

    void keyPressedEvent(int keyCode) {
        if (down.add(keyCode)) {
            pressed.add(keyCode);
        }
    }

    void keyReleasedEvent(int keyCode) {
        down.remove(keyCode);
    }

    void mouseMoved(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    void mousePressed(int x, int y) {
        mouseX = x;
        mouseY = y;
        if (!mouseDown) {
            mouseClicked = true;
        }
        mouseDown = true;
    }

    void mouseReleased() {
        mouseDown = false;
    }

    void endFrame() {
        pressed.clear();
        mouseClicked = false;
    }
}
