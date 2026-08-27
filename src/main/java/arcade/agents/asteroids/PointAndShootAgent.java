package arcade.agents.asteroids;

import arcade.asteroids.AsteroidsAgent;
import arcade.asteroids.AsteroidsControls;
import arcade.asteroids.AsteroidsState;
import arcade.asteroids.SpaceBody;

/**
 * Aim at the nearest asteroid, shoot when lined up, and boost away if it is
 * about to hit the ship.
 */
public final class PointAndShootAgent implements AsteroidsAgent {
    @Override
    public AsteroidsControls tick(AsteroidsState state) {
        AsteroidsControls controls = new AsteroidsControls();
        SpaceBody rock = state.nearestAsteroid();
        if (rock == null) {
            return controls;
        }
        double distance = state.distance(state.ship().x(), state.ship().y(), rock.x(), rock.y());
        if (distance < 90) {
            double away = AsteroidsState.wrapAngle(state.angleTo(rock.x(), rock.y()) + Math.PI);
            double error = AsteroidsState.wrapAngle(away - state.ship().angle());
            if (error > 0.2) {
                controls.rotateLeft = true;
            } else if (error < -0.2) {
                controls.rotateRight = true;
            } else {
                controls.thrust = true;
            }
            return controls;
        }
        double error = state.facingError(rock.x(), rock.y());
        if (error > 0.1) {
            controls.rotateLeft = true;
        } else if (error < -0.1) {
            controls.rotateRight = true;
        } else {
            controls.fire = true;
        }
        return controls;
    }
}
