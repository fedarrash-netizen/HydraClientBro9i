package win.hydra.client.screen.hud.impl;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.MouseEvent;
import win.hydra.client.event.Render2DEvent;
import win.hydra.client.hud.IRenderer;
import win.hydra.client.managers.module.impl.render.InterFace;
import win.hydra.client.module.Module;
import win.hydra.client.module.ModuleManager;
import win.hydra.client.util.animation.NumberTransition;
import win.hydra.client.util.render.color.ColorUtil;
import win.hydra.client.util.render.font.Fonts;

import java.util.HashMap;
import java.util.Map;

/**
 * Рендерер активных кейбиндов с перетаскиванием.
 */
public class KeybindsRenderer implements IRenderer {

    private final Minecraft mc = Minecraft.getInstance();

    private float posX = 10.0F;
    private float posY = 100.0F;
    private float width = 80.0F;
    private float height = 30.0F;

    private final Map<String, Float> moduleAnimations = new HashMap<>();

    private static final ResourceLocation ICON_LOCATION = ResourceLocation.fromNamespaceAndPath("hydra", "icon.png");

    private boolean dragging = false;
    private double dragStartX = 0;
    private double dragStartY = 0;
    private float dragOffsetX = 0;
    private float dragOffsetY = 0;

    @EventHandler
    @Override
    public void render(Render2DEvent e) {
        GuiGraphics g = e.getGraphics();

        float fontSize = 7.0F;
        float padding = 5.0F;
        float iconSize = 10.0F;

        drawBackground(posX, posY, width, height, 3.0F);

        drawIcon(g, posX + 3.0F, posY + 2.0F, iconSize);

        Fonts.SFP_MEDIUM.draw(g, "KeyBinds", posX + padding + 12.0F, posY + 3.5F, -1, fontSize + 1.0F);

        // Разделительная линия
        int lineColor = ColorUtil.multAlpha(InterFace.getInstance().themeColor(), 0.4F);
        g.fill((int) (posX + padding - 2.0F), (int) (posY + 14.0F),
               (int) (posX + width - 2.0F), (int) (posY + 15.0F), lineColor);

        float contentY = posY + 18.0F;
        float maxWidth = Fonts.SFP_MEDIUM.getWidth("KeyBinds", fontSize) + padding * 2.0F + iconSize;
        float totalHeight = 18.0F;

        // Рендер активных модулей с кейбиндами
        for (Module module : ModuleManager.getInstance().getModules()) {
            // Обновляем анимацию
            float currentAnim = moduleAnimations.getOrDefault(module.getName(), 0.0F);
            float targetAnim = module.isEnabled() && module.getKey() > 0 ? 1.0F : 0.0F;
            float newAnim = NumberTransition.result(currentAnim, targetAnim);
            moduleAnimations.put(module.getName(), newAnim);

            if (newAnim <= 0.01F) continue;

            String moduleName = module.getName();
            String bindText = "[" + getKeyName(module.getKey()) + "]";

            float moduleWidth = Fonts.SFP_MEDIUM.getWidth(moduleName, fontSize);
            float bindWidth = Fonts.SFP_MEDIUM.getWidth(bindText, fontSize);
            float itemWidth = moduleWidth + bindWidth + padding * 3.0F;

            // Позиция Y с анимацией
            float animY = contentY + (1.0F - newAnim) * 5.0F;
            float alpha = newAnim;

            // Название модуля
            int moduleColor = ColorUtil.multAlpha(-1, alpha);
            Fonts.SFP_MEDIUM.draw(g, moduleName, posX + padding, animY, moduleColor, fontSize + 0.5F);

            // Бинд
            int bindColor = ColorUtil.multAlpha(InterFace.getInstance().themeColor(), alpha);
            Fonts.SFP_MEDIUM.draw(g, bindText, posX + width - padding - bindWidth, animY, bindColor, fontSize + 0.5F);

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }

            contentY += (fontSize + padding * 0.5F) * newAnim;
            totalHeight += (fontSize + padding * 0.5F) * newAnim;
        }

        // Обновляем размеры
        width = Math.max(maxWidth + 4.0F, 70.0F);
        height = totalHeight + 6.0F;
    }

    @EventHandler
    public void onMouse(MouseEvent e) {
        int mouseX = e.getMouseX();
        int mouseY = e.getMouseY();

        // Проверяем клик в области Keybinds
        boolean isInside = mouseX >= posX && mouseX <= posX + width &&
                          mouseY >= posY && mouseY <= posY + height;

        if (e.getButton() == 0) { // ЛКМ
            if (e.isPressed() && isInside) {
                dragging = true;
                dragStartX = mouseX;
                dragStartY = mouseY;
                dragOffsetX = posX;
                dragOffsetY = posY;
            } else if (!e.isPressed()) {
                dragging = false;
            }
        }

        if (dragging) {
            double deltaX = mouseX - dragStartX;
            double deltaY = mouseY - dragStartY;
            posX = (float) (dragOffsetX + deltaX);
            posY = (float) (dragOffsetY + deltaY);
        }
    }

    private void drawBackground(float x, float y, float w, float h, float radius) {
        int bgColor = ColorUtil.multAlpha(0xFF090817, 0.7F);
        int x1 = Math.round(x - 0.5F);
        int y1 = Math.round(y - 0.5F);
        int x2 = Math.round(x + w + 0.5F);
        int y2 = Math.round(y + h - 2.5F);

        // Закруглённый прямоугольник (упрощённо)
        fillRoundedRect(x1, y1, x2, y2, radius, bgColor);
    }

    private void fillRoundedRect(int x1, int y1, int x2, int y2, float radius, int color) {
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics g = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

        // Основная часть
        g.fill(x1 + (int) radius, y1, x2 - (int) radius, y2, color);
        g.fill(x1, y1 + (int) radius, x2, y2 - (int) radius, color);

        // Углы (упрощённо - квадраты)
        g.fill(x1 + (int) radius, y1, x2 - (int) radius, y1 + (int) radius, color);
        g.fill(x1 + (int) radius, y2 - (int) radius, x2 - (int) radius, y2, color);
    }

    private void drawIcon(GuiGraphics g, float x, float y, float size) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int x1 = (int) x;
        int y1 = (int) y;
        int s = (int) size;

        g.blit(RenderType::guiTextured, ICON_LOCATION, x1, y1, 0, 0, s, s, s, s);

        RenderSystem.disableBlend();
    }

    private String getKeyName(int keyCode) {
        if (keyCode <= 0) return "None";

        try {
            String name = InputConstants.getKey(keyCode, 0).getDisplayName().getString();
            return name.isEmpty() ? "Key " + keyCode : name;
        } catch (Exception e) {
            return "Key " + keyCode;
        }
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
