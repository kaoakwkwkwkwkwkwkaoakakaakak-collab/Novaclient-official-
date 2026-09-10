package com.goodboysoul.pojavxoptimizer.module.battery;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;

/**
 * Chooses the frame rate cap for the current moment.
 *
 * <p>This is the cheapest large win available on a phone, and it is also the most misunderstood. On
 * a handset the GPU is the dominant power consumer, and its power draw tracks how often it is woken
 * up rather than how hard each wake is. Rendering at an uncapped 90 fps while the player stands in a
 * menu or waits at a spawn point therefore burns battery producing frames nobody is looking at.
 *
 * <p>The cap is chosen from three inputs, most restrictive wins:
 * <ol>
 *   <li>the display refresh rate, because frames beyond it are discarded anyway;</li>
 *   <li>idle state — the camera has not moved and no screen is open;</li>
 *   <li>thermal pressure, escalated by {@link ThermalGovernor}.</li>
 * </ol>
 *
 * <p>Idle detection keys on camera movement rather than on input events. Touch input is noisy:
 * a resting thumb resting on the look region produces a trickle of zero-delta events, so counting
 * events would read as "active" forever. Camera position and rotation do not have that problem.
 */
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

    /**
     * Called once per client tick with the camera state and whether a full-screen menu is open.
     *
     * @param x        camera x
     * @param y        camera y
     * @param z        camera z
     * @param yaw      camera yaw
     * @param pitch    camera pitch
     * @param guiOpen  true when a screen is covering the world
     * @param thermal  the current thermal pressure level
     * @return the cap to apply this tick
     */
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

    /**
     * True once the camera has been stationary for a full second. The delay exists so that a
     * momentary pause while aiming does not drop the frame rate mid-action.
     */
    public boolean isIdle() {
        return nowMillis - lastMovementMillis > 1000L;
    }

    private boolean hasCameraMoved(double x, double y, double z, float yaw, float pitch) {
        if (Double.isNaN(lastX)) {
            return true;
        }
        // 1/64th of a block and 1/8th of a degree: below these the change is not visible, and
        // treating them as movement would defeat idle detection on a jittery touch input.
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
