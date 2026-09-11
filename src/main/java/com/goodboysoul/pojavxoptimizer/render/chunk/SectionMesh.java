package com.goodboysoul.pojavxoptimizer.render.chunk;

import java.nio.ByteBuffer;

public final class SectionMesh {

    public enum Pass {
        OPAQUE,
        TRANSLUCENT,
        CUTOUT
    }

    private final Pass pass;
    private ByteBuffer vertices;
    private ByteBuffer indices;
    private int vertexCount;
    private int indexCount;
    private int glBufferId;
    private boolean uploaded;
    private long buildTimeNanos;
    private int rebuildCount;

    public SectionMesh(Pass pass) {
        this.pass = pass;
    }

    public Pass pass() {
        return pass;
    }

    public ByteBuffer vertices() {
        return vertices;
    }

    public void setVertices(ByteBuffer vertices, int vertexCount, int stride) {
        if (vertexCount * stride > vertices.remaining() + vertices.position()) {
            throw new IllegalArgumentException("vertex buffer too small for " + vertexCount
                    + " vertices at stride " + stride);
        }
        this.vertices = vertices;
        this.vertexCount = vertexCount;
        this.uploaded = false;
    }

    public ByteBuffer indices() {
        return indices;
    }

    public void setIndices(ByteBuffer indices, int indexCount) {
        this.indices = indices;
        this.indexCount = indexCount;
        this.uploaded = false;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int indexCount() {
        return indexCount;
    }

    public boolean isEmpty() {
        return vertexCount == 0;
    }

    public int quadCount() {
        return indexCount / 6;
    }

    public void attachGlBuffer(int id) {
        this.glBufferId = id;
    }

    public int glBufferId() {
        return glBufferId;
    }

    public void markUploaded() {
        uploaded = true;
        rebuildCount++;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void recordBuildTime(long nanos) {
        if (nanos < 0) {
            throw new IllegalArgumentException("build time cannot be negative");
        }
        this.buildTimeNanos = nanos;
    }

    public long buildTimeNanos() {
        return buildTimeNanos;
    }

    public float buildTimeMillis() {
        return buildTimeNanos / 1_000_000.0f;
    }

    public int rebuildCount() {
        return rebuildCount;
    }

    public int vertexBytes() {
        return vertices == null ? 0 : vertices.capacity();
    }

    public int indexBytes() {
        return indices == null ? 0 : indices.capacity();
    }

    public int totalBytes() {
        return vertexBytes() + indexBytes();
    }

    public void discard() {
        vertices = null;
        indices = null;
        vertexCount = 0;
        indexCount = 0;
        uploaded = false;
    }
}
