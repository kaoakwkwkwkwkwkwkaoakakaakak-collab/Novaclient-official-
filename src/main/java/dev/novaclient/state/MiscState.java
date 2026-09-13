package dev.novaclient.state;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class MiscState {

    private static final java.util.concurrent.atomic.AtomicBoolean SILENT_SCREENSHOTS =
            new java.util.concurrent.atomic.AtomicBoolean();

    private static final java.util.concurrent.atomic.AtomicReference<String> LATEST_NOTIFICATION =
            new java.util.concurrent.atomic.AtomicReference<>();

    private static final AtomicBoolean WATERMARK = new AtomicBoolean();
    private static final AtomicReference<String> WATERMARK_POSITION = new AtomicReference<>("TopLeft");
    private static final AtomicBoolean NOTIFICATIONS = new AtomicBoolean();
    private static final AtomicInteger NOTIFICATION_SECONDS = new AtomicInteger(2);
    private static final AtomicBoolean RICH_PRESENCE = new AtomicBoolean();
    private static final AtomicBoolean SCREENSHOTS = new AtomicBoolean();
    private static final AtomicBoolean DEBUG_INFO = new AtomicBoolean();
    private static final AtomicInteger ANTI_AFK_ACTIONS = new AtomicInteger();

    private MiscState() {
    }

    public static void watermark(boolean enabled, String position) {
        WATERMARK.set(enabled);
        WATERMARK_POSITION.set(position == null ? "TopLeft" : position);
    }

    public static boolean watermark() {
        return WATERMARK.get();
    }

    public static String watermarkPosition() {
        return WATERMARK_POSITION.get();
    }

    public static void silentScreenshots(boolean enabled) {
        SILENT_SCREENSHOTS.set(enabled);
    }

    public static boolean silentScreenshots() {
        return SILENT_SCREENSHOTS.get();
    }

    public static String latestNotification() {
        return LATEST_NOTIFICATION.get();
    }

    public static void pushNotification(String message) {
        LATEST_NOTIFICATION.set(message);
    }

    public static void notifications(boolean enabled, float seconds) {
        NOTIFICATIONS.set(enabled);
        NOTIFICATION_SECONDS.set(Math.max(1, Math.round(seconds)));
    }

    public static boolean notifications() {
        return NOTIFICATIONS.get();
    }

    public static int notificationSeconds() {
        return NOTIFICATION_SECONDS.get();
    }

    public static void richPresence(boolean enabled) {
        RICH_PRESENCE.set(enabled);
    }

    public static boolean richPresence() {
        return RICH_PRESENCE.get();
    }

    public static void screenshots(boolean enabled) {
        SCREENSHOTS.set(enabled);
    }

    public static boolean screenshots() {
        return SCREENSHOTS.get();
    }

    public static void debugInfo(boolean enabled) {
        DEBUG_INFO.set(enabled);
    }

    public static boolean debugInfo() {
        return DEBUG_INFO.get();
    }

    public static void markAntiAfkAction() {
        ANTI_AFK_ACTIONS.incrementAndGet();
    }

    public static int antiAfkActions() {
        return ANTI_AFK_ACTIONS.get();
    }

    public static void reset() {
        WATERMARK.set(false);
        WATERMARK_POSITION.set("TopLeft");
        NOTIFICATIONS.set(false);
        NOTIFICATION_SECONDS.set(2);
        RICH_PRESENCE.set(false);
        SCREENSHOTS.set(false);
        DEBUG_INFO.set(false);
        ANTI_AFK_ACTIONS.set(0);
    }
}
