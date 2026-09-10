package com.goodboysoul.pojavxoptimizer.module.chunk;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;

/**
 * Decides which section to build next.
 *
 * <p>Vanilla orders pending builds largely by distance from the camera. On a phone with two or three
 * build threads that ordering produces a very visible artefact: chunks fill in as a disc around the
 * player, so the ground directly underfoot appears early while the horizon the player is actually
 * looking at stays empty for seconds.
 *
 * <p>Weighting by view direction fixes the perceived load time without changing how much work is
 * done. The player cannot see behind them, so sections in the rear half are worth far less than
 * sections in the cone they are facing, even at identical distance.
 *
 * <p>The score is a plain double: lower is more urgent. It is computed per candidate on every
 * re-prioritisation, so it must stay cheap — no allocation, no square roots unless the result
 * actually changes the ordering.
 */
public final class ChunkPrioritizer {

    /** Multiplier applied to the distance term, in "chunks of perceived delay". */
    private static final double DISTANCE_WEIGHT = 1.0;

    /** Penalty for being directly behind the camera. Sized so rear sections sort last. */
    private static final double BEHIND_PENALTY = 64.0;

    /** Sections already visible last frame skip the queue jump; they are already on screen. */
    private static final double ALREADY_VISIBLE_BONUS = -8.0;

    private final PjoConfig config;

    private double cameraX;
    private double cameraY;
    private double cameraZ;
    private double lookX;
    private double lookY;
    private double lookZ;

    public ChunkPrioritizer(PjoConfig config) {
        this.config = config;
    }

    /**
     * Updates the reference point for subsequent scoring. Called when the camera moves, not per
     * frame per section — the values are shared across a whole prioritisation pass.
     */
    public void setView(double camX, double camY, double camZ,
                        double lookVecX, double lookVecY, double lookVecZ) {
        this.cameraX = camX;
        this.cameraY = camY;
        this.cameraZ = camZ;
        double length = Math.sqrt(lookVecX * lookVecX + lookVecY * lookVecY + lookVecZ * lookVecZ);
        if (length > 1.0e-6) {
            this.lookX = lookVecX / length;
            this.lookY = lookVecY / length;
            this.lookZ = lookVecZ / length;
        } else {
            this.lookX = 0.0;
            this.lookY = 0.0;
            this.lookZ = 1.0;
        }
    }

    /**
     * Scores one section.
     *
     * @param sectionCentreX centre of the section, block coordinates
     * @param sectionCentreY centre of the section, block coordinates
     * @param sectionCentreZ centre of the section, block coordinates
     * @param visibleLastFrame whether this section was in the visible set last frame
     * @return priority score; lower builds sooner
     */
    public double score(double sectionCentreX, double sectionCentreY, double sectionCentreZ,
                        boolean visibleLastFrame) {
        double dx = sectionCentreX - cameraX;
        double dy = (sectionCentreY - cameraY) * 0.5; // vertical distance matters less than horizontal
        double dz = sectionCentreZ - cameraZ;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double score = distance * DISTANCE_WEIGHT;

        if (config.viewWeightedChunkPriority() && distance > 1.0e-6) {
            double dot = (dx * lookX + dy * lookY + dz * lookZ) / distance;
            // dot is +1 looking straight at the section, -1 straight away.
            if (dot < 0.0) {
                score += BEHIND_PENALTY * (-dot);
            } else {
                // In front: pull it forward in proportion to how centred it is.
                score -= 8.0 * dot;
            }
        }

        if (visibleLastFrame) {
            score += ALREADY_VISIBLE_BONUS;
        }

        return score;
    }

    /**
     * Convenience overload for integer block coordinates, which is what the caller usually holds.
     */
    public double score(int blockX, int blockY, int blockZ, boolean visibleLastFrame) {
        return score(blockX + 0.5, blockY + 0.5, blockZ + 0.5, visibleLastFrame);
    }

    public boolean isViewWeighted() {
        return config.viewWeightedChunkPriority();
    }
}
