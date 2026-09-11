package com.goodboysoul.pojavxoptimizer.platform;

import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;

public final class RendererDetector {

    private static final String OVERRIDE_PROPERTY = "pojavxoptimizer.renderer";

    private RendererDetector() {
    }

    public static RendererBackend detect() {
        RendererBackend fromOverride = readOverride();
        if (fromOverride != null) {
            PJO.info("Renderer forced by -D{}={}", OVERRIDE_PROPERTY, fromOverride.displayName());
            return fromOverride;
        }

        RendererBackend fromProperty = readLauncherProperties();
        if (fromProperty != null) {
            PJO.info("Renderer detected from launcher properties: {}", fromProperty.displayName());
            return fromProperty;
        }

        String glRenderer = System.getProperty("org.lwjgl.opengl.Display.renderer", "");
        RendererBackend fromGlString = matchGlString(glRenderer);
        if (fromGlString != null) {
            PJO.info("Renderer detected from GL_RENDERER: {} -> {}", glRenderer,
                    fromGlString.displayName());
            return fromGlString;
        }

        RendererBackend fromEnvironment = readEnvironment();
        if (fromEnvironment != null) {
            PJO.info("Renderer detected from launcher environment: {}",
                    fromEnvironment.displayName());
            return fromEnvironment;
        }

        RendererBackend fromLibrary = matchNativeLibraryPath();
        if (fromLibrary != null) {
            PJO.info("Renderer detected from native library path: {}", fromLibrary.displayName());
            return fromLibrary;
        }

        PJO.warn("Could not identify the renderer backend. Every optional optimisation is disabled. "
                + "Set -D" + OVERRIDE_PROPERTY + "=<name> to force one.");
        return RendererBackend.UNKNOWN;
    }

    public static RendererBackend refine(String glRenderer, String glVendor) {
        RendererBackend refined = matchGlString(glRenderer + " " + glVendor);
        if (refined != null) {
            PJO.info("Renderer refined from live GL strings: {}", refined.displayName());
        }
        return refined;
    }

    private static RendererBackend readOverride() {
        String value = System.getProperty(OVERRIDE_PROPERTY, "").trim();
        if (value.isEmpty()) {
            return null;
        }
        for (RendererBackend backend : RendererBackend.values()) {
            if (backend.name().equalsIgnoreCase(value)
                    || backend.displayName().equalsIgnoreCase(value)) {
                return backend;
            }
        }
        PJO.warn("Ignoring unrecognised -D{}={} (expected one of MobileGlues, Krypton, LTW, "
                + "HolyGL4ES, GL4ES, Zink, ANGLE)", OVERRIDE_PROPERTY, value);
        return null;
    }

    private static RendererBackend readLauncherProperties() {
        String[] candidates = {
                "pojav.renderer",
                "pojavlauncher.renderer",
                "net.kdt.pojavlaunch.renderer",
                "mojo.renderer",
                "org.lwjgl.opengl.libname",
        };
        StringBuilder combined = new StringBuilder();
        for (String key : candidates) {
            String value = System.getProperty(key, "");
            if (!value.isEmpty()) {
                combined.append(value).append(' ');
            }
        }
        if (combined.length() == 0) {
            return null;
        }
        return matchGlString(combined.toString());
    }

    private static RendererBackend readEnvironment() {
        String[] candidates = {
                "SDL_OPENGL_LIBRARY",
                "POJAV_RENDERER",
                "LIBGL_RENDERER",
        };
        StringBuilder combined = new StringBuilder();
        for (String key : candidates) {
            String value = readEnv(key);
            if (value != null && !value.isEmpty()) {
                combined.append(value).append(' ');
            }
        }
        if (combined.length() == 0) {
            return null;
        }
        return matchGlString(combined.toString());
    }

    private static String readEnv(String key) {
        try {
            return System.getenv(key);
        } catch (RuntimeException unavailable) {
            return null;
        }
    }

    private static RendererBackend matchGlString(String haystack) {
        if (haystack == null || haystack.isEmpty()) {
            return null;
        }
        String needle = haystack.toLowerCase();

        if (needle.contains("mobileglues") || needle.contains("mobile glues")
                || needle.contains("mg_")) {
            return RendererBackend.MOBILEGLUES;
        }
        if (needle.contains("ng_gl4es") || needle.contains("ng-gl4es")
                || needle.contains("krypton")) {
            return RendererBackend.KRYPTON;
        }
        if (needle.contains("ltw")) {
            return RendererBackend.LTW;
        }
        if (needle.contains("holy")) {
            return RendererBackend.HOLYGL4ES;
        }
        if (needle.contains("zink")) {
            return RendererBackend.ZINK;
        }
        if (needle.contains("angle")) {
            return RendererBackend.ANGLE;
        }
        if (needle.contains("gl4es")) {
            return RendererBackend.GL4ES;
        }
        return null;
    }

    private static RendererBackend matchNativeLibraryPath() {
        String path = System.getProperty("java.library.path", "");
        if (path.isEmpty()) {
            return null;
        }
        String lower = path.toLowerCase();
        if (lower.contains("mobileglues")) {
            return RendererBackend.MOBILEGLUES;
        }
        if (lower.contains("ng_gl4es") || lower.contains("ng-gl4es")) {
            return RendererBackend.KRYPTON;
        }
        if (lower.contains("ltw")) {
            return RendererBackend.LTW;
        }
        if (lower.contains("gl4es")) {
            return RendererBackend.GL4ES;
        }
        if (Debug.isVerbose()) {
            PJO.debug("library path gave no renderer hint: {}", path);
        }
        return null;
    }
}
