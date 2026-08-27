package arcade.missile;

/**
 * A missile battery.
 */
public record Base(double x, double y, int ammo) {
    public boolean hasAmmo() {
        return ammo > 0;
    }
}
