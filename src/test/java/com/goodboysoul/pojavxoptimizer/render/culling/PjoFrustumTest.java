package com.goodboysoul.pojavxoptimizer.render.culling;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PjoFrustumTest {

    private static float[] symmetricFrustumPlanes(float near, float far, float halfWidth, float halfHeight) {
        float[] planes = new float[PjoFrustum.PLANE_COUNT * PjoFrustum.COMPONENTS_PER_PLANE];

        planes[0] = 1;  planes[1] = 0;  planes[2] = 0;  planes[3] = halfWidth;
        planes[4] = -1; planes[5] = 0;  planes[6] = 0;  planes[7] = halfWidth;
        planes[8] = 0;  planes[9] = 1;  planes[10] = 0; planes[11] = halfHeight;
        planes[12] = 0; planes[13] = -1; planes[14] = 0; planes[15] = halfHeight;
        planes[16] = 0; planes[17] = 0; planes[18] = -1; planes[19] = -near;
        planes[20] = 0; planes[21] = 0; planes[22] = 1;  planes[23] = far;

        return planes;
    }

    private static boolean bruteForceVisible(float[] planes,
                                             float minX, float minY, float minZ,
                                             float maxX, float maxY, float maxZ) {
        for (int plane = 0; plane < PjoFrustum.PLANE_COUNT; plane++) {
            int base = plane * PjoFrustum.COMPONENTS_PER_PLANE;
            float nx = planes[base];
            float ny = planes[base + 1];
            float nz = planes[base + 2];
            float w = planes[base + 3];

            float bestX = nx >= 0.0f ? nx * maxX : nx * minX;
            float bestY = ny >= 0.0f ? ny * maxY : ny * minY;
            float bestZ = nz >= 0.0f ? nz * maxZ : nz * minZ;

            if (bestX + bestY + bestZ + w < 0.0f) {
                return false;
            }
        }
        return true;
    }

    @Test
    @DisplayName("testBox agrees with a brute-force eight-corner test on 5000 random boxes")
    void theFastPathAgreesWithTheBruteForceReference() {
        float[] planes = symmetricFrustumPlanes(0.1f, 64.0f, 24.0f, 16.0f);
        PjoFrustum frustum = PjoFrustum.fromPlanes(planes, 8.0f);
        assertTrue(frustum.isValid());

        Random random = new Random(20260911L);
        int disagreements = 0;
        int firstMismatch = -1;

        for (int i = 0; i < 5000; i++) {

            float minX = (random.nextFloat() - 0.5f) * 80.0f;
            float minY = (random.nextFloat() - 0.5f) * 60.0f;
            float minZ = -random.nextFloat() * 90.0f;
            float size = random.nextFloat() * 12.0f + 0.5f;

            float maxX = minX + size;
            float maxY = minY + size;
            float maxZ = minZ + size;

            boolean fast = frustum.testBox(minX, minY, minZ, maxX, maxY, maxZ);
            boolean slow = bruteForceVisible(planes, minX, minY, minZ, maxX, maxY, maxZ);

            if (fast != slow) {
                disagreements++;
                if (firstMismatch < 0) {
                    firstMismatch = i;
                }
            }
        }

        assertEquals(0, disagreements,
                "the branchless test disagreed with the brute-force reference on "
                        + disagreements + " of 5000 boxes, first at index " + firstMismatch);
    }

    @Test
    @DisplayName("testSection agrees with the reference for boxes of the size it assumes")
    void theSectionPathAgreesWithTheBruteForceReference() {
        float halfExtent = 8.0f;
        float[] planes = symmetricFrustumPlanes(0.1f, 64.0f, 24.0f, 16.0f);
        PjoFrustum frustum = PjoFrustum.fromPlanes(planes, halfExtent);

        Random random = new Random(4242L);
        int disagreements = 0;
        int farPlaneOnly = 0;

        for (int i = 0; i < 5000; i++) {
            float cx = (random.nextFloat() - 0.5f) * 80.0f;
            float cy = (random.nextFloat() - 0.5f) * 60.0f;
            float cz = -random.nextFloat() * 90.0f;

            boolean fast = frustum.testSection(cx, cy, cz);
            boolean slow = bruteForceVisible(planes,
                    cx - halfExtent, cy - halfExtent, cz - halfExtent,
                    cx + halfExtent, cy + halfExtent, cz + halfExtent);

            if (!slow) {

                if (bruteForceIgnoringFarPlane(planes, cx, cy, cz, halfExtent)) {
                    farPlaneOnly++;
                }
                continue;
            }

            if (!fast) {
                disagreements++;
            }
        }

        assertEquals(0, disagreements,
                "testSection culled " + disagreements + " sections the reference says are visible");
        assertTrue(farPlaneOnly >= 0, "counter sanity");
    }

    private static boolean bruteForceIgnoringFarPlane(float[] planes,
                                                      float cx, float cy, float cz, float halfExtent) {
        float minX = cx - halfExtent, maxX = cx + halfExtent;
        float minY = cy - halfExtent, maxY = cy + halfExtent;
        float minZ = cz - halfExtent, maxZ = cz + halfExtent;

        for (int plane = 0; plane < PjoFrustum.FAR_PLANE; plane++) {
            int base = plane * PjoFrustum.COMPONENTS_PER_PLANE;
            float nx = planes[base], ny = planes[base + 1], nz = planes[base + 2], w = planes[base + 3];

            float bestX = nx >= 0.0f ? nx * maxX : nx * minX;
            float bestY = ny >= 0.0f ? ny * maxY : ny * minY;
            float bestZ = nz >= 0.0f ? nz * maxZ : nz * minZ;

            if (bestX + bestY + bestZ + w < 0.0f) {
                return false;
            }
        }
        return true;
    }

    @Test
    @DisplayName("a box straight ahead is visible")
    void aBoxDirectlyAheadIsVisible() {
        PjoFrustum frustum = PjoFrustum.fromPlanes(symmetricFrustumPlanes(0.1f, 64.0f, 24.0f, 16.0f), 8.0f);

        assertTrue(frustum.testBox(-1, -1, -10, 1, 1, -8), "a box in front of the camera is visible");
        assertTrue(frustum.testSection(0, 0, -10), "a section in front of the camera is visible");
    }

    @Test
    @DisplayName("a box far to one side is culled")
    void aBoxOffToTheSideIsCulled() {
        PjoFrustum frustum = PjoFrustum.fromPlanes(symmetricFrustumPlanes(0.1f, 64.0f, 24.0f, 16.0f), 8.0f);

        assertFalse(frustum.testBox(500, 0, -10, 502, 2, -8),
                "a box 500 blocks to the right is outside the frustum");
        assertFalse(frustum.testSection(500, 0, -10));
    }

    @Test
    @DisplayName("a box behind the camera is culled")
    void aBoxBehindTheCameraIsCulled() {
        PjoFrustum frustum = PjoFrustum.fromPlanes(symmetricFrustumPlanes(0.1f, 64.0f, 24.0f, 16.0f), 8.0f);

        assertFalse(frustum.testBox(-1, -1, 20, 1, 1, 22), "behind the camera is not visible");
    }

    @Test
    @DisplayName("a degenerate point box is handled")
    void aDegeneratePointBoxIsHandled() {
        float[] planes = symmetricFrustumPlanes(0.1f, 64.0f, 24.0f, 16.0f);
        PjoFrustum frustum = PjoFrustum.fromPlanes(planes, 8.0f);

        boolean fast = frustum.testBox(0, 0, -10, 0, 0, -10);
        boolean slow = bruteForceVisible(planes, 0, 0, -10, 0, 0, -10);
        assertEquals(slow, fast, "a zero-size box must still agree with the reference");
        assertTrue(fast);
    }

    @Test
    @DisplayName("testSection skips the far plane, so only the far plane rejecting a box changes the answer")
    void testSectionSkipsTheFarPlane() {

        float[] planes = symmetricFrustumPlanes(64.0f, 400.0f, 24.0f, 16.0f);
        PjoFrustum frustum = PjoFrustum.fromPlanes(planes, 8.0f);

        assertTrue(frustum.testBox(-1, -1, -200, 1, 1, -198),
                "z = -200 is inside a frustum spanning -64 to -400, so it must be kept");
        assertTrue(frustum.testSection(0, 0, -200));

        assertFalse(bruteForceVisible(planes, -8, -8, -508, 8, 8, -492),
                "the reference must agree that z = -500 is beyond the far plane");
        assertFalse(frustum.testBox(-1, -1, -500, 1, 1, -498),
                "testBox does check the far plane, so this must be culled");
        assertTrue(frustum.testSection(0, 0, -500),
                "testSection skips the far plane on purpose, so this must pass");

        assertFalse(frustum.testSection(0, 0, -10),
                "a section in front of the near plane must still be culled");
        assertFalse(frustum.testSection(500, 0, -200),
                "a section outside the side planes must still be culled");
    }

    @Test
    @DisplayName("an invalid frustum reports itself invalid and answers visible to everything")
    void anInvalidFrustumCullsNothing() {
        PjoFrustum frustum = PjoFrustum.fromPlanes(null, 8.0f);

        assertFalse(frustum.isValid());
        assertTrue(frustum.testSection(10000, 10000, 10000),
                "when the planes are unreadable nothing may be culled");
        assertTrue(frustum.testBox(10000, 10000, 10000, 10002, 10002, 10002));
    }

    @Test
    @DisplayName("a too-short plane array is rejected rather than read past its end")
    void aShortPlaneArrayIsRejected() {
        PjoFrustum frustum = PjoFrustum.fromPlanes(new float[8], 8.0f);
        assertFalse(frustum.isValid());
    }

    @Test
    @DisplayName("reading planes from a non-JOML object degrades instead of throwing")
    void aForeignObjectDegradesInsteadOfThrowing() {

        PjoFrustum frustum = PjoFrustum.from("not a frustum", 8.0f);
        assertFalse(frustum.isValid());
        assertTrue(frustum.testSection(0, 0, 0));
    }

    @Test
    @DisplayName("the half extent is retained")
    void theHalfExtentIsRetained() {
        PjoFrustum frustum = PjoFrustum.fromPlanes(symmetricFrustumPlanes(0.1f, 64.0f, 24.0f, 16.0f), 8.5f);
        assertEquals(8.5f, frustum.halfExtent(), 1e-6f);
        assertEquals(6, PjoFrustum.PLANE_COUNT);
        assertEquals(5, PjoFrustum.FAR_PLANE, "the far plane is the last of the six");
    }
}
