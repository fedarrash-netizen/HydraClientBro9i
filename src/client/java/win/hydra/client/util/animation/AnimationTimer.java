package win.hydra.client.util.animation;

/**
 * Simple timer-based animation helper.
 */
public class AnimationTimer {

    private long startTime;
    private long duration;

    public AnimationTimer(long durationMillis) {
        this.duration = durationMillis;
        reset();
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
    }

    public float progress() {
        long now = System.currentTimeMillis();
        if (duration <= 0) return 1.0F;
        float t = (now - startTime) / (float) duration;
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        return t;
    }
}


