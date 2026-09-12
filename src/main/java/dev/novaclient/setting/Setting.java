package dev.novaclient.setting;

public abstract class Setting<T> {

    private final String name;
    private T value;
    private final T defaultValue;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = clamp(value);
    }

    public T defaultValue() {
        return defaultValue;
    }

    public void reset() {
        this.value = defaultValue;
    }

    protected abstract T clamp(T value);

    public abstract String displayValue();

    public abstract String serialise();

    public abstract boolean deserialise(String raw);
}
