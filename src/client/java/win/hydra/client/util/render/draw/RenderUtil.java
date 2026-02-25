package win.hydra.client.util.render.draw;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * General render helpers (textures, images).
 */
public class RenderUtil {

    /**
     * Draw a simple textured rectangle.
     *
     * @param texture  resource location of the texture
     * @param graphics gui graphics to draw with
     * @param x        x position
     * @param y        y position
     * @param width    width in pixels
     * @param height   height in pixels
     * @param color    tint color (currently unused, reserved for future)
     */
    public static void drawImage(ResourceLocation texture, GuiGraphics graphics,
                                 float x, float y, int width, int height, int color) {
        // Stub implementation for now; you can replace this with a proper blit call
        // once you decide how you want to handle texture rendering and tinting.
    }
}


