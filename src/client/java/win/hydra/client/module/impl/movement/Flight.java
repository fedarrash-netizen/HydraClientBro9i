package win.hydra.client.module.impl.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.UpdateEvent;
import win.hydra.client.module.Category;
import win.hydra.client.module.Module;
import win.hydra.client.module.setting.SliderSetting;

/**
 * Very simple horizontal flight/air-walk for demonstration.
 */
public class Flight extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    private final SliderSetting speed = new SliderSetting("Скорость", 0.1, 2.0, 0.5, 0.05);

    public Flight() {
        super("Flight", Category.MOVEMENT);
        addSetting(speed);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled()) return;
        if (mc.player == null) return;

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


