package win.hydra.client.screen.hud.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.Render2DEvent;
import win.hydra.client.hud.IRenderer;
import win.hydra.client.managers.module.impl.render.InterFace;
import win.hydra.client.module.impl.combat.KillAura;
import win.hydra.client.module.impl.hud.Hud;
import win.hydra.client.util.animation.NumberTransition;
import win.hydra.client.util.render.color.ColorUtil;
import win.hydra.client.util.render.font.Fonts;

import java.awt.*;

/**
 * TargetHud - отображение информации о цели с улучшенной графикой и эффектом жидкого стекла.
 */
public class TargetHud implements IRenderer {

    private final Minecraft mc = Minecraft.getInstance();

    private float posX = 0;
    private float posY = 0;
    private float width = 160;
    private float height = 50;

    private LivingEntity lastTarget;
    private LivingEntity currentTarget;

    // Анимации
    private float healthAnimation = 0;
    private float trailAnimation = 0;
    private float absorptionAnimation = 0;
    private float displayedHealth = 0;
    private float scaleAnimation = 0;
    private float hurtAnimation = 0;

    private long lastUpdateTime = System.currentTimeMillis();
    private long lastTargetTime = System.currentTimeMillis();
    private long hurtStartTime = 0;

    @EventHandler
    @Override
    public void render(Render2DEvent e) {
        GuiGraphics graphics = e.getGraphics();

        // Проверяем включён ли TargetHud
        Hud hud = Hud.getInstance();
        if (hud != null && !hud.targetHudEnabled.get()) {
            return;
        }

        // Получаем цель из KillAura
        currentTarget = getTargetFromKillAura();

        // Обновляем последнюю цель
        if (currentTarget != null) {
            lastTarget = currentTarget;
            lastTargetTime = System.currentTimeMillis();
        }

        // Проверяем нужно ли показывать (цель была в последние 3 секунды)
        boolean shouldShow = System.currentTimeMillis() - lastTargetTime < 3000;

        // Обновляем анимацию появления
        float targetScale = shouldShow && lastTarget != null ? 1.0F : 0.0F;
        scaleAnimation = (float) NumberTransition.result(scaleAnimation, targetScale);

        if (scaleAnimation < 0.01F) return;

        // Позиция - по центру ниже прицела
        float screenWidth = mc.getWindow().getGuiScaledWidth();
        float screenHeight = mc.getWindow().getGuiScaledHeight();
        posX = (screenWidth - width) / 2.0F;

        // Используем настройку Y из Hud модуля
        if (hud != null) {
            posY = screenHeight / 2.0F + hud.targetHudY.get().floatValue();
        } else {
            posY = screenHeight / 2.0F + 35.0F;
        }

        // Обновляем время
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0F;
        deltaTime = Math.min(deltaTime, 0.1F);
        lastUpdateTime = currentTime;

        // Обновляем анимацию получения урона
        if (lastTarget != null && lastTarget.hurtTime > 0) {
            hurtStartTime = currentTime;
            hurtAnimation = 1.0F;
        } else if (hurtAnimation > 0) {
            hurtAnimation -= deltaTime * 1.5F;
            if (hurtAnimation < 0) hurtAnimation = 0;
        }

        // Рендер
        drawTargetHud(graphics, deltaTime);
    }

    private LivingEntity getTargetFromKillAura() {
        KillAura aura = KillAura.getInstance();
        if (aura != null && aura.isEnabled()) {
            return aura.getTarget();
        }
        return null;
    }

    private void drawTargetHud(GuiGraphics g, float deltaTime) {
        if (lastTarget == null) return;

        float alpha = scaleAnimation;

        // Жидкое стекло фон (blur эффект)
        drawFrostedGlassBackground(g, posX, posY, width, height, alpha);

        // Закруглённый фон с градиентом
        drawRoundedBackground(g, posX, posY, width, height, alpha);

        // Лицо с скином
        drawFace(g, posX + 8, posY + 8, 30, alpha); // Уменьшил размер с 34 до 30

        // Контент
        drawContent(g, posX, posY, deltaTime, alpha);
    }

