package win.hydra.client.util.render.gif;

import net.minecraft.resources.ResourceLocation;

/**
 * Placeholder GIF renderer.
 *
 * The original client most likely animates and draws GIF frames.
 * Here we only keep the resource location so the watermark code compiles.
 */
public class GifRender {

    private final ResourceLocation location;

    public GifRender(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation getLocation() {
        return location;
    }
}


