package win.hydra.client.util.render.color;

/**
 * Simple string color formatting helper.
 *
 * In the original client this likely prepends special formatting codes
 * to the string. For now we don't encode colors in the string itself,
 * so this just returns an empty prefix and rendering uses integer colors.
 */
public class ColorFormatting {

    public static String getColor(int rgb) {
        // You can implement custom inline color codes here if you need them.
        return "";
    }
}