    private void drawFrostedGlassBackground(GuiGraphics g, float x, float y, float w, float h, float alpha) {
        float radius = 12.0f;

        // Сохраняем текущую матрицу
        PoseStack poseStack = g.pose();
        poseStack.pushPose();

        // Создаём эффект жидкого стекла с помощью полупрозрачных слоёв
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Основной слой с размытием (имитация через градиент)
        int glassColor1 = ColorUtil.multAlpha(0x4D0A0A1A, alpha * 0.8f); // Полупрозрачный тёмный
        int glassColor2 = ColorUtil.multAlpha(0x4D15152A, alpha * 0.7f); // Светлее для градиента

        // Рисуем градиентный прямоугольник с закруглёнными углами
        drawRoundedGradientRect(g, x, y, w, h, radius, glassColor1, glassColor2);

        // Добавляем блики (эффект стекла)
        int highlightColor = ColorUtil.multAlpha(0x33FFFFFF, alpha * 0.6f);
        drawRoundedRect(g, x + 1, y + 1, w - 2, h / 3, radius / 2, highlightColor);

        // Добавляем обводку для эффекта глубины
        int borderColor = ColorUtil.multAlpha(0x66FFFFFF, alpha * 0.4f);
        drawRoundedOutline(g, x, y, w, h, radius, 1.5f, borderColor);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private void drawRoundedGradientRect(GuiGraphics g, float x, float y, float w, float h, float radius, int color1, int color2) {
        // Рисуем основной прямоугольник с закруглёнными углами
        drawRoundedRect(g, x, y, w, h, radius, color1);

        // Добавляем градиент сверху для эффекта глубины
        int steps = 8;
        for (int i = 0; i < steps; i++) {
            float alpha1 = 1.0f - (float)i / steps;
            float alpha2 = 1.0f - (float)(i + 1) / steps;
            int gradientColor1 = ColorUtil.multAlpha(color2, alpha1);
            int gradientColor2 = ColorUtil.multAlpha(color2, alpha2);

            float stepY = y + (i * h / steps);
            float stepHeight = h / steps;

            // Рисуем полоску градиента
            g.fill((int) (x + radius), (int) stepY, (int) (x + w - radius), (int) (stepY + stepHeight), gradientColor1);
        }
    }

    private void drawRoundedBackground(GuiGraphics g, float x, float y, float w, float h, float alpha) {
        int borderColor = ColorUtil.multAlpha(InterFace.getInstance().themeColor(), alpha * 0.7F);
        float radius = 10.0F;

        // Основная часть прямоугольника с более плавными краями
        g.fill((int) (x + radius), (int) y, (int) (x + w - radius), (int) (y + h),
                ColorUtil.multAlpha(0x4D0A0A0F, alpha));
        g.fill((int) x, (int) (y + radius), (int) (x + w), (int) (y + h - radius),
                ColorUtil.multAlpha(0x4D0A0A0F, alpha));

        // Закругления по углам с плавными краями
        drawSmoothCorner(g, x, y, radius, 0, 0, borderColor, alpha); // верх-лево
        drawSmoothCorner(g, x + w - radius, y, radius, 1, 0, borderColor, alpha); // верх-право
        drawSmoothCorner(g, x, y + h - radius, radius, 0, 1, borderColor, alpha); // низ-лево
        drawSmoothCorner(g, x + w - radius, y + h - radius, radius, 1, 1, borderColor, alpha); // низ-право

        // Границы с размытыми краями
        g.fill((int) (x + radius), (int) y, (int) (x + w - radius), (int) (y + 2.0F), borderColor);
        g.fill((int) (x + radius), (int) (y + h - 2.0F), (int) (x + w - radius), (int) (y + h), borderColor);

        // Добавляем тонкие боковые границы
        g.fill((int) x, (int) (y + radius), (int) (x + 2.0F), (int) (y + h - radius), borderColor);
        g.fill((int) (x + w - 2.0F), (int) (y + radius), (int) (x + w), (int) (y + h - radius), borderColor);
    }

    private void drawSmoothCorner(GuiGraphics g, float x, float y, float radius, int right, int bottom, int color, float alpha) {
        int steps = 16; // Увеличил количество шагов для более плавных краёв
        for (int i = 0; i < steps; i++) {
            float angle1 = (float) Math.toRadians(90.0F * i / steps);
            float angle2 = (float) Math.toRadians(90.0F * (i + 1) / steps);

            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);
            float cos2 = (float) Math.cos(angle2);
            float sin2 = (float) Math.sin(angle2);

            float x1 = x + (right == 1 ? radius - cos1 * radius : cos1 * radius);
            float y1 = y + (bottom == 1 ? radius - sin1 * radius : sin1 * radius);
            float x2 = x + (right == 1 ? radius - cos2 * radius : cos2 * radius);
            float y2 = y + (bottom == 1 ? radius - sin2 * radius : sin2 * radius);

            // Рисуем треугольник для плавного закругления
            fillTriangle(g, x1, y1, x2, y2,
                    x + (right == 1 ? radius : 0),
                    y + (bottom == 1 ? radius : 0),
                    ColorUtil.multAlpha(color, alpha * 0.7f));
        }
    }

