package dev.novaclient.setting;

import java.util.Arrays;
import java.util.List;

public final class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        if (modes.length == 0) {
            throw new IllegalArgumentException("a mode setting needs at least one mode");
        }
        this.modes = Arrays.asList(modes);
        if (!this.modes.contains(defaultValue)) {
            throw new IllegalArgumentException("default " + defaultValue + " is not one of the modes");
        }
    }

    public List<String> modes() {
        return modes;
    }

    public int index() {
        return modes.indexOf(get());
    }

    public boolean is(String mode) {
        return get().equalsIgnoreCase(mode);
    }

    public void cycle() {
        int next = index() + 1;
        if (next >= modes.size()) {
            next = 0;
        }
        set(modes.get(next));
    }

    public void cycleBack() {
        int previous = index() - 1;
        if (previous < 0) {
            previous = modes.size() - 1;
        }
        set(modes.get(previous));
    }

    @Override
    public void set(String value) {
        if (value == null) {
            return;
        }
        for (String mode : modes) {
            if (mode.equalsIgnoreCase(value.trim())) {
                super.set(mode);
                return;
            }
        }

    }

    @Override
    protected String clamp(String value) {
        if (value == null) {
            return modes.get(0);
        }
        for (String mode : modes) {
            if (mode.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        return modes.get(0);
    }

    @Override
    public String displayValue() {
        return get();
    }

    @Override
    public String serialise() {
        return get();
    }

    @Override
    public boolean deserialise(String raw) {
        if (raw == null) {
            return false;
        }
        String trimmed = raw.trim();
        for (String mode : modes) {
            if (mode.equalsIgnoreCase(trimmed)) {
                set(mode);
                return true;
            }
        }
        return false;
    }
}
