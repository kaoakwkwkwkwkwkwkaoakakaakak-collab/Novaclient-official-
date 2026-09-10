package com.goodboysoul.pojavxoptimizer.util;

/**
 * Fixed-window arithmetic mean over a ring buffer.
 *
 * <p>Used where the signal is noisy and we care about the mean rather than the trend: frame times,
 * build latencies. Allocation-free after construction, which matters because these are sampled
 * every single frame on a device where GC pauses are visible as stutter.
 */
public final class MovingAverage {

    private final double[] samples;
    private int cursor;
    private int filled;
    private double sum;

    public MovingAverage(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1, got " + capacity);
        }
        this.samples = new double[capacity];
    }

    public void push(double value) {
        if (filled == samples.length) {
            sum -= samples[cursor];
        } else {
            filled++;
        }
        samples[cursor] = value;
        sum += value;
        cursor = (cursor + 1) % samples.length;
    }

    /** Mean of the samples recorded so far, or {@code 0} if none. */
    public double average() {
        return filled == 0 ? 0.0 : sum / filled;
    }

    /** True once the window has been filled at least once. */
    public boolean isWarm() {
        return filled == samples.length;
    }

    public int size() {
        return filled;
    }

    public void reset() {
        cursor = 0;
        filled = 0;
        sum = 0.0;
        for (int i = 0; i < samples.length; i++) {
            samples[i] = 0.0;
        }
    }
}
