package win.hydra.client.module.impl.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.UpdateEvent;
import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.BooleanSetting;
import win.hydra.client.module.setting.SliderSetting;
import win.hydra.client.util.time.StopWatch;

/**
 * AutoTotem - автоматическая установка тотема в оффхенд.
 */
public class AutoTotem extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    // Настройки
    private final SliderSetting healthThreshold = new SliderSetting("HP Порог", 1.0, 20.0, 6.0, 0.5);
    private final SliderSetting delay = new SliderSetting("Задержка (мс)", 0.0, 1000.0, 50.0, 10.0);
    private final BooleanSetting onlyDanger = new BooleanSetting("Только опасность", false);
    private final BooleanSetting alwaysTotem = new BooleanSetting("Всегда тотем", true);

    private final StopWatch timer = new StopWatch();
    private int lastTotemSlot = -1;

    public AutoTotem() {
        super("AutoTotem", Category.PLAYER);
        addSetting(healthThreshold);
        addSetting(delay);
        addSetting(onlyDanger);
        addSetting(alwaysTotem);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.level == null) return;

        // Проверяем есть ли уже тотем в оффхенде
        ItemStack offhandStack = mc.player.getOffhandItem();
        boolean hasTotem = isTotem(offhandStack);

        // Если тотем уже есть, проверяем нужно ли заменить
        if (hasTotem) {
            if (onlyDanger.get() && mc.player.getHealth() > healthThreshold.get()) {
                return;
            }
            if (!alwaysTotem.get() && mc.player.getHealth() > healthThreshold.get()) {
                return;
            }
        }

        // Если тотема нет или нужно заменить - ищем в хотбаре
        if (!hasTotem || (onlyDanger.get() && mc.player.getHealth() <= healthThreshold.get())) {
            if (timer.hasTimeElapsed(delay.get().longValue())) {
                int totemSlot = findTotemInHotbar();
                
                if (totemSlot != -1 && totemSlot != lastTotemSlot) {
                    moveTotemToOffhand(totemSlot);
                    lastTotemSlot = totemSlot;
                    timer.reset();
                }
            }
        }
    }

    /**
     * Проверяет является ли предмет тотемом.
     */
    private boolean isTotem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == Items.TOTEM_OF_UNDYING;
    }

    /**
     * Ищет тотем в хотбаре (слоты 0-8).
     */
    private int findTotemInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (isTotem(stack)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Перемещает тотем из хотбара в оффхенд.
     */
    private void moveTotemToOffhand(int slot) {
        if (mc.player == null) return;

        // Инвентарь игрока имеет id 0
        int inventoryId = mc.player.inventoryMenu.containerId;
        
        // Слоты хотбара: 36-44 (0-8 хотбара = 36-44 инвентаря)
        // Оффхенд: слот 45
        int hotbarSlot = slot + 36;
        int offhandSlot = 45;

        // Клик по тотему в хотбаре (swap с оффхендом)
        mc.gameMode.handleInventoryMouseClick(inventoryId, hotbarSlot, offhandSlot, net.minecraft.world.inventory.ClickType.SWAP, mc.player);
        
        lastTotemSlot = slot;
    }

    /**
     * Возвращает текущее HP игрока.
     */
    private float getCurrentHealth() {
        if (mc.player == null) return 0.0F;
        return mc.player.getHealth();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        lastTotemSlot = -1;
        timer.reset();
    }

    // Геттеры для настроек
    public float getHealthThreshold() {
        return healthThreshold.get().floatValue();
    }

    public long getDelay() {
        return delay.get().longValue();
    }

    public boolean isOnlyDanger() {
        return onlyDanger.get();
    }

    public boolean isAlwaysTotem() {
        return alwaysTotem.get();
    }
}
