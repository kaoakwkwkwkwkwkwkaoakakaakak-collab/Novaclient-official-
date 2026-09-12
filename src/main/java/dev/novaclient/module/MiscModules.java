package dev.novaclient.module;

import dev.novaclient.effect.MiscEffects;
import dev.novaclient.setting.ModeSetting;
import dev.novaclient.setting.SliderSetting;
import dev.novaclient.setting.ToggleSetting;

final class MiscModules {

    private MiscModules() {
    }

    static void register(ModuleManager manager) {
        ActiveModule watermark = new ActiveModule("Watermark",
                "Draws the client name in the corner.", Category.MISC);
        ModeSetting corner = watermark.register(new ModeSetting("Position", "TopLeft",
                "TopLeft", "TopRight", "BottomLeft", "BottomRight"));
        watermark.register(new ToggleSetting("Show Version", true));
        watermark.effect(enabled -> MiscEffects.watermark(enabled, corner.get()));
        manager.registerInternal(watermark);

        ActiveModule notifications = new ActiveModule("Notifications",
                "Shows a toast when a module is toggled.", Category.MISC);
        SliderSetting seconds = notifications.register(new SliderSetting("Duration", 2.0, 0.5, 8.0, 0.5, "s"));
        notifications.effect(enabled -> MiscEffects.notifications(enabled, (float) seconds.floatValue()));
        manager.registerInternal(notifications);

        ActiveModule discord = new ActiveModule("Rich Presence",
                "Publishes your game state to Discord.", Category.MISC);
        discord.register(new ToggleSetting("Show Server", false));
        discord.register(new ToggleSetting("Show Health", false));
        discord.effect(enabled -> MiscEffects.richPresence(enabled));
        manager.registerInternal(discord);

        ActiveModule screenshots = new ActiveModule("Screenshot Manager",
                "Saves screenshots without the chat message.", Category.MISC);
        screenshots.register(new ToggleSetting("Copy To Clipboard", true));
        screenshots.effect(enabled -> MiscEffects.screenshots(enabled));
        manager.registerInternal(screenshots);

        ActiveModule profiles = new ActiveModule("Config Profiles",
                "Switches between saved module sets.", Category.MISC);
        ModeSetting profile = profiles.register(new ModeSetting("Profile", "Default",
                "Default", "Performance", "Visual", "Competitive"));
        profiles.effect(enabled -> MiscEffects.applyProfile(profile.get()));
        manager.registerInternal(profiles);

        ActiveModule antiAfk = new ActiveModule("Anti AFK",
                "Keeps you from being kicked for idling.", Category.MISC);
        SliderSetting interval = antiAfk.register(new SliderSetting("Interval", 60, 10, 300, 10, "s"));
        antiAfk.onTick(() -> MiscEffects.antiAfkTick(interval.intValue()));
        manager.registerInternal(antiAfk);

        ActiveModule debugInfo = new ActiveModule("Debug Info",
                "Shows renderer and timing details.", Category.MISC);
        debugInfo.register(new ToggleSetting("Frame Times", true));
        debugInfo.register(new ToggleSetting("Draw Calls", false));
        debugInfo.effect(enabled -> MiscEffects.debugInfo(enabled));
        manager.registerInternal(debugInfo);
    }
}
