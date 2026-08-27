package arcade;

import arcade.catalog.Agents;
import arcade.engine.ArcadeApp;
import arcade.engine.GameId;
import arcade.engine.GameScreen;
import arcade.engine.Games;

/**
 * Entry point. With no flags, opens the menu. See {@code --help}.
 */
public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        Args parsed = Args.parse(args);
        if (parsed.help) {
            System.out.println("""
                    Arcade Agents
                      ./gradlew run
                      ./gradlew run --args="--game pacman --agent my"
                      ./gradlew run --args="--game asteroids --agent sample"
                      ./gradlew run --args="--game missile --agent sample --headless --ticks 4000"

                    Games:  pacman | asteroids | missile
                    Agents: my (default, keyboard until you uncomment tick) | keyboard | random | sample
                    """);
            return;
        }
        if (parsed.game == null) {
            ArcadeApp.launchMenu();
            return;
        }
        Agents.Option agent = Agents.find(parsed.game, parsed.agent);
        if (parsed.headless) {
            GameScreen screen = Games.create(parsed.game, agent, true);
            int score = ArcadeApp.playHeadless(screen, parsed.ticks);
            System.out.println(parsed.game.id() + " score=" + score);
            return;
        }
        ArcadeApp.launch(parsed.game, agent);
    }

    private static final class Args {
        GameId game;
        String agent = Agents.DEFAULT;
        boolean headless;
        boolean help;
        int ticks = 10_000;

        static Args parse(String[] args) {
            Args parsed = new Args();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--help", "-h" -> parsed.help = true;
                    case "--game", "-g" -> parsed.game = GameId.fromArg(requireValue(args, ++i, arg));
                    case "--agent", "-a" -> parsed.agent = requireValue(args, ++i, arg);
                    case "--headless" -> parsed.headless = true;
                    case "--ticks" -> parsed.ticks = Integer.parseInt(requireValue(args, ++i, arg));
                    default -> throw new IllegalArgumentException("Unknown flag: " + arg + " (try --help)");
                }
            }
            if (parsed.headless && parsed.game == null) {
                throw new IllegalArgumentException("--headless requires --game");
            }
            return parsed;
        }

        private static String requireValue(String[] args, int index, String flag) {
            if (index >= args.length) {
                throw new IllegalArgumentException(flag + " needs a value");
            }
            return args[index];
        }
    }
}
