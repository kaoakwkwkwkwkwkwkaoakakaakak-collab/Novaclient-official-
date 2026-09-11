package com.goodboysoul.pojavxoptimizer.module.input;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;

public final class InputSmoother {

    private static final float FLICK_THRESHOLD = 8.0f;

    private final PjoConfig config;

    private float smoothedYawDelta;
    private float smoothedPitchDelta;
    private boolean primed;

    public InputSmoother(PjoConfig config) {
        this.config = config;
    }

    public float[] filter(float yawDelta, float pitchDelta) {
        float[] out = new float[2];

        if (!config.inputSmoothing()) {
            out[0] = yawDelta;
            out[1] = pitchDelta;
            return out;
        }

        if (!primed) {
            smoothedYawDelta = yawDelta;
            smoothedPitchDelta = pitchDelta;
            primed = true;
            out[0] = yawDelta;
            out[1] = pitchDelta;
            return out;
        }

        float strength = (float) config.inputSmoothingStrength();

        if (Math.abs(yawDelta) >= FLICK_THRESHOLD) {
            smoothedYawDelta = yawDelta;
            out[0] = yawDelta;
        } else {
            smoothedYawDelta += strength * (yawDelta - smoothedYawDelta);
            out[0] = smoothedYawDelta;
        }

        if (Math.abs(pitchDelta) >= FLICK_THRESHOLD) {
            smoothedPitchDelta = pitchDelta;
            out[1] = pitchDelta;
        } else {
            smoothedPitchDelta += strength * (pitchDelta - smoothedPitchDelta);
            out[1] = smoothedPitchDelta;
        }

        return out;
    }

    public boolean isEnabled() {
        return config.inputSmoothing();
    }

    public void reset() {
        primed = false;
        smoothedYawDelta = 0.0f;
        smoothedPitchDelta = 0.0f;
    }
}
