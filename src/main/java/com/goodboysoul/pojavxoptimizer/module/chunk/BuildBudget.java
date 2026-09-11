package com.goodboysoul.pojavxoptimizer.module.chunk;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.util.MovingAverage;

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

    public void beginFrame() {
        frameStartNanos = System.nanoTime();
        accumulatedNanos = 0L;
        buildsThisFrame = 0;
    }

    public boolean canStartBuild() {
        if (frameStartNanos == 0L) {
            return true;
        }
        double budgetNanos = config.buildTimeBudgetMs() * 1_000_000.0;
        return accumulatedNanos < budgetNanos;
    }

    public void recordBuild(long durationNanos) {
        accumulatedNanos += durationNanos;
        buildsThisFrame++;
        totalBuilds++;
        buildLatency.push(durationNanos / 1_000_000.0);
    }

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

    public boolean isChronicallyOverBudget() {
        return totalBuilds > 64 && overBudgetFrames * 4 > totalBuilds;
    }

    public String describe() {
        return "builds=" + buildsThisFrame
                + " avgLatency=" + String.format("%.1f", buildLatency.average()) + "ms";
    }
}
