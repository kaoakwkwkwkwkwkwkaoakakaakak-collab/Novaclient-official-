package com.goodboysoul.pojavxoptimizer.module.thermal;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.util.Ema;

public final class ThermalGovernor {

    public enum Pressure {
        NONE("nominal", 1.00),
        MILD("warming", 0.85),
        MODERATE("hot", 0.70),
        SEVERE("throttling", 0.50);

        private final String label;
        private final double capFraction;

        Pressure(String label, double capFraction) {
            this.label = label;
            this.capFraction = capFraction;
        }

        public String label() {
            return label;
        }

        public int suggestedFpsCap(int refreshRate) {
            return Math.max(5, (int) Math.round(refreshRate * capFraction));
        }

        public int renderDistanceReduction() {
            return switch (this) {
                case NONE -> 0;
                case MILD -> 1;
                case MODERATE -> 2;
                case SEVERE -> 4;
            };
        }

        public boolean suppressParticles() {
            return this == SEVERE;
        }
    }

    private final PjoConfig config;
    private final Ema smoothedFrameMs = new Ema(0.05);

    private double baselineFrameMs = -1.0;
    private Pressure pressure = Pressure.NONE;
    private long lastEscalationMillis;
    private long lastDeescalationMillis;

    public ThermalGovernor(PjoConfig config) {
        this.config = config;
    }

    public void recordFrame(double frameMs) {
        if (frameMs <= 0.0 || frameMs > 2000.0) {

            return;
        }
        smoothedFrameMs.push(frameMs);

        if (baselineFrameMs < 0.0 && smoothedFrameMs.isSeeded()) {
            baselineFrameMs = smoothedFrameMs.get();
        }
    }

    public Pressure update() {
        if (!config.thermalGovernor() || baselineFrameMs <= 0.0) {
            return pressure = Pressure.NONE;
        }

        long now = System.currentTimeMillis();
        double current = smoothedFrameMs.get();
        double degradation = (current - baselineFrameMs) / baselineFrameMs;
        double threshold = config.thermalBackoffThreshold();

        Pressure target;
        if (degradation >= threshold * 2.0) {
            target = Pressure.SEVERE;
        } else if (degradation >= threshold * 1.4) {
            target = Pressure.MODERATE;
        } else if (degradation >= threshold) {
            target = Pressure.MILD;
        } else {
            target = Pressure.NONE;
        }

        if (target.ordinal() > pressure.ordinal()) {

            if (now - lastEscalationMillis > 2000L) {
                lastEscalationMillis = now;
                pressure = target;
                PJO.info("Thermal pressure -> {} (frame time {} ms vs {} ms baseline)",
                        pressure.label(), Math.round(current), Math.round(baselineFrameMs));
            }
        } else if (target.ordinal() < pressure.ordinal()) {

            long recoverMillis = config.thermalRecoverSeconds() * 1000L;
            if (now - lastDeescalationMillis > recoverMillis) {
                lastDeescalationMillis = now;
                pressure = Pressure.values()[Math.max(0, pressure.ordinal() - 1)];

                baselineFrameMs = baselineFrameMs * 0.9 + current * 0.1;
                PJO.info("Thermal pressure recovering -> {}", pressure.label());
            }
        } else {
            lastDeescalationMillis = now;
        }

        if (Debug.isVerbose()) {
            PJO.debug("Thermal: {} ms smoothed vs {} ms baseline ({}%), pressure={}",
                    Math.round(current), Math.round(baselineFrameMs),
                    Math.round(degradation * 100), pressure.label());
        }
        return pressure;
    }

    public Pressure pressure() {
        return pressure;
    }

    public double baselineFrameMs() {
        return baselineFrameMs;
    }

    public double smoothedFrameMs() {
        return smoothedFrameMs.get();
    }

    public void reset() {
        baselineFrameMs = -1.0;
        smoothedFrameMs.reset();
        pressure = Pressure.NONE;
        lastEscalationMillis = 0L;
        lastDeescalationMillis = 0L;
    }
}
