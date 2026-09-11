package com.goodboysoul.pojavxoptimizer.module.chunk;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;

public final class ChunkPrioritizer {

    private static final double DISTANCE_WEIGHT = 1.0;

    private static final double BEHIND_PENALTY = 64.0;

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

    public double score(double sectionCentreX, double sectionCentreY, double sectionCentreZ,
                        boolean visibleLastFrame) {
        double dx = sectionCentreX - cameraX;
        double dy = (sectionCentreY - cameraY) * 0.5;
        double dz = sectionCentreZ - cameraZ;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double score = distance * DISTANCE_WEIGHT;

        if (config.viewWeightedChunkPriority() && distance > 1.0e-6) {
            double dot = (dx * lookX + dy * lookY + dz * lookZ) / distance;

            if (dot < 0.0) {
                score += BEHIND_PENALTY * (-dot);
            } else {

                score -= 8.0 * dot;
            }
        }

        if (visibleLastFrame) {
            score += ALREADY_VISIBLE_BONUS;
        }

        return score;
    }

    public double score(int blockX, int blockY, int blockZ, boolean visibleLastFrame) {
        return score(blockX + 0.5, blockY + 0.5, blockZ + 0.5, visibleLastFrame);
    }

    public boolean isViewWeighted() {
        return config.viewWeightedChunkPriority();
    }
}
