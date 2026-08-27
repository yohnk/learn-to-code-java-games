package student.missile;

import arcade.agents.missile.KeyboardAgent;
import arcade.missile.IncomingMissile;
import arcade.missile.MissileCommandAction;
import arcade.missile.MissileCommandState;

/**
 * YOUR Missile Command code lives in this file.
 *
 * <p>Until you uncomment {@code tick}, this agent is a keyboard: arrows move
 * the crosshair, space fires. Uncomment {@code tick} to take over. Comment it
 * out again to play yourself.
 *
 * <p>The game creates this class once. Add fields below to remember information
 * between frames.
 */
public final class MyAgent extends KeyboardAgent {

    @SuppressWarnings("unused")
    private int framesAlive;

    // Uncomment this method to write your own Missile Command brain.
    // Comment it out again if you want the keyboard back.
    //
    // @Override
    // public MissileCommandAction tick(MissileCommandState state) {
    //     framesAlive++;
    //
    //     IncomingMissile threat = state.mostUrgent();
    //     if (threat == null) {
    //         return MissileCommandAction.NONE;
    //     }
    //     // Starter strategy: shoot at where the missile is right now.
    //     // Explosions take time to bloom, so leading the target works better.
    //     return MissileCommandAction.fireAt(threat.x(), threat.y());
    // }
}
