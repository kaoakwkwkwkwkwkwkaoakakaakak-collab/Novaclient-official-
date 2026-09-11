package com.goodboysoul.pojavxoptimizer.render.vertex;

import com.goodboysoul.pojavxoptimizer.render.gl.GlFormat;

public final class VertexAttribute {

    private final String name;
    private final GlFormat format;
    private final int offset;
    private final int shaderLocation;

    VertexAttribute(String name, GlFormat format, int offset, int shaderLocation) {
        this.name = name;
        this.format = format;
        this.offset = offset;
        this.shaderLocation = shaderLocation;
    }

    public static VertexAttribute at(String name, GlFormat format, int offset, int shaderLocation) {
        return new VertexAttribute(name, format, offset, shaderLocation);
    }

    public String name() {
        return name;
    }

    public GlFormat format() {
        return format;
    }

    public int offset() {
        return offset;
    }

    public int shaderLocation() {
        return shaderLocation;
    }

    public int byteSize() {
        return format.byteSize();
    }

    @Override
    public String toString() {
        return name + "@" + offset + " " + format;
    }
}
