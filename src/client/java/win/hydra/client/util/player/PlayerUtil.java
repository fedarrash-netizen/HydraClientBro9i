package win.hydra.client.util.player;

import net.minecraft.client.Minecraft;

/**
 * Basic player-related helpers.
 */
public class PlayerUtil {

    private static final Minecraft MC = Minecraft.getInstance();

    /**
     * Placeholder ping helper. You can replace this with a real implementation
     * that queries the network handler when needed.
     */
    public static int getPing() {
        // Return a dummy ping for now to avoid dealing with network classes.
        return 0;
    }

    public static boolean isInGame() {
        return MC.player != null && MC.level != null;
    }
}


