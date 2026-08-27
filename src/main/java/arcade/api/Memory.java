package arcade.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Optional scratch space for student agents.
 *
 * <p>You can also just declare fields on your agent class. {@code Memory} is
 * here if you want a simple key/value bag without inventing your own types yet.
 *
 * <pre>{@code
 * memory.set("targetX", 4);
 * int x = memory.getInt("targetX", -1);
 * }</pre>
 */
public final class Memory {
    private final Map<String, Object> data = new HashMap<>();

    public void set(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public <T> T getOrDefault(String key, T fallback) {
        T value = get(key);
        return value != null ? value : fallback;
    }

    public int getInt(String key, int fallback) {
        Object value = data.get(key);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    public double getDouble(String key, double fallback) {
        Object value = data.get(key);
        return value instanceof Number number ? number.doubleValue() : fallback;
    }

    public boolean getBoolean(String key, boolean fallback) {
        Object value = data.get(key);
        return value instanceof Boolean bool ? bool : fallback;
    }

    public void addInt(String key, int delta) {
        set(key, getInt(key, 0) + delta);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public void clear() {
        data.clear();
    }
}
