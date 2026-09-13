package dev.novaclient.state;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class PlayerState {

    private static final AtomicBoolean SNEAK_TOGGLE = new AtomicBoolean();
    private static final AtomicBoolean NO_SLOW = new AtomicBoolean();
    private static final AtomicBoolean AUTO_TOOL = new AtomicBoolean();
    private static final AtomicBoolean PERSPECTIVE_HOLD = new AtomicBoolean();
    private static final AtomicInteger PLACE_DELAY = new AtomicInteger(4);
    private static final AtomicBoolean REACH_DISPLAY = new AtomicBoolean();
    private static final AtomicBoolean INVENTORY_MOVE = new AtomicBoolean();

    private PlayerState() {
    }

    public static void sneakToggle(boolean enabled) {
        SNEAK_TOGGLE.set(enabled);
    }

    public static boolean sneakToggle() {
        return SNEAK_TOGGLE.get();
    }

    public static void noSlow(boolean enabled) {
        NO_SLOW.set(enabled);
    }

    public static boolean noSlow() {
        return NO_SLOW.get();
    }

    public static void autoTool(boolean enabled) {
        AUTO_TOOL.set(enabled);
    }

    public static boolean autoTool() {
        return AUTO_TOOL.get();
    }

    public static void perspectiveHold(boolean enabled) {
        PERSPECTIVE_HOLD.set(enabled);
    }

    public static boolean perspectiveHold() {
        return PERSPECTIVE_HOLD.get();
    }

    public static void placeDelay(int ticks) {
        PLACE_DELAY.set(ticks);
    }

    public static int placeDelay() {
        return PLACE_DELAY.get();
    }

    public static void reachDisplay(boolean enabled) {
        REACH_DISPLAY.set(enabled);
    }

    public static boolean reachDisplay() {
        return REACH_DISPLAY.get();
    }

    public static void inventoryMove(boolean enabled) {
        INVENTORY_MOVE.set(enabled);
    }

    public static boolean inventoryMove() {
        return INVENTORY_MOVE.get();
    }

    public static void reset() {
        SNEAK_TOGGLE.set(false);
        NO_SLOW.set(false);
        AUTO_TOOL.set(false);
        PERSPECTIVE_HOLD.set(false);
        PLACE_DELAY.set(4);
        REACH_DISPLAY.set(false);
        INVENTORY_MOVE.set(false);
    }
}
