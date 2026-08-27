package arcade.engine;

/**
 * Which game to launch, and who is steering it.
 */
public enum GameId {
    PACMAN("pacman", "PAC-MAN"),
    ASTEROIDS("asteroids", "ASTEROIDS"),
    MISSILE("missile", "MISSILE COMMAND");

    private final String id;
    private final String title;

    GameId(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public static GameId fromArg(String value) {
        String key = value.trim().toLowerCase();
        if (key.equals("missilecommand") || key.equals("missile-command")) {
            return MISSILE;
        }
        for (GameId game : values()) {
            if (game.id.equals(key) || game.name().equalsIgnoreCase(key)) {
                return game;
            }
        }
        throw new IllegalArgumentException("Unknown game: " + value + " (use pacman, asteroids, missile)");
    }
}
