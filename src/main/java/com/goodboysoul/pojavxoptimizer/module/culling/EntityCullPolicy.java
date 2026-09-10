package com.goodboysoul.pojavxoptimizer.module.culling;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;

/**
 * Decides whether an entity is worth drawing.
 *
 * <p>Vanilla culls entities against the frustum, which removes things off-screen but keeps
 * everything inside it — including a cow 60 blocks away occupying four pixels. On a desktop that
 * costs nothing measurable. On a phone, each of those entities means model matrix setup, vertex
 * submission, and draw calls pushed through a translation layer, and a mob farm or a village can put
 * hundreds of them on screen at once.
 *
 * <p>Two extra tests catch what the frustum cannot:
 * <ul>
 *   <li><b>Distance.</b> Beyond a threshold the entity cannot contribute anything visible.</li>
 *   <li><b>Projected size.</b> If the entity would cover fewer than a few pixels, drawing it is
 *       pure cost. This is the test that actually matters, because it scales correctly with field of
 *       view, resolution, and entity size instead of guessing from distance alone.</li>
 * </ul>
 *
 * <p>This class only decides. It holds no Minecraft references and touches no render state, which is
 * what lets the mixin that consults it stay three lines long and stay out of other mods' way.
 */
public final class EntityCullPolicy {

    private final PjoConfig config;

    private ThermalGovernor.Pressure thermal = ThermalGovernor.Pressure.NONE;
    private int culledCount;
    private int consideredCount;

    public EntityCullPolicy(PjoConfig config) {
        this.config = config;
    }

    public void setThermal(ThermalGovernor.Pressure pressure) {
        this.thermal = pressure;
    }

    /**
     * @param distanceSquared squared distance from the camera to the entity, in blocks
     * @param entityWidth     the entity's bounding-box width, in blocks
     * @param fovDegrees      current vertical field of view
     * @param viewportHeight  viewport height in pixels
     * @return true if the entity should be skipped
     */
    public boolean shouldCull(double distanceSquared, double entityWidth, double fovDegrees,
                              int viewportHeight) {
        if (!config.entityCulling()) {
            return false;
        }
        consideredCount++;

        double maxDistance = effectiveCullDistance();
        if (distanceSquared > maxDistance * maxDistance) {
            culledCount++;
            return true;
        }

        double minPixels = config.entityCullPixelSize();
        if (minPixels > 0.0 && viewportHeight > 0) {
            double distance = Math.sqrt(distanceSquared);
            if (distance > 1.0e-3) {
                // Projected height in pixels: (size / distance) scaled by the vertical fov.
                double tanHalfFov = Math.tan(Math.toRadians(fovDegrees) * 0.5);
                double projectedPixels = (entityWidth / (distance * tanHalfFov))
                        * (viewportHeight * 0.5);
                if (projectedPixels < minPixels) {
                    culledCount++;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Distance threshold after thermal adjustment. Under pressure the threshold tightens, which is a
     * cheap way to shed work without touching anything the player is looking at directly.
     */
    private double effectiveCullDistance() {
        double base = config.entityCullDistance();
        return switch (thermal) {
            case SEVERE -> base * 0.40;
            case MODERATE -> base * 0.60;
            case MILD -> base * 0.80;
            case NONE -> base;
        };
    }

    public int culledCount() {
        return culledCount;
    }

    public int consideredCount() {
        return consideredCount;
    }

    public void resetCounters() {
        culledCount = 0;
        consideredCount = 0;
    }

    public String describe() {
        return consideredCount == 0
                ? "entities=none considered"
                : "entities culled " + culledCount + "/" + consideredCount;
    }
}
