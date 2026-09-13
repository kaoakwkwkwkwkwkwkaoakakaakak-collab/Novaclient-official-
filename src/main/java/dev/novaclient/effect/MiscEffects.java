package dev.novaclient.effect;

import dev.novaclient.NovaClient;
import dev.novaclient.state.MiscState;

public final class MiscEffects {

    private static final String TITLE_PREFIX = "Minecraft* 1.21.1 - NovaClient";

    private static long lastActionMillis;

    private MiscEffects() {
    }

    public static void watermark(boolean enabled, String position) {
        MiscState.watermark(enabled, position);
        dev.novaclient.hud.HudState.show(dev.novaclient.hud.HudElement.WATERMARK, enabled);
    }

    public static void notifications(boolean enabled, float seconds) {
        MiscState.notifications(enabled, seconds);
        dev.novaclient.hud.HudState.show(dev.novaclient.hud.HudElement.NOTIFICATIONS, enabled);
    }

    public static void richPresence(boolean enabled) {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft != null && minecraft.getWindow() != null) {
            minecraft.getWindow().setTitle(enabled ? TITLE_PREFIX : "Minecraft* 1.21.1");
        }
        MiscState.richPresence(enabled);
    }

    public static void screenshots(boolean enabled) {
        MiscState.silentScreenshots(enabled);
    }

    public static void applyProfile(String profile) {
        if (!NovaClient.isReady() || profile == null) {
            return;
        }
        NovaClient.get().config().applyProfile(profile);
    }

    public static void antiAfkTick(int intervalSeconds) {
        long now = System.currentTimeMillis();
        if (now - lastActionMillis < intervalSeconds * 1000L) {
            return;
        }
        lastActionMillis = now;
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null || minecraft.screen != null) {
            return;
        }
        minecraft.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        MiscState.markAntiAfkAction();
    }

    public static void debugInfo(boolean enabled) {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft != null && minecraft.gui != null && minecraft.gui.getDebugOverlay() != null) {
            minecraft.gui.getDebugOverlay().renderDebug = enabled;
        }
        MiscState.debugInfo(enabled);
    }
}
