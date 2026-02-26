package win.hydra.client.hud;

import win.hydra.client.Client;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.MouseEvent;
import win.hydra.client.event.Render2DEvent;
import win.hydra.client.hud.IRenderer;
import win.hydra.client.screen.hud.impl.KeybindsRenderer;
import win.hydra.client.screen.hud.impl.TargetHud;
import win.hydra.client.screen.hud.impl.WatermarkRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер HUD элементов.
 */
public class HudManager {

    private static final HudManager INSTANCE = new HudManager();

    public static HudManager getInstance() {
        return INSTANCE;
    }

    private final List<IRenderer> renderers = new ArrayList<>();
    private final TargetHud targetHud = new TargetHud();

    private HudManager() {
        // Регистрируем HUD элементы
        renderers.add(new WatermarkRenderer());
        renderers.add(new KeybindsRenderer());
        renderers.add(targetHud);
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        for (IRenderer renderer : renderers) {
            renderer.render(event);
        }
    }

    @EventHandler
    public void onMouse(MouseEvent event) {
        for (IRenderer renderer : renderers) {
            if (renderer instanceof KeybindsRenderer) {
                ((KeybindsRenderer) renderer).onMouse(event);
            }
        }
    }

    public void register(IRenderer renderer) {
        renderers.add(renderer);
    }

    public void unregister(IRenderer renderer) {
        renderers.remove(renderer);
    }

    public List<IRenderer> getRenderers() {
        return renderers;
    }

    public TargetHud getTargetHud() {
        return targetHud;
    }
}
