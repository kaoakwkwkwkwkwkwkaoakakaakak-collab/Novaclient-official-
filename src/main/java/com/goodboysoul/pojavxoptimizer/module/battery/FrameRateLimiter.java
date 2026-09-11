package com.goodboysoul.pojavxoptimizer.module.battery;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

/**
 * Applies the frame rate cap that {@link FramePacer} computes.
 *
 * <p>This class exists because the cap was previously computed every tick and then discarded. A cap
 * that is never written to the game's own framerate setting does nothing at all — the game keeps
 * rendering as fast as it can — so the whole battery and thermal story depended on this one write
 * actually happening.
 *
 * <p>Two constraints on how it writes:
 *
 * <ul>
 *   <li><b>It never overwrites a limit the user chose.</b> Minecraft uses 260 to mean "unlimited".
 *       If the player has deliberately set 60, imposing our own number would be overriding an
 *       explicit choice, so we only take over while the setting is unlimited.</li>
 *   <li><b>It only writes when the value changes.</b> {@code OptionInstance.set} propagates to
 *       listeners and marks options dirty for saving. Doing that 60 times a second would rewrite
 *       {@code options.txt} constantly, which on mobile storage is exactly the kind of pointless
 *       work this mod is meant to remove.</li>
 * </ul>
 */
public final class FrameRateLimiter {

    /** Minecraft's sentinel for "no limit", matching {@code Options.UNLIMITED_FRAMERATE_CUTOFF}. */
    private static final int UNLIMITED = 260;

    private final PjoConfig config;

    private int appliedCap = -1;
    private int userCapBeforeWeTookOver = UNLIMITED;
    private boolean weAreLimiting;
    private long writeCount;

    public FrameRateLimiter(PjoConfig config) {
        this.config = config;
    }

    /**
     * Applies the cap for this tick if, and only if, something needs to change.
     *
     * @param options the client's options, or {@code null} before the client exists
     * @param desiredCap the cap {@link FramePacer} wants in force
     */
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
                // The user has their own limit in place. Leave it alone entirely.
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

    /**
     * Hands the setting back to the player, restoring whatever they had before we took over.
     * Called when the mod disables itself or the world is unloaded.
     */
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

    /** Convenience overload for the caller that only has the Options object. */
    public void release(Options options) {
        if (options != null) {
            release(options.framerateLimit());
        }
    }

    /** True while the mod, rather than the player, is controlling the frame rate limit. */
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
