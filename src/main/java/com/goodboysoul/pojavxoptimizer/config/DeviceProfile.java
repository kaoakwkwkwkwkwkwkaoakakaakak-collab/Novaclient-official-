package com.goodboysoul.pojavxoptimizer.config;

import com.goodboysoul.pojavxoptimizer.core.PJO;

/**
 * Coarse device classes used to pick sane defaults before the user has touched a single setting.
 *
 * <p>Android does not let a JVM process ask the system how much physical RAM the handset has, and
 * the launcher's heap allocation is a user setting rather than a hardware fact. Classification is
 * therefore derived from the two numbers the JVM can actually see — {@link Runtime#maxMemory()} and
 * {@link Runtime#availableProcessors()} — which together separate the cases that matter: a 2 GB
 * handset given a 1 GB heap must behave very differently from a 12 GB handset given a 4 GB heap.
 */
public enum DeviceProfile {

    /** Very constrained. Minimise memory at all costs; visuals are secondary. */
    POTATO("Potato", 1024, 2, 8, 2, false, true, 24, 0.60f),

    /** Typical budget handset. The case this mod is really built for. */
    LOW("Low", 2048, 4, 12, 3, true, true, 30, 0.70f),

    /** Mid-range. Comfortable at moderate render distance. */
    MEDIUM("Medium", 3072, 6, 16, 4, true, false, 60, 0.75f),

    /** Upper mid-range. Only mild throttling needed. */
    HIGH("High", 4096, 8, 20, 6, true, false, 90, 0.80f),

    /** Flagship or tablet. Optimisations that trade memory for speed are now worthwhile. */
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

    /**
     * Classifies the current device. Deliberately conservative: when the signals are ambiguous it
     * returns a lower tier, because over-promising on a phone shows up as the launcher being killed
     * by the low-memory killer mid-session, which is much worse than a slightly lower frame rate.
     */
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
