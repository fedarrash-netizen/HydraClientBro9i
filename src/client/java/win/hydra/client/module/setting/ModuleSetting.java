package win.hydra.client.module.setting;

import win.hydra.client.module.Module;

/**
 * Setting that references another module (for dependencies, etc).
 */
public class ModuleSetting extends Setting<Module> {

    public ModuleSetting(String name, Module module) {
        super(name, module);
    }
}


