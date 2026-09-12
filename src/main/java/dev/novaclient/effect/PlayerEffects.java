package dev.novaclient.effect;

import dev.novaclient.state.PlayerState;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

public final class PlayerEffects {

    private PlayerEffects() {
    }

    public static void autoSprint() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null || minecraft.screen != null) {
            return;
        }
        if (minecraft.player.input == null) {
            return;
        }
        boolean movingForward = minecraft.player.input.forwardImpulse > 0.0f;
        if (movingForward && !minecraft.player.isSprinting() && !minecraft.player.isShiftKeyDown()) {
            minecraft.player.setSprinting(true);
        }
    }

    public static void sneakToggle(boolean enabled) {
        OptionInstance<Boolean> option = OptionAccess.toggleCrouch();
        if (option != null) {
            if (enabled) {
                OptionAccess.override(option, Boolean.TRUE, "toggleCrouch");
            } else {
                OptionAccess.restore(option, "toggleCrouch");
            }
        }
        PlayerState.sneakToggle(enabled);
    }

    public static void noSlow(boolean enabled) {
        PlayerState.noSlow(enabled);
    }

    public static void autoTool(boolean enabled) {
        PlayerState.autoTool(enabled);
    }

    public static void perspectiveHold(boolean enabled) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return;
        }
        PlayerState.perspectiveHold(enabled);
        if (!enabled) {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    public static void placeDelay(int ticks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.rightClickDelay = Math.max(0, ticks);
        }
        PlayerState.placeDelay(ticks);
    }

    public static void reachDisplay(boolean enabled) {
        PlayerState.reachDisplay(enabled);
        dev.novaclient.hud.HudState.show(dev.novaclient.hud.HudElement.REACH, enabled);
    }

    public static void inventoryMove(boolean enabled) {
        PlayerState.inventoryMove(enabled);
    }
}
