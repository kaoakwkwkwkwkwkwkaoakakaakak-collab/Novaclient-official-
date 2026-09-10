package com.goodboysoul.pojavxoptimizer.util;

/**
 * Exponential moving average.
 *
 * <p>Cheaper and more responsive than a windowed mean for signals that drift slowly, which is
 * exactly what thermal pressure and sustained frame time are. {@code alpha} near 0 is sluggish and
 * smooth; near 1 is twitchy. The thermal governor deliberately uses a low alpha so that a single
 * dropped frame from a garbage collection cannot trigger a quality downgrade.
 */
public final class Ema {

    private final double alpha;
    private double value;
    private boolean seeded;

    public Ema(double alpha) {
        if (alpha <= 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("alpha must be in (0, 1], got " + alpha);
        }
        this.alpha = alpha;
    }

    public void push(double sample) {
        value = seeded ? value + alpha * (sample - value) : sample;
        seeded = true;
    }

    public double get() {
        return value;
    }

    public boolean isSeeded() {
        return seeded;
    }

    public void reset() {
        value = 0.0;
        seeded = false;
    }
}
