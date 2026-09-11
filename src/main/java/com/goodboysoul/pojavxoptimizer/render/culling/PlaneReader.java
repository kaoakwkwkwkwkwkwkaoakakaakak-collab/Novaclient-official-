package com.goodboysoul.pojavxoptimizer.render.culling;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class PlaneReader {

    private static final String PLANES_FIELD = "planes";

    private static final MethodHandle PLANES_GETTER = resolve();

    private static final Field[] COMPONENT_FIELDS = new Field[PjoFrustum.COMPONENTS_PER_PLANE];

    private static final String[] COMPONENT_NAMES = {"x", "y", "z", "w"};

    private static volatile boolean reported;

    private PlaneReader() {
    }

    private static MethodHandle resolve() {
        try {
            Class<?> intersection = Class.forName("org.joml.FrustumIntersection");
            Field field = intersection.getDeclaredField(PLANES_FIELD);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(field);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException
                | RuntimeException e) {
            return null;
        }
    }

    public static float[] readNormals(Object frustumIntersection) {
        if (frustumIntersection == null || PLANES_GETTER == null) {
            reportOnce();
            return null;
        }

        try {
            Object planesObject = PLANES_GETTER.invoke(frustumIntersection);
            if (!(planesObject instanceof Object[] planes)) {
                reportOnce();
                return null;
            }
            if (planes.length < PjoFrustum.PLANE_COUNT) {
                reportOnce();
                return null;
            }

            float[] out = new float[PjoFrustum.PLANE_COUNT * PjoFrustum.COMPONENTS_PER_PLANE];
            for (int plane = 0; plane < PjoFrustum.PLANE_COUNT; plane++) {
                Object vector = planes[plane];
                if (vector == null) {
                    reportOnce();
                    return null;
                }
                int base = plane * PjoFrustum.COMPONENTS_PER_PLANE;
                out[base] = readComponent(vector, 0);
                out[base + 1] = readComponent(vector, 1);
                out[base + 2] = readComponent(vector, 2);
                out[base + 3] = readComponent(vector, 3);
            }
            return out;
        } catch (Throwable t) {
            reportOnce();
            return null;
        }
    }

    private static float readComponent(Object vector, int index) throws ReflectiveOperationException {
        Field field = COMPONENT_FIELDS[index];
        if (field == null) {
            field = vector.getClass().getField(COMPONENT_NAMES[index]);
            COMPONENT_FIELDS[index] = field;
        }
        return field.getFloat(vector);
    }

    private static void reportOnce() {
        if (reported) {
            return;
        }
        reported = true;
        try {
            System.err.println("[PojavXOptimizer] Could not read frustum planes from JOML; "
                    + "frustum culling is disabled. The game will still render correctly, just "
                    + "without the culling optimisation.");
        } catch (RuntimeException ignored) {

        }
    }

    public static boolean isAvailable() {
        return PLANES_GETTER != null;
    }
}
