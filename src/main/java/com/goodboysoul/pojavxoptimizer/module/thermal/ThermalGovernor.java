package com.goodboysoul.pojavxoptimizer.module.thermal;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.util.Ema;

/**
 * Reduces work when the device is throttling, and recovers slowly once it stops.
 *
 * <p>A JVM process inside a launcher cannot read Android's thermal APIs; they are not exposed
 * through the Android SDK to ordinary apps in any reliable form, and certainly not to a mod running
 * inside a JVM. So the governor infers throttling from the only signal it genuinely has: sustained
 * frame-time degradation while the amount of work being requested stays constant.
 *
 * <p>The inference is deliberately slow and asymmetric. Throttling builds over seconds, so the
 * average is smoothed hard; a single dropped frame is almost always a garbage collection or a chunk
 * upload, not heat. Recovery is slower than the drop, because a governor that oscillates — dropping
 * quality, cooling, restoring quality, heating, dropping again — is more annoying to play on than
 * a steady moderate frame rate. The asymmetric timing is what prevents that oscillation.
 */
public final class ThermalGovernor {

    /** Escalating pressure levels, each with a concrete effect on the frame rate cap. */
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

        /** Cap to apply, as a fraction of the display refresh rate. */
        public int suggestedFpsCap(int refreshRate) {
            return Math.max(5, (int) Math.round(refreshRate * capFraction));
        }

        /** How much to reduce render distance at this level, in chunks. */
        public int renderDistanceReduction() {
            return switch (this) {
                case NONE -> 0;
                case MILD -> 1;
                case MODERATE -> 2;
                case SEVERE -> 4;
            };
        }

        /** Whether to suppress particles entirely at this level. */
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

    /**
     * Feeds one frame time in milliseconds.
     *
     * @param frameMs the measured frame time for the frame that just completed
     */
    public void recordFrame(double frameMs) {
        if (frameMs <= 0.0 || frameMs > 2000.0) {
            // Non-positive means the timer was unavailable; multi-second values mean the game was
            // paused, in a loading screen, or the process was suspended by Android. None of those
            // indicate heat, and all of them would poison the baseline.
            return;
        }
        smoothedFrameMs.push(frameMs);

        if (baselineFrameMs < 0.0 && smoothedFrameMs.isSeeded()) {
            baselineFrameMs = smoothedFrameMs.get();
        }
    }

    /**
     * Re-evaluates pressure. Called at most once per second; calling it more often gains nothing
     * because the underlying signal is smoothed over seconds anyway.
     */
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
            // Escalate quickly: staying hot is actively damaging the experience.
            if (now - lastEscalationMillis > 2000L) {
                lastEscalationMillis = now;
                pressure = target;
                PJO.info("Thermal pressure -> {} (frame time {} ms vs {} ms baseline)",
                        pressure.label(), Math.round(current), Math.round(baselineFrameMs));
            }
        } else if (target.ordinal() < pressure.ordinal()) {
            // De-escalate slowly. This delay is the whole reason the governor does not oscillate.
            long recoverMillis = config.thermalRecoverSeconds() * 1000L;
            if (now - lastDeescalationMillis > recoverMillis) {
                lastDeescalationMillis = now;
                pressure = Pressure.values()[Math.max(0, pressure.ordinal() - 1)];
                // The device has proven it can sustain a bit more, so lift the baseline with it.
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

    /** Called on world change, where frame times legitimately jump for reasons unrelated to heat. */
    public void reset() {
        baselineFrameMs = -1.0;
        smoothedFrameMs.reset();
        pressure = Pressure.NONE;
        lastEscalationMillis = 0L;
        lastDeescalationMillis = 0L;
    }
}
