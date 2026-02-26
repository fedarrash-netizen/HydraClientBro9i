package win.hydra.client.module.impl.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.UpdateEvent;
import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.ModeSetting;
import win.hydra.client.module.setting.SliderSetting;

/**
 * Flight с режимом Shulker для подброски от шалкеров.
 */
public class Flight extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    private final ModeSetting mode = new ModeSetting("Режим", "Normal", "Shulker");
    private final SliderSetting speed = new SliderSetting("Скорость", 0.1, 2.0, 0.5, 0.05);
    private final SliderSetting shulkerPower = new SliderSetting("Shulker Сила", 0.5, 3.0, 1.0, 0.1);
    private final SliderSetting shulkerMaxFall = new SliderSetting("Shulker Макс. падение", 0.0, 5.0, 0.0, 0.1);

    public Flight() {
        super("Flight", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speed);
        addSetting(shulkerPower);
        addSetting(shulkerMaxFall);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled()) return;
        if (mc.player == null) return;

        if (mode.get().equals("Shulker")) {
            handleShulkerMode();
        } else {
            handleNormalMode();
        }
    }

    private void handleShulkerMode() {
        boolean foundShulker = false;
        
        // Проходим по блокам вокруг игрока
        BlockPos playerPos = BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockEntity blockEntity = mc.level.getBlockEntity(pos);
                    
                    if (blockEntity instanceof ShulkerBoxBlockEntity shulkerBox) {
                        // Дистанция до шалкера по горизонтали
                        double horizontalDist = Math.sqrt(
                            Math.pow(mc.player.getX() - (pos.getX() + 0.5), 2) +
                            Math.pow(mc.player.getZ() - (pos.getZ() + 0.5), 2)
                        );
                        
                        // Дистанция по вертикали
                        double verticalDist = Math.abs(mc.player.getY() - (pos.getY() + 0.5));
                        
                        // Проверяем дистанцию
                        if (horizontalDist <= 1.0) {
                            // Проверяем вертикальную дистанцию (зависит от скорости падения)
                            double maxVerticalDist = Math.abs(mc.player.getDeltaMovement().y) > 1.0 ? 30.0 : 2.0;
                            
                            if (verticalDist <= maxVerticalDist) {
                                // Проверяем что игрок не падал долго
                                if (mc.player.fallDistance <= shulkerMaxFall.get()) {
                                    foundShulker = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (foundShulker) break;
            }
            if (foundShulker) break;
        }
        
        // Если шалкер рядом - летим вверх
        if (foundShulker) {
            mc.player.setDeltaMovement(
                0.0,
                shulkerPower.get(),
                0.0
            );
            mc.player.fallDistance = 0.0F;
        } else {
            // Иначе обычное горизонтальное перемещение
            handleHorizontalMovement();
        }
    }

    private void handleNormalMode() {
        handleHorizontalMovement();
    }

    private void handleHorizontalMovement() {
        // Basic WASD air movement
        float yaw = mc.player.getYRot();
        double rad = Math.toRadians(yaw + 90.0F);
        double inputX = 0.0;
        double inputZ = 0.0;

        if (mc.options.keyUp.isDown()) inputZ += 1.0;
        if (mc.options.keyDown.isDown()) inputZ -= 1.0;
        if (mc.options.keyLeft.isDown()) inputX += 1.0;
        if (mc.options.keyRight.isDown()) inputX -= 1.0;

        double length = Math.hypot(inputX, inputZ);
        if (length < 0.1) return;

        inputX /= length;
        inputZ /= length;

        double sp = speed.get();

        double motionX = (inputX * Math.cos(rad) - inputZ * Math.sin(rad)) * sp;
        double motionZ = (inputZ * Math.cos(rad) + inputX * Math.sin(rad)) * sp;

        double motionY = 0.0;
        if (mc.options.keyJump.isDown()) motionY += sp;
        if (mc.options.keyShift.isDown()) motionY -= sp;

        mc.player.setDeltaMovement(new Vec3(motionX, motionY, motionZ));
        mc.player.fallDistance = 0.0F;
    }
}


