package com.goodboysoul.pojavxoptimizer.render.vertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Converts a vanilla block mesh into {@link PackedVertexFormat}.
 *
 * <h2>What it does and why it is the highest-value change in this mod</h2>
 *
 * <p>Every vertex Minecraft bakes into a chunk section is 32 bytes. This class rewrites that buffer
 * to 16 bytes, halving both the VRAM a loaded world occupies and the bytes pushed across the memory
 * bus during chunk rebuilds and vertex fetch. On a phone the bus is shared and contended, so the
 * win is larger than the raw 50% suggests.
 *
 * <h2>Why it is safe</h2>
 *
 * <p>The conversion is lossy in principle and lossless in practice, and it is worth being precise
 * about where the losses land:
 *
 * <ul>
 *   <li><b>Position</b> goes from float to short at 1/16-block precision. Vanilla's block models are
 *       authored on a 1/16 grid, so the quantisation lands exactly on the values the models already
 *       use. Nothing rounds.</li>
 *   <li><b>Colour</b> is already four bytes in vanilla. It is copied unchanged.</li>
 *   <li><b>Texture coordinates</b> go from float to 16-bit normalised. A block atlas is 256 or 512
 *       pixels wide, so 16 bits is finer than the source texture.</li>
 *   <li><b>Light</b> goes from float to 8 bits per channel. The game produces 16 distinct light
 *       levels, so 8 bits is four times finer than the data.</li>
 * </ul>
 *
 * <p>The one real constraint is that positions become section-relative, which is what makes them fit
 * in a short. The caller must supply the section origin and the renderer must apply it.
 *
 * <h2>Thread safety</h2>
 *
 * <p>Instances are stateless and may be shared across chunk build threads. All buffers are supplied
 * by the caller, so nothing here allocates in the hot path.
 */
public final class SectionMeshCompactor {

    // ---- vanilla DefaultVertexFormat.BLOCK layout, in bytes ----
    /** Three floats: x, y, z. */
    public static final int VANILLA_POSITION_OFFSET = 0;
    /** Four unsigned bytes: RGBA. */
    public static final int VANILLA_COLOR_OFFSET = 12;
    /** Two floats: u, v. */
    public static final int VANILLA_UV_OFFSET = 16;
    /** Two floats: block light, sky light. */
    public static final int VANILLA_LIGHT_OFFSET = 24;

    /** Statistics for the last conversion, used by the debug overlay. */
    public static final class Stats {
        private int vertexCount;
        private long inputBytes;
        private long outputBytes;

        public int vertexCount() { return vertexCount; }

        public long inputBytes() { return inputBytes; }

        public long outputBytes() { return outputBytes; }

        public long bytesSaved() { return inputBytes - outputBytes; }
    }

    /**
     * Compacts one section's vertex buffer.
     *
     * @param source        the vanilla vertex buffer, laid out as {@code DefaultVertexFormat.BLOCK}
     * @param sourceStride  stride of the source, normally 32; passed in rather than assumed so a
     *                      future vanilla change fails loudly instead of silently corrupting meshes
     * @param sectionOriginX world X of the section's minimum corner
     * @param sectionOriginY world Y of the section's minimum corner
     * @param sectionOriginZ world Z of the section's minimum corner
     * @param destination   buffer to write into; must hold
     *                      {@code vertexCount * PackedVertexFormat.STRIDE} bytes
     * @return the number of vertices converted
     * @throws IllegalArgumentException if the source length is not a whole number of vertices, or the
     *                                  destination is too small
     */
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

        // Native order matches what the GPU expects and what Minecraft itself uses, so the buffer
        // can be handed to GL without a byte-swapping copy.
        ByteBuffer src = source.duplicate().order(ByteOrder.nativeOrder());
        ByteBuffer dst = destination.duplicate().order(ByteOrder.nativeOrder());

        for (int i = 0; i < vertexCount; i++) {
            int srcBase = i * sourceStride;

            float x = src.getFloat(srcBase + VANILLA_POSITION_OFFSET);
            float y = src.getFloat(srcBase + VANILLA_POSITION_OFFSET + 4);
            float z = src.getFloat(srcBase + VANILLA_POSITION_OFFSET + 8);

            // Colour is already four bytes in vanilla; take it as a single word rather than four
            // reads, then re-emit it in the packed layout's RGBA order.
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

            // Vanilla writes colour as four bytes A, B, G, R in memory order, so a native-order
            // int read of those bytes yields 0xAABBGGRR. Red is therefore the MOST significant
            // byte, not the least - getting this backwards swaps red and blue across every chunk.
            dst.putInt(dstBase + PackedVertexFormat.COLOR_OFFSET,
                    PackedVertexFormat.packColor(
                            (vanillaColor >>> 24) & 0xFF,
                            (vanillaColor >>> 16) & 0xFF,
                            (vanillaColor >>> 8) & 0xFF,
                            vanillaColor & 0xFF));

            dst.putShort(dstBase + PackedVertexFormat.UV_OFFSET,
                    (short) PackedVertexFormat.quantiseUv(u));
            dst.putShort(dstBase + PackedVertexFormat.UV_OFFSET + 2,
                    (short) PackedVertexFormat.quantiseUv(v));

            dst.putShort(dstBase + PackedVertexFormat.LIGHT_OFFSET,
                    (short) PackedVertexFormat.packLight(blockLight, skyLight));
        }

        return vertexCount;
    }

    /**
     * Compacts and reports how much was saved. The stats exist because "we halved the mesh" is only
     * a useful claim if it can be measured per device.
     */
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
        // Restore the caller's position; compact() works on a duplicate and must not move the
        // original, or a caller that reuses the buffer would silently skip vertices.
        source.position(sourcePosition);
        return stats;
    }

    /**
     * Computes the exact destination size for a source buffer, so callers can allocate without
     * guessing.
     */
    public static int destinationSize(int sourceByteCount, int sourceStride) {
        return (sourceByteCount / sourceStride) * PackedVertexFormat.STRIDE;
    }
}
