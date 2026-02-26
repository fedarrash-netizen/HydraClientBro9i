package win.hydra.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import win.hydra.client.Client;
import win.hydra.client.event.Render3DEvent;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(at = @At("TAIL"), method = "renderLevel")
    private void onRenderLevel(PoseStack poseStack, long finishNanoTime, boolean renderBlockOutline, Camera camera, 
                               GameRenderer gameRenderer, Matrix4f frustumMatrix, Matrix4f projectionMatrix, 
                               CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        
        float tickDelta = finishNanoTime > 0 ? 1.0F : 0.0F;
        Render3DEvent event = new Render3DEvent(null, poseStack, tickDelta);
        Client.inst().eventBus().post(event);
    }
}
