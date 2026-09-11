package com.goodboysoul.pojavxoptimizer.module.culling;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;

public final class ParticleBudget {

    private final PjoConfig config;

    private ThermalGovernor.Pressure thermal = ThermalGovernor.Pressure.NONE;
    private int aliveCount;
    private long spawned;
    private long suppressed;

    public ParticleBudget(PjoConfig config) {
        this.config = config;
        if (Debug.isVerbose()) {
            PJO.debug("ParticleBudget initialised with limit {}", config.particleBudget());
        }
    }

    public void setThermal(ThermalGovernor.Pressure pressure) {
        this.thermal = pressure;
    }

    public void onSpawned() {
        spawned++;
        aliveCount++;
    }

    public void onRemoved() {
        aliveCount = Math.max(0, aliveCount - 1);
    }

    public boolean shouldSuppressSpawn() {
        if (!config.particleBudgeting()) {
            return false;
        }
        if (thermal.suppressParticles()) {
            suppressed++;
            return true;
        }
        int limit = thermalLimit();
        if (aliveCount >= limit) {
            suppressed++;
            return true;
        }
        return false;
    }

    public int aliveCount() {
        return aliveCount;
    }

    public int effectiveLimit() {
        return config.particleBudgeting() ? thermalLimit() : Integer.MAX_VALUE;
    }

    private int thermalLimit() {
        int base = config.particleBudget();
        return switch (thermal) {
            case SEVERE -> base / 8;
            case MODERATE -> base / 3;
            case MILD -> base * 2 / 3;
            case NONE -> base;
        };
    }

    public long spawned() {
        return spawned;
    }

    public long suppressed() {
        return suppressed;
    }

    public void reset() {
        aliveCount = 0;
        spawned = 0;
        suppressed = 0;
    }

    public String describe() {
        if (!config.particleBudgeting()) {
            return "particles=unlimited";
        }
        return "particles " + aliveCount + "/" + effectiveLimit()
                + " (suppressed " + suppressed + ")";
    }
}
