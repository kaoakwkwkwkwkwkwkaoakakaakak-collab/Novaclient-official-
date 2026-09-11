package com.goodboysoul.pojavxoptimizer.module.render;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;
import com.goodboysoul.pojavxoptimizer.util.Ema;

public final class DynamicResolution {

    private static final double STEP = 0.02;

    private final PjoConfig config;
    private final Ema smoothedFrameMs = new Ema(0.10);

    private float scale;
    private int panelWidth = 1080;
    private int panelHeight = 2400;
    private boolean engaged;

    public DynamicResolution(PjoConfig config) {
        this.config = config;
        this.scale = config.resolutionScaleMax();
    }

    public void setPanelSize(int width, int height) {
        if (width > 0 && height > 0) {
            this.panelWidth = width;
            this.panelHeight = height;
        }
    }

    public float update(double frameMs, double targetFrameMs, ThermalGovernor.Pressure thermal) {
        if (frameMs <= 0.0 || frameMs > 2000.0) {
            return scale;
        }
        smoothedFrameMs.push(frameMs);

        if (!config.dynamicResolution()) {
            engaged = false;
            return scale = config.resolutionScaleMax();
        }

        double min = config.resolutionScaleMin();
        double max = config.resolutionScaleMax();

        double ceiling = switch (thermal) {
            case SEVERE -> Math.min(max, 0.60);
            case MODERATE -> Math.min(max, 0.75);
            case MILD -> Math.min(max, 0.85);
            case NONE -> max;
        };

        double smoothed = smoothedFrameMs.get();
        if (smoothed > targetFrameMs * 1.10) {
            scale = (float) Math.max(min, scale - STEP);
        } else if (smoothed < targetFrameMs * 0.85) {
            scale = (float) Math.min(ceiling, scale + STEP);
        }

        scale = (float) Math.max(min, Math.min(ceiling, scale));
        engaged = scale < max - 0.001;

        if (Debug.isVerbose() && (System.currentTimeMillis() & 0xFF) == 0) {
            PJO.debug("DynamicResolution: scale={} ({}x{}) smoothed={}ms target={}ms engaged={}",
                    String.format("%.2f", scale), renderWidth(), renderHeight(),
                    String.format("%.1f", smoothed), String.format("%.1f", targetFrameMs), engaged);
        }
        return scale;
    }

    public float scale() {
        return scale;
    }

    public int renderWidth() {
        return Math.max(160, (int) Math.round(panelWidth * scale));
    }

    public int renderHeight() {
        return Math.max(160, (int) Math.round(panelHeight * scale));
    }

    public boolean isEngaged() {
        return engaged;
    }

    public void reset() {
        scale = config.resolutionScaleMax();
        smoothedFrameMs.reset();
        engaged = false;
    }
}
