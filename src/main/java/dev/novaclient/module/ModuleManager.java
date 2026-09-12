package dev.novaclient.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {

    private final Map<String, Module> byName = new LinkedHashMap<>();
    private final Map<Category, List<Module>> byCategory = new EnumMap<>(Category.class);

    public void register() {
        for (Category category : Category.values()) {
            byCategory.put(category, new ArrayList<>());
        }
        registerAll();
    }

    private void add(Module module) {
        String key = module.name().toLowerCase();
        if (byName.containsKey(key)) {
            throw new IllegalStateException("duplicate module name: " + module.name());
        }
        byName.put(key, module);
        byCategory.get(module.category()).add(module);
    }

    private void registerAll() {
        PerformanceModules.register(this);
        RenderModules.register(this);
        VisualModules.register(this);
        HudModules.register(this);
        PlayerModules.register(this);
        MiscModules.register(this);
    }

    void registerInternal(Module module) {
        add(module);
    }

    public Module byName(String name) {
        return name == null ? null : byName.get(name.toLowerCase());
    }

    public List<Module> all() {
        return Collections.unmodifiableList(new ArrayList<>(byName.values()));
    }

    public List<Module> inCategory(Category category) {
        return Collections.unmodifiableList(byCategory.getOrDefault(category, List.of()));
    }

    public List<Category> categories() {
        return List.of(Category.tabs());
    }

    public List<Module> enabled() {
        List<Module> result = new ArrayList<>();
        for (Module module : byName.values()) {
            if (module.isEnabled()) {
                result.add(module);
            }
        }
        return result;
    }

    public int enabledCount() {
        int count = 0;
        for (Module module : byName.values()) {
            if (module.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    public void tickEnabled() {
        for (Module module : byName.values()) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void disableAll() {
        for (Module module : byName.values()) {
            if (module.isEnabled()) {
                module.setEnabled(false);
            }
        }
    }

    public List<Module> boundTo(int keyCode) {
        if (keyCode == -1) {
            return List.of();
        }
        List<Module> result = new ArrayList<>();
        for (Module module : byName.values()) {
            if (module.keyCode() == keyCode) {
                result.add(module);
            }
        }
        return result;
    }

    public List<Module> search(String query) {
        if (query == null || query.isBlank()) {
            return all();
        }
        String needle = query.trim().toLowerCase();
        List<Module> result = new ArrayList<>();
        for (Module module : byName.values()) {
            if (module.name().toLowerCase().contains(needle)
                    || module.description().toLowerCase().contains(needle)
                    || module.category().displayName().toLowerCase().contains(needle)) {
                result.add(module);
            }
        }
        return result;
    }
}
