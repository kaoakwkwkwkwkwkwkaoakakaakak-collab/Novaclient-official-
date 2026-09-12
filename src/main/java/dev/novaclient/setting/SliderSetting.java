package dev.novaclient.setting;

public final class SliderSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;
    private final String suffix;

    public SliderSetting(String name, double defaultValue, double min, double max, double step) {
        this(name, defaultValue, min, max, step, "");
    }

    public SliderSetting(String name, double defaultValue, double min, double max, double step,
                         String suffix) {
        super(name, defaultValue);
        if (min > max) {
            throw new IllegalArgumentException("min " + min + " exceeds max " + max);
        }
        if (step <= 0) {
            throw new IllegalArgumentException("step must be positive");
        }
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix == null ? "" : suffix;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double step() {
        return step;
    }

    public double floatValue() {
        return get();
    }

    public int intValue() {
        return (int) Math.round(get());
    }

    public double fraction() {
        if (max == min) {
            return 0.0;
        }
        return (get() - min) / (max - min);
    }

    public void setFromFraction(double fraction) {
        set(min + fraction * (max - min));
    }

    public void increment() {
        set(get() + step);
    }

    public void decrement() {
        set(get() - step);
    }

    @Override
    protected Double clamp(Double value) {
        if (value == null || Double.isNaN(value)) {
            return min;
        }
        double snapped = Math.round(value / step) * step;
        if (snapped < min) {
            return min;
        }
        return Math.min(snapped, max);
    }

    @Override
    public String displayValue() {
        double value = get();
        String text = step >= 1.0
                ? String.valueOf((long) Math.round(value))
                : String.format("%.2f", value);
        return text + suffix;
    }

    @Override
    public String serialise() {
        return Double.toString(get());
    }

    @Override
    public boolean deserialise(String raw) {
        try {
            set(Double.parseDouble(raw.trim()));
            return true;
        } catch (NumberFormatException bad) {
            return false;
        }
    }
}
