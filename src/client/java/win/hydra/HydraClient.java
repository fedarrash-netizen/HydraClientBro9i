package win.hydra;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import win.hydra.client.Client;
import win.hydra.client.event.Render2DEvent;
import win.hydra.client.event.UpdateEvent;
import win.hydra.client.module.ModuleManager;
import win.hydra.client.screen.clickgui.Window;

public class HydraClient implements ClientModInitializer {
	private static KeyMapping CLICKGUI_KEY;

	@Override
	public void onInitializeClient() {
		// Initialize our client singleton and hook HUD rendering into the event bus
		Client.inst(); // ensure static init

		HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
			// For now we don't use tick delta, so just pass 0f
			Client.inst().eventBus().post(new Render2DEvent(guiGraphics, 0.0F));
		});

		// ClickGui keybind (Right Shift)
		CLICKGUI_KEY = KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						"key.hydra.clickgui",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_RIGHT_SHIFT,
						"key.categories.hydra"
				)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Global per-tick update event
			Client.inst().eventBus().post(new UpdateEvent());

			// Module binds (skip while clickgui is open to avoid accidental toggles during typing/binding)
			if (!(client.screen instanceof Window)) {
				ModuleManager.getInstance().handleBinds(client.getWindow().getWindow());
			}

			// ClickGui keybind
			while (CLICKGUI_KEY.consumeClick()) {
				client.setScreen(new Window());
			}
		});
	}
}
