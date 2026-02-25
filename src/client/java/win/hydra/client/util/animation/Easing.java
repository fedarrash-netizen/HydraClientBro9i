package win.hydra.client.util.animation;

/**
 * Collection of easing functions for animations.
 */
public final class Easing {

    private Easing() {
    }

    public static float linear(float t) {
        return t;
    }

    public static float easeInQuad(float t) {
        return t * t;
    }

    public static float easeOutQuad(float t) {
        return t * (2 - t);
    }

    public static float easeInOutQuad(float t) {
        return t < 0.5F ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }
}


