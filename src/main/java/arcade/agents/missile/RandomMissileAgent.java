package arcade.agents.missile;

import arcade.missile.IncomingMissile;
import arcade.missile.MissileCommandAction;
import arcade.missile.MissileCommandAgent;
import arcade.missile.MissileCommandState;
import java.util.List;
import java.util.Random;

/** Fires at a random inbound missile. Wasteful, but it does fire. */
public final class RandomMissileAgent implements MissileCommandAgent {
    private final Random random = new Random();

    @Override
    public MissileCommandAction tick(MissileCommandState state) {
        List<IncomingMissile> incoming = state.incoming();
        if (incoming.isEmpty()) {
            return MissileCommandAction.NONE;
        }
        IncomingMissile missile = incoming.get(random.nextInt(incoming.size()));
        return MissileCommandAction.fireAt(missile.x(), missile.y());
    }
}
