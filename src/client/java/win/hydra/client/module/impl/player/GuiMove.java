package win.hydra.client.module.impl.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.UpdateEvent;
import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.BooleanSetting;

/**
 * GuiMove - передвижение с открытым инвентарём.
 */
public class GuiMove extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    private final BooleanSetting moveInGui = new BooleanSetting("Движение в GUI", true);
    private final BooleanSetting jumpInGui = new BooleanSetting("Прыжки в GUI", true);
    private final BooleanSetting sneakInGui = new BooleanSetting("Приседание в GUI", true);

    public GuiMove() {
        super("GuiMove", Category.PLAYER);
        addSetting(moveInGui);
        addSetting(jumpInGui);
        addSetting(sneakInGui);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.level == null) return;

        if (moveInGui.get() && mc.screen != null) {
            handleMovement();
        }
    }

    private void handleMovement() {
        handleInventoryMovement();
    }

    private void handleInventoryMovement() {
        if (mc.player == null) return;

        float moveSpeed = 0.15F;
        double moveX = 0.0;
        double moveZ = 0.0;

        if (mc.options.keyUp.isDown()) moveZ = -moveSpeed;
        if (mc.options.keyDown.isDown()) moveZ = moveSpeed;
        if (mc.options.keyLeft.isDown()) moveX = -moveSpeed;
        if (mc.options.keyRight.isDown()) moveX = moveSpeed;

        if (moveX != 0.0 || moveZ != 0.0) {
            float yaw = mc.player.getYRot();
            double sin = Math.sin(Math.toRadians(yaw));
            double cos = Math.cos(Math.toRadians(yaw));
            
            double strafeX = moveX * cos - moveZ * sin;
            double strafeZ = moveX * sin + moveZ * cos;
            
            mc.player.setDeltaMovement(
                mc.player.getDeltaMovement().x + strafeX * 0.3,
                mc.player.getDeltaMovement().y,
                mc.player.getDeltaMovement().z + strafeZ * 0.3
            );
        }

        if (jumpInGui.get() && mc.options.keyJump.isDown()) {
            if (mc.player.onGround()) {
                mc.player.jumpFromGround();
            }
        }

        if (sneakInGui.get()) {
            if (mc.options.keyShift.isDown()) {
                mc.player.setShiftKeyDown(true);
            } else {
                mc.player.setShiftKeyDown(false);
            }
        }
    }

    public void dropAllItems() {
        if (mc.player == null) return;

        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                mc.player.drop(stack, false);
            }
        }
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        if (mc.player != null) {
            mc.player.setShiftKeyDown(false);
        }
    }
}
