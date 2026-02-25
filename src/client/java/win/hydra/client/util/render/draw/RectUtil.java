package win.hydra.client.util.render.draw;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Basic rectangle drawing helpers backed by {@link GuiGraphics}.
 */
public class RectUtil {

    public static void drawRect(GuiGraphics graphics, float x, float y, float width, float height, int color) {
        int x1 = Math.round(x);
        int y1 = Math.round(y);
        int x2 = Math.round(x + width);
        int y2 = Math.round(y + height);
        graphics.fill(x1, y1, x2, y2, color);
    }
}


