package dev.novaclient.setting;

public final class ToggleSetting extends Setting<Boolean> {

    public ToggleSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    protected Boolean clamp(Boolean value) {
        return value == null ? Boolean.FALSE : value;
    }

    @Override
    public String displayValue() {
        return get() ? "On" : "Off";
    }

    @Override
    public String serialise() {
        return get() ? "true" : "false";
    }

    @Override
    public boolean deserialise(String raw) {
        if (!"true".equalsIgnoreCase(raw) && !"false".equalsIgnoreCase(raw)) {
            return false;
        }
        set(Boolean.parseBoolean(raw));
        return true;
    }
}
