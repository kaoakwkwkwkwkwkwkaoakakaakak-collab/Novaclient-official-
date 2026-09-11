package com.goodboysoul.pojavxoptimizer.render.culling;

public final class PjoFrustum {

    public static final int PLANE_COUNT = 6;

    public static final int COMPONENTS_PER_PLANE = 4;

    public static final int FAR_PLANE = 5;

    private final float[] planes;

    private final float[] foldedConstants;

    private final float halfExtent;

    private final boolean valid;

    private PjoFrustum(float[] planes, float[] foldedConstants, float halfExtent, boolean valid) {
        this.planes = planes;
        this.foldedConstants = foldedConstants;
        this.halfExtent = halfExtent;
        this.valid = valid;
    }

    public static PjoFrustum from(Object frustumIntersection, float halfExtent) {
        return fromPlanes(PlaneReader.readNormals(frustumIntersection), halfExtent);
    }

    public static PjoFrustum fromPlanes(float[] normals, float halfExtent) {
        if (normals == null || normals.length < PLANE_COUNT * COMPONENTS_PER_PLANE) {
            int size = PLANE_COUNT * COMPONENTS_PER_PLANE;
            return new PjoFrustum(new float[size], new float[PLANE_COUNT], halfExtent, false);
        }

        float[] raw = new float[PLANE_COUNT * COMPONENTS_PER_PLANE];
        float[] folded = new float[PLANE_COUNT];

        for (int plane = 0; plane < PLANE_COUNT; plane++) {
            int base = plane * COMPONENTS_PER_PLANE;
            float nx = normals[base];
            float ny = normals[base + 1];
            float nz = normals[base + 2];
            float w = normals[base + 3];

            raw[base] = nx;
            raw[base + 1] = ny;
            raw[base + 2] = nz;
            raw[base + 3] = w;

            float corner = nx * (nx < 0.0f ? -halfExtent : halfExtent)
                    + ny * (ny < 0.0f ? -halfExtent : halfExtent)
                    + nz * (nz < 0.0f ? -halfExtent : halfExtent);

            folded[plane] = -(w + corner);
        }
        return new PjoFrustum(raw, folded, halfExtent, true);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean testSection(float centreX, float centreY, float centreZ) {
        if (!valid) {
            return true;
        }
        for (int plane = 0; plane < FAR_PLANE; plane++) {
            int base = plane * COMPONENTS_PER_PLANE;
            float dot = planes[base] * centreX
                    + planes[base + 1] * centreY
                    + planes[base + 2] * centreZ;
            if (dot < foldedConstants[plane]) {
                return false;
            }
        }
        return true;
    }

    public boolean testBox(float minX, float minY, float minZ,
                           float maxX, float maxY, float maxZ) {
        if (!valid) {
            return true;
        }
        for (int plane = 0; plane < PLANE_COUNT; plane++) {
            int base = plane * COMPONENTS_PER_PLANE;
            float nx = planes[base];
            float ny = planes[base + 1];
            float nz = planes[base + 2];

            float dot = nx * (nx < 0.0f ? minX : maxX)
                    + ny * (ny < 0.0f ? minY : maxY)
                    + nz * (nz < 0.0f ? minZ : maxZ);
            if (dot < -planes[base + 3]) {
                return false;
            }
        }
        return true;
    }

    public float halfExtent() {
        return halfExtent;
    }
}
