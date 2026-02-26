package win.hydra;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import win.hydra.client.Client;
import win.hydra.client.command.EzCommand;
import win.hydra.client.event.Render2DEvent;
import win.hydra.client.event.UpdateEvent;
import win.hydra.client.hud.HudManager;
import win.hydra.client.module.ModuleManager;
import win.hydra.client.module.impl.misc.SelfDestruct;
import win.hydra.client.screen.clickgui.Window;

public class HydraClient implements ClientModInitializer {
	private static KeyMapping CLICKGUI_KEY;
	private static SelfDestruct selfDestruct;

	@Override
	public void onInitializeClient() {
		Client.inst();

		// Регистрируем HUD менеджер
		Client.inst().eventBus().register(HudManager.getInstance());

		HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
			Client.inst().eventBus().post(new Render2DEvent(guiGraphics, 0.0F));
		});

		CLICKGUI_KEY = KeyBindingHelper.registerKeyBinding(
				new KeyMapping(
						"key.hydra.clickgui",
						InputConstants.Type.KEYSYM,
						GLFW.GLFW_KEY_RIGHT_SHIFT,
						"key.categories.hydra"
				)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			Client.inst().eventBus().post(new UpdateEvent());

			if (!(client.screen instanceof Window)) {
				ModuleManager.getInstance().handleBinds(client.getWindow().getWindow());
			}

			if (selfDestruct == null || !selfDestruct.shouldBlockClickGui()) {
				while (CLICKGUI_KEY.consumeClick()) {
					client.setScreen(new Window());
				}
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			EzCommand.register(dispatcher);
		});
	}

	public static SelfDestruct getSelfDestruct() {
		if (selfDestruct == null) {
			selfDestruct = (SelfDestruct) ModuleManager.getInstance()
					.getModuleByName("SelfDestruct");
		}
		return selfDestruct;
	}
}
