package win.hydra.client.util.render.color;

import java.awt.Color;

public class ColorUtil {

    public static int multAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public static int multDark(int color, float factor) {
        Color c = new Color(color, true);
        int r = (int) (c.getRed() * factor);
        int g = (int) (c.getGreen() * factor);
        int b = (int) (c.getBlue() * factor);
        return new Color(r, g, b, c.getAlpha()).getRGB();
    }

    public static int fade(int speed, int offset, int color1, int color2) {
        double t = (System.currentTimeMillis() / (double) (1000 / speed) + offset) % 1.0;
        return lerpColor(color1, color2, (float) t);
    }

    public static int getColor(int gray) {
        return new Color(gray, gray, gray, 255).getRGB();
    }

    public static int lerpColor(int c1, int c2, float t) {
        Color a = new Color(c1, true);
        Color b = new Color(c2, true);
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, al).getRGB();
    }
}


