package com.goodboysoul.pojavxoptimizer.config;

import com.goodboysoul.pojavxoptimizer.core.PJO;

public enum DeviceProfile {

    POTATO("Potato", 1024, 2, 8, 2, false, true, 24, 0.60f),

    LOW("Low", 2048, 4, 12, 3, true, true, 30, 0.70f),

    MEDIUM("Medium", 3072, 6, 16, 4, true, false, 60, 0.75f),

    HIGH("High", 4096, 8, 20, 6, true, false, 90, 0.80f),

    FLAGSHIP("Flagship", 6144, 10, 26, 8, true, false, 120, 0.85f);

    private final String displayName;
    private final long suggestedHeapMb;
    private final int buildThreads;
    private final int renderDistanceCap;
    private final int particleBudget;
    private final boolean aggressiveMemoryReclaim;
    private final boolean thermalGovernorEnabled;
    private final int idleFpsCap;
    private final float heapPressureThreshold;

    DeviceProfile(String displayName, long suggestedHeapMb, int buildThreads, int renderDistanceCap,
                  int particleBudget, boolean aggressiveMemoryReclaim, boolean thermalGovernorEnabled,
                  int idleFpsCap, float heapPressureThreshold) {
        this.displayName = displayName;
        this.suggestedHeapMb = suggestedHeapMb;
        this.buildThreads = buildThreads;
        this.renderDistanceCap = renderDistanceCap;
        this.particleBudget = particleBudget;
        this.aggressiveMemoryReclaim = aggressiveMemoryReclaim;
        this.thermalGovernorEnabled = thermalGovernorEnabled;
        this.idleFpsCap = idleFpsCap;
        this.heapPressureThreshold = heapPressureThreshold;
    }

    public String displayName() {
        return displayName;
    }

    public long suggestedHeapMb() {
        return suggestedHeapMb;
    }

    public int buildThreads() {
        return buildThreads;
    }

    public int renderDistanceCap() {
        return renderDistanceCap;
    }

    public int particleBudget() {
        return particleBudget;
    }

    public boolean aggressiveMemoryReclaim() {
        return aggressiveMemoryReclaim;
    }

    public boolean thermalGovernorEnabled() {
        return thermalGovernorEnabled;
    }

    public int idleFpsCap() {
        return idleFpsCap;
    }

    public float heapPressureThreshold() {
        return heapPressureThreshold;
    }

    public static DeviceProfile detect() {
        long heapMb = Runtime.getRuntime().maxMemory() / (1024L * 1024L);
        int cores = Runtime.getRuntime().availableProcessors();

        DeviceProfile profile;
        if (heapMb < 1536 || cores <= 2) {
            profile = POTATO;
        } else if (heapMb < 2560 || cores <= 4) {
            profile = LOW;
        } else if (heapMb < 3584) {
            profile = MEDIUM;
        } else if (heapMb < 5120) {
            profile = HIGH;
        } else {
            profile = FLAGSHIP;
        }

        PJO.info("Device profile: {} (heap={} MB, cores={})",
                profile.displayName(), heapMb, cores);
        return profile;
    }
}
