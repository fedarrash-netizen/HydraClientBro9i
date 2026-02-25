package win.hydra.client.module.setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(String name, String... modes) {
        super(name, modes.length > 0 ? modes[0] : "");
        this.modes = Arrays.asList(modes);
    }

    public List<String> getModes() {
        return modes;
    }

    public void next() {
        if (modes.isEmpty()) return;
        int idx = modes.indexOf(value);
        if (idx < 0) idx = 0;
        idx = (idx + 1) % modes.size();
        value = modes.get(idx);
    }
}


