package dev.novaclient.module;

import dev.novaclient.hud.HudElement;
import dev.novaclient.hud.HudState;
import dev.novaclient.setting.ModeSetting;
import dev.novaclient.setting.SliderSetting;
import dev.novaclient.setting.ToggleSetting;

final class HudModules {

    private HudModules() {
    }

    static void register(ModuleManager manager) {
        manager.registerInternal(hudModule("FPS Counter", "Draws the current frame rate.",
                HudElement.FPS, "BottomLeft"));

        manager.registerInternal(hudModule("CPS Counter", "Draws your clicks per second.",
                HudElement.CPS, "BottomLeft"));

        manager.registerInternal(hudModule("Ping", "Draws your latency to the server.",
                HudElement.PING, "BottomLeft"));

        manager.registerInternal(coordinates());
        manager.registerInternal(hudModule("Direction", "Draws the compass direction you face.",
                HudElement.DIRECTION, "BottomLeft"));
        manager.registerInternal(hudModule("Biome", "Draws the biome you are standing in.",
                HudElement.BIOME, "BottomLeft"));
        manager.registerInternal(keystrokes());
        manager.registerInternal(hudModule("Armor Status", "Draws your armour and its durability.",
                HudElement.ARMOR, "TopRight"));
        manager.registerInternal(hudModule("Potion Status", "Draws active effects and time left.",
                HudElement.POTIONS, "TopRight"));
        manager.registerInternal(hudModule("Clock", "Draws the world time.",
                HudElement.CLOCK, "TopRight"));
        manager.registerInternal(hudModule("Memory", "Draws heap usage.",
                HudElement.MEMORY, "BottomLeft"));
        manager.registerInternal(hudModule("Server IP", "Draws the address you are connected to.",
                HudElement.SERVER_IP, "TopRight"));
        manager.registerInternal(arraylist());
    }

    private static Module hudModule(String name, String description, HudElement element,
                                    String defaultAnchor) {
        ActiveModule module = new ActiveModule(name, description, Category.HUD) {
            @Override
            protected void onEnable() {
                HudState.show(element, true);
            }

            @Override
            protected void onDisable() {
                HudState.show(element, false);
            }
        };
        module.register(new ModeSetting("Position", defaultAnchor,
                "TopLeft", "TopRight", "BottomLeft", "BottomRight"));
        module.register(new SliderSetting("Scale", 1.0, 0.5, 2.0, 0.1, "x"));
        module.register(new ToggleSetting("Shadow", true));
        return module;
    }

    private static Module coordinates() {
        ActiveModule module = new ActiveModule("Coordinates", "Draws your X, Y and Z.",
                Category.HUD) {
            @Override
            protected void onEnable() {
                HudState.show(HudElement.COORDS, true);
            }

            @Override
            protected void onDisable() {
                HudState.show(HudElement.COORDS, false);
            }
        };
        module.register(new ModeSetting("Position", "TopLeft",
                "TopLeft", "TopRight", "BottomLeft", "BottomRight"));
        module.register(new SliderSetting("Scale", 1.0, 0.5, 2.0, 0.1, "x"));
        module.register(new ToggleSetting("Decimals", false));
        module.register(new ToggleSetting("Show Facing", true));
        module.register(new ToggleSetting("Shadow", true));
        return module;
    }

    private static Module keystrokes() {
        ActiveModule module = new ActiveModule("Keystrokes", "Draws the keys you are pressing.",
                Category.HUD) {
            @Override
            protected void onEnable() {
                HudState.show(HudElement.KEYSTROKES, true);
            }

            @Override
            protected void onDisable() {
                HudState.show(HudElement.KEYSTROKES, false);
            }
        };
        module.register(new ModeSetting("Position", "BottomLeft",
                "TopLeft", "TopRight", "BottomLeft", "BottomRight"));
        module.register(new SliderSetting("Scale", 1.0, 0.5, 2.0, 0.1, "x"));
        module.register(new ToggleSetting("Show Mouse", true));
        module.register(new ToggleSetting("Show CPS", true));
        module.register(new ModeSetting("Style", "Rounded", "Rounded", "Flat", "Outline"));
        return module;
    }

    private static Module arraylist() {
        ActiveModule module = new ActiveModule("Module List", "Draws the active modules on screen.",
                Category.HUD) {
            @Override
            protected void onEnable() {
                HudState.show(HudElement.ARRAYLIST, true);
            }

            @Override
            protected void onDisable() {
                HudState.show(HudElement.ARRAYLIST, false);
            }
        };
        module.register(new ModeSetting("Position", "TopRight",
                "TopLeft", "TopRight", "BottomLeft", "BottomRight"));
        module.register(new SliderSetting("Scale", 1.0, 0.5, 2.0, 0.1, "x"));
        module.register(new ModeSetting("Sort", "Length", "Length", "Alphabetical", "Category"));
        module.register(new ToggleSetting("Show Keybinds", false));
        module.register(new ToggleSetting("Shadow", true));
        return module;
    }
}
