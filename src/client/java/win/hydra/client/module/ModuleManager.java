package win.hydra.client.module;

import net.minecraft.client.Minecraft;
import win.hydra.client.Client;
import win.hydra.client.module.impl.combat.KillAura;
import win.hydra.client.module.impl.hud.ClickGuiSettings;
import win.hydra.client.module.impl.movement.Flight;
import win.hydra.client.screen.clickgui.Window;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds all modules.
 */
public class ModuleManager {

    private static final ModuleManager INSTANCE = new ModuleManager();

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    private final List<Module> modules = new ArrayList<>();
    private final Set<Module> pressedBinds = new HashSet<>();

    private ModuleManager() {
        // ClickGui module
        Module clickGui = new Module("ClickGui", Category.HUD) {
            @Override
            protected void onEnable() {
                Minecraft.getInstance().setScreen(new Window());
                // auto disable after open
                setEnabled(false);
            }
        };
        modules.add(clickGui);

        // ClickGui settings module (size, blur)
        ClickGuiSettings clickGuiSettings = new ClickGuiSettings();
        modules.add(clickGuiSettings);

        // Actual modules
        KillAura killAura = new KillAura();
        Flight flight = new Flight();

        modules.add(killAura);
        modules.add(flight);

        // Register modules that listen to events
        Client.inst().eventBus().register(killAura);
        Client.inst().eventBus().register(flight);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getModules(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public Module getModuleByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public void handleBinds(long windowHandle) {
        for (Module module : modules) {
            int key = module.getKey();
            if (key == GLFW.GLFW_KEY_UNKNOWN || key <= 0) {
                continue;
            }

            boolean down = GLFW.glfwGetKey(windowHandle, key) == GLFW.GLFW_PRESS;
            if (down) {
                if (!pressedBinds.contains(module)) {
                    module.toggle();
                    pressedBinds.add(module);
                }
            } else {
                pressedBinds.remove(module);
            }
        }
    }
}
