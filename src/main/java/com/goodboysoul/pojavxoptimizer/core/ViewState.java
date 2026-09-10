package com.goodboysoul.pojavxoptimizer.core;

/**
 * The most recently observed camera state.
 *
 * <p>This exists as a plain class rather than as statics on a mixin because mixin classes do not
 * exist at runtime: their members are merged into the target class. A static declared on
 * {@code LevelRendererMixin} would end up living on {@code LevelRenderer}, which means it could not
 * be called as {@code LevelRendererMixin.something()} from anywhere.
 *
 * <p>The indirection is also what keeps the mixins trivial. {@code setupRender} receives the camera
 * as a parameter and is the one place it can be read in 1.21.1 without shadowing a private field;
 * everything else that needs the camera position reads it from here instead of injecting its own
 * hook.
 *
 * <p>Written on the render thread, read on the render thread. No synchronisation is needed, and the
 * values are plain doubles so a torn read is impossible in practice; a stale read by one frame is
 * harmless because every consumer treats this as a hint rather than as authoritative state.
 */
public final class ViewState {

    private static double cameraX;
    private static double cameraY;
    private static double cameraZ;
    private static double lookX;
    private static double lookY = 1.0;
    private static double lookZ;
    private static boolean valid;

    private ViewState() {
    }

    public static void update(double x, double y, double z,
                              double lx, double ly, double lz) {
        cameraX = x;
        cameraY = y;
        cameraZ = z;
        double length = Math.sqrt(lx * lx + ly * ly + lz * lz);
        if (length > 1.0e-6) {
            lookX = lx / length;
            lookY = ly / length;
            lookZ = lz / length;
        }
        valid = true;
    }

    /** False until {@code setupRender} has run at least once, i.e. before a world is drawn. */
    public static boolean isValid() {
        return valid;
    }

    public static double cameraX() { return cameraX; }

    public static double cameraY() { return cameraY; }

    public static double cameraZ() { return cameraZ; }

    public static double lookX() { return lookX; }

    public static double lookY() { return lookY; }

    public static double lookZ() { return lookZ; }

    /** Clears state on world change so a new world never inherits the previous camera. */
    public static void reset() {
        cameraX = 0.0;
        cameraY = 0.0;
        cameraZ = 0.0;
        lookX = 0.0;
        lookY = 1.0;
        lookZ = 0.0;
        valid = false;
    }
}
