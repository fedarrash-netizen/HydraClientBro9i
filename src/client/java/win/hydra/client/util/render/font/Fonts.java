package win.hydra.client.util.render.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Very simple font wrapper that mimics the API of the original client.
 *
 * All fonts currently delegate to Minecraft's default font.
 */
public enum Fonts {

    SFP_MEDIUM,
    ICON_NURIK,
    ICON_ESSENS;

    private static final Minecraft MC = Minecraft.getInstance();

    private Font font() {
        return MC.font;
    }

    public float getWidth(String text, float size) {
        if (text == null) return 0.0F;
        // Base width on vanilla font, scaled by requested size
        float base = font().width(text);
        return base * (size / 9.0F);
    }

    public void draw(GuiGraphics graphics, String text, float x, float y, int color, float size) {
        if (text == null) return;
        float scale = size / 9.0F;

        // Push scaling
        var pose = graphics.pose();
        pose.pushPose();
        pose.scale(scale, scale, 1.0F);

        // Adjust coordinates back to unscaled space
        float sx = x / scale;
        float sy = y / scale;

        graphics.drawString(font(), text, (int) sx, (int) sy, color, false);
        pose.popPose();
    }
}


