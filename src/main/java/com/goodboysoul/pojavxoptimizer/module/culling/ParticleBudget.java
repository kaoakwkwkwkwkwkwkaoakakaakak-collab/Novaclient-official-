package com.goodboysoul.pojavxoptimizer.module.culling;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;

/**
 * Limits how many particles are alive at once.
 *
 * <p>Particles are the cheapest thing to spawn and among the most expensive to keep: each one is a
 * separate transformed quad, and rain, explosions, and potion effects can produce hundreds per
 * second. Worse, the count is unbounded in practice — a single rainstorm over a large flat area can
 * sustain a population that costs more to draw than the terrain does.
 *
 * <p>The budget is a hard population cap rather than a spawn-rate limit. Rate limiting sounds fairer
 * but produces visible pulsing as bursts get through and then starve; a population cap degrades
 * smoothly, dropping new particles while existing ones expire naturally.
 *
 * <p>Which particles get dropped is decided by dropping the newest, not the oldest. The oldest are
 * mid-animation and mid-fade, so removing them reads as particles vanishing; removing a newly
 * spawned one is far less noticeable because it has not established itself visually yet.
 */
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

    /** Called when a particle is actually added. */
    public void onSpawned() {
        spawned++;
        aliveCount++;
    }

    /** Called when a particle expires or is removed. */
    public void onRemoved() {
        aliveCount = Math.max(0, aliveCount - 1);
    }

    /**
     * @return true if spawning a new particle should be refused right now
     */
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

    /** The cap in force right now, or unbounded when budgeting is off. */
    public int effectiveLimit() {
        return config.particleBudgeting() ? thermalLimit() : Integer.MAX_VALUE;
    }

    /** The configured budget scaled by thermal pressure, ignoring the on/off switch. */
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
