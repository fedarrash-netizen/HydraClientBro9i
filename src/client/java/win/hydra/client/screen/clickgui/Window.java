package win.hydra.client.screen.clickgui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.ModuleManager;
import win.hydra.client.module.impl.hud.ClickGuiSettings;
import win.hydra.client.module.setting.BooleanSetting;
import win.hydra.client.module.setting.ColorSetting;
import win.hydra.client.module.setting.ModeSetting;
import win.hydra.client.module.setting.ModuleSetting;
import win.hydra.client.module.setting.Setting;
import win.hydra.client.module.setting.SliderSetting;
import win.hydra.client.util.render.color.ColorUtil;
import win.hydra.client.util.text.FontUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Window extends Screen {

    private static final int DEFAULT_WINDOW_WIDTH = 560;
    private static final int DEFAULT_WINDOW_HEIGHT = 350;
    private static final int HEADER_HEIGHT = 26;
    private static final int SIDEBAR_WIDTH = 118;
    private static final int SEARCH_HEIGHT = 18;
    private static final int CATEGORY_ROW_HEIGHT = 19;
    private static final int MODULE_HEADER_HEIGHT = 22;
    private static final int MODULE_GAP = 6;
    private static final int ACCENT_COLOR = 0xFF52A8FF;

    private static final int[] ACCENT_PALETTE = {
            0xFF52A8FF,
            0xFF8BC8FF,
            0xFF5EE6B5,
            0xFFFFBF4D,
            0xFFFF6E8B
    };

    private final Minecraft mc = Minecraft.getInstance();
    private final Set<Module> expandedModules = new HashSet<>();

    private float openAnim = 0.0F;
    private float windowX;
    private float windowY;
    private boolean positioned = false;

    private boolean draggingWindow = false;
    private float dragOffsetX;
    private float dragOffsetY;

    private Category selectedCategory = Category.values().length > 0 ? Category.values()[0] : Category.HUD;
    private String searchQuery = "";
    private boolean searchFocused = false;

    private float scroll = 0.0F;
    private float targetScroll = 0.0F;

    private SliderSetting activeSlider = null;
    private float activeSliderLeft = 0.0F;
    private float activeSliderRight = 0.0F;
    private Module bindingModule = null;

    public Window() {
        super(Component.literal("Hydra"));
    }

    @Override
    protected void init() {
        int windowWidth = windowWidth();
        int windowHeight = windowHeight();

        if (!positioned) {
            windowX = (width - windowWidth) / 2.0F;
            windowY = (height - windowHeight) / 2.0F;
            positioned = true;
        } else {
            clampWindowToScreen();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        openAnim += (1.0F - openAnim) * 0.18F;
        if (openAnim > 0.999F) openAnim = 1.0F;

        scroll += (targetScroll - scroll) * 0.30F;
        clampScroll();
        clampWindowToScreen();

        if (isBlurEnabled()) {
            renderBlurredBackground();
        }
        renderBackdrop(g);
        renderWindow(g, mouseX, mouseY);
        super.render(g, mouseX, mouseY, delta);
    }

    private void renderBackdrop(GuiGraphics g) {
        int top = 0x660B0F16;
        int bottom = 0x3D06080F;
        g.fillGradient(0, 0, width, height, top, bottom);

        int glow = 28 + (int) (Math.sin(System.currentTimeMillis() * 0.002D) * 8.0D);
        int c1 = ColorUtil.multAlpha(0xFF2B76DF, openAnim * 0.12F);
        int c2 = ColorUtil.multAlpha(0xFF3FD8A2, openAnim * 0.10F);
        g.fill(glow, glow, glow + 180, glow + 110, c1);
        g.fill(width - glow - 180, height - glow - 110, width - glow, height - glow, c2);
    }

    private void renderWindow(GuiGraphics g, int mouseX, int mouseY) {
        int windowWidth = windowWidth();
        int windowHeight = windowHeight();
        int x = Math.round(windowX);
        int y = Math.round(windowY - (1.0F - openAnim) * 12.0F);
        int right = x + windowWidth;
        int bottom = y + windowHeight;

        int panel = ColorUtil.multAlpha(0xF211131A, openAnim);
        int border = ColorUtil.multAlpha(0xFF2C3445, openAnim);
        int header = ColorUtil.multAlpha(0xFF151A23, openAnim);

        g.fill(x - 1, y - 1, right + 1, bottom + 1, border);
        g.fill(x, y, right, bottom, panel);
        g.fill(x, y, right, y + HEADER_HEIGHT, header);

        FontUtil.drawString(g, "HYDRA CLIENT", x + 10, y + 8, 0xFFFFFFFF, 0.95F, false);
        FontUtil.drawString(g, "LMB: toggle | RMB: expand | MMB: bind | CTRL+F: search", right - 260, y + 9, 0xFFA6B0C2, 0.75F, false);

        renderSidebar(g, mouseX, mouseY, x, y);
        renderSearch(g, mouseX, mouseY, x, y);
        renderModules(g, mouseX, mouseY, x, y);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // ClickGUI draws its own opaque background in renderBackdrop.
    }

    @Override
    protected void renderBlurredBackground() {
        if (isBlurEnabled()) {
            super.renderBlurredBackground();
        }
    }

    private void renderSidebar(GuiGraphics g, int mouseX, int mouseY, int x, int y) {
        int windowHeight = windowHeight();
        int sidebarX1 = x;
        int sidebarX2 = x + SIDEBAR_WIDTH;
        int sidebarY1 = y + HEADER_HEIGHT;
        int sidebarY2 = y + windowHeight;

        g.fill(sidebarX1, sidebarY1, sidebarX2, sidebarY2, ColorUtil.multAlpha(0xFF10151E, openAnim));
        g.fill(sidebarX2 - 1, sidebarY1, sidebarX2, sidebarY2, ColorUtil.multAlpha(0xFF2B3448, openAnim));

        int rowY = sidebarY1 + 8;
        for (Category category : Category.values()) {
            int rowX1 = sidebarX1 + 7;
            int rowX2 = sidebarX2 - 7;
            int rowY2 = rowY + CATEGORY_ROW_HEIGHT;

            boolean selected = category == selectedCategory;
            boolean hovered = isInside(mouseX, mouseY, rowX1, rowY, rowX2, rowY2);
            int fill = selected ? 0xFF2A7FE2 : (hovered ? 0xFF1B2433 : 0xFF121824);
            int text = selected ? 0xFFFFFFFF : 0xFFB7C0D1;

            g.fill(rowX1, rowY, rowX2, rowY2, ColorUtil.multAlpha(fill, openAnim));
            FontUtil.drawString(g, category.getDisplayName(), rowX1 + 6, rowY + 6, text, 0.85F, false);

            int count = ModuleManager.getInstance().getModules(category).size();
            FontUtil.drawString(g, String.valueOf(count), rowX2 - 14, rowY + 6, selected ? 0xFFFFFFFF : 0xFF73809A, 0.75F, false);
            rowY += CATEGORY_ROW_HEIGHT + 4;
        }
    }

    private void renderSearch(GuiGraphics g, int mouseX, int mouseY, int x, int y) {
        int windowWidth = windowWidth();
        int contentX = x + SIDEBAR_WIDTH + 10;
        int searchX = contentX;
        int searchY = y + HEADER_HEIGHT + 9;
        int searchW = windowWidth - SIDEBAR_WIDTH - 20;
        int searchH = SEARCH_HEIGHT;

        boolean hovered = isInside(mouseX, mouseY, searchX, searchY, searchX + searchW, searchY + searchH);

        int border = searchFocused ? 0xFF4B9BFF : (hovered ? 0xFF32425C : 0xFF273142);
        int bg = 0xFF0E131C;
        g.fill(searchX - 1, searchY - 1, searchX + searchW + 1, searchY + searchH + 1, ColorUtil.multAlpha(border, openAnim));
        g.fill(searchX, searchY, searchX + searchW, searchY + searchH, ColorUtil.multAlpha(bg, openAnim));

        String shown = searchQuery;
        if (shown.isEmpty() && !searchFocused) {
            shown = "Search modules...";
        }
        if (searchFocused && ((System.currentTimeMillis() / 400L) % 2L == 0L)) {
            shown += "|";
        }

        int color = (searchFocused || !searchQuery.isEmpty()) ? 0xFFE8ECF4 : 0xFF6E7A8E;
        FontUtil.drawString(g, shown, searchX + 6, searchY + 5, color, 0.85F, false);
    }

    private void renderModules(GuiGraphics g, int mouseX, int mouseY, int x, int y) {
        int windowWidth = windowWidth();
        int windowHeight = windowHeight();
        int listX = x + SIDEBAR_WIDTH + 10;
        int listY = y + HEADER_HEIGHT + SEARCH_HEIGHT + 14;
        int listW = windowWidth - SIDEBAR_WIDTH - 20;
        int listH = windowHeight - HEADER_HEIGHT - SEARCH_HEIGHT - 22;
        int listBottom = listY + listH;

        g.fill(listX, listY, listX + listW, listBottom, ColorUtil.multAlpha(0xFF0C1119, openAnim));
        g.fill(listX + listW - 1, listY, listX + listW, listBottom, ColorUtil.multAlpha(0xFF202A3A, openAnim));

        List<Module> modules = getFilteredModules();
        if (modules.isEmpty()) {
            FontUtil.drawCentered(g, "No modules found", listX + (listW / 2.0F), listY + 15, 0xFF8B95A6, 0.9F);
            return;
        }

        float drawY = listY + 6.0F - scroll;
        g.enableScissor(listX, listY, listX + listW, listBottom);

        for (Module module : modules) {
            int cardHeight = getModuleCardHeight(module);
            float cardBottom = drawY + cardHeight;

            if (cardBottom < listY) {
                drawY += cardHeight + MODULE_GAP;
                continue;
            }
            if (drawY > listBottom) break;

            renderModuleCard(g, mouseX, mouseY, listX + 6, drawY, listW - 12, module);
            drawY += cardHeight + MODULE_GAP;
        }

        g.disableScissor();
    }

    private void renderModuleCard(GuiGraphics g, int mouseX, int mouseY, int x, float y, int w, Module module) {
        int yInt = Math.round(y);
        int headerY2 = yInt + MODULE_HEADER_HEIGHT;

        boolean headerHover = isInside(mouseX, mouseY, x, yInt, x + w, headerY2);
        boolean enabled = module.isEnabled();
        boolean expanded = expandedModules.contains(module) && !module.getSettings().isEmpty();

        int header = enabled ? 0xFF245CA2 : (headerHover ? 0xFF1A2433 : 0xFF141B27);
        int border = enabled ? 0xFF4FA2FF : 0xFF2D374B;

        g.fill(x - 1, yInt - 1, x + w + 1, yInt + getModuleCardHeight(module) + 1, ColorUtil.multAlpha(border, openAnim));
        g.fill(x, yInt, x + w, headerY2, ColorUtil.multAlpha(header, openAnim));

        FontUtil.drawString(g, module.getName(), x + 8, yInt + 7, 0xFFF3F6FC, 0.88F, false);

        String state = enabled ? "ON" : "OFF";
        int stateBg = enabled ? 0xFF45C989 : 0xFF384357;
        int stateW = 26;
        int stateX = x + w - stateW - 8;
        g.fill(stateX, yInt + 5, stateX + stateW, yInt + 17, ColorUtil.multAlpha(stateBg, openAnim));
        FontUtil.drawCentered(g, state, stateX + stateW / 2.0F, yInt + 8, 0xFFFFFFFF, 0.72F);

        String bindText = bindingModule == module ? "..." : getBindText(module.getKey());
        int bindColor = bindingModule == module ? 0xFFFFD36E : 0xFF8EA2C1;
        FontUtil.drawString(g, bindText, stateX - FontUtil.width(bindText) - 8, yInt + 8, bindColor, 0.72F, false);

        if (!expanded) {
            return;
        }

        float settingY = headerY2 + 4.0F;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof SliderSetting slider) {
                renderSliderSetting(g, x + 5, settingY, w - 10, slider);
                settingY += 24.0F;
            } else if (setting instanceof BooleanSetting bool) {
                renderBooleanSetting(g, x + 5, settingY, w - 10, bool);
                settingY += 17.0F;
            } else if (setting instanceof ModeSetting mode) {
                renderModeSetting(g, x + 5, settingY, w - 10, mode);
                settingY += 17.0F;
            } else if (setting instanceof ColorSetting color) {
                renderColorSetting(g, x + 5, settingY, w - 10, color);
                settingY += 17.0F;
            } else if (setting instanceof ModuleSetting moduleSetting) {
                renderModuleSetting(g, x + 5, settingY, w - 10, moduleSetting);
                settingY += 17.0F;
            }
        }
    }

    private void renderBooleanSetting(GuiGraphics g, int x, float y, int w, BooleanSetting setting) {
        int yInt = Math.round(y);
        g.fill(x, yInt, x + w, yInt + 14, ColorUtil.multAlpha(0xFF111827, openAnim));

        int swX = x + w - 24;
        int swColor = setting.get() ? 0xFF40C98B : 0xFF3A4355;
        g.fill(swX, yInt + 3, swX + 18, yInt + 11, ColorUtil.multAlpha(swColor, openAnim));

        int knobX = setting.get() ? swX + 11 : swX + 3;
        g.fill(knobX, yInt + 4, knobX + 4, yInt + 10, ColorUtil.multAlpha(0xFFF5F8FF, openAnim));

        FontUtil.drawString(g, setting.getName(), x + 5, yInt + 4, 0xFFCED7E8, 0.78F, false);
    }

    private void renderModeSetting(GuiGraphics g, int x, float y, int w, ModeSetting setting) {
        int yInt = Math.round(y);
        g.fill(x, yInt, x + w, yInt + 14, ColorUtil.multAlpha(0xFF111827, openAnim));
        FontUtil.drawString(g, setting.getName(), x + 5, yInt + 4, 0xFFCED7E8, 0.78F, false);
        FontUtil.drawString(g, setting.get(), x + w - FontUtil.width(setting.get()) - 8, yInt + 4, 0xFF6FC1FF, 0.78F, false);
    }

    private void renderColorSetting(GuiGraphics g, int x, float y, int w, ColorSetting setting) {
        int yInt = Math.round(y);
        g.fill(x, yInt, x + w, yInt + 14, ColorUtil.multAlpha(0xFF111827, openAnim));
        FontUtil.drawString(g, setting.getName(), x + 5, yInt + 4, 0xFFCED7E8, 0.78F, false);

        int color = setting.get() == null ? 0xFFFFFFFF : setting.get();
        int boxX = x + w - 20;
        g.fill(boxX, yInt + 3, boxX + 14, yInt + 11, ColorUtil.multAlpha(0xFF2D3647, openAnim));
        g.fill(boxX + 1, yInt + 4, boxX + 13, yInt + 10, ColorUtil.multAlpha(color, openAnim));
    }

    private void renderModuleSetting(GuiGraphics g, int x, float y, int w, ModuleSetting setting) {
        int yInt = Math.round(y);
        g.fill(x, yInt, x + w, yInt + 14, ColorUtil.multAlpha(0xFF111827, openAnim));

        Module linked = setting.get();
        String state = linked != null && linked.isEnabled() ? "ON" : "OFF";
        int stateColor = linked != null && linked.isEnabled() ? 0xFF40C98B : 0xFF8390A8;

        FontUtil.drawString(g, setting.getName(), x + 5, yInt + 4, 0xFFCED7E8, 0.78F, false);
        FontUtil.drawString(g, state, x + w - 20, yInt + 4, stateColor, 0.78F, false);
    }

    private void renderSliderSetting(GuiGraphics g, int x, float y, int w, SliderSetting setting) {
        int yInt = Math.round(y);
        g.fill(x, yInt, x + w, yInt + 20, ColorUtil.multAlpha(0xFF111827, openAnim));

        String value = formatSliderValue(setting);
        FontUtil.drawString(g, setting.getName(), x + 5, yInt + 3, 0xFFCED7E8, 0.78F, false);
        FontUtil.drawString(g, value, x + w - FontUtil.width(value) - 8, yInt + 3, 0xFF8ECEFF, 0.78F, false);

        int barX = x + 6;
        int barW = w - 12;
        int barY = yInt + 13;

        g.fill(barX, barY, barX + barW, barY + 3, ColorUtil.multAlpha(0xFF2A3345, openAnim));
        float pct = (float) ((setting.get() - setting.getMin()) / (setting.getMax() - setting.getMin()));
        pct = Mth.clamp(pct, 0.0F, 1.0F);

        int fill = (int) (barW * pct);
        g.fill(barX, barY, barX + fill, barY + 3, ColorUtil.multAlpha(ACCENT_COLOR, openAnim));

        int knobX = barX + fill;
        g.fill(knobX - 1, barY - 2, knobX + 1, barY + 5, ColorUtil.multAlpha(0xFFFFFFFF, openAnim));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (openAnim < 0.6F) {
            return true;
        }

        int windowWidth = windowWidth();
        int windowHeight = windowHeight();
        int x = Math.round(windowX);
        int y = Math.round(windowY);
        int right = x + windowWidth;
        int bottom = y + windowHeight;

        if (!isInside(mouseX, mouseY, x, y, right, bottom)) {
            searchFocused = false;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int searchX = x + SIDEBAR_WIDTH + 10;
        int searchY = y + HEADER_HEIGHT + 9;
        int searchW = windowWidth - SIDEBAR_WIDTH - 20;
        int searchH = SEARCH_HEIGHT;

        if (isInside(mouseX, mouseY, searchX, searchY, searchX + searchW, searchY + searchH)) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isInside(mouseX, mouseY, x, y, right, y + HEADER_HEIGHT)) {
            draggingWindow = true;
            dragOffsetX = (float) mouseX - windowX;
            dragOffsetY = (float) mouseY - windowY;
            return true;
        }

        if (handleCategoryClick(mouseX, mouseY)) {
            return true;
        }

        if (handleModuleClick(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingWindow && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            windowX = (float) mouseX - dragOffsetX;
            windowY = (float) mouseY - dragOffsetY;
            clampWindowToScreen();
            return true;
        }
        if (activeSlider != null) {
            updateActiveSlider(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            draggingWindow = false;
            activeSlider = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int windowWidth = windowWidth();
        int windowHeight = windowHeight();
        int x = Math.round(windowX);
        int y = Math.round(windowY);
        int listX = x + SIDEBAR_WIDTH + 10;
        int listY = y + HEADER_HEIGHT + SEARCH_HEIGHT + 14;
        int listW = windowWidth - SIDEBAR_WIDTH - 20;
        int listH = windowHeight - HEADER_HEIGHT - SEARCH_HEIGHT - 22;

        if (isInside(mouseX, mouseY, listX, listY, listX + listW, listY + listH)) {
            targetScroll -= (float) verticalAmount * 16.0F;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule = null;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindingModule.setKey(GLFW.GLFW_KEY_UNKNOWN);
                bindingModule = null;
                return true;
            }
            bindingModule.setKey(keyCode);
            bindingModule = null;
            return true;
        }

        if (hasControlDown() && keyCode == GLFW.GLFW_KEY_F) {
            searchFocused = true;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchFocused) {
                searchFocused = false;
                return true;
            }
            if (!searchQuery.isEmpty()) {
                searchQuery = "";
                targetScroll = 0.0F;
                return true;
            }
            mc.setScreen(null);
            return true;
        }

        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    targetScroll = 0.0F;
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                searchFocused = false;
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!searchFocused) {
            return super.charTyped(codePoint, modifiers);
        }
        if (Character.isISOControl(codePoint)) {
            return super.charTyped(codePoint, modifiers);
        }
        if (searchQuery.length() >= 32) {
            return true;
        }
        searchQuery += codePoint;
        targetScroll = 0.0F;
        return true;
    }

    private boolean handleCategoryClick(double mouseX, double mouseY) {
        int x = Math.round(windowX);
        int y = Math.round(windowY);

        int rowY = y + HEADER_HEIGHT + 8;
        for (Category category : Category.values()) {
            int rowX1 = x + 7;
            int rowX2 = x + SIDEBAR_WIDTH - 7;
            int rowY2 = rowY + CATEGORY_ROW_HEIGHT;

            if (isInside(mouseX, mouseY, rowX1, rowY, rowX2, rowY2)) {
                selectedCategory = category;
                targetScroll = 0.0F;
                scroll = 0.0F;
                return true;
            }
            rowY += CATEGORY_ROW_HEIGHT + 4;
        }
        return false;
    }

    private boolean handleModuleClick(double mouseX, double mouseY, int button) {
        int windowWidth = windowWidth();
        int windowHeight = windowHeight();
        int x = Math.round(windowX);
        int y = Math.round(windowY);
        int listX = x + SIDEBAR_WIDTH + 10;
        int listY = y + HEADER_HEIGHT + SEARCH_HEIGHT + 14;
        int listW = windowWidth - SIDEBAR_WIDTH - 20;
        int listH = windowHeight - HEADER_HEIGHT - SEARCH_HEIGHT - 22;
        int listBottom = listY + listH;

        if (!isInside(mouseX, mouseY, listX, listY, listX + listW, listBottom)) {
            return false;
        }

        List<Module> modules = getFilteredModules();
        float drawY = listY + 6.0F - scroll;

        for (Module module : modules) {
            int cardX = listX + 6;
            int cardW = listW - 12;
            int cardH = getModuleCardHeight(module);
            float cardBottom = drawY + cardH;

            if (cardBottom < listY) {
                drawY += cardH + MODULE_GAP;
                continue;
            }
            if (drawY > listBottom) break;

            int headerY = Math.round(drawY);
            int headerBottom = headerY + MODULE_HEADER_HEIGHT;

            if (isInside(mouseX, mouseY, cardX, headerY, cardX + cardW, headerBottom)) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    module.toggle();
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !module.getSettings().isEmpty()) {
                    if (!expandedModules.add(module)) {
                        expandedModules.remove(module);
                    }
                    clampScroll();
                } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                    bindingModule = module;
                    searchFocused = false;
                }
                return true;
            }

            if (expandedModules.contains(module) && !module.getSettings().isEmpty()) {
                float settingY = headerBottom + 4.0F;
                for (Setting<?> setting : module.getSettings()) {
                    int sx = cardX + 5;
                    int sw = cardW - 10;

                    if (setting instanceof SliderSetting slider) {
                        int sy = Math.round(settingY);
                        if (isInside(mouseX, mouseY, sx, sy, sx + sw, sy + 20)) {
                            int barX = sx + 6;
                            int barW = sw - 12;
                            int barY = sy + 13;

                            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isInside(mouseX, mouseY, barX, barY - 2, barX + barW, barY + 5)) {
                                activeSlider = slider;
                                activeSliderLeft = barX;
                                activeSliderRight = barX + barW;
                                updateActiveSlider(mouseX);
                                return true;
                            }
                        }
                        settingY += 24.0F;
                    } else if (setting instanceof BooleanSetting bool) {
                        int sy = Math.round(settingY);
                        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isInside(mouseX, mouseY, sx, sy, sx + sw, sy + 14)) {
                            bool.toggle();
                            return true;
                        }
                        settingY += 17.0F;
                    } else if (setting instanceof ModeSetting mode) {
                        int sy = Math.round(settingY);
                        if (isInside(mouseX, mouseY, sx, sy, sx + sw, sy + 14)) {
                            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                                mode.next();
                                return true;
                            }
                            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                                previousMode(mode);
                                return true;
                            }
                        }
                        settingY += 17.0F;
                    } else if (setting instanceof ColorSetting color) {
                        int sy = Math.round(settingY);
                        if (isInside(mouseX, mouseY, sx, sy, sx + sw, sy + 14)) {
                            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                                cycleColor(color, true);
                                return true;
                            }
                            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                                cycleColor(color, false);
                                return true;
                            }
                        }
                        settingY += 17.0F;
                    } else if (setting instanceof ModuleSetting moduleSetting) {
                        int sy = Math.round(settingY);
                        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isInside(mouseX, mouseY, sx, sy, sx + sw, sy + 14)) {
                            Module linked = moduleSetting.get();
                            if (linked != null) linked.toggle();
                            return true;
                        }
                        settingY += 17.0F;
                    }
                }
            }

            drawY += cardH + MODULE_GAP;
        }

        return false;
    }

    private List<Module> getFilteredModules() {
        List<Module> source = ModuleManager.getInstance().getModules(selectedCategory);
        String q = searchQuery == null ? "" : searchQuery.trim().toLowerCase();
        if (q.isEmpty()) {
            return source;
        }

        List<Module> filtered = new ArrayList<>();
        for (Module module : source) {
            if (module.getName().toLowerCase().contains(q)) {
                filtered.add(module);
            }
        }
        return filtered;
    }

    private int getModuleCardHeight(Module module) {
        int h = MODULE_HEADER_HEIGHT;
        if (!expandedModules.contains(module)) return h;

        List<Setting<?>> settings = module.getSettings();
        if (settings.isEmpty()) return h;

        h += 4;
        for (Setting<?> setting : settings) {
            if (setting instanceof SliderSetting) h += 24;
            else h += 17;
        }
        return h;
    }

    private void clampWindowToScreen() {
        int windowWidth = windowWidth();
        int windowHeight = windowHeight();
        windowX = Mth.clamp(windowX, 4.0F, Math.max(4.0F, width - windowWidth - 4.0F));
        windowY = Mth.clamp(windowY, 4.0F, Math.max(4.0F, height - windowHeight - 4.0F));
    }

    private void clampScroll() {
        float maxScroll = getMaxScroll();
        targetScroll = Mth.clamp(targetScroll, 0.0F, maxScroll);
        scroll = Mth.clamp(scroll, 0.0F, maxScroll);
    }

    private float getMaxScroll() {
        List<Module> modules = getFilteredModules();
        int contentHeight = 0;
        for (Module module : modules) {
            contentHeight += getModuleCardHeight(module) + MODULE_GAP;
        }
        if (contentHeight > 0) {
            contentHeight -= MODULE_GAP;
        }

        int viewHeight = windowHeight() - HEADER_HEIGHT - SEARCH_HEIGHT - 34;
        return Math.max(0.0F, contentHeight - viewHeight);
    }

    private void updateActiveSlider(double mouseX) {
        if (activeSlider == null) return;

        double pct = (mouseX - activeSliderLeft) / (activeSliderRight - activeSliderLeft);
        pct = Mth.clamp(pct, 0.0D, 1.0D);
        double value = activeSlider.getMin() + (activeSlider.getMax() - activeSlider.getMin()) * pct;
        activeSlider.set(value);
    }

    private void previousMode(ModeSetting mode) {
        List<String> modes = mode.getModes();
        if (modes.isEmpty()) return;
        int idx = modes.indexOf(mode.get());
        if (idx < 0) idx = 0;
        idx = (idx - 1 + modes.size()) % modes.size();
        mode.set(modes.get(idx));
    }

    private void cycleColor(ColorSetting setting, boolean forward) {
        int current = setting.get() == null ? ACCENT_PALETTE[0] : setting.get();
        int idx = 0;
        for (int i = 0; i < ACCENT_PALETTE.length; i++) {
            if (ACCENT_PALETTE[i] == current) {
                idx = i;
                break;
            }
        }

        idx = forward
                ? (idx + 1) % ACCENT_PALETTE.length
                : (idx - 1 + ACCENT_PALETTE.length) % ACCENT_PALETTE.length;

        setting.set(ACCENT_PALETTE[idx]);
    }

    private String formatSliderValue(SliderSetting setting) {
        double step = setting.getStep();
        if (step >= 1.0) {
            return String.valueOf((int) Math.round(setting.get()));
        }
        return String.format("%.2f", setting.get());
    }

    private int windowWidth() {
        ClickGuiSettings settings = ClickGuiSettings.getInstance();
        if (settings == null) {
            return DEFAULT_WINDOW_WIDTH;
        }
        return Mth.clamp(settings.guiWidth(), 420, Math.max(420, width - 8));
    }

    private int windowHeight() {
        ClickGuiSettings settings = ClickGuiSettings.getInstance();
        if (settings == null) {
            return DEFAULT_WINDOW_HEIGHT;
        }
        return Mth.clamp(settings.guiHeight(), 280, Math.max(280, height - 8));
    }

    private boolean isBlurEnabled() {
        ClickGuiSettings settings = ClickGuiSettings.getInstance();
        return settings != null && settings.blurEnabled();
    }

    private String getBindText(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode <= 0) {
            return "None";
        }
        String name = InputConstants.getKey(keyCode, 0).getDisplayName().getString();
        return name == null || name.isEmpty() ? "Key " + keyCode : name;
    }

    private boolean isInside(double mouseX, double mouseY, double x1, double y1, double x2, double y2) {
        return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
    }
}
