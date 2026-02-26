package win.hydra.client.util.render.draw;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Утилиты для 3D рендеринга в мире.
 */
public class Render3DUtil {

    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * Рисует коробку вокруг entity.
     */
    public static void drawEntityESP(Entity entity, int color, float lineWidth) {
        if (entity == null || mc.getCameraEntity() == null) return;

        Vec3 cameraPos = mc.getCameraEntity().position();
        AABB bb = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        double x = entity.getX() - cameraPos.x;
        double y = entity.getY() - cameraPos.y;
        double z = entity.getZ() - cameraPos.z;

        double minX = bb.minX + x;
        double minY = bb.minY + y;
        double minZ = bb.minZ + z;
        double maxX = bb.maxX + x;
        double maxY = bb.maxY + y;
        double maxZ = bb.maxZ + z;

        addEdge(buffer, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        addEdge(buffer, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        addEdge(buffer, minX, minY, maxZ, maxX, minY, maxZ, red, green, blue, alpha);
        addEdge(buffer, minX, maxY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);

        addEdge(buffer, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        addEdge(buffer, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        addEdge(buffer, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        addEdge(buffer, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);

        addEdge(buffer, minX, minY, minZ, minX, minY, maxZ, red, green, blue, alpha);
        addEdge(buffer, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
        addEdge(buffer, minX, maxY, minZ, minX, maxY, maxZ, red, green, blue, alpha);
        addEdge(buffer, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void addEdge(BufferBuilder buffer, double x1, double y1, double z1, 
                                 double x2, double y2, double z2, 
                                 float r, float g, float b, float a) {
        Matrix4f matrix = new Matrix4f().translation(0, 0, 0);
        buffer.addVertex(matrix, (float) x1, (float) y1, (float) z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, (float) x2, (float) y2, (float) z2).setColor(r, g, b, a);
    }
}
