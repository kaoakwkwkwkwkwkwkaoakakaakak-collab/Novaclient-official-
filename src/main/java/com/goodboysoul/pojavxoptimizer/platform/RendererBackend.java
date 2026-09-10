package com.goodboysoul.pojavxoptimizer.platform;

/**
 * The OpenGL translation layer the launcher is currently running Minecraft through.
 *
 * <p>This is the single most important fact about a Pojav installation, and it is the reason this
 * mod exists at all. A phone has no desktop OpenGL driver; the launcher interposes a translation
 * layer that turns desktop GL calls into something the mobile GPU understands. Every call crossing
 * that boundary costs real microseconds, so the winning strategy on mobile is not "use newer GL
 * features" (Sodium's approach, which is why Sodium targets desktop OpenGL 4.5) but "make
 * drastically fewer calls, each carrying more work".
 *
 * <p>Backends differ enough in what they actually implement that the feature set has to be chosen
 * per backend rather than from a single optimistic code path.
 */
public enum RendererBackend {

    /** Pojav's default. OpenGL ES 3.0 base with optional 3.1/3.2 features. Broadest device support. */
    MOBILEGLUES("MobileGlues", true, 30),

    /** NG-GL4ES, the Krypton Wrapper. GL4ES-lineage backend; conservative feature set. */
    KRYPTON("Krypton (NG-GL4ES)", true, 30),

    /** Artdeell's lightweight wrapper. Good performance, historically rough with Create. */
    LTW("LTW", true, 30),

    /** The long-standing Pojav fork of GL4ES. Runs nearly everything, slowest of the set. */
    HOLYGL4ES("HolyGL4ES", true, 20),

    /** Classic GL4ES. Oldest path, kept because very old devices still need it. */
    GL4ES("GL4ES", true, 20),

    /** Mesa's Vulkan-backed GL. Present on newer Pojav builds for Mali-Gx7+/Adreno 6xx/7xx. */
    ZINK("Zink", false, 40),

    /** ANGLE. Mostly an iOS/Windows path; included so detection does not silently misreport. */
    ANGLE("ANGLE", false, 40),

    /** Something we did not recognise. Every optional optimisation is disabled. */
    UNKNOWN("Unknown", true, 0);

    private final String displayName;
    private final boolean translationLayer;
    private final int assumedGlEsVersion;

    RendererBackend(String displayName, boolean translationLayer, int assumedGlEsVersion) {
        this.displayName = displayName;
        this.translationLayer = translationLayer;
        this.assumedGlEsVersion = assumedGlEsVersion;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * True when GL calls are being translated. When this holds, the cost model changes completely:
     * per-call overhead dominates, so batching and call reduction beat feature usage.
     */
    public boolean isTranslationLayer() {
        return translationLayer;
    }

    /**
     * The GL ES version this backend is known to expose, as {@code major * 10 + minor}. Used only
     * to order fallbacks; the real capability probe in {@link GpuCaps} overrides it at runtime.
     */
    public int assumedGlEsVersion() {
        return assumedGlEsVersion;
    }
}
