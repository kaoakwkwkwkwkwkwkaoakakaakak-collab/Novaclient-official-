package com.goodboysoul.pojavxoptimizer.render.gl;

public final class GlBuffer {

    public enum Usage {
        STATIC_DRAW,
        DYNAMIC_DRAW,
        STREAM_DRAW
    }

    private final int id;
    private final int target;
    private Usage usage;
    private int allocatedBytes;
    private int uploadCount;
    private long uploadedBytes;
    private boolean mapped;
    private boolean deleted;

    GlBuffer(int id, int target, Usage usage) {
        this.id = id;
        this.target = target;
        this.usage = usage;
    }

    public static GlBuffer create(int id, int target, Usage usage) {
        return new GlBuffer(id, target, usage);
    }

    public int id() {
        return id;
    }

    public int target() {
        return target;
    }

    public Usage usage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public void recordAllocation(int bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("allocation cannot be negative: " + bytes);
        }
        this.allocatedBytes = bytes;
    }

    public void recordUpload(int bytes) {
        if (deleted) {
            throw new IllegalStateException("buffer " + id + " has been deleted");
        }
        if (bytes < 0) {
            throw new IllegalArgumentException("upload cannot be negative: " + bytes);
        }
        if (bytes > allocatedBytes) {
            allocatedBytes = bytes;
        }
        uploadCount++;
        uploadedBytes += bytes;
    }

    public void enterMappedState() {
        if (mapped) {
            throw new IllegalStateException("buffer " + id + " is already mapped");
        }
        mapped = true;
    }

    public void leaveMappedState() {
        if (!mapped) {
            throw new IllegalStateException("buffer " + id + " is not mapped");
        }
        mapped = false;
    }

    public boolean isMapped() {
        return mapped;
    }

    public int allocatedBytes() {
        return allocatedBytes;
    }

    public int uploadCount() {
        return uploadCount;
    }

    public long uploadedBytes() {
        return uploadedBytes;
    }

    public long averageUploadBytes() {
        return uploadCount == 0 ? 0 : uploadedBytes / uploadCount;
    }

    public void markDeleted() {
        deleted = true;
        mapped = false;
        allocatedBytes = 0;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Usage suggestedUsage(int framesObserved) {
        if (framesObserved <= 0) {
            return usage;
        }
        float uploadsPerFrame = (float) uploadCount / framesObserved;
        if (uploadsPerFrame >= 0.75f) {
            return Usage.STREAM_DRAW;
        }
        if (uploadsPerFrame <= 0.05f) {
            return Usage.STATIC_DRAW;
        }
        return Usage.DYNAMIC_DRAW;
    }
}
