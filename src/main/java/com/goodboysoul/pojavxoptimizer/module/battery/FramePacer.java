package com.goodboysoul.pojavxoptimizer.module.battery;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;

public final class FramePacer {

    private final PjoConfig config;

    private double lastX = Double.NaN;
    private double lastY = Double.NaN;
    private double lastZ = Double.NaN;
    private float lastYaw;
    private float lastPitch;
    private long lastMovementMillis;
    private long nowMillis;

    private int refreshRate = 60;
    private int effectiveCap;

    public FramePacer(PjoConfig config) {
        this.config = config;
    }

    public void setRefreshRate(int hertz) {
        if (hertz >= 20 && hertz <= 240) {
            this.refreshRate = hertz;
        }
    }

    public int refreshRate() {
        return refreshRate;
    }

    public int update(double x, double y, double z, float yaw, float pitch, boolean guiOpen,
                      ThermalGovernor.Pressure thermal) {
        nowMillis = System.currentTimeMillis();

        boolean moved = hasCameraMoved(x, y, z, yaw, pitch);
        if (moved) {
            lastMovementMillis = nowMillis;
        }
        lastX = x;
        lastY = y;
        lastZ = z;
        lastYaw = yaw;
        lastPitch = pitch;

        if (!config.batterySaver()) {
            effectiveCap = refreshRate;
            return effectiveCap;
        }

        int cap = refreshRate;

        if (guiOpen) {
            cap = Math.min(cap, config.guiFpsCap());
        } else if (isIdle()) {
            cap = Math.min(cap, config.idleFpsCap());
        }

        cap = Math.min(cap, thermal.suggestedFpsCap(refreshRate));

        effectiveCap = Math.max(5, cap);
        if (Debug.isVerbose() && (nowMillis & 0x3F) == 0) {
            PJO.debug("FramePacer: cap={} guiOpen={} idle={} thermal={}",
                    effectiveCap, guiOpen, isIdle(), thermal);
        }
        return effectiveCap;
    }

    public int effectiveCap() {
        return effectiveCap;
    }

    public boolean isIdle() {
        return nowMillis - lastMovementMillis > 1000L;
    }

    private boolean hasCameraMoved(double x, double y, double z, float yaw, float pitch) {
        if (Double.isNaN(lastX)) {
            return true;
        }

        double distanceSquared = (x - lastX) * (x - lastX)
                + (y - lastY) * (y - lastY)
                + (z - lastZ) * (z - lastZ);
        return distanceSquared > 0.000244
                || Math.abs(yaw - lastYaw) > 0.125f
                || Math.abs(pitch - lastPitch) > 0.125f;
    }

    public void reset() {
        lastX = Double.NaN;
        lastMovementMillis = nowMillis;
    }
}
