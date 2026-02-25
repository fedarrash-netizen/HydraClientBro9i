package win.hydra.client.module.setting;

public class SliderSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;

    public SliderSetting(String name, double min, double max, double defaultValue, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    @Override
    public void set(Double value) {
        double v = Math.max(min, Math.min(max, value));
        if (step > 0) {
            v = Math.round(v / step) * step;
        }
        super.set(v);
    }
}


