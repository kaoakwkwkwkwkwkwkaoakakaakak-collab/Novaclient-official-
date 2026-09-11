package com.goodboysoul.pojavxoptimizer.render.vertex;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the vertex compaction is correct.
 *
 * <p>These tests exist because the compactor is the one part of this mod that can be verified
 * without a phone. It is pure arithmetic over byte buffers: no GL context, no display, no Minecraft
 * instance. So the claim "we halve the mesh without losing anything visible" can actually be checked
 * here rather than asserted.
 */
class SectionMeshCompactorTest {

    private static final int VANILLA_STRIDE = 32;

    private final SectionMeshCompactor compactor = new SectionMeshCompactor();

    /** Writes one vertex in vanilla's BLOCK layout. */
    private static void putVanillaVertex(ByteBuffer buffer, float x, float y, float z,
                                         int r, int g, int b, int a,
                                         float u, float v,
                                         float blockLight, float skyLight) {
        buffer.putFloat(x).putFloat(y).putFloat(z);
        // Vanilla stores ABGR in this order; the compactor is expected to re-emit RGBA.
        buffer.put((byte) a).put((byte) b).put((byte) g).put((byte) r);
        buffer.putFloat(u).putFloat(v);
        buffer.putFloat(blockLight).putFloat(skyLight);
    }

    @Test
    void halvesTheBufferSize() {
        ByteBuffer source = ByteBuffer.allocate(VANILLA_STRIDE * 4).order(ByteOrder.nativeOrder());
        for (int i = 0; i < 4; i++) {
            putVanillaVertex(source, i, i, i, 255, 128, 64, 255, 0.25f, 0.75f, 1.0f, 0.5f);
        }
        source.flip();

        ByteBuffer destination = ByteBuffer
                .allocate(SectionMeshCompactor.destinationSize(source.remaining(), VANILLA_STRIDE))
                .order(ByteOrder.nativeOrder());

        int count = compactor.compact(source, VANILLA_STRIDE, 0, 0, 0, destination);

        assertEquals(4, count, "all four vertices should convert");
        assertEquals(source.limit(), 4 * VANILLA_STRIDE);
        assertEquals(4 * PackedVertexFormat.STRIDE, destination.limit(),
                "output must be exactly half the input for a 32-byte source");
    }

    @Test
    void positionIsQuantisedExactlyOnTheSixteenthGrid() {
        // Vanilla block models are authored on a 1/16 grid, so this must round-trip with no loss.
        ByteBuffer source = ByteBuffer.allocate(VANILLA_STRIDE).order(ByteOrder.nativeOrder());
        putVanillaVertex(source, 0.0625f, 0.5f, 1.0f, 0, 0, 0, 255, 0, 0, 0, 0);
        source.flip();

        ByteBuffer destination = ByteBuffer
                .allocate(PackedVertexFormat.STRIDE).order(ByteOrder.nativeOrder());
        compactor.compact(source, VANILLA_STRIDE, 0, 0, 0, destination);

        destination.rewind();
        short x = destination.getShort(PackedVertexFormat.POSITION_OFFSET);
        short y = destination.getShort(PackedVertexFormat.POSITION_OFFSET + 2);
        short z = destination.getShort(PackedVertexFormat.POSITION_OFFSET + 4);

        assertEquals(1, x, "1/16 of a block must quantise to exactly 1");
        assertEquals(8, y, "half a block must quantise to exactly 8");
        assertEquals(16, z, "one whole block must quantise to exactly 16");

        assertEquals(0.0625f, PackedVertexFormat.dequantisePosition(x), 1e-6f);
        assertEquals(0.5f, PackedVertexFormat.dequantisePosition(y), 1e-6f);
        assertEquals(1.0f, PackedVertexFormat.dequantisePosition(z), 1e-6f);
    }

    @Test
    void positionIsStoredRelativeToTheSectionOrigin() {
        // A section at world (64, 32, -16) must store small values, which is what lets a short work.
        ByteBuffer source = ByteBuffer.allocate(VANILLA_STRIDE).order(ByteOrder.nativeOrder());
        putVanillaVertex(source, 65.5f, 33.0f, -15.5f, 0, 0, 0, 255, 0, 0, 0, 0);
        source.flip();

        ByteBuffer destination = ByteBuffer
                .allocate(PackedVertexFormat.STRIDE).order(ByteOrder.nativeOrder());
        compactor.compact(source, VANILLA_STRIDE, 64, 32, -16, destination);

        destination.rewind();
        assertEquals(24, destination.getShort(PackedVertexFormat.POSITION_OFFSET),
                "65.5 - 64 = 1.5 blocks = 24 units");
        assertEquals(16, destination.getShort(PackedVertexFormat.POSITION_OFFSET + 2));
        assertEquals(8, destination.getShort(PackedVertexFormat.POSITION_OFFSET + 4),
                "-15.5 - (-16) = 0.5 blocks = 8 units");
    }

    @Test
    void colourIsPreservedWithoutLoss() {
        ByteBuffer source = ByteBuffer.allocate(VANILLA_STRIDE).order(ByteOrder.nativeOrder());
        putVanillaVertex(source, 0, 0, 0, 200, 100, 50, 25, 0, 0, 0, 0);
        source.flip();

        ByteBuffer destination = ByteBuffer
                .allocate(PackedVertexFormat.STRIDE).order(ByteOrder.nativeOrder());
        compactor.compact(source, VANILLA_STRIDE, 0, 0, 0, destination);

        int packed = destination.getInt(PackedVertexFormat.COLOR_OFFSET);
        assertEquals(200, PackedVertexFormat.colorRed(packed));
        assertEquals(100, PackedVertexFormat.colorGreen(packed));
        assertEquals(50, PackedVertexFormat.colorBlue(packed));
        assertEquals(25, PackedVertexFormat.colorAlpha(packed));
    }

