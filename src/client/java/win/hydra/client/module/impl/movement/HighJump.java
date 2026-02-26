package win.hydra.client.module.impl.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.UpdateEvent;
import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.BooleanSetting;
import win.hydra.client.module.setting.SliderSetting;

/**
 * HighJump - высокий прыжок с использованием шалкер-боксов.
 */
public class HighJump extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    // Настройки
    private final SliderSetting boostPower = new SliderSetting("Сила подброски", 0.5, 2.0, 1.0, 0.1);
    private final SliderSetting maxFallDistance = new SliderSetting("Макс. падение", 0.0, 5.0, 0.0, 0.1);
    private final BooleanSetting debug = new BooleanSetting("Отладка", false);

    public HighJump() {
        super("HighJump", Category.MOVEMENT);
        addSetting(boostPower);
        addSetting(maxFallDistance);
        addSetting(debug);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled()) return;
        if (mc.player == null || mc.level == null) return;

        // Проверяем что игрок падает
        if (!mc.player.onGround() && mc.player.getDeltaMovement().y < 0) {
            // Проходим по блокам вокруг игрока
            BlockPos playerPos = BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos pos = playerPos.offset(x, y, z);
                        BlockEntity blockEntity = mc.level.getBlockEntity(pos);
                        
                        if (blockEntity instanceof ShulkerBoxBlockEntity) {
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
                                    if (mc.player.fallDistance <= maxFallDistance.get()) {
                                        applyBoost();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void applyBoost() {
        mc.player.setDeltaMovement(
            mc.player.getDeltaMovement().x,
            boostPower.get(),
            mc.player.getDeltaMovement().z
        );
        
        if (debug.get()) {
            mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("HighJump: Boost!"),
                true
            );
        }
    }
}
