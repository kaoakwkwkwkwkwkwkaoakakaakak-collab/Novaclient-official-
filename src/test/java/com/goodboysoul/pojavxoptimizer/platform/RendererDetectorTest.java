package com.goodboysoul.pojavxoptimizer.platform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RendererDetectorTest {

    private static final String LIBNAME_PROPERTY = "org.lwjgl.opengl.libname";

    @AfterEach
    void clearProperty() {
        System.clearProperty(LIBNAME_PROPERTY);
        System.clearProperty("pojavxoptimizer.renderer");
    }

    @Test
    @DisplayName("the libgl4es library name Pojav actually passes is recognised")
    void detectsGl4esFromTheLwjglLibraryProperty() {
        System.setProperty(LIBNAME_PROPERTY, "libgl4es_114.so");

        RendererBackend backend = RendererDetector.detect();

        assertEquals(RendererBackend.GL4ES, backend,
                "Pojav starts the JVM with -Dorg.lwjgl.opengl.libname=libgl4es_114.so, and the "
                        + "detector used to ignore that property entirely, so every optional "
                        + "optimisation was switched off on a working renderer");
    }

    @Test
    @DisplayName("a Krypton library name is distinguished from plain gl4es")
    void detectsKryptonFromTheLwjglLibraryProperty() {
        System.setProperty(LIBNAME_PROPERTY, "libng_gl4es.so");
        assertEquals(RendererBackend.KRYPTON, RendererDetector.detect());
    }

    @Test
    @DisplayName("MobileGlues is recognised from the library name")
    void detectsMobileGluesFromTheLwjglLibraryProperty() {
        System.setProperty(LIBNAME_PROPERTY, "libmobileglues.so");
        assertEquals(RendererBackend.MOBILEGLUES, RendererDetector.detect());
    }

    @Test
    @DisplayName("an explicit override still wins over detection")
    void theOverrideStillWins() {
        System.setProperty(LIBNAME_PROPERTY, "libgl4es_114.so");
        System.setProperty("pojavxoptimizer.renderer", "LTW");

        assertEquals(RendererBackend.LTW, RendererDetector.detect(),
                "the override exists for the cases detection cannot resolve, so it must take priority");
    }

    @Test
    @DisplayName("a GL string naming Holy GL4ES resolves rather than falling through to unknown")
    void holyGl4esIsRecognised() {
        assertEquals(RendererBackend.HOLYGL4ES,
                RendererDetector.refine("Holy GL4ES", "gl4es"));
    }

    @Test
    @DisplayName("an unrecognised library name still reports unknown rather than guessing")
    void anUnknownLibraryIsNotGuessed() {
        System.setProperty(LIBNAME_PROPERTY, "libsomethingelse.so");
        assertEquals(RendererBackend.UNKNOWN, RendererDetector.detect());
    }
}