    @Test
    void textureCoordinatesSurviveAtSixteenBitPrecision() {
        float[] samples = {0.0f, 0.00390625f, 0.25f, 0.5f, 0.75f, 0.99609375f, 1.0f};
        for (float sample : samples) {
            int quantised = PackedVertexFormat.quantiseUv(sample);
            float back = PackedVertexFormat.dequantiseUv(quantised);
            // A 512px atlas needs 1/512 = 0.00195 precision; 16 bits gives 0.0000153.
            assertTrue(Math.abs(back - sample) < 1.0f / 65535.0f,
                    "uv " + sample + " round-tripped to " + back);
        }
    }

    @Test
    void outOfRangeInputIsClampedRatherThanWrapped() {
        // A negative or >1 coordinate must clamp. Wrapping would sample the opposite edge of the
        // atlas, which shows up as visibly wrong textures rather than as a crash.
        assertEquals(0, PackedVertexFormat.quantiseUv(-1.0f));
        assertEquals(65535, PackedVertexFormat.quantiseUv(2.0f));
        assertEquals(0, PackedVertexFormat.quantiseUv(Float.NaN));
    }

    @Test
    void lightLevelsArePreserved() {
        int packed = PackedVertexFormat.packLight(1.0f, 0.5f);
        assertEquals(255, PackedVertexFormat.lightBlock(packed));
        assertEquals(128, PackedVertexFormat.lightSky(packed),
                "0.5 must map to the midpoint");

        int clamped = PackedVertexFormat.packLight(9.0f, -3.0f);
        assertEquals(255, PackedVertexFormat.lightBlock(clamped));
        assertEquals(0, PackedVertexFormat.lightSky(clamped));
    }

    @Test
    void aWholeSectionCompactsCorrectly() {
        // A realistic section: 4000 quads = 16000 vertices, which is typical for dense terrain.
        int vertexCount = 16_000;
        ByteBuffer source = ByteBuffer.allocate(vertexCount * VANILLA_STRIDE)
                .order(ByteOrder.nativeOrder());
        for (int i = 0; i < vertexCount; i++) {
            float f = (i % 256) / 16.0f;
            putVanillaVertex(source, f, f, f, i % 256, (i * 7) % 256, (i * 13) % 256, 255,
                    (i % 16) / 16.0f, (i % 8) / 8.0f, (i % 16) / 15.0f, (i % 16) / 15.0f);
        }
        source.flip();

        ByteBuffer destination = ByteBuffer
                .allocate(SectionMeshCompactor.destinationSize(source.remaining(), VANILLA_STRIDE))
                .order(ByteOrder.nativeOrder());

        SectionMeshCompactor.Stats stats =
                compactor.compactWithStats(source, VANILLA_STRIDE, 0, 0, 0, destination);

        assertEquals(vertexCount, stats.vertexCount());
        assertEquals(vertexCount * 32L, stats.inputBytes());
        assertEquals(vertexCount * 16L, stats.outputBytes());
        assertEquals(vertexCount * 16L, stats.bytesSaved(),
                "a 16k-vertex section must save exactly 256 KiB");
    }

    @Test
    void compactionDoesNotConsumeTheSourceBuffer() {
        // Callers reuse source buffers across sections. If compact() advanced the caller's
        // position, the second call would silently skip vertices.
        ByteBuffer source = ByteBuffer.allocate(VANILLA_STRIDE * 2).order(ByteOrder.nativeOrder());
        putVanillaVertex(source, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0);
        putVanillaVertex(source, 1, 1, 1, 0, 0, 0, 255, 0, 0, 0, 0);
        source.flip();

        ByteBuffer destination = ByteBuffer
                .allocate(PackedVertexFormat.STRIDE * 2).order(ByteOrder.nativeOrder());

        compactor.compactWithStats(source, VANILLA_STRIDE, 0, 0, 0, destination);
        int positionAfterFirst = source.position();
        compactor.compactWithStats(source, VANILLA_STRIDE, 0, 0, 0, destination);

        assertEquals(positionAfterFirst, source.position(),
                "the caller's buffer position must not move");
    }

    @Test
    void aRaggedSourceIsRejectedInsteadOfSilentlyTruncated() {
        ByteBuffer source = ByteBuffer.allocate(VANILLA_STRIDE + 7).order(ByteOrder.nativeOrder());
        source.limit(VANILLA_STRIDE + 7);
        ByteBuffer destination = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());

        assertThrows(IllegalArgumentException.class,
                () -> compactor.compact(source, VANILLA_STRIDE, 0, 0, 0, destination),
                "a partial vertex must be an error, not a silently dropped one");
    }

    @Test
    void anUndersizedDestinationIsRejected() {
        // Note: four real vertices must be written before flip(). flip() sets limit to the
        // current position, so flipping an unwritten buffer yields zero remaining bytes and
        // zero vertices, which would skip the guard this test exists to check.
        ByteBuffer source = ByteBuffer.allocate(VANILLA_STRIDE * 4).order(ByteOrder.nativeOrder());
        for (int i = 0; i < 4; i++) {
            putVanillaVertex(source, 0, 0, 0, 0, 0, 0, 255, 0, 0, 0, 0);
        }
        source.flip();
        ByteBuffer destination = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder());

        assertThrows(IllegalArgumentException.class,
                () -> compactor.compact(source, VANILLA_STRIDE, 0, 0, 0, destination),
                "64 bytes of output cannot fit in a 4-byte destination");
    }

    @Test
    void reductionIsExactlyFiftyPercent() {
        assertEquals(50, PackedVertexFormat.reductionPercent());
        assertEquals(16, PackedVertexFormat.bytesSaved(1));
    }
}