    private void fillTriangle(GuiGraphics g, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        // Рисуем треугольник через заливку пикселей
        int minX = (int) Math.min(Math.min(x1, x2), x3);
        int maxX = (int) Math.max(Math.max(x1, x2), x3);
        int minY = (int) Math.min(Math.min(y1, y2), y3);
        int maxY = (int) Math.max(Math.max(y1, y2), y3);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (pointInTriangle(x, y, x1, y1, x2, y2, x3, y3)) {
                    g.fill(x, y, x + 1, y + 1, color);
                }
            }
        }
    }

    private boolean pointInTriangle(float px, float py, float x1, float y1, float x2, float y2, float x3, float y3) {
        float d1 = sign(px, py, x1, y1, x2, y2);
        float d2 = sign(px, py, x2, y2, x3, y3);
        float d3 = sign(px, py, x3, y3, x1, y1);

        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(hasNeg && hasPos);
    }

    private float sign(float px, float py, float x1, float y1, float x2, float y2) {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2);
    }

    private void drawRoundedOutline(GuiGraphics g, float x, float y, float w, float h, float radius, float thickness, int color) {
        // Рисуем внешнюю обводку
        for (float t = 0; t < thickness; t += 0.5f) {
            drawRoundedRect(g, x - t, y - t, w + t * 2, h + t * 2, radius + t, color);
        }
    }

    private void drawFace(GuiGraphics g, float x, float y, float size, float alpha) {
        if (lastTarget == null) return;

        float radius = 6.0F; // Увеличил радиус для более плавных краёв

        // Закруглённый фон под головой с эффектом свечения
        int faceBgColor = ColorUtil.multAlpha(0x80151A2A, alpha * 0.9f);
        drawRoundedRect(g, x - 2, y - 2, size + 4, size + 4, radius + 2, faceBgColor);

        // Добавляем обводку
        int outlineColor = ColorUtil.multAlpha(InterFace.getInstance().themeColor(), alpha * 0.8f);
        drawRoundedOutline(g, x - 1, y - 1, size + 2, size + 2, radius, 1.0f, outlineColor);

        // Проверяем получение урона
        float hurtFactor = hurtAnimation;

        // Рисуем голову из скина
        if (lastTarget instanceof Player) {
            // Голова игрока с настоящим скином
            drawPlayerHead(g, x, y, size, alpha, hurtFactor);
        } else {
            // Голова моба (упрощённо)
            drawMobHead(g, x, y, size, alpha, hurtFactor);
        }
    }

    private void drawPlayerHead(GuiGraphics g, float x, float y, float size, float alpha, float hurtFactor) {
        if (!(lastTarget instanceof AbstractClientPlayer)) return;

        AbstractClientPlayer player = (AbstractClientPlayer) lastTarget;

        // Получаем PlayerSkin и извлекаем текстуру
        var playerSkin = DefaultPlayerSkin.get(player.getUUID());
        ResourceLocation skinLocation = playerSkin.texture();

        // Сохраняем текущую матрицу
        PoseStack poseStack = g.pose();
        poseStack.pushPose();

        // Применяем трансформации для уменьшения размера и добавления блюра
        float centerX = x + size / 2;
        float centerY = y + size / 2;

        // Немного уменьшаем размер (skin scale)
        float skinScale = 0.95f;

        // Перемещаем к центру, уменьшаем, затем возвращаем
        poseStack.translate(centerX, centerY, 0);
        poseStack.scale(skinScale, skinScale, 1.0f);
        poseStack.translate(-centerX, -centerY, 0);

        // Включаем блендинг для блюра
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Применяем цвет с учётом урона
        float[] colorFactors;
        if (hurtFactor > 0.1F) {
            // Красный оттенок при уроне
            colorFactors = new float[]{1.0f, 1.0f - hurtFactor * 0.5f, 1.0f - hurtFactor * 0.5f, alpha};
        } else {
            colorFactors = new float[]{1.0f, 1.0f, 1.0f, alpha};
        }

        RenderSystem.setShaderColor(colorFactors[0], colorFactors[1], colorFactors[2], colorFactors[3]);
        RenderSystem.setShaderTexture(0, skinLocation);

        // Рисуем голову с закруглёнными углами
        float radius = 5.0f;
        drawTexturedRoundedRect(g, x, y, size, size, radius, skinLocation);

        // Добавляем эффект блюра через второй проход с прозрачностью
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha * 0.2f);
        drawTexturedRoundedRect(g, x - 1, y - 1, size + 2, size + 2, radius + 1, skinLocation);

        // Восстанавливаем цвет
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Добавляем блик на голову для эффекта стекла
        int highlightColor = ColorUtil.multAlpha(0x33FFFFFF, alpha * 0.3f);
        drawRoundedRect(g, x, y, size, size / 4, radius, highlightColor);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private void drawTexturedRoundedRect(GuiGraphics g, float x, float y, float w, float h, float radius, ResourceLocation texture) {
        // Рисуем текстурированный прямоугольник с закруглёнными углами
        // Используем GuiGraphics.blit с правильными UV координатами для лица

        // Основная часть текстуры
        g.blit(RenderType::guiTextured, texture,
                (int) (x + radius), (int) y,
                (int) (radius), 0,
                (int) (w - radius * 2), (int) h,
                64, 64);

        g.blit(RenderType::guiTextured, texture,
                (int) x, (int) (y + radius),
                0, (int) radius,
                (int) radius, (int) (h - radius * 2),
                64, 64);

        g.blit(RenderType::guiTextured, texture,
                (int) (x + w - radius), (int) (y + radius),
                (int) (64 - radius), (int) radius,
                (int) radius, (int) (h - radius * 2),
                64, 64);

        // Углы (текстура лица имеет 8x8 пикселей на лицо в скине)
        // Верхний левый угол
        g.blit(RenderType::guiTextured, texture,
                (int) x, (int) y,
                0, 0,
                (int) radius, (int) radius,
                64, 64);

        // Верхний правый угол
        g.blit(RenderType::guiTextured, texture,
                (int) (x + w - radius), (int) y,
                (int) (64 - radius), 0,
                (int) radius, (int) radius,
                64, 64);

        // Нижний левый угол
        g.blit(RenderType::guiTextured, texture,
                (int) x, (int) (y + h - radius),
                0, (int) (64 - radius),
                (int) radius, (int) radius,
                64, 64);

        // Нижний правый угол
        g.blit(RenderType::guiTextured, texture,
                (int) (x + w - radius), (int) (y + h - radius),
                (int) (64 - radius), (int) (64 - radius),
                (int) radius, (int) radius,
                64, 64);
    }

    private void drawMobHead(GuiGraphics g, float x, float y, float size, float alpha, float hurtFactor) {
        // Цвет зависит от типа моба
        int mobColor;
        if (hurtFactor > 0.1F) {
            int r = 255;
            int gr = (int) (100 * (1.0F - hurtFactor));
            int b = (int) (100 * (1.0F - hurtFactor));
            mobColor = new Color(r, gr, b, (int) (255 * alpha)).getRGB();
        } else {
            mobColor = ColorUtil.multAlpha(0xFF558055, alpha); // Зелёный для зомби
        }

        float radius = 5.0F; // Увеличил радиус для плавности
        drawRoundedRect(g, x, y, size, size, radius, mobColor);

        // Добавляем блик
        int highlightColor = ColorUtil.multAlpha(0x33FFFFFF, alpha * 0.3f);
        drawRoundedRect(g, x + 1, y + 1, size - 2, size / 4, radius / 2, highlightColor);

        // Глаза
        int eyeSize = (int) (size / 5);
        int eyeY = (int) (y + size / 2.5F);
        int eyeColor = hurtFactor > 0.3F ? 0xFFFF3030 : 0xFF00FF00;
        eyeColor = ColorUtil.multAlpha(eyeColor, alpha);

        // Глаза с закруглёнными углами
        drawRoundedRect(g, x + size / 4, eyeY, eyeSize, eyeSize, 1.5f, eyeColor);
        drawRoundedRect(g, x + size * 3 / 4 - eyeSize, eyeY, eyeSize, eyeSize, 1.5f, eyeColor);
    }

    private void drawRoundedRect(GuiGraphics g, float x, float y, float w, float h, float radius, int color) {
        // Основная часть
        g.fill((int) (x + radius), (int) y, (int) (x + w - radius), (int) (y + h), color);
        g.fill((int) x, (int) (y + radius), (int) (x + w), (int) (y + h - radius), color);

        // Углы с плавными краями
        for (int i = 0; i < 4; i++) {
            float angle = (float) Math.toRadians(i * 90);
            float cx = i % 2 == 0 ? x + radius : x + w - radius;
            float cy = i < 2 ? y + radius : y + h - radius;

            for (int j = 0; j < 8; j++) {
                float a1 = (float) Math.toRadians(j * 11.25);
                float a2 = (float) Math.toRadians((j + 1) * 11.25);

                float px1 = cx + (float) Math.cos(angle + a1) * radius;
                float py1 = cy + (float) Math.sin(angle + a1) * radius;
                float px2 = cx + (float) Math.cos(angle + a2) * radius;
                float py2 = cy + (float) Math.sin(angle + a2) * radius;

                // Рисуем линию между точками для плавного закругления
                drawLine(g, px1, py1, px2, py2, color);
            }
        }
    }

    private void drawLine(GuiGraphics g, float x1, float y1, float x2, float y2, int color) {
        float dx = Math.abs(x2 - x1);
        float dy = Math.abs(y2 - y1);
        float steps = Math.max(dx, dy);

        for (int i = 0; i <= steps; i++) {
            float t = steps == 0 ? 0 : i / steps;
            float x = x1 + t * (x2 - x1);
            float y = y1 + t * (y2 - y1);
            g.fill((int) x, (int) y, (int) (x + 1), (int) (y + 1), color);
        }
    }

    private void drawContent(GuiGraphics g, float x, float y, float deltaTime, float alpha) {
        float faceSize = 30; // Уменьшил с 34 до 30 для соответствия новому размеру головы
        float contentX = x + faceSize + 18;
        float nameY = y + 10;

        // Получаем здоровье
        float hp = lastTarget.getHealth();
        float maxHp = lastTarget.getMaxHealth();
        float absorp = lastTarget.getAbsorptionAmount();

        boolean isInvisible = lastTarget.isInvisible();

        // Обновляем анимацию здоровья
        float targetDisplayHealth = isInvisible ? maxHp : hp + absorp;
        displayedHealth = lerp(displayedHealth, targetDisplayHealth, deltaTime, 5.0F);
        float snappedHealth = snapToStep(displayedHealth, 0.25F);

        // Имя
        String name = lastTarget.getName().getString();
        Fonts.SFP_MEDIUM.draw(g, name, contentX, nameY, ColorUtil.multAlpha(-1, alpha), 0.9F);

        // HP текст (20/20)
        String hpText = String.format("%.1f / %.1f", snappedHealth, maxHp);
        float hpTextWidth = Fonts.SFP_MEDIUM.getWidth(hpText, 0.7F);
        Fonts.SFP_MEDIUM.draw(g, hpText, contentX, nameY + 12, ColorUtil.multAlpha(0xFFAAAAAA, alpha), 0.7F);

        // Полоска здоровья
        float barX = contentX;
        float barY = nameY + 24;
        float barWidth = width - faceSize - 28;
        float barHeight = 5;
        float barRadius = 3.0F; // Увеличил радиус для плавности

        // Фон полоски (закруглённый) с эффектом стекла
        drawRoundedRect(g, barX, barY, barWidth, barHeight, barRadius, ColorUtil.multAlpha(0x801E1E2E, alpha));

        // Анимация здоровья
        float targetHealth = isInvisible ? 1.0F : Mth.clamp(hp / maxHp, 0, 1);
        healthAnimation = lerp(healthAnimation, targetHealth, deltaTime, 3.0F);

        // Trail эффект
        if (targetHealth > trailAnimation) {
            trailAnimation = targetHealth;
        }
        trailAnimation = lerp(trailAnimation, targetHealth, deltaTime, 3.5F);

        // Trail с закруглёнными краями
        if (trailAnimation > healthAnimation) {
            drawRoundedRect(g, barX, barY, barWidth * trailAnimation, barHeight, barRadius,
                    ColorUtil.multAlpha(0xFF474757, alpha));
        }

        // Основное здоровье с градиентом
        if (healthAnimation > 0.01F) {
            int healthColor = InterFace.getInstance().themeColor();

            // Добавляем градиент для полоски здоровья
            int healthColorStart = ColorUtil.multAlpha(healthColor, alpha);
            int healthColorEnd = ColorUtil.multAlpha(ColorUtil.brighter(healthColor, 0.8f), alpha);

            drawGradientRoundedRect(g, barX, barY, barWidth * healthAnimation, barHeight, barRadius,
                    healthColorStart, healthColorEnd, true);
        }

        // Absorption (золотая полоска) с градиентом
        float targetAbsorption = isInvisible ? 0 : Mth.clamp(absorp / maxHp, 0, 1);
        absorptionAnimation = lerp(absorptionAnimation, targetAbsorption, deltaTime, 3.0F);

        if (absorptionAnimation > 0.01F) {
            int goldColorStart = ColorUtil.multAlpha(0xFFFFD700, alpha);
            int goldColorEnd = ColorUtil.multAlpha(0xFFFFA500, alpha);
            drawGradientRoundedRect(g, barX, barY, barWidth * absorptionAnimation, barHeight, barRadius,
                    goldColorStart, goldColorEnd, true);
        }
    }

    private void drawGradientRoundedRect(GuiGraphics g, float x, float y, float w, float h, float radius, int color1, int color2, boolean vertical) {
        int steps = 8;
        for (int i = 0; i < steps; i++) {
            float progress1 = (float) i / steps;
            float progress2 = (float) (i + 1) / steps;

            int blendedColor1 = blendColors(color1, color2, progress1);
            int blendedColor2 = blendColors(color1, color2, progress2);

            float rectY = y + (vertical ? i * h / steps : 0);
            float rectHeight = vertical ? h / steps : h;
            float rectX = x + (!vertical ? i * w / steps : 0);
            float rectWidth = !vertical ? w / steps : w;

            drawRoundedRect(g, rectX, rectY, rectWidth, rectHeight, radius, blendedColor1);
        }
    }

    private int blendColors(int color1, int color2, float progress) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * progress);
        int r = (int) (r1 + (r2 - r1) * progress);
        int g = (int) (g1 + (g2 - g1) * progress);
        int b = (int) (b1 + (b2 - b1) * progress);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private float lerp(float current, float target, float deltaTime, float speed) {
        float factor = (float) (1.0 - Math.pow(0.001, deltaTime * speed));
        return current + (target - current) * factor;
    }

    private float snapToStep(float value, float step) {
        return Math.round(value / step) * step;
    }

    // Геттеры
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