package arcade.pacman;

/**
 * One ghost, as the agent sees it.
 *
 * @param name        blinky, pinky, inky, or clyde
 * @param tileX       grid column
 * @param tileY       grid row
 * @param frightened  true while a power pellet is making this ghost edible
 * @param eaten       true while the ghost is eyes-only, heading home
 */
public record GhostInfo(String name, int tileX, int tileY, boolean frightened, boolean eaten) {
    /**
     * True when touching this ghost would cost a life.
     */
    public boolean dangerous() {
        return !frightened && !eaten;
    }
}
