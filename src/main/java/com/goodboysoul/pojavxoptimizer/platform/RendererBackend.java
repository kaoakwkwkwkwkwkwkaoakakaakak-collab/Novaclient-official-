package com.goodboysoul.pojavxoptimizer.platform;

public enum RendererBackend {

    MOBILEGLUES("MobileGlues", true, 30),

    KRYPTON("Krypton (NG-GL4ES)", true, 30),

    LTW("LTW", true, 30),

    HOLYGL4ES("HolyGL4ES", true, 20),

    GL4ES("GL4ES", true, 20),

    ZINK("Zink", false, 40),

    ANGLE("ANGLE", false, 40),

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

    public boolean isTranslationLayer() {
        return translationLayer;
    }

    public int assumedGlEsVersion() {
        return assumedGlEsVersion;
    }
}
