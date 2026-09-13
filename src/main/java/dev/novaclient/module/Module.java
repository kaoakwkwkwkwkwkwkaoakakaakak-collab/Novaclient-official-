package dev.novaclient.module;

import dev.novaclient.setting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;

    private boolean enabled;
    private int keyCode = -1;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean expanded;

    private long enableCount;
    private long lastToggleMillis;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Category category() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        if (this.enabled == value) {
            return;
        }
        this.enabled = value;
        this.lastToggleMillis = System.currentTimeMillis();
        if (value) {
            this.enableCount++;
            try {
                onEnable();
            } catch (RuntimeException failure) {
                this.enabled = false;
                onEnableFailed(failure);
            }
        } else {
            try {
                onDisable();
            } catch (RuntimeException failure) {
                onDisableFailed(failure);
            }
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected void onEnableFailed(RuntimeException failure) {
    }

    protected void onDisableFailed(RuntimeException failure) {
    }

    public void onTick() {
    }

    public int keyCode() {
        return keyCode;
    }

    public void keyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public boolean hasKeybind() {
        return keyCode != -1;
    }

    protected <T extends Setting<?>> T register(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> settings() {
        return Collections.unmodifiableList(settings);
    }

    public boolean hasSettings() {
        return !settings.isEmpty();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded && hasSettings();
    }

    public long enableCount() {
        return enableCount;
    }

    public long lastToggleMillis() {
        return lastToggleMillis;
    }

    @Override
    public String toString() {
        return category.displayName() + "/" + name;
    }
}
