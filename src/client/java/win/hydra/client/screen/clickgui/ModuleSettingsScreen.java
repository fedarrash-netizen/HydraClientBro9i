package win.hydra.client.screen.clickgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.BooleanSetting;
import win.hydra.client.module.setting.ModeSetting;
import win.hydra.client.module.setting.SliderSetting;
import win.hydra.client.util.text.FontUtil;

/**
 * Simple per-module settings screen opened from ClickGui (right click).
 */
public class ModuleSettingsScreen extends Screen {

    private final Screen parent;
    private final Module module;

    public ModuleSettingsScreen(Screen parent, Module module) {
        super(Component.literal(module.getName() + " Settings"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        this.renderBackground(g, mouseX, mouseY, delta);

        int boxWidth = 230;
        int boxHeight = 24 + module.getSettings().size() * 20 + 20;
        int x = this.width / 2 - boxWidth / 2;
        int y = this.height / 2 - boxHeight / 2;

        g.fill(x, y, x + boxWidth, y + boxHeight, 0xD0000000);

        FontUtil.drawCentered(g, module.getName(), this.width / 2.0F, y + 8, 0xFFFFFFFF, 1.0F);

        int lineY = y + 26;

        for (var setting : module.getSettings()) {
            if (setting instanceof BooleanSetting bool) {
                int boxX = x + 10;
                int boxY = lineY;
                int boxSize = 10;
                int boxColor = bool.get() ? 0xFF2E86FF : 0xFF202020;
                g.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, boxColor);
                FontUtil.drawString(g, bool.getName(), boxX + 14, boxY + 1, 0xFFFFFFFF, 0.9F, false);
                lineY += 18;
            } else if (setting instanceof ModeSetting mode) {
                String text = mode.getName() + ": " + mode.get();
                FontUtil.drawString(g, text, x + 10, lineY, 0xFFFFFFFF, 0.9F, false);
                lineY += 18;
            } else if (setting instanceof SliderSetting slider) {
                String text = slider.getName() + ": " + String.format("%.2f", slider.get());
                FontUtil.drawString(g, text, x + 10, lineY, 0xFFFFFFFF, 0.9F, false);
                int barX1 = x + 10;
                int barX2 = x + boxWidth - 10;
                int barY = lineY + 8;
                g.fill(barX1, barY, barX2, barY + 2, 0xFF202020);
                double t = (slider.get() - slider.getMin()) / (slider.getMax() - slider.getMin());
                int knobX = (int) (barX1 + t * (barX2 - barX1));
                g.fill(knobX - 2, barY - 1, knobX + 2, barY + 3, 0xFF2E86FF);
                lineY += 20;
            }
        }

        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int boxWidth = 230;
            int boxHeight = 24 + module.getSettings().size() * 20 + 20;
            int x = this.width / 2 - boxWidth / 2;
            int y = this.height / 2 - boxHeight / 2;

            int lineY = y + 26;

            for (var setting : module.getSettings()) {
                if (setting instanceof BooleanSetting bool) {
                    int boxX = x + 10;
                    int boxY = lineY;
                    int boxSize = 10;
                    if (mouseX >= boxX && mouseX <= boxX + boxSize &&
                            mouseY >= boxY && mouseY <= boxY + boxSize) {
                        bool.toggle();
                        return true;
                    }
                    lineY += 18;
                } else if (setting instanceof ModeSetting mode) {
                    int textY = lineY;
                    if (mouseY >= textY - 2 && mouseY <= textY + 10) {
                        mode.next();
                        return true;
                    }
                    lineY += 18;
                } else if (setting instanceof SliderSetting slider) {
                    int barX1 = x + 10;
                    int barX2 = x + boxWidth - 10;
                    int barY = lineY + 8;
                    if (mouseY >= barY - 4 && mouseY <= barY + 6 &&
                            mouseX >= barX1 && mouseX <= barX2) {
                        double t = (mouseX - barX1) / (double) (barX2 - barX1);
                        double value = slider.getMin() + t * (slider.getMax() - slider.getMin());
                        slider.set(value);
                        return true;
                    }
                    lineY += 20;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}


