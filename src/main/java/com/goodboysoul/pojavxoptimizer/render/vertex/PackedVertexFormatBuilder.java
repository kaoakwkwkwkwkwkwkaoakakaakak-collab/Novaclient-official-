package com.goodboysoul.pojavxoptimizer.render.vertex;

import com.goodboysoul.pojavxoptimizer.render.gl.GlFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PackedVertexFormatBuilder {

    public static final String POSITION = "a_Position";
    public static final String COLOR = "a_Color";
    public static final String TEXTURE = "a_TexCoord";
    public static final String LIGHT = "a_Light";

    private final List<VertexAttribute> attributes = new ArrayList<>();
    private int cursor;

    public PackedVertexFormatBuilder position() {
        attributes.add(VertexAttribute.at(POSITION, GlFormat.signedShort(3, false),
                cursor, 0));
        cursor += 6;
        return this;
    }

    public PackedVertexFormatBuilder color() {
        attributes.add(VertexAttribute.at(COLOR, GlFormat.unsignedByte(4, true),
                cursor, 1));
        cursor += 4;
        return this;
    }

    public PackedVertexFormatBuilder texture() {
        attributes.add(VertexAttribute.at(TEXTURE, GlFormat.unsignedShort(2, false),
                cursor, 2));
        cursor += 4;
        return this;
    }

    public PackedVertexFormatBuilder light() {
        attributes.add(VertexAttribute.at(LIGHT, GlFormat.unsignedByte(2, true),
                cursor, 3));
        cursor += 2;
        return this;
    }

    public int stride() {
        return cursor;
    }

    public List<VertexAttribute> attributes() {
        return Collections.unmodifiableList(attributes);
    }

    public VertexAttribute require(String name) {
        for (VertexAttribute attribute : attributes) {
            if (attribute.name().equals(name)) {
                return attribute;
            }
        }
        throw new IllegalArgumentException("no attribute named " + name);
    }

    public static PackedVertexFormatBuilder standard() {
        PackedVertexFormatBuilder builder = new PackedVertexFormatBuilder()
                .position().color().texture().light();

        if (builder.stride() != PackedVertexFormat.STRIDE) {
            throw new IllegalStateException("built stride " + builder.stride()
                    + " does not match PackedVertexFormat.STRIDE " + PackedVertexFormat.STRIDE);
        }
        requireOffset(builder, POSITION, PackedVertexFormat.POSITION_OFFSET);
        requireOffset(builder, COLOR, PackedVertexFormat.COLOR_OFFSET);
        requireOffset(builder, TEXTURE, PackedVertexFormat.UV_OFFSET);
        requireOffset(builder, LIGHT, PackedVertexFormat.LIGHT_OFFSET);
        return builder;
    }

    private static void requireOffset(PackedVertexFormatBuilder builder, String name, int expected) {
        int actual = builder.require(name).offset();
        if (actual != expected) {
            throw new IllegalStateException(name + " sits at " + actual + ", expected " + expected);
        }
    }
}
