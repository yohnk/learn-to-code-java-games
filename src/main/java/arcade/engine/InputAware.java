package arcade.engine;

/**
 * Agents that read the keyboard or mouse implement this. The game loop calls
 * {@link #setInput(Input)} each frame before {@code tick}.
 */
public interface InputAware {
    void setInput(Input input);

    static void feed(Object agent, Input input) {
        if (agent instanceof InputAware aware) {
            aware.setInput(input);
        }
    }
}
