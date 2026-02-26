package win.hydra.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Событие для рендеринга в мире (3D).
 */
public class Render3DEvent {

    private final GuiGraphics graphics;
    private final PoseStack poseStack;
    private final float tickDelta;

    public Render3DEvent(GuiGraphics graphics, PoseStack poseStack, float tickDelta) {
        this.graphics = graphics;
        this.poseStack = poseStack;
        this.tickDelta = tickDelta;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public float getTickDelta() {
        return tickDelta;
    }
}
