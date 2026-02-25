package win.hydra.client.util.animation;

public class NumberTransition {

    /**
     * Simple linear interpolation helper used to smooth values like FPS.
     */
    public static float result(float current, float target) {
        float speed = 0.2F;
        return current + (target - current) * speed;
    }
}


