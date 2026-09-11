package com.goodboysoul.pojavxoptimizer.module.memory;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;

import java.util.ArrayList;
import java.util.List;

public final class HeapGuard {

    public interface EvictionListener {

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

            lastUrgency = Math.min(1.0, (ratio - threshold) / Math.max(0.01, 0.98 - threshold));
            if (!pressureActive) {
                pressureActive = true;
                PJO.warn("Heap pressure: {} MB of {} MB used ({}%). Releasing cached meshes.",
                        used >> 20, max >> 20, Math.round(ratio * 100));
            }
            notifyListeners(lastUrgency);
        } else if (pressureActive && ratio < threshold - 0.05) {

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
