package dev.novaclient.hud;

import java.util.EnumMap;
import java.util.Map;

public final class HudState {

    private static final Map<HudElement, Boolean> VISIBLE = new EnumMap<>(HudElement.class);
    private static final Map<HudElement, Anchor> ANCHORS = new EnumMap<>(HudElement.class);

    private static long leftClickAt;
    private static long rightClickAt;
    private static int leftClicksLastSecond;
    private static int rightClicksLastSecond;
    private static int leftClickCounter;
    private static int rightClickCounter;
    private static long leftWindowStart;
    private static long rightWindowStart;

    private HudState() {
    }

    public static void show(HudElement element, boolean visible) {
        VISIBLE.put(element, visible);
    }

    public static boolean isVisible(HudElement element) {
        return Boolean.TRUE.equals(VISIBLE.get(element));
    }

    public static void anchor(HudElement element, Anchor anchor) {
        ANCHORS.put(element, anchor);
    }

    public static Anchor anchor(HudElement element) {
        return ANCHORS.getOrDefault(element, Anchor.TOP_LEFT);
    }

    public static void recordLeftClick() {
        long now = System.currentTimeMillis();
        if (now - leftWindowStart > 1000L) {
            leftWindowStart = now;
            leftClicksLastSecond = leftClickCounter;
            leftClickCounter = 0;
        }
        leftClickCounter++;
        leftClickAt = now;
    }

    public static void recordRightClick() {
        long now = System.currentTimeMillis();
        if (now - rightWindowStart > 1000L) {
            rightWindowStart = now;
            rightClicksLastSecond = rightClickCounter;
            rightClickCounter = 0;
        }
        rightClickCounter++;
        rightClickAt = now;
    }

    public static int leftCps() {
        rollLeft();
        return leftClicksLastSecond;
    }

    public static int rightCps() {
        rollRight();
        return rightClicksLastSecond;
    }

    private static void rollLeft() {
        long now = System.currentTimeMillis();
        if (now - leftWindowStart > 1000L) {
            leftWindowStart = now;
            leftClicksLastSecond = leftClickCounter;
            leftClickCounter = 0;
        }
    }

    private static void rollRight() {
        long now = System.currentTimeMillis();
        if (now - rightWindowStart > 1000L) {
            rightWindowStart = now;
            rightClicksLastSecond = rightClickCounter;
            rightClickCounter = 0;
        }
    }

    public static long millisSinceLeftClick() {
        return leftClickAt == 0 ? Long.MAX_VALUE : System.currentTimeMillis() - leftClickAt;
    }

    public static long millisSinceRightClick() {
        return rightClickAt == 0 ? Long.MAX_VALUE : System.currentTimeMillis() - rightClickAt;
    }
}
