package com.goodboysoul.pojavxoptimizer.render.vertex;

/**
 * The packed vertex layout PojavXOptimizer uploads to the GPU.
 *
 * <h2>Why this is the single largest win available on a phone</h2>
 *
 * <p>Vanilla's {@code DefaultVertexFormat.BLOCK} stores every vertex as four floats for position,
 * four unsigned bytes for colour, two floats for texture coordinates, and two more floats for the
 * lightmap — <b>32 bytes per vertex</b>. A loaded world at render distance 12 holds millions of
 * vertices, so that layout costs tens of megabytes of VRAM and, more importantly, tens of megabytes
 * pushed across the bus every time chunks are rebuilt.
 *
 * <p>On a phone that bus is not a PCIe lane. It is shared memory, often with the CPU and GPU
 * contending for the same bandwidth, and every byte that crosses it competes with everything else
 * the system is doing. Halving vertex size therefore does not just halve VRAM; it roughly halves the
 * time spent on uploads and on vertex fetch during rendering. This is where the bulk of Sodium's
 * gain comes from on desktop, and on constrained mobile hardware the same change is worth more, not
 * less.
 *
 * <h2>The layout</h2>
 *
 * <pre>
 *   offset  size  contents
 *   0       6     position, three shorts, relative to the section origin, 1/16 block units
 *   6       4     colour, four unsigned bytes RGBA
 *   10      4     texture uv, two unsigned shorts normalised to [0,1]
 *   14      2     lightmap, two unsigned bytes
 *   ------------------------------
 *   total  16 bytes
 * </pre>
 *
 * <p>Position is stored relative to the section origin because a chunk section is 16 blocks across.
 * Sixteen blocks in 1/16-block units is 256, which fits in a short with a great deal of room to
 * spare. Storing absolute world coordinates would not fit, and would also destroy the precision that
 * makes short positions viable.
 *
 * <h2>What is deliberately not here</h2>
 *
 * <p>No normals. Vanilla's block format carries no normal either — block lighting is baked into the
 * per-vertex colour, which is exactly why the four colour bytes can stay and a normal cannot be
 * added without growing the format back out.
 */
public final class PackedVertexFormat {

    /** Bytes per vertex in this format. */
    public static final int STRIDE = 16;

    /** Bytes per vertex in vanilla's {@code DefaultVertexFormat.BLOCK}, for comparison and checks. */
    public static final int VANILLA_BLOCK_STRIDE = 32;

    public static final int POSITION_OFFSET = 0;
    public static final int COLOR_OFFSET = 6;
    public static final int UV_OFFSET = 10;
    public static final int LIGHT_OFFSET = 14;

    /**
     * Positions are stored in 1/16 block units so that a block face's sub-block detail survives the
     * conversion to integers. A section is 16 blocks, so the range is [0, 256].
     */
    public static final int POSITION_SCALE = 16;

    /** Largest coordinate representable at {@link #POSITION_SCALE} within a 16-block section. */
    public static final int MAX_SECTION_COORD = 16 * POSITION_SCALE;

    private PackedVertexFormat() {
    }

    /**
     * Quantises a section-relative block coordinate into the stored short form.
     *
     * @param relative coordinate relative to the section origin, in blocks, [0, 16]
     * @return the stored value, clamped to the representable range
     */
    public static short quantisePosition(float relative) {
        int scaled = Math.round(relative * POSITION_SCALE);
        if (scaled < Short.MIN_VALUE) {
            return Short.MIN_VALUE;
        }
        if (scaled > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }
        return (short) scaled;
    }

    /**
     * Reverses {@link #quantisePosition(float)}. Used by the tests and by anything that needs to
     * read the format back, such as the frustum bounds computation.
     */
    public static float dequantisePosition(short stored) {
        return stored / (float) POSITION_SCALE;
    }

    /**
     * Normalises a texture coordinate into the stored unsigned short form.
     *
     * <p>Vanilla stores uv as a float. Here it becomes a 16-bit value, which is a far finer
     * quantisation than an atlas texture actually needs — a block atlas is 256 or 512 pixels across,
     * so 16 bits is more precision than the source data has. Nothing is visibly lost.
     *
     * @param u the float coordinate, normally in [0, 1]
     * @return the stored unsigned short, as a widened int
     */
    public static int quantiseUv(float u) {
        if (Float.isNaN(u)) {
            return 0;
        }
        float clamped = u < 0.0f ? 0.0f : Math.min(u, 1.0f);
        return Math.round(clamped * 65535.0f);
    }

    /** Reverses {@link #quantiseUv(float)}. */
    public static float dequantiseUv(int stored) {
        return (stored & 0xFFFF) / 65535.0f;
    }

    /**
     * Packs four 8-bit colour channels into the stored word, in RGBA order with red in the most
     * significant byte.
     */
    public static int packColor(int r, int g, int b, int a) {
        return ((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF);
    }

    public static int colorRed(int packed) {
        return (packed >>> 24) & 0xFF;
    }

    public static int colorGreen(int packed) {
        return (packed >>> 16) & 0xFF;
    }

    public static int colorBlue(int packed) {
        return (packed >>> 8) & 0xFF;
    }

    public static int colorAlpha(int packed) {
        return packed & 0xFF;
    }

    /**
     * Packs the two lightmap coordinates into one word.
     *
     * <p>Vanilla passes these as a float pair encoding block light and sky light, each in [0, 15].
     * Eight bits per channel is four times finer than the 16 distinct light levels the game
     * actually produces.
     */
    public static int packLight(float blockLight, float skyLight) {
        int bl = clampByte(Math.round(normaliseLight(blockLight) * 255.0f));
        int sl = clampByte(Math.round(normaliseLight(skyLight) * 255.0f));
        return (bl << 8) | sl;
    }

    /**
     * Maps a raw lightmap float to [0, 1].
     *
     * <p>Minecraft encodes lightmap coordinates so that level 15 sits at 1.0; the encoding is not a
     * plain division, but for the purpose of packing into a byte a linear normalisation preserves the
     * ordering and the endpoints, which is what the shader needs to reproduce vanilla's lighting.
     */
    private static float normaliseLight(float light) {
        if (Float.isNaN(light)) {
            return 0.0f;
        }
        float clamped = light < 0.0f ? 0.0f : Math.min(light, 1.0f);
        return clamped;
    }

    private static int clampByte(int value) {
        return value < 0 ? 0 : Math.min(value, 255);
    }

    public static int lightBlock(int packed) {
        return (packed >>> 8) & 0xFF;
    }

    public static int lightSky(int packed) {
        return packed & 0xFF;
    }

    /**
     * Vertices saved per section is not a meaningful number, but bytes saved is. This returns the
     * byte reduction for a given vertex count, which is what the debug overlay reports.
     */
    public static long bytesSaved(int vertexCount) {
        return (long) vertexCount * (VANILLA_BLOCK_STRIDE - STRIDE);
    }

    /** Compression ratio as a percentage reduction, for display. */
    public static int reductionPercent() {
        return Math.round((VANILLA_BLOCK_STRIDE - STRIDE) * 100.0f / VANILLA_BLOCK_STRIDE);
    }
}
