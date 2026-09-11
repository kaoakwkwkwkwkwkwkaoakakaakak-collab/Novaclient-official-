package com.goodboysoul.pojavxoptimizer.core;

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

    public static boolean isValid() {
        return valid;
    }

    public static double cameraX() { return cameraX; }

    public static double cameraY() { return cameraY; }

    public static double cameraZ() { return cameraZ; }

    public static double lookX() { return lookX; }

    public static double lookY() { return lookY; }

    public static double lookZ() { return lookZ; }

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
