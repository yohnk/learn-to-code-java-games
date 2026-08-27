package arcade.pacman;

/**
 * What is on one maze tile. Walls cannot be walked through. Pac-Man cannot
 * walk through {@link #DOOR} (that is the ghost house).
 */
public enum Tile {
    WALL,
    PELLET,
    POWER,
    EMPTY,
    DOOR
}
