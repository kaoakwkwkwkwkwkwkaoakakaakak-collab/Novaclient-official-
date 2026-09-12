package dev.novaclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.novaclient.module.Category;
import dev.novaclient.module.Module;
import dev.novaclient.module.ModuleManager;
import dev.novaclient.setting.Setting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigManager {

    private static final String FILE_NAME = "novaclient.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path directory;
    private final Path file;
    private final ModuleManager modules;

    private final Map<String, Boolean> profileDefaults = new HashMap<>();
    private String activeProfile = "Default";
    private boolean loaded;

    public ConfigManager(Path directory, ModuleManager modules) {
        this.directory = directory;
        this.file = directory.resolve(FILE_NAME);
        this.modules = modules;
    }

    public Path file() {
        return file;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String activeProfile() {
        return activeProfile;
    }

    public void load() {
        if (!Files.exists(file)) {
            applyProfileDefaults();
            save();
            return;
        }
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (root.has("modules")) {
                JsonObject saved = root.getAsJsonObject("modules");
                for (Module module : modules.all()) {
                    JsonElement element = saved.get(module.name());
                    if (!(element instanceof JsonObject object)) {
                        continue;
                    }
                    if (object.has("enabled")) {
                        module.setEnabled(object.get("enabled").getAsBoolean());
                    }
                    if (object.has("key")) {
                        module.keyCode(object.get("key").getAsInt());
                    }
                    if (object.has("settings") && object.get("settings").isJsonObject()) {
                        JsonObject settings = object.getAsJsonObject("settings");
                        for (Setting<?> setting : module.settings()) {
                            JsonElement value = settings.get(setting.name());
                            if (value != null && !value.isJsonNull()) {
                                setting.deserialise(value.getAsString());
                            }
                        }
                    }
                }
            }
            if (root.has("profile")) {
                activeProfile = root.get("profile").getAsString();
            }
            loaded = true;
        } catch (IOException | RuntimeException failed) {
            applyProfileDefaults();
            loaded = true;
        }
    }

    public void save() {
        try {
            if (directory != null) {
                Files.createDirectories(directory);
            }
            JsonObject root = new JsonObject();
            root.addProperty("profile", activeProfile);

            JsonObject saved = new JsonObject();
            for (Module module : modules.all()) {
                JsonObject object = new JsonObject();
                object.addProperty("enabled", module.isEnabled());
                object.addProperty("key", module.keyCode());

                JsonObject settings = new JsonObject();
                for (Setting<?> setting : module.settings()) {
                    settings.addProperty(setting.name(), setting.serialise());
                }
                object.add("settings", settings);
                saved.add(module.name(), object);
            }
            root.add("modules", saved);

            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException failed) {

        }
    }

    public void applyProfile(String name) {
        if (name == null) {
            return;
        }
        activeProfile = name;
        switch (name.toLowerCase()) {
            case "performance" -> applyPreset(true, false, false);
            case "visual" -> applyPreset(false, true, false);
            case "competitive" -> applyPreset(true, false, true);
            default -> applyProfileDefaults();
        }
        save();
    }

    private void applyPreset(boolean performance, boolean visual, boolean competitive) {
        for (Module module : modules.all()) {
            boolean enable;
            if (module.category() == Category.HUD) {
                enable = true;
            } else if (module.category() == Category.PERFORMANCE) {
                enable = performance || competitive;
            } else if (module.category() == Category.VISUAL) {
                enable = visual;
            } else if (module.category() == Category.RENDER) {
                enable = performance;
            } else {
                enable = false;
            }
            module.setEnabled(enable);
        }
    }

    private void applyProfileDefaults() {
        for (Module module : modules.all()) {
            Boolean wanted = profileDefaults.get(module.name());
            module.setEnabled(module.category() == Category.HUD || Boolean.TRUE.equals(wanted));
        }
    }

    public Map<String, Boolean> snapshot() {
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (Module module : modules.all()) {
            result.put(module.name(), module.isEnabled());
        }
        return result;
    }
}
