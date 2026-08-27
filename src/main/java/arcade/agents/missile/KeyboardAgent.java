package arcade.agents.missile;

import arcade.engine.Input;
import arcade.engine.InputAware;
import arcade.missile.MissileCommandAction;
import arcade.missile.MissileCommandAgent;
import arcade.missile.MissileCommandState;
import java.awt.event.KeyEvent;

/**
 * Arrow keys / WASD move the crosshair. Space (or a click) fires.
 */
public class KeyboardAgent implements MissileCommandAgent, InputAware {
    private static final double SPEED = 10;

    private Input input;
    private double aimX = 480;
    private double aimY = 280;

    @Override
    public void setInput(Input input) {
        this.input = input;
    }

    public double aimX() {
        return aimX;
    }

    public double aimY() {
        return aimY;
    }

    @Override
    public MissileCommandAction tick(MissileCommandState state) {
        if (input == null) {
            return MissileCommandAction.NONE;
        }
        if (input.left()) {
            aimX -= SPEED;
        }
        if (input.right()) {
            aimX += SPEED;
        }
        if (input.up()) {
            aimY -= SPEED;
        }
        if (input.down()) {
            aimY += SPEED;
        }
        aimX = clamp(aimX, 8, state.width() - 8);
        aimY = clamp(aimY, 8, state.height() - 40);

        if (input.mouseClicked()) {
            aimX = input.mouseX();
            aimY = input.mouseY();
            return MissileCommandAction.fireAt(aimX, aimY);
        }
        if (input.keyPressed(KeyEvent.VK_SPACE)) {
            return MissileCommandAction.fireAt(aimX, aimY);
        }
        return MissileCommandAction.NONE;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
