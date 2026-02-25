package win.hydra.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import win.hydra.client.module.impl.misc.SelfDestruct;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Команда для восстановления после SelfDestruct.
 */
public class EzCommand {

    private static final Minecraft MC = Minecraft.getInstance();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("ez")
            .executes(context -> {
                handleCommand("");
                return 1;
            })
            .then(argument("text", StringArgumentType.string())
                .executes(context -> {
                    String text = StringArgumentType.getString(context, "text");
                    handleCommand(text);
                    return 1;
                })
            )
        );
    }

    private static void handleCommand(String text) {
        if (MC.player != null) {
            SelfDestruct selfDestruct = getSelfDestruct();
            if (selfDestruct != null && selfDestruct.isDestroyed()) {
                selfDestruct.restore();
                sendMessage("§a[Hydra] Restored. Welcome back!");
            } else {
                sendMessage("§c[Hydra] SelfDestruct not active.");
            }
        }
    }

    private static SelfDestruct getSelfDestruct() {
        try {
            var mm = win.hydra.client.module.ModuleManager.getInstance();
            if (mm != null) {
                return (SelfDestruct) mm.getModuleByName("SelfDestruct");
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        return null;
    }

    private static void sendMessage(String text) {
        if (MC.player != null && MC.gui != null && MC.gui.getChat() != null) {
            MC.gui.getChat().addMessage(Component.literal(text));
        }
    }
}
