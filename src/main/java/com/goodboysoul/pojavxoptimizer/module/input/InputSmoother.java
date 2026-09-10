package com.goodboysoul.pojavxoptimizer.module.input;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;

/**
 * Optional low-pass filter for look input.
 *
 * <p>Touch look input arrives as a stream of small deltas, and a thumb sliding across glass produces
 * micro-jitter that reads as camera shake. Smoothing removes it.
 *
 * <p>It is <b>off by default</b>, and that is a deliberate choice rather than an oversight. Any
 * filter on look input adds latency between moving a thumb and the world responding, and in
 * first-person play that latency is felt immediately and disliked intensely, especially in PvP.
 * Players who want it tend to be building or exploring, where steadiness beats responsiveness.
 *
 * <p>Implemented as a one-pole filter with velocity-based bypass: fast flicks pass through
 * unfiltered, because smoothing a deliberate flick feels like input lag, while slow drift is
 * filtered, because that is where jitter lives.
 */
public final class InputSmoother {

    /** Above this delta per tick the input is treated as a deliberate flick. */
    private static final float FLICK_THRESHOLD = 8.0f;

    private final PjoConfig config;

    private float smoothedYawDelta;
    private float smoothedPitchDelta;
    private boolean primed;

    public InputSmoother(PjoConfig config) {
        this.config = config;
    }

    /**
     * Filters one tick of look input.
     *
     * @param yawDelta   raw yaw delta for this tick
     * @param pitchDelta raw pitch delta for this tick
     * @return filtered yaw delta at index 0, filtered pitch delta at index 1
     */
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
