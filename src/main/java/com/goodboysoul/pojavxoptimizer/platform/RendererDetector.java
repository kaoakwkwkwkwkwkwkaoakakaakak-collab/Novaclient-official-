package com.goodboysoul.pojavxoptimizer.platform;

import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;

/**
 * Identifies the translation layer in use.
 *
 * <p>Detection is deliberately layered and ordered cheapest-and-most-reliable first:
 * <ol>
 *   <li>an explicit user override system property, because when detection is wrong the user needs a
 *       way to force the answer without waiting for a fix;</li>
 *   <li>launcher-provided system properties, which are authoritative when present;</li>
 *   <li>the {@code GL_RENDERER} / {@code GL_VENDOR} strings, which every backend sets differently
 *       and which are the only signal available on builds that expose nothing else;</li>
 *   <li>known shared-object names on the native library path, as a last resort.</li>
 * </ol>
 *
 * <p>Failing to detect falls through to {@link RendererBackend#UNKNOWN} with every optional
 * optimisation off. A wrong guess that enables a feature the backend cannot honour produces visual
 * corruption, which is far worse than simply not optimising.
 */
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

        RendererBackend fromLibrary = matchNativeLibraryPath();
        if (fromLibrary != null) {
            PJO.info("Renderer detected from native library path: {}", fromLibrary.displayName());
            return fromLibrary;
        }

        PJO.warn("Could not identify the renderer backend. Every optional optimisation is disabled. "
                + "Set -D" + OVERRIDE_PROPERTY + "=<name> to force one.");
        return RendererBackend.UNKNOWN;
    }

    /**
     * Called once GL is current with the real {@code GL_RENDERER} string, which is the most
     * accurate signal available but is not readable before the window exists.
     */
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

    private static RendererBackend matchGlString(String haystack) {
        if (haystack == null || haystack.isEmpty()) {
            return null;
        }
        String needle = haystack.toLowerCase();
        // Order matters: "mobileglues" contains no substring of the others, but "ng_gl4es" and
        // "gl4es" overlap, so the more specific names must be tested first.
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
