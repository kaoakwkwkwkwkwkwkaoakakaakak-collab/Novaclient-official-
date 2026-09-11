package com.goodboysoul.pojavxoptimizer.render.chunk;

public final class ChunkSectionIndex {

    public static final int SECTION_SIZE = 16;

    private final int radiusX;
    private final int radiusY;
    private final int radiusZ;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final int size;

    public ChunkSectionIndex(int radiusX, int radiusY, int radiusZ) {
        if (radiusX < 0 || radiusY < 0 || radiusZ < 0) {
            throw new IllegalArgumentException("radius cannot be negative");
        }
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.sizeX = radiusX * 2 + 1;
        this.sizeY = radiusY * 2 + 1;
        this.sizeZ = radiusZ * 2 + 1;
        this.size = sizeX * sizeY * sizeZ;
    }

    public static ChunkSectionIndex forRenderDistance(int renderDistanceChunks) {
        int sections = renderDistanceChunks + 1;
        return new ChunkSectionIndex(sections, verticalSections(), sections);
    }

    public static int verticalSections() {
        return 12;
    }

    public int capacity() {
        return size;
    }

    public int sizeX() {
        return sizeX;
    }

    public int sizeY() {
        return sizeY;
    }

    public int sizeZ() {
        return sizeZ;
    }

    public int sectionX(int blockX) {
        return blockX >> 4;
    }

    public int sectionY(int blockY) {
        return blockY >> 4;
    }

    public int sectionZ(int blockZ) {
        return blockZ >> 4;
    }

    public int index(int sectionX, int sectionY, int sectionZ) {
        int localX = sectionX - radiusX;
        int localY = sectionY - radiusY;
        int localZ = sectionZ - radiusZ;
        if (localX < 0 || localX >= sizeX
                || localY < 0 || localY >= sizeY
                || localZ < 0 || localZ >= sizeZ) {
            return -1;
        }
        return (localY * sizeZ + localZ) * sizeX + localX;
    }

    public boolean contains(int sectionX, int sectionY, int sectionZ) {
        return index(sectionX, sectionY, sectionZ) >= 0;
    }

    public int radiusX() {
        return radiusX;
    }

    public int radiusY() {
        return radiusY;
    }

    public int radiusZ() {
        return radiusZ;
    }

    public int chebyshevDistance(int fromSectionX, int fromSectionY, int fromSectionZ,
                                 int toSectionX, int toSectionY, int toSectionZ) {
        int dx = Math.abs(fromSectionX - toSectionX);
        int dy = Math.abs(fromSectionY - toSectionY);
        int dz = Math.abs(fromSectionZ - toSectionZ);
        return Math.max(dx, Math.max(dy, dz));
    }

    public long squaredDistance(int fromSectionX, int fromSectionY, int fromSectionZ,
                                int toSectionX, int toSectionY, int toSectionZ) {
        long dx = fromSectionX - toSectionX;
        long dy = fromSectionY - toSectionY;
        long dz = fromSectionZ - toSectionZ;
        return dx * dx + dy * dy + dz * dz;
    }
}
