package com.goodboysoul.pojavxoptimizer.module.culling;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;

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
