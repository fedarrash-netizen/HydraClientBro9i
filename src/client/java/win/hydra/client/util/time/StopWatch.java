package win.hydra.client.util.time;

/**
 * Simple millisecond stopwatch used for attack delays, animations, etc.
 */
public class StopWatch {

    private long lastMS = System.currentTimeMillis();

    public boolean hasTimeElapsed(long delay) {
        return System.currentTimeMillis() - lastMS >= delay;
    }

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public void setLastMS(long ms) {
        this.lastMS = System.currentTimeMillis() - ms;
    }
}


