package dev.novaclient;

import dev.novaclient.config.ConfigManager;
import dev.novaclient.gui.ClickGuiScreen;
import dev.novaclient.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class NovaClient implements ClientModInitializer {

    public static final String NAME = "NovaClient";
    public static final String VERSION = "0.1.0";
    public static final int TOGGLE_KEY = 347;

    private static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    private static NovaClient instance;

    private final ModuleManager modules = new ModuleManager();
    private ConfigManager config;
    private Path configDir;
    private boolean ready;
    private boolean disabled;

    @Override
    public void onInitializeClient() {
        instance = this;
        try {
            Minecraft minecraft = Minecraft.getInstance();
            this.configDir = minecraft == null
                    ? Path.of("config", "novaclient")
                    : minecraft.gameDirectory.toPath().resolve("config").resolve("novaclient");

            modules.register();
            this.config = new ConfigManager(configDir, modules);
            config.load();

            ready = true;
            LOGGER.info("{} {} ready: {} modules across {} tabs",
                    NAME, VERSION, modules.all().size(), modules.categories().size());
        } catch (RuntimeException startupFailed) {
            disabled = true;
            LOGGER.error("{} failed to start and is now inert. The game will run normally.",
                    NAME, startupFailed);
        }
    }

    public static NovaClient get() {
        return instance;
    }

    public static boolean isReady() {
        return instance != null && instance.ready && !instance.disabled;
    }

    public ModuleManager modules() {
        return modules;
    }

    public ConfigManager config() {
        return config;
    }

    public Path configDir() {
        return configDir;
    }

    public void saveConfig() {
        if (config != null) {
            config.save();
        }
    }

    public void onTick() {
        if (!isReady()) {
            return;
        }
        try {
            modules.tickEnabled();
        } catch (RuntimeException tickFailed) {
            LOGGER.warn("A module failed during tick and was disabled.", tickFailed);
        }
    }

    public void openGui() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        minecraft.setScreen(new ClickGuiScreen(minecraft.screen));
    }

    public void onShutdown() {
        saveConfig();
    }

    public String statusLine() {
        if (!isReady()) {
            return NAME + ": inactive";
        }
        return NAME + " " + VERSION + " | " + modules.enabledCount() + " modules on";
    }
}
