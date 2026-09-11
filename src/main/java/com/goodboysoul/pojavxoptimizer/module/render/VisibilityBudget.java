package com.goodboysoul.pojavxoptimizer.module.render;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;

public final class VisibilityBudget {

    private static final int RATCHET_STEP = 64;
    private static final long GROW_DELAY_MILLIS = 4000L;

    private final PjoConfig config;

    private int hardCap;
    private int currentCap;
    private int lastVisibleSections;
    private long lastShrinkMillis;
    private long lastGrowMillis;
    private boolean capEngaged;

    public VisibilityBudget(PjoConfig config) {
        this.config = config;
        this.hardCap = config.visibilitySectionCap();
        this.currentCap = hardCap;
    }

    public int update(int visibleSections, double frameMs, double targetFrameMs,
                      ThermalGovernor.Pressure thermal) {
        lastVisibleSections = visibleSections;
        long now = System.currentTimeMillis();

        int thermalCap = hardCap;
        switch (thermal) {
            case SEVERE -> thermalCap = hardCap / 2;
            case MODERATE -> thermalCap = hardCap * 3 / 4;
            case MILD -> thermalCap = hardCap * 7 / 8;
            default -> { }
        }
        thermalCap = Math.max(256, thermalCap);

        boolean overTarget = frameMs > targetFrameMs * 1.15;
        boolean underTarget = frameMs < targetFrameMs * 0.85;

        if (overTarget && now - lastShrinkMillis > 500L) {
            lastShrinkMillis = now;
            currentCap = Math.max(256, Math.min(currentCap - RATCHET_STEP, thermalCap));
            capEngaged = true;
        } else if (underTarget && currentCap < thermalCap && now - lastGrowMillis > GROW_DELAY_MILLIS) {
            lastGrowMillis = now;
            currentCap = Math.min(currentCap + RATCHET_STEP, thermalCap);
            capEngaged = currentCap < thermalCap;
        }

        if (Debug.isVerbose() && (now & 0xFF) == 0) {
            PJO.debug("VisibilityBudget: cap={} visible={} engaged={} frame={}ms target={}ms",
                    currentCap, visibleSections, capEngaged,
                    String.format("%.1f", frameMs), String.format("%.1f", targetFrameMs));
        }
        return currentCap;
    }

    public boolean admit(int rank) {
        return rank < currentCap;
    }

    public int currentCap() {
        return currentCap;
    }

    public int hardCap() {
        return hardCap;
    }

    public void hardCap(int value) {
        this.hardCap = Math.max(256, value);
        this.currentCap = Math.min(currentCap, hardCap);
    }

    public int lastVisibleSections() {
        return lastVisibleSections;
    }

    public boolean isCapEngaged() {
        return capEngaged;
    }

    public void reset() {
        currentCap = hardCap;
        capEngaged = false;
        lastShrinkMillis = 0L;
        lastGrowMillis = 0L;
    }
}
