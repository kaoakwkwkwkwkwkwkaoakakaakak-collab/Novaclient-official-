package com.goodboysoul.pojavxoptimizer.module.memory;

import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

public final class BufferArena implements HeapGuard.EvictionListener {

    private static final int DEFAULT_SEGMENT_BYTES = 1 << 16;

    private final Deque<ByteBuffer> freeSegments = new ArrayDeque<>();
    private final int segmentBytes;
    private final int maxRetainedSegments;

    private int allocatedSegments;
    private long peakBytes;
    private long borrowCount;
    private long reuseCount;

    public BufferArena(int maxRetainedSegments) {
        this(DEFAULT_SEGMENT_BYTES, maxRetainedSegments);
    }

    public BufferArena(int segmentBytes, int maxRetainedSegments) {
        if (segmentBytes <= 0) {
            throw new IllegalArgumentException("segmentBytes must be positive");
        }
        this.segmentBytes = segmentBytes;
        this.maxRetainedSegments = Math.max(1, maxRetainedSegments);
        if (Debug.isVerbose()) {
            PJO.debug("BufferArena initialised with {} KiB segments", segmentBytes >> 10);
        }
    }

    public synchronized ByteBuffer borrow(int minimumBytes) {
        borrowCount++;
        if (minimumBytes <= segmentBytes) {
            ByteBuffer pooled = freeSegments.pollFirst();
            if (pooled != null) {
                reuseCount++;
                pooled.clear();
                return pooled;
            }
            allocatedSegments++;
            peakBytes = Math.max(peakBytes, (long) allocatedSegments * segmentBytes);
            return ByteBuffer.allocateDirect(segmentBytes);
        }
        allocatedSegments++;
        peakBytes = Math.max(peakBytes, (long) allocatedSegments * segmentBytes);
        return ByteBuffer.allocateDirect(minimumBytes);
    }

    public synchronized void giveBack(ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || buffer.capacity() != segmentBytes) {
            return;
        }
        if (freeSegments.size() >= maxRetainedSegments) {
            allocatedSegments = Math.max(0, allocatedSegments - 1);
            return;
        }
        buffer.clear();
        freeSegments.addLast(buffer);
    }

    @Override
    public synchronized void onMemoryPressure(double urgency) {
        int retain = urgency >= 0.75 ? 0 : Math.max(1, maxRetainedSegments / 4);
        int released = trimTo(retain);
        if (released > 0) {
            PJO.info("BufferArena released {} pooled segment(s) under memory pressure.", released);
        }
    }

    public synchronized int trimTo(int retain) {
        int released = 0;
        while (freeSegments.size() > retain) {
            freeSegments.pollLast();
            allocatedSegments = Math.max(0, allocatedSegments - 1);
            released++;
        }
        return released;
    }

    public synchronized void clear() {
        trimTo(0);
    }

    public synchronized int pooledSegments() {
        return freeSegments.size();
    }

    public synchronized long approximateBytes() {
        return (long) allocatedSegments * segmentBytes;
    }

    public long peakBytes() {
        return peakBytes;
    }

    public long borrowCount() {
        return borrowCount;
    }

    public double reuseRate() {
        return borrowCount == 0 ? 0.0 : (double) reuseCount / (double) borrowCount;
    }

    public String describe() {
        return "arena[pooled=" + pooledSegments()
                + " ~" + (approximateBytes() >> 10) + " KiB"
                + " reuse=" + Math.round(reuseRate() * 100) + "%]";
    }
}
