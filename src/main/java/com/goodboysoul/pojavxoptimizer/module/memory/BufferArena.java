package com.goodboysoul.pojavxoptimizer.module.memory;

import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Pools off-heap scratch buffers used while building section meshes.
 *
 * <p>Vanilla allocates fresh heap-backed builders for every rebuild. On a phone that is doubly
 * expensive: the allocation is large enough to be humongous for most collectors, and it happens on
 * worker threads at exactly the moment the render thread is also allocating, which is how a chunk
 * load turns into a visible pause.
 *
 * <p>Pooled direct buffers avoid both problems. They sit outside the heap entirely, so the garbage
 * collector never sees them and their memory is not counted against the limit that triggers the
 * Android low-memory killer. They are reused, so steady-state allocation drops to zero.
 *
 * <p>The trade is that pooled memory is not returned to the OS while the pool holds it, which is why
 * {@link #trimTo(int)} exists and why {@link HeapGuard} is allowed to call it.
 */
public final class BufferArena implements HeapGuard.EvictionListener {

    private static final int DEFAULT_SEGMENT_BYTES = 1 << 16; // 64 KiB

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

    /**
     * Takes a buffer of at least {@code minimumBytes}. Larger requests get a dedicated buffer that
     * is not pooled, since retaining a one-off huge segment would defeat the point.
     */
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

    /**
     * Returns a buffer to the pool. Buffers that are not the pooled size, or that would take the
     * pool over its limit, are dropped so the direct memory can be reclaimed.
     */
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

    /**
     * Releases pooled segments, keeping at most {@code retain}. Invoked by {@link HeapGuard} when
     * the heap is tight: direct buffers are outside the heap, but the pages behind them still count
     * against the process total that Android watches.
     */
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

    /** Fraction of borrows served from the pool. A low value means the pool is undersized. */
    public double reuseRate() {
        return borrowCount == 0 ? 0.0 : (double) reuseCount / (double) borrowCount;
    }

    public String describe() {
        return "arena[pooled=" + pooledSegments()
                + " ~" + (approximateBytes() >> 10) + " KiB"
                + " reuse=" + Math.round(reuseRate() * 100) + "%]";
    }
}
