package win.hydra.client.event;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Simple 2D render event fired from the HUD callback.
 */
public class Render2DEvent {

    private final GuiGraphics graphics;
    private final float tickDelta;

    public Render2DEvent(GuiGraphics graphics, float tickDelta) {
        this.graphics = graphics;
        this.tickDelta = tickDelta;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}

