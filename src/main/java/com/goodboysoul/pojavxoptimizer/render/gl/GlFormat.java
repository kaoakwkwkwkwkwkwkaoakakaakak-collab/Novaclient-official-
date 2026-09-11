package com.goodboysoul.pojavxoptimizer.render.gl;

public final class GlFormat {

    private final int componentCount;
    private final int componentBytes;
    private final boolean normalised;
    private final boolean signed;
    private final boolean integer;

    private GlFormat(int componentCount, int componentBytes, boolean normalised,
                     boolean signed, boolean integer) {
        this.componentCount = componentCount;
        this.componentBytes = componentBytes;
        this.normalised = normalised;
        this.signed = signed;
        this.integer = integer;
    }

    public static GlFormat unsignedByte(int components, boolean normalised) {
        return new GlFormat(components, 1, normalised, false, !normalised);
    }

    public static GlFormat unsignedShort(int components, boolean normalised) {
        return new GlFormat(components, 2, normalised, false, !normalised);
    }

    public static GlFormat signedShort(int components, boolean normalised) {
        return new GlFormat(components, 2, normalised, true, !normalised);
    }

    public static GlFormat unsignedInt(int components) {
        return new GlFormat(components, 4, false, false, true);
    }

    public static GlFormat float32(int components) {
        return new GlFormat(components, 4, false, true, false);
    }

    public int componentCount() {
        return componentCount;
    }

    public int componentBytes() {
        return componentBytes;
    }

    public int byteSize() {
        return componentCount * componentBytes;
    }

    public boolean isNormalised() {
        return normalised;
    }

    public boolean isSigned() {
        return signed;
    }

    public boolean isInteger() {
        return integer;
    }

    public float maxValue() {
        if (normalised) {
            return 1.0f;
        }
        return switch (componentBytes) {
            case 1 -> signed ? 127.0f : 255.0f;
            case 2 -> signed ? 32767.0f : 65535.0f;
            case 4 -> signed ? 2147483647.0f : 4294967295.0f;
            default -> 0.0f;
        };
    }

    public float minValue() {
        if (normalised) {
            return signed ? -1.0f : 0.0f;
        }
        return signed ? -maxValue() - 1.0f : 0.0f;
    }

    @Override
    public String toString() {
        String base = switch (componentBytes) {
            case 1 -> "8";
            case 2 -> "16";
            case 4 -> "32";
            default -> "?";
        };
        String type = switch (componentBytes) {
            case 4 -> integer ? "UINT" : "FLOAT";
            default -> {
                String sign = signed ? "" : "U";
                yield integer ? sign + "INT" : sign + "NORM";
            }
        };
        String prefix = switch (componentCount) {
            case 1 -> "R";
            case 2 -> "RG";
            case 3 -> "RGB";
            case 4 -> "RGBA";
            default -> "V" + componentCount;
        };
        return prefix + base + "_" + type;
    }
}
