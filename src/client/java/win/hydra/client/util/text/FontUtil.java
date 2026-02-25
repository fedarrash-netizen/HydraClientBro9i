package win.hydra.client.util.text;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Simple custom text utility. Wraps the vanilla font renderer and adds
 * helpers for scaling and centering text. Later you can swap the underlying
 * font to use JSON/TTF fonts from assets without touching callers.
 */
public final class FontUtil {

    private static final Minecraft MC = Minecraft.getInstance();

    private FontUtil() {
    }

    private static Font font() {
        return MC.font;
    }

    public static void drawString(GuiGraphics g, String text, float x, float y, int color) {
        drawString(g, text, x, y, color, 1.0F, false);
    }

    public static void drawStringShadow(GuiGraphics g, String text, float x, float y, int color) {
        drawString(g, text, x, y, color, 1.0F, true);
    }

    public static void drawString(GuiGraphics g, String text, float x, float y,
                                  int color, float scale, boolean shadow) {
        if (text == null || text.isEmpty()) return;

        var pose = g.pose();
        pose.pushPose();
        pose.scale(scale, scale, 1.0F);

        float sx = x / scale;
        float sy = y / scale;

        if (shadow) {
            g.drawString(font(), text, (int) sx, (int) sy, color, true);
        } else {
            g.drawString(font(), text, (int) sx, (int) sy, color, false);
        }

        pose.popPose();
    }

    public static void drawCentered(GuiGraphics g, String text, float x, float y, int color, float scale) {
        float w = width(text) * scale;
        drawString(g, text, x - w / 2.0F, y, color, scale, false);
    }

    public static int width(String text) {
        if (text == null) return 0;
        return font().width(text);
    }

    public static int height() {
        return font().lineHeight;
    }
}


