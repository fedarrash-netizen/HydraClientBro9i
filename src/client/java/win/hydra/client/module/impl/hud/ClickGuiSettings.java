package win.hydra.client.module.impl.hud;

import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.BooleanSetting;
import win.hydra.client.module.setting.SliderSetting;

public class ClickGuiSettings extends Module {

    private static ClickGuiSettings INSTANCE;

    private final SliderSetting width = addSetting(new SliderSetting("Gui Width", 420.0, 900.0, 560.0, 10.0));
    private final SliderSetting height = addSetting(new SliderSetting("Gui Height", 280.0, 560.0, 350.0, 10.0));
    private final BooleanSetting blur = addSetting(new BooleanSetting("Blur", false));

    public ClickGuiSettings() {
        super("ClickGuiSettings", Category.HUD);
        INSTANCE = this;
    }

    public static ClickGuiSettings getInstance() {
        return INSTANCE;
    }

    public int guiWidth() {
        return width.get().intValue();
    }

    public int guiHeight() {
        return height.get().intValue();
    }

    public boolean blurEnabled() {
        return blur.get();
    }
}
