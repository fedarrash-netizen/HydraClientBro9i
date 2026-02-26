package win.hydra.client.module.impl.hud;

import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.BooleanSetting;
import win.hydra.client.module.setting.ColorSetting;
import win.hydra.client.module.setting.SliderSetting;

/**
 * Модуль для управления HUD элементами (Watermark, Keybinds, TargetHud).
 */
public class Hud extends Module {

    private static Hud instance;

    public static Hud getInstance() {
        return instance;
    }

    // Настройки Watermark
    public final BooleanSetting watermarkEnabled = new BooleanSetting("Watermark", true);
    public final BooleanSetting wtTime = new BooleanSetting("Время", true);
    public final BooleanSetting wtFps = new BooleanSetting("Фпс", true);
    public final BooleanSetting wtUsername = new BooleanSetting("Юзер нейм", true);
    public final BooleanSetting wtCoords = new BooleanSetting("Кординаты", true);
    public final BooleanSetting wtPing = new BooleanSetting("Пинг", true);
    public final BooleanSetting wtTps = new BooleanSetting("Тпс", true);
    public final BooleanSetting wtBps = new BooleanSetting("Бпс", true);

    // Настройки Keybinds
    public final BooleanSetting keybindsEnabled = new BooleanSetting("Keybinds", true);
    public final SliderSetting keybindsX = new SliderSetting("Keybinds X", 0.0, 1000.0, 10.0, 1.0);
    public final SliderSetting keybindsY = new SliderSetting("Keybinds Y", 0.0, 1000.0, 100.0, 1.0);

    // Настройки TargetHud
    public final BooleanSetting targetHudEnabled = new BooleanSetting("TargetHud", true);
    public final SliderSetting targetHudY = new SliderSetting("TargetHud Y", 0.0, 1000.0, 35.0, 1.0);

    // Общие настройки
    public final ColorSetting themeColor = new ColorSetting("Цвет темы", 0xFF52A8FF);
    public final SliderSetting scale = new SliderSetting("Масштаб", 0.5, 2.0, 1.0, 0.1);

    public Hud() {
        super("Hud", Category.HUD);
        instance = this;

        addSetting(watermarkEnabled);
        addSetting(wtTime);
        addSetting(wtFps);
        addSetting(wtUsername);
        addSetting(wtCoords);
        addSetting(wtPing);
        addSetting(wtTps);
        addSetting(wtBps);

        addSetting(keybindsEnabled);
        addSetting(keybindsX);
        addSetting(keybindsY);

        addSetting(targetHudEnabled);
        addSetting(targetHudY);

        addSetting(themeColor);
        addSetting(scale);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
    }
}
