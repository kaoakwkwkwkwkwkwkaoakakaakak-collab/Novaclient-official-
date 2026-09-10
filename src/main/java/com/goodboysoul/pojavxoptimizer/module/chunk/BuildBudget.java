package com.goodboysoul.pojavxoptimizer.module.chunk;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.util.MovingAverage;

/**
 * Bounds how long chunk building may spend per frame.
 *
 * <p>Chunk compilation is the main source of stutter when moving fast, and on a phone the build
 * threads and the render thread are fighting for two to four cores. Giving builders an unbounded
 * time slice means a single complex section — lots of glass, lots of cutout models — can hold a core
 * for tens of milliseconds and produce a hitch.
 *
 * <p>The budget is expressed in milliseconds per frame rather than as a thread count, because the
 * cost varies enormously by content: the same thread count that sails through a plains biome stalls
 * on a redstone contraption. Measuring the actual build latency and reporting it lets the governor
 * and the debug overlay tell the difference between "not enough threads" and "this chunk is
 * expensive".
 */
public final class BuildBudget {

    private final PjoConfig config;
    private final MovingAverage buildLatency = new MovingAverage(64);

    private long frameStartNanos;
    private long accumulatedNanos;
    private int buildsThisFrame;
    private long totalBuilds;
    private long overBudgetFrames;

    public BuildBudget(PjoConfig config) {
        this.config = config;
    }

    /** Marks the start of a frame's build window. */
    public void beginFrame() {
        frameStartNanos = System.nanoTime();
        accumulatedNanos = 0L;
        buildsThisFrame = 0;
    }

    /**
     * Asks whether another build may start in this frame.
     *
     * @return true if the remaining budget is sufficient to start one more build
     */
    public boolean canStartBuild() {
        if (frameStartNanos == 0L) {
            return true;
        }
        double budgetNanos = config.buildTimeBudgetMs() * 1_000_000.0;
        return accumulatedNanos < budgetNanos;
    }

    /** Records a completed build of the given duration. */
    public void recordBuild(long durationNanos) {
        accumulatedNanos += durationNanos;
        buildsThisFrame++;
        totalBuilds++;
        buildLatency.push(durationNanos / 1_000_000.0);
    }

    /** Closes the frame and notes whether the budget was exceeded. */
    public void endFrame() {
        if (frameStartNanos != 0L && accumulatedNanos > config.buildTimeBudgetMs() * 1_000_000.0) {
            overBudgetFrames++;
        }
        if (Debug.isVerbose() && buildsThisFrame > 0 && (totalBuilds & 0xFF) == 0) {
            PJO.debug("BuildBudget: {} builds this frame, avg latency {} ms, over-budget frames {}",
                    buildsThisFrame, String.format("%.2f", buildLatency.average()), overBudgetFrames);
        }
    }

    public double averageBuildLatencyMs() {
        return buildLatency.average();
    }

    public int buildsThisFrame() {
        return buildsThisFrame;
    }

    public long totalBuilds() {
        return totalBuilds;
    }

    public long overBudgetFrames() {
        return overBudgetFrames;
    }

    /**
     * True when builds are routinely blowing the budget, which means the budget is too small for
     * this device rather than that the device is slow. Used to auto-relax the limit.
     */
    public boolean isChronicallyOverBudget() {
        return totalBuilds > 64 && overBudgetFrames * 4 > totalBuilds;
    }

    public String describe() {
        return "builds=" + buildsThisFrame
                + " avgLatency=" + String.format("%.1f", buildLatency.average()) + "ms";
    }
}
