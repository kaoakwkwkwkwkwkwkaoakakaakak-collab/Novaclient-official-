package dev.novaclient.hud;

public final class ClickTracker {

    private static final int[] LEFT_HISTORY = new int[20];
    private static final int[] RIGHT_HISTORY = new int[20];
    private static int leftIndex;
    private static int rightIndex;

    private ClickTracker() {
    }

    public static void recordLeft(long millis) {
        LEFT_HISTORY[leftIndex % LEFT_HISTORY.length] = 1;
        leftIndex++;
        HudState.recordLeftClick();
    }

    public static void recordRight(long millis) {
        RIGHT_HISTORY[rightIndex % RIGHT_HISTORY.length] = 1;
        rightIndex++;
        HudState.recordRightClick();
    }

    public static int leftTotal() {
        return leftIndex;
    }

    public static int rightTotal() {
        return rightIndex;
    }
}
