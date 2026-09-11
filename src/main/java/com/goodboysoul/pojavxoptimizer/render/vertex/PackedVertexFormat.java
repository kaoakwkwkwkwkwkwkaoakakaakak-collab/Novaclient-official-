package com.goodboysoul.pojavxoptimizer.render.vertex;

public final class PackedVertexFormat {

    public static final int STRIDE = 16;

    public static final int VANILLA_BLOCK_STRIDE = 32;

    public static final int REFERENCE_COMPACT_STRIDE = 20;

    public static final int POSITION_OFFSET = 0;
    public static final int COLOR_OFFSET = 6;
    public static final int UV_OFFSET = 10;
    public static final int LIGHT_OFFSET = 14;

    public static final int POSITION_SCALE = 16;

    public static final int MAX_SECTION_COORD = 16 * POSITION_SCALE;

    public static final int TEXTURE_MAX_VALUE = 1 << 15;

    public static final int TEXTURE_BIAS_SIGN_BIT = 1 << 15;

    public static final int TEXTURE_MAGNITUDE_MASK = TEXTURE_MAX_VALUE - 1;

    public static final int LIGHTMAP_ZERO = 8;

    public static final float LIGHTMAP_RANGE = 240.0f;

    public static final int LIGHTMAP_MAX = 248;

    private PackedVertexFormat() {
    }

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

    public static float dequantisePosition(short stored) {
        return stored / (float) POSITION_SCALE;
    }

    public static float texelCentre(float atlasResolution, float texel) {
        return (texel + 0.5f) / atlasResolution;
    }

    public static int quantiseUv(float u, float centre) {
        if (Float.isNaN(u)) {
            return 0;
        }
        float clamped = u < 0.0f ? 0.0f : Math.min(u, 1.0f);
        float centreSafe = Float.isNaN(centre) ? clamped : centre;

        int bias = clamped < centreSafe ? 1 : -1;

        int quantised = Math.round(clamped * TEXTURE_MAX_VALUE) + bias;

        int magnitude = quantised < 0 ? 0 : Math.min(quantised, TEXTURE_MAGNITUDE_MASK);
        int sign = bias < 0 ? TEXTURE_BIAS_SIGN_BIT : 0;
        return magnitude | sign;
    }

    public static float dequantiseUv(int stored) {
        int magnitude = stored & TEXTURE_MAGNITUDE_MASK;
        boolean biasNegative = (stored & TEXTURE_BIAS_SIGN_BIT) != 0;
        float correction = biasNegative ? -0.5f : 0.5f;
        float recovered = (magnitude - correction) / (float) TEXTURE_MAX_VALUE;
        return recovered < 0.0f ? 0.0f : Math.min(recovered, 1.0f);
    }

    public static float uvEpsilon(float atlasResolution) {
        return 0.5f / (float) TEXTURE_MAX_VALUE / atlasResolution;
    }

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

    public static int packLight(float blockLight, float skyLight) {
        int bl = encodeLight(blockLight);
        int sl = encodeLight(skyLight);
        return (bl << 8) | sl;
    }

    public static int encodeLight(float light) {
        if (Float.isNaN(light)) {
            return LIGHTMAP_ZERO;
        }
        float scaled = light * LIGHTMAP_RANGE + LIGHTMAP_ZERO;
        int value = Math.round(scaled);
        if (value < LIGHTMAP_ZERO) {
            return LIGHTMAP_ZERO;
        }
        if (value > LIGHTMAP_MAX) {
            return LIGHTMAP_MAX;
        }
        return value;
    }

    public static float lightLevelFromByte(int storedByte) {
        int value = storedByte < 0 ? 0 : Math.min(storedByte, 255);
        float level = value - LIGHTMAP_ZERO;
        if (level < 0.0f) {
            level = 0.0f;
        }
        if (level > LIGHTMAP_RANGE) {
            level = LIGHTMAP_RANGE;
        }
        return level / LIGHTMAP_RANGE;
    }

    public static int lightBlock(int packed) {
        return (packed >>> 8) & 0xFF;
    }

    public static int lightSky(int packed) {
        return packed & 0xFF;
    }

    public static long bytesSaved(int vertexCount) {
        return (long) vertexCount * (VANILLA_BLOCK_STRIDE - STRIDE);
    }

    public static int reductionPercent() {
        return Math.round((VANILLA_BLOCK_STRIDE - STRIDE) * 100.0f / VANILLA_BLOCK_STRIDE);
    }
}
