package arcade.agents.pacman;

import arcade.pacman.PacManAction;
import arcade.pacman.PacManAgent;
import arcade.pacman.PacManState;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Picks a random legal direction each tick. A baseline, not a strategy. */
public final class RandomPacManAgent implements PacManAgent {
    private final Random random = new Random();

    @Override
    public PacManAction tick(PacManState state) {
        List<PacManAction> options = new ArrayList<>();
        for (PacManAction action : PacManAction.moves()) {
            if (state.canMove(action)) {
                options.add(action);
            }
        }
        if (options.isEmpty()) {
            return PacManAction.NONE;
        }
        return options.get(random.nextInt(options.size()));
    }
}
