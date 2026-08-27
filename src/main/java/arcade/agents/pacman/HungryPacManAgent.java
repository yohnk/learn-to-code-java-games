package arcade.agents.pacman;

import arcade.engine.GridPos;
import arcade.pacman.GhostInfo;
import arcade.pacman.PacManAction;
import arcade.pacman.PacManAgent;
import arcade.pacman.PacManState;

/**
 * A readable sample: run from nearby ghosts, otherwise eat the closest pellet
 * (or an edible ghost if one is close).
 */
public final class HungryPacManAgent implements PacManAgent {
    @Override
    public PacManAction tick(PacManState state) {
        GhostInfo danger = state.nearestDangerousGhost();
        if (danger != null && state.distanceTo(danger.tileX(), danger.tileY()) <= 5) {
            return state.awayFrom(danger.tileX(), danger.tileY());
        }

        GhostInfo snack = state.nearestEdibleGhost();
        if (snack != null && state.distanceTo(snack.tileX(), snack.tileY()) <= 12) {
            return state.pathToward(snack.tileX(), snack.tileY());
        }

        GridPos power = state.nearestPowerPellet();
        if (danger != null && power != null && state.distanceTo(danger.tileX(), danger.tileY()) <= 8) {
            return state.pathToward(power.x(), power.y());
        }

        GridPos pellet = state.nearestPellet();
        if (pellet != null) {
            return state.pathToward(pellet.x(), pellet.y());
        }
        return PacManAction.NONE;
    }
}
