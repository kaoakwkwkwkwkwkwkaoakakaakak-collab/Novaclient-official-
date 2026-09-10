package com.goodboysoul.pojavxoptimizer.module.memory;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the heap below the point where Android kills the process.
 *
 * <p>On a desktop, running out of memory produces an OutOfMemoryError with a stack trace. On
 * Android, the low-memory killer terminates the whole app without warning, and the user sees the
 * launcher sitting at the main menu with no explanation. The heap ceiling is also not a hardware
 * fact but a launcher setting the user chose, so it varies wildly between installations of the same
 * phone.
 *
 * <p>The guard therefore watches the ratio of used to maximum heap and, before it reaches the
 * danger line, asks registered listeners to release what they can. Chunk meshes are the only large
 * reclaimable allocation on the client, so in practice this is "drop meshes for sections the player
 * cannot see, then rebuild them lazily", which trades a small hitch for not losing the session.
 */
public final class HeapGuard {

    /** Notified when heap pressure crosses the configured threshold. */
    public interface EvictionListener {
        /**
         * Release reclaimable memory.
         *
         * @param urgency 0.0 at the threshold rising to 1.0 at the hard limit
         */
        void onMemoryPressure(double urgency);
    }

    private static final int SAMPLE_INTERVAL_TICKS = 20;

    private final PjoConfig config;
    private final List<EvictionListener> listeners = new ArrayList<>(4);

    private long maxBytes = Runtime.getRuntime().maxMemory();
    private int tickCounter;
    private double lastUrgency;
    private boolean pressureActive;
    private long evictionCount;

    public HeapGuard(PjoConfig config) {
        this.config = config;
    }

    public void register(EvictionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregister(EvictionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Samples heap usage. Deliberately not every tick: {@link Runtime#totalMemory()} and friends are
     * cheap but not free, and the heap does not move fast enough for a 1-second sample interval to
     * miss anything actionable.
     */
    public void tick() {
        if (!config.heapGuard() || ++tickCounter < SAMPLE_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = Math.max(runtime.maxMemory(), used);
        maxBytes = max;

        double ratio = (double) used / (double) max;
        double threshold = config.heapPressureThreshold();

        if (ratio >= threshold) {
            // Urgency ramps from 0 at the threshold to 1 at 98%, so listeners can scale how much
            // they release instead of always dumping everything.
            lastUrgency = Math.min(1.0, (ratio - threshold) / Math.max(0.01, 0.98 - threshold));
            if (!pressureActive) {
                pressureActive = true;
                PJO.warn("Heap pressure: {} MB of {} MB used ({}%). Releasing cached meshes.",
                        used >> 20, max >> 20, Math.round(ratio * 100));
            }
            notifyListeners(lastUrgency);
        } else if (pressureActive && ratio < threshold - 0.05) {
            // Hysteresis: only clear the flag well below the threshold, otherwise the guard
            // re-triggers on every sample while the heap hovers near the line.
            pressureActive = false;
            PJO.info("Heap pressure cleared ({}% used).", Math.round(ratio * 100));
        }

        if (Debug.isVerbose()) {
            PJO.debug("HeapGuard: {} / {} MB ({}%), urgency={}",
                    used >> 20, max >> 20, Math.round(ratio * 100), String.format("%.2f", lastUrgency));
        }
    }

    private void notifyListeners(double urgency) {
        for (int i = 0; i < listeners.size(); i++) {
            try {
                listeners.get(i).onMemoryPressure(urgency);
                evictionCount++;
            } catch (RuntimeException listenerFailed) {
                // A listener that throws must not be able to break the tick loop.
                PJO.warn("A memory eviction listener failed; continuing.", listenerFailed);
            }
        }
    }

    public boolean isPressureActive() {
        return pressureActive;
    }

    public double lastUrgency() {
        return lastUrgency;
    }

    public long maxHeapMb() {
        return maxBytes >> 20;
    }

    public long evictionCount() {
        return evictionCount;
    }

    /**
     * Warns the user when the launcher's heap setting is small enough that heavy content will not
     * fit, which is the most common cause of "it just closes" reports on Pojav.
     */
    public void reportStartup() {
        long heapMb = maxBytes >> 20;
        long totalRamMb = Runtime.getRuntime().maxMemory() >> 20;
        PJO.info("Heap guard active: {} MB allocated to Minecraft.", heapMb);
        if (totalRamMb < 1024) {
            PJO.warn("Only {} MB of heap is allocated. Raise the launcher's memory setting to at "
                    + "least 2048 MB for modern modpacks.", totalRamMb);
        }
    }
}
