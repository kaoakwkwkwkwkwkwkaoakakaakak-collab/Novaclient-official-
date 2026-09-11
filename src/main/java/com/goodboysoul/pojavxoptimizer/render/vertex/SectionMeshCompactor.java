package com.goodboysoul.pojavxoptimizer.render.vertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SectionMeshCompactor {

    public static final int VANILLA_POSITION_OFFSET = 0;

    public static final int VANILLA_COLOR_OFFSET = 12;

    public static final int VANILLA_UV_OFFSET = 16;

    public static final int VANILLA_LIGHT_OFFSET = 24;

    public static final float ATLAS_RESOLUTION = 512.0f;

    public static float texelCentre(float coordinate) {
        float safe = Float.isNaN(coordinate) ? 0.0f : coordinate;
        int texel = (int) Math.floor(safe * ATLAS_RESOLUTION);

        if (texel < 0) {
            texel = 0;
        }
        int maxTexel = (int) ATLAS_RESOLUTION - 1;
        if (texel > maxTexel) {
            texel = maxTexel;
        }
        return PackedVertexFormat.texelCentre(ATLAS_RESOLUTION, texel);
    }

    public static final class Stats {
        private int vertexCount;
        private long inputBytes;
        private long outputBytes;

        public int vertexCount() { return vertexCount; }

        public long inputBytes() { return inputBytes; }

        public long outputBytes() { return outputBytes; }

        public long bytesSaved() { return inputBytes - outputBytes; }
    }

    public int compact(ByteBuffer source, int sourceStride,
                       float sectionOriginX, float sectionOriginY, float sectionOriginZ,
                       ByteBuffer destination) {
        if (sourceStride < VANILLA_LIGHT_OFFSET + 8) {
            throw new IllegalArgumentException(
                    "source stride " + sourceStride + " is too small for a BLOCK vertex");
        }
        int remaining = source.remaining();
        if (remaining % sourceStride != 0) {
            throw new IllegalArgumentException(
                    "source holds " + remaining + " bytes, which is not a whole number of "
                            + sourceStride + "-byte vertices");
        }

        int vertexCount = remaining / sourceStride;
        int required = vertexCount * PackedVertexFormat.STRIDE;
        if (destination.remaining() < required) {
            throw new IllegalArgumentException(
                    "destination holds " + destination.remaining() + " bytes, needs " + required);
        }

        ByteBuffer src = source.duplicate().order(ByteOrder.nativeOrder());
        ByteBuffer dst = destination.duplicate().order(ByteOrder.nativeOrder());

        for (int i = 0; i < vertexCount; i++) {
            int srcBase = i * sourceStride;

            float x = src.getFloat(srcBase + VANILLA_POSITION_OFFSET);
            float y = src.getFloat(srcBase + VANILLA_POSITION_OFFSET + 4);
            float z = src.getFloat(srcBase + VANILLA_POSITION_OFFSET + 8);

            int vanillaColor = src.getInt(srcBase + VANILLA_COLOR_OFFSET);

            float u = src.getFloat(srcBase + VANILLA_UV_OFFSET);
            float v = src.getFloat(srcBase + VANILLA_UV_OFFSET + 4);

            float blockLight = src.getFloat(srcBase + VANILLA_LIGHT_OFFSET);
            float skyLight = src.getFloat(srcBase + VANILLA_LIGHT_OFFSET + 4);

            int dstBase = i * PackedVertexFormat.STRIDE;

            dst.putShort(dstBase + PackedVertexFormat.POSITION_OFFSET,
                    PackedVertexFormat.quantisePosition(x - sectionOriginX));
            dst.putShort(dstBase + PackedVertexFormat.POSITION_OFFSET + 2,
                    PackedVertexFormat.quantisePosition(y - sectionOriginY));
            dst.putShort(dstBase + PackedVertexFormat.POSITION_OFFSET + 4,
                    PackedVertexFormat.quantisePosition(z - sectionOriginZ));

            dst.putInt(dstBase + PackedVertexFormat.COLOR_OFFSET,
                    PackedVertexFormat.packColor(
                            (vanillaColor >>> 24) & 0xFF,
                            (vanillaColor >>> 16) & 0xFF,
                            (vanillaColor >>> 8) & 0xFF,
                            vanillaColor & 0xFF));

            dst.putShort(dstBase + PackedVertexFormat.UV_OFFSET,
                    (short) PackedVertexFormat.quantiseUv(u, texelCentre(u)));
            dst.putShort(dstBase + PackedVertexFormat.UV_OFFSET + 2,
                    (short) PackedVertexFormat.quantiseUv(v, texelCentre(v)));

            dst.putShort(dstBase + PackedVertexFormat.LIGHT_OFFSET,
                    (short) PackedVertexFormat.packLight(blockLight, skyLight));
        }

        return vertexCount;
    }

    public Stats compactWithStats(ByteBuffer source, int sourceStride,
                                  float originX, float originY, float originZ,
                                  ByteBuffer destination) {
        int sourcePosition = source.position();
        long inputBytes = source.remaining();
        int count = compact(source, sourceStride, originX, originY, originZ, destination);
        Stats stats = new Stats();
        stats.vertexCount = count;
        stats.inputBytes = inputBytes;
        stats.outputBytes = (long) count * PackedVertexFormat.STRIDE;

        source.position(sourcePosition);
        return stats;
    }

    public static int destinationSize(int sourceByteCount, int sourceStride) {
        return (sourceByteCount / sourceStride) * PackedVertexFormat.STRIDE;
    }
}
