package win.hydra.client.module.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.ModuleManager;
import win.hydra.client.module.setting.BooleanSetting;

/**
 * Self-destruct - полное отключение чита.
 * Отключает все модули и визуалы, блокирует открытие ClickGUI.
 * Для включения обратно нужно написать в чат /ez.
 */
public class SelfDestruct extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    private final BooleanSetting blockClickGui = new BooleanSetting("Блокировка GUI", true);
    private final BooleanSetting sendDisableMessage = new BooleanSetting("Сообщение в чат", false);

    private boolean isDestroyed = false;
    private boolean wasEnabled = false;

    public SelfDestruct() {
        super("SelfDestruct", Category.MISC);
        addSetting(blockClickGui);
        addSetting(sendDisableMessage);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        
        if (isDestroyed) {
            setEnabled(false);
            return;
        }

        wasEnabled = true;
        
        // Отключаем все модули
        disableAllModules();
        
        // Отправляем сообщение в чат
        if (sendDisableMessage.get()) {
            sendChatMessage("§c[Hydra] Self-destruct activated. Type '/ez' in chat to restore.");
        }
        
        isDestroyed = true;
        setEnabled(false);
    }

    /**
     * Отключает все модули.
     */
    private void disableAllModules() {
        if (ModuleManager.getInstance() == null) return;
        
        for (Module module : ModuleManager.getInstance().getModules()) {
            if (module != this && module.isEnabled()) {
                module.setEnabled(false);
            }
        }
    }

    /**
     * Проверяет, нужно ли открыть ClickGUI.
     */
    public boolean shouldBlockClickGui() {
        return blockClickGui.get() && isDestroyed;
    }

    /**
     * Восстанавливает чит (после команды /ez).
     */
    public void restore() {
        isDestroyed = false;
        wasEnabled = false;
        
        sendChatMessage("§a[Hydra] Restored. Welcome back!");
    }

    /**
     * Отправляет сообщение в чат.
     */
    private void sendChatMessage(String text) {
        if (mc.player != null && mc.getConnection() != null) {
            mc.gui.getChat().addMessage(Component.literal(text));
        }
    }

    /**
     * Проверяет, активен ли self-destruct.
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    protected void onDisable() {
        super.onDisable();
    }
}
