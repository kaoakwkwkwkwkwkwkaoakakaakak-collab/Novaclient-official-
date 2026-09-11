package com.goodboysoul.pojavxoptimizer.util;

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

    public double average() {
        return filled == 0 ? 0.0 : sum / filled;
    }

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
