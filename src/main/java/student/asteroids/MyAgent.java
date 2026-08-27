package student.asteroids;

import arcade.agents.asteroids.KeyboardAgent;
import arcade.asteroids.AsteroidsControls;
import arcade.asteroids.AsteroidsState;
import arcade.asteroids.SpaceBody;

/**
 * YOUR Asteroids code lives in this file.
 *
 * <p>Until you uncomment {@code tick}, this agent is a keyboard: left/right
 * rotate, up thrust, space fire. Uncomment {@code tick} to take over.
 * Comment it out again to play yourself.
 *
 * <p>The game creates this class once. Add fields below to remember information
 * between frames.
 */
public final class MyAgent extends KeyboardAgent {

    @SuppressWarnings("unused")
    private int framesAlive;

    // Uncomment this method to write your own Asteroids brain.
    // Comment it out again if you want the keyboard back.
    //
    // @Override
    // public AsteroidsControls tick(AsteroidsState state) {
    //     framesAlive++;
    //     AsteroidsControls controls = new AsteroidsControls();
    //
    //     SpaceBody rock = state.nearestAsteroid();
    //     if (rock == null) {
    //         return controls;
    //     }
    //
    //     // Starter strategy: turn toward the nearest rock and shoot.
    //     // You will still crash — try thrusting away when a rock is close.
    //     double error = state.facingError(rock.x(), rock.y());
    //     if (error > 0.12) {
    //         controls.rotateLeft = true;
    //     } else if (error < -0.12) {
    //         controls.rotateRight = true;
    //     } else {
    //         controls.fire = true;
    //     }
    //     return controls;
    // }
}
