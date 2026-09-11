package com.goodboysoul.pojavxoptimizer.render.vertex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionMeshCompactorTest {

    private static final int VANILLA_STRIDE = 32;
    private static final SectionMeshCompactor COMPACTOR = new SectionMeshCompactor();

    private static void writeVertex(ByteBuffer buf, float x, float y, float z,
                                    int r, int g, int b, int a,
                                    float u, float v, float blockLight, float skyLight) {
        buf.putFloat(x);
        buf.putFloat(y);
        buf.putFloat(z);

        buf.put((byte) a).put((byte) b).put((byte) g).put((byte) r);
        buf.putFloat(u);
        buf.putFloat(v);
        buf.putFloat(blockLight);
        buf.putFloat(skyLight);
    }

    private static ByteBuffer vanillaBuffer(int vertexCount) {
        return ByteBuffer.allocate(vertexCount * VANILLA_STRIDE).order(ByteOrder.nativeOrder());
    }

    @Test
    @DisplayName("the packed format is exactly half of vanilla's")
    void reductionIsExactlyFiftyPercent() {
        assertEquals(32, PackedVertexFormat.VANILLA_BLOCK_STRIDE);
        assertEquals(16, PackedVertexFormat.STRIDE);
        assertEquals(50, PackedVertexFormat.reductionPercent());
        assertEquals(16L * 1024L, PackedVertexFormat.bytesSaved(1024));
    }

    @Test
    @DisplayName("a 16k-vertex section saves exactly 256 KiB")
    void aWholeSectionCompactsCorrectly() {
        int vertexCount = 16384;
        ByteBuffer source = vanillaBuffer(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            writeVertex(source, i % 16, (i / 16) % 16, i / 256 % 16,
                    200, 100, 50, 255, 0.25f, 0.75f, 0.5f, 0.5f);
        }
        source.flip();

        ByteBuffer destination =
                ByteBuffer.allocate(vertexCount * PackedVertexFormat.STRIDE)
                        .order(ByteOrder.nativeOrder());

        SectionMeshCompactor.Stats stats =
                COMPACTOR.compactWithStats(source, VANILLA_STRIDE, 0f, 0f, 0f, destination);

        assertEquals(vertexCount, stats.vertexCount());
        assertEquals(vertexCount * 32L, stats.inputBytes());
        assertEquals(vertexCount * 16L, stats.outputBytes());
        assertEquals(256L * 1024L, stats.bytesSaved());
    }

    @Test
    @DisplayName("halving the buffer size is reflected in destinationSize")
    void halvesTheBufferSize() {
        assertEquals(512, SectionMeshCompactor.destinationSize(1024, VANILLA_STRIDE));
        assertEquals(0, SectionMeshCompactor.destinationSize(31, VANILLA_STRIDE));
    }

    @Test
    @DisplayName("a ragged source is rejected rather than silently truncated")
    void aRaggedSourceIsRejectedInsteadOfSilentlyTruncated() {
        ByteBuffer source = vanillaBuffer(2);
        writeVertex(source, 0, 0, 0, 255, 255, 255, 255, 0, 0, 0, 0);
        writeVertex(source, 1, 1, 1, 255, 255, 255, 255, 0, 0, 0, 0);
        source.flip();
        source.limit(source.limit() - 7);

        ByteBuffer destination =
                ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> COMPACTOR.compact(source, VANILLA_STRIDE, 0f, 0f, 0f, destination));
        assertTrue(thrown.getMessage().contains("not a whole number"),
                "expected the message to explain the misalignment, got: " + thrown.getMessage());
    }

    @Test
    @DisplayName("an undersized destination is rejected, not overrun")
    void anUndersizedDestinationIsRejected() {
        ByteBuffer source = vanillaBuffer(4);
        for (int i = 0; i < 4; i++) {
            writeVertex(source, i, 0, 0, 255, 255, 255, 255, 0, 0, 0, 0);
        }
        source.flip();
        assertEquals(128, source.remaining());

        ByteBuffer destination = ByteBuffer.allocate(63).order(ByteOrder.nativeOrder());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> COMPACTOR.compact(source, VANILLA_STRIDE, 0f, 0f, 0f, destination));
        assertTrue(thrown.getMessage().contains("needs 64"),
                "expected the message to state the requirement, got: " + thrown.getMessage());
    }

    @Test
    @DisplayName("position quantises exactly on the 1/16 grid the models use")
    void positionIsQuantisedExactlyOnTheSixteenthGrid() {
        for (int sixteenths = 0; sixteenths <= 256; sixteenths++) {
            float coordinate = sixteenths / 16.0f;
            short stored = PackedVertexFormat.quantisePosition(coordinate);
            assertEquals(sixteenths, stored,
                    "a model coordinate on the 1/16 grid must survive without rounding");
            assertEquals(coordinate, PackedVertexFormat.dequantisePosition(stored), 1e-6f);
        }
    }

    @Test
    @DisplayName("positions are stored relative to the section origin")
    void positionIsStoredRelativeToTheSectionOrigin() {
        ByteBuffer source = vanillaBuffer(1);
        writeVertex(source, 100.0f, 64.5f, -32.0f, 255, 255, 255, 255, 0, 0, 0, 0);
        source.flip();

        ByteBuffer destination =
                ByteBuffer.allocate(PackedVertexFormat.STRIDE).order(ByteOrder.nativeOrder());
        COMPACTOR.compact(source, VANILLA_STRIDE, 100.0f, 64.0f, -32.0f, destination);

        short x = destination.getShort(PackedVertexFormat.POSITION_OFFSET);
        short y = destination.getShort(PackedVertexFormat.POSITION_OFFSET + 2);
        short z = destination.getShort(PackedVertexFormat.POSITION_OFFSET + 4);

        assertEquals(0, x, "the origin itself must quantise to zero");
        assertEquals(8, y, "half a block above the origin is 8 sixteenths");
        assertEquals(0, z);
        assertTrue(Math.abs(x) <= PackedVertexFormat.MAX_SECTION_COORD);
    }

    @Test
    @DisplayName("colour survives the round trip with red in the right channel")
    void colourIsPreservedWithoutLoss() {
        ByteBuffer source = vanillaBuffer(1);
        writeVertex(source, 0, 0, 0, 200, 100, 50, 255, 0, 0, 0, 0);
        source.flip();

        ByteBuffer destination =
                ByteBuffer.allocate(PackedVertexFormat.STRIDE).order(ByteOrder.nativeOrder());
        COMPACTOR.compact(source, VANILLA_STRIDE, 0f, 0f, 0f, destination);

        int packed = destination.getInt(PackedVertexFormat.COLOR_OFFSET);
        assertEquals(200, PackedVertexFormat.colorRed(packed),
                "red must not be read from the alpha byte");
        assertEquals(100, PackedVertexFormat.colorGreen(packed));
        assertEquals(50, PackedVertexFormat.colorBlue(packed));
        assertEquals(255, PackedVertexFormat.colorAlpha(packed));
    }

    @Test
    @DisplayName("uv quantisation is biased toward the texel centre to stop atlas bleed")
    void textureBleedIsPreventedByTheUvBias() {

        float resolution = SectionMeshCompactor.ATLAS_RESOLUTION;
        float[] samples = {0.0f, 0.001f, 0.25f, 0.5f, 0.7501f, 0.7515f, 0.9999f, 1.0f};

        boolean sawAboveCentre = false;
        boolean sawBelowCentre = false;

        for (float coordinate : samples) {
            float centre = SectionMeshCompactor.texelCentre(coordinate);
            int texel = (int) Math.floor(coordinate * resolution);

            int expectedTexel = Math.min(Math.max(texel, 0), (int) resolution - 1);
            assertEquals((expectedTexel + 0.5f) / resolution, centre, 1e-6f,
                    "texelCentre must return the middle of texel " + expectedTexel);

            int stored = PackedVertexFormat.quantiseUv(coordinate, centre);
            int naive = Math.round(coordinate * PackedVertexFormat.TEXTURE_MAX_VALUE);
            int magnitude = stored & PackedVertexFormat.TEXTURE_MAGNITUDE_MASK;
            boolean signSet = (stored & PackedVertexFormat.TEXTURE_BIAS_SIGN_BIT) != 0;

            if (coordinate < centre) {

                sawBelowCentre = true;
                assertEquals(Math.min(Math.max(0, naive + 1), PackedVertexFormat.TEXTURE_MAGNITUDE_MASK),
                        magnitude,
                        "coordinate " + coordinate + " is below its centre " + centre
                                + " and must be pulled up by one step");
                assertFalse(signSet,
                        "coordinate " + coordinate + " is pulled up, so the bias sign must be clear");
            } else {

                sawAboveCentre = true;
                assertEquals(Math.min(naive - 1, PackedVertexFormat.TEXTURE_MAGNITUDE_MASK), magnitude,
                        "coordinate " + coordinate + " is above its centre " + centre
                                + " and must be pulled down by one step");
                assertTrue(signSet,
                        "coordinate " + coordinate + " is pulled down, so the bias sign must be set");
            }

            assertTrue(Math.abs(magnitude - naive) <= 1,
                    "the bias must move the coordinate by exactly one step");
        }

        assertTrue(sawAboveCentre, "the samples must include a coordinate above its texel centre");
        assertTrue(sawBelowCentre, "the samples must include a coordinate below its texel centre");
    }

    @Test
    @DisplayName("uv still round trips to within a small fraction of a texel")
    void textureCoordinateStillRoundTripsWithinOneTexel() {
        float[] samples = {0.0f, 0.001f, 0.25f, 0.5f, 0.7501f, 0.9999f, 1.0f};

        for (float original : samples) {
            int stored = PackedVertexFormat.quantiseUv(
                    original, SectionMeshCompactor.texelCentre(original));
            float recovered = PackedVertexFormat.dequantiseUv(stored);

            float halfTexel = 0.5f / SectionMeshCompactor.ATLAS_RESOLUTION;
            assertTrue(Math.abs(original - recovered) < halfTexel,
                    "coordinate " + original + " came back as " + recovered
                            + ", which is more than half a texel away");
        }
    }

    @Test
    @DisplayName("the bias epsilon is a fraction of a texel, not half of one")
    void theBiasEpsilonIsFarSmallerThanATexel() {
        float epsilon = PackedVertexFormat.uvEpsilon(SectionMeshCompactor.ATLAS_RESOLUTION);
        float halfTexel = 0.5f / SectionMeshCompactor.ATLAS_RESOLUTION;

        assertTrue(epsilon > 0.0f, "epsilon must be positive or the shader cannot use it");
        assertTrue(epsilon < halfTexel * 0.01f,
                "the whole point of storing the bias sign is a much finer epsilon; got "
                        + epsilon + " against a half texel of " + halfTexel);
    }

    @Test
    @DisplayName("light level 0 is not stored as zero, matching Minecraft's encoding")
    void lightLevelsPreserveMinecraftsEncodedZeroPoint() {
        int packed = PackedVertexFormat.packLight(0.0f, 0.0f);

        int block = PackedVertexFormat.lightBlock(packed);
        int sky = PackedVertexFormat.lightSky(packed);

        assertNotEquals(0, block, "full darkness must not collapse to a zero byte");
        assertNotEquals(0, sky);
        assertEquals(PackedVertexFormat.LIGHTMAP_ZERO, block);
        assertEquals(PackedVertexFormat.LIGHTMAP_ZERO, sky);

        assertEquals(0.0f, PackedVertexFormat.lightLevelFromByte(block), 1e-3f);
    }

    @Test
    @DisplayName("the full light range maps onto the usable byte range")
    void theLightRangeCoversLevelZeroToLevelFifteen() {
        int darkest = PackedVertexFormat.packLight(0.0f, 0.0f);
        int brightest = PackedVertexFormat.packLight(1.0f, 1.0f);

        assertEquals(0.0f, PackedVertexFormat.lightLevelFromByte(
                PackedVertexFormat.lightBlock(darkest)), 1e-3f);
        assertEquals(1.0f, PackedVertexFormat.lightLevelFromByte(
                PackedVertexFormat.lightBlock(brightest)), 1e-3f);

        int previous = -1;
        for (int level = 0; level <= 15; level++) {
            int packed = PackedVertexFormat.packLight(level / 15.0f, level / 15.0f);
            int value = PackedVertexFormat.lightBlock(packed);
            assertTrue(value > previous,
                    "light level " + level + " must be distinguishable from the one below it");
            previous = value;
        }
    }

    @Test
    @DisplayName("out of range light is clamped, not wrapped")
    void outOfRangeLightIsClampedNotWrapped() {
        int packed = PackedVertexFormat.packLight(5.0f, -3.0f);
        assertEquals(PackedVertexFormat.LIGHTMAP_MAX, PackedVertexFormat.lightBlock(packed));
        assertEquals(PackedVertexFormat.LIGHTMAP_ZERO, PackedVertexFormat.lightSky(packed));
    }

    @Test
    @DisplayName("the format is smaller than a 20-byte compact reference layout")
    void formatIsSmallerThanA20ByteReferenceLayout() {

        assertEquals(20, PackedVertexFormat.REFERENCE_COMPACT_STRIDE);
        assertTrue(PackedVertexFormat.STRIDE < PackedVertexFormat.REFERENCE_COMPACT_STRIDE,
                "expected 16 to beat 20");
    }

    @Test
    @DisplayName("compaction does not consume the source buffer")
    void compactionDoesNotConsumeTheSourceBuffer() {
        ByteBuffer source = vanillaBuffer(3);
        for (int i = 0; i < 3; i++) {
            writeVertex(source, i, 0, 0, 10, 20, 30, 255, 0.5f, 0.5f, 0.5f, 0.5f);
        }
        source.flip();

        ByteBuffer destination =
                ByteBuffer.allocate(3 * PackedVertexFormat.STRIDE).order(ByteOrder.nativeOrder());

        COMPACTOR.compactWithStats(source, VANILLA_STRIDE, 0f, 0f, 0f, destination);

        assertEquals(0, source.position(), "the caller's position must be restored");
        assertEquals(3 * VANILLA_STRIDE, source.remaining(),
                "a caller that reuses this buffer must still see every vertex");
    }

    @Test
    @DisplayName("the whole 16-byte layout lands at the documented offsets")
    void theLayoutMatchesTheDocumentation() {
        assertEquals(0, PackedVertexFormat.POSITION_OFFSET);
        assertEquals(6, PackedVertexFormat.COLOR_OFFSET);
        assertEquals(10, PackedVertexFormat.UV_OFFSET);
        assertEquals(14, PackedVertexFormat.LIGHT_OFFSET);

        assertArrayEquals(new int[]{0, 6, 10, 14},
                new int[]{PackedVertexFormat.POSITION_OFFSET,
                        PackedVertexFormat.COLOR_OFFSET,
                        PackedVertexFormat.UV_OFFSET,
                        PackedVertexFormat.LIGHT_OFFSET});
        assertEquals(PackedVertexFormat.STRIDE,
                PackedVertexFormat.LIGHT_OFFSET + 2,
                "the last field plus its width must equal the stride");
    }
}
