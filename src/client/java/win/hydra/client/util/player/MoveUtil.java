package win.hydra.client.util.player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * Movement-related helpers (e.g. speed/BPS).
 */
public class MoveUtil {

    private static final Minecraft MC = Minecraft.getInstance();

    /**
     * Returns horizontal speed (XZ) in blocks per tick.
     */
    public static double speedSqrt() {
        if (MC.player == null) return 0.0D;
        Vec3 delta = MC.player.getDeltaMovement();
        return Math.sqrt(delta.x * delta.x + delta.z * delta.z);
    }
}


