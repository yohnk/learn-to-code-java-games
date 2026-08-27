package arcade.agents.missile;

import arcade.missile.IncomingMissile;
import arcade.missile.MissileCommandAction;
import arcade.missile.MissileCommandAgent;
import arcade.missile.MissileCommandState;

/**
 * Always intercepts the inbound missile that will hit soonest.
 */
public final class NearestThreatAgent implements MissileCommandAgent {
    @Override
    public MissileCommandAction tick(MissileCommandState state) {
        IncomingMissile threat = state.mostUrgent();
        if (threat == null) {
            return MissileCommandAction.NONE;
        }
        return state.intercept(threat);
    }
}
