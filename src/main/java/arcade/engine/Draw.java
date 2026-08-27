package arcade.engine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared drawing helpers so the games have a consistent arcade look.
 */
public final class Draw {
    public static final Color SCREEN = new Color(8, 8, 16);
    public static final Color HUD = new Color(255, 230, 80);
    public static final Color MUTED = new Color(180, 180, 200);
    public static final Color ACCENT = new Color(80, 200, 255);

    private Draw() {
    }

    public static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static Font font(int size, boolean bold) {
        return new Font(Font.MONOSPACED, bold ? Font.BOLD : Font.PLAIN, size);
    }

    public static void fillScreen(Graphics2D g, int width, int height) {
        g.setColor(SCREEN);
        g.fillRect(0, 0, width, height);
    }

    public static void text(Graphics2D g, String value, int x, int y, Color color, int size, boolean bold) {
        g.setFont(font(size, bold));
        g.setColor(color);
        g.drawString(value, x, y);
    }

    public static void centered(Graphics2D g, String value, int cx, int y, Color color, int size, boolean bold) {
        g.setFont(font(size, bold));
        FontMetrics metrics = g.getFontMetrics();
        g.setColor(color);
        g.drawString(value, cx - metrics.stringWidth(value) / 2, y);
    }

    public static void outlined(Graphics2D g, String value, int cx, int y, Color fill, Color outline, int size) {
        g.setFont(font(size, true));
        FontMetrics metrics = g.getFontMetrics();
        int x = cx - metrics.stringWidth(value) / 2;
        g.setColor(outline);
        g.drawString(value, x - 2, y);
        g.drawString(value, x + 2, y);
        g.drawString(value, x, y - 2);
        g.drawString(value, x, y + 2);
        g.setColor(fill);
        g.drawString(value, x, y);
    }

    public static void panel(Graphics2D g, int x, int y, int w, int h, boolean selected) {
        g.setColor(selected ? new Color(30, 50, 90) : new Color(20, 20, 35));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 18, 18));
        g.setStroke(new BasicStroke(selected ? 3f : 1.5f));
        g.setColor(selected ? ACCENT : new Color(70, 70, 100));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 18, 18));
    }
}
