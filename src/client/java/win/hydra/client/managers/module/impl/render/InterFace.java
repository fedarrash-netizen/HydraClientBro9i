package win.hydra.client.managers.module.impl.render;

import net.minecraft.client.gui.GuiGraphics;
import win.hydra.client.util.render.color.ColorUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Very simplified version of InterFace module used by the watermark.
 */
public class InterFace {

    private static final InterFace INSTANCE = new InterFace();

    public static InterFace getInstance() {
        return INSTANCE;
    }

    public final StringSetting wtnameSF = new StringSetting("Hydra");
    public final StringSetting namewatermerk = new StringSetting("Player");

    public final ElementGroup wtElement = new ElementGroup();

    private final int themeColor = ColorUtil.getColor(180);
    private final int iconColor = ColorUtil.getColor(255);

    private InterFace() {
        // enable all watermark elements by default
        wtElement.setValue("Время", true);
        wtElement.setValue("Фпс", true);
        wtElement.setValue("Юзер найм", true);
        wtElement.setValue("Кординаты", true);
        wtElement.setValue("Пинг", true);
        wtElement.setValue("Тпс", true);
        wtElement.setValue("Бпс", true);
    }

    public int themeColor() {
        return themeColor;
    }

    public int iconColor() {
        return iconColor;
    }

    public void drawClientRect(GuiGraphics graphics, float x, float y, float width, float height,
                               float radius, float border) {
        int color = ColorUtil.multAlpha(0xFF000000, 0.6F);
        int ix1 = Math.round(x);
        int iy1 = Math.round(y);
        int ix2 = Math.round(x + width);
        int iy2 = Math.round(y + height);
        graphics.fill(ix1, iy1, ix2, iy2, color);
    }

    // --- helper types ---

    public static class StringSetting {
        private String value;

        public StringSetting(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public boolean is(String other) {
            return value != null && value.equalsIgnoreCase(other);
        }
    }

    public static class ElementGroup {
        private final Map<String, Boolean> map = new HashMap<>();

        public void setValue(String key, boolean value) {
            map.put(key, value);
        }

        public boolean getValue(String key) {
            return map.getOrDefault(key, false);
        }
    }
}


