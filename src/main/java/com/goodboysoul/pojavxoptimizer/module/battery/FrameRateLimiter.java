package com.goodboysoul.pojavxoptimizer.module.battery;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

public final class FrameRateLimiter {

    private static final int UNLIMITED = 260;

    private final PjoConfig config;

    private int appliedCap = -1;
    private int userCapBeforeWeTookOver = UNLIMITED;
    private boolean weAreLimiting;
    private long writeCount;

    public FrameRateLimiter(PjoConfig config) {
        this.config = config;
    }

    public void apply(Options options, int desiredCap) {
        if (options == null || desiredCap < 5) {
            return;
        }

        OptionInstance<Integer> limit = options.framerateLimit();
        if (limit == null) {
            return;
        }

        Integer current = limit.get();
        if (current == null) {
            return;
        }

        if (!weAreLimiting) {
            if (current < UNLIMITED) {

                userCapBeforeWeTookOver = current;
                return;
            }
            weAreLimiting = true;
        }

        if (!config.capToRefreshRate() && !config.batterySaver()) {
            release(limit);
            return;
        }

        if (desiredCap != appliedCap) {
            appliedCap = desiredCap;
            writeCount++;
            try {
                limit.set(desiredCap);
            } catch (RuntimeException setFailed) {
                PJO.warn("Could not apply the frame rate cap.", setFailed);
            }
            if (Debug.isVerbose()) {
                PJO.debug("FrameRateLimiter: cap {} -> {}", current, desiredCap);
            }
        }
    }

    public void release(OptionInstance<Integer> limit) {
        if (!weAreLimiting || limit == null) {
            return;
        }
        weAreLimiting = false;
        appliedCap = -1;
        try {
            limit.set(userCapBeforeWeTookOver);
            PJO.debug("FrameRateLimiter: released, restored {}", userCapBeforeWeTookOver);
        } catch (RuntimeException restoreFailed) {
            PJO.warn("Could not restore the player's frame rate limit.", restoreFailed);
        }
    }

    public void release(Options options) {
        if (options != null) {
            release(options.framerateLimit());
        }
    }

    public boolean isControlling() {
        return weAreLimiting;
    }

    public int appliedCap() {
        return appliedCap;
    }

    public long writeCount() {
        return writeCount;
    }

    public void reset() {
        appliedCap = -1;
        weAreLimiting = false;
        userCapBeforeWeTookOver = UNLIMITED;
    }
}
