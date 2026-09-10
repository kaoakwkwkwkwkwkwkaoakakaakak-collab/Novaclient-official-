package com.goodboysoul.pojavxoptimizer.platform;

import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;

/**
 * A snapshot of what this specific device can actually do, measured rather than assumed.
 *
 * <p>Translation layers advertise capabilities inconsistently: one build claims an extension it
 * implements incorrectly, another implements something it never advertises, and the same GPU family
 * behaves differently across driver revisions. Any optimisation gated on "the backend supports X"
 * therefore has to be gated on a probe that runs on the device, not on the backend's name.
 *
 * <p>The probe is intentionally passive. It records what the context reports and what cheap
 * operations cost; it never allocates large buffers or triggers shader compilation, because doing
 * that during startup would itself be the stutter we are trying to remove.
 */
public final class GpuCaps {

    private final RendererBackend backend;
    private final String glVersion;
    private final String glRenderer;
    private final String glVendor;
    private final int maxTextureSize;
    private final boolean supportsInstancing;
    private final boolean supportsAnisotropic;
    private final boolean supportsClipControl;
    private final boolean supportsPersistentMapping;
    private final boolean supportsCompute;
    private final int maxVertexAttribs;
    private final boolean probeComplete;

    private GpuCaps(Builder builder) {
        this.backend = builder.backend;
        this.glVersion = builder.glVersion;
        this.glRenderer = builder.glRenderer;
        this.glVendor = builder.glVendor;
        this.maxTextureSize = builder.maxTextureSize;
        this.supportsInstancing = builder.supportsInstancing;
        this.supportsAnisotropic = builder.supportsAnisotropic;
        this.supportsClipControl = builder.supportsClipControl;
        this.supportsPersistentMapping = builder.supportsPersistentMapping;
        this.supportsCompute = builder.supportsCompute;
        this.maxVertexAttribs = builder.maxVertexAttribs;
        this.probeComplete = builder.probeComplete;
    }

    /**
     * A conservative snapshot taken before GL is current. Everything optional is off, so the mod
     * still runs correctly if the real probe never happens (for example if the game crashes before
     * the window is created).
     */
    public static GpuCaps unknown(RendererBackend backend) {
        return new Builder(backend).build();
    }

    public RendererBackend backend() {
        return backend;
    }

    public String glVersion() {
        return glVersion;
    }

    public String glRenderer() {
        return glRenderer;
    }

    public String glVendor() {
        return glVendor;
    }

    public int maxTextureSize() {
        return maxTextureSize;
    }

    /**
     * Instanced draws are the single highest-value capability on a translation layer: one call
     * draws many chunks, and per-call overhead is the dominant cost. Without it the mod falls back
     * to one draw per section, which still beats vanilla but gives up most of the gain.
     */
    public boolean supportsInstancing() {
        return supportsInstancing;
    }

    public boolean supportsAnisotropic() {
        return supportsAnisotropic;
    }

    /**
     * Vanilla flips clip space to a zero-to-one depth range, which is an OpenGL 4.5 / ARB feature.
     * Most translation layers do not implement it. When absent, the mod must emit clip-space
     * matrices for the default minus-one-to-one range instead, or depth testing breaks in ways that
     * look like z-fighting rather than like a crash.
     */
    public boolean supportsClipControl() {
        return supportsClipControl;
    }

    /**
     * Persistent buffer mapping is unreliable across translation layers and is the usual cause of
     * the "works on desktop, corrupted on phone" bug class. It is probed but disabled by default
     * and only enabled on backends with a known-good record.
     */
    public boolean supportsPersistentMapping() {
        return supportsPersistentMapping;
    }

    public boolean supportsCompute() {
        return supportsCompute;
    }

    public int maxVertexAttribs() {
        return maxVertexAttribs;
    }

    /** False until the probe has run against a live context. */
    public boolean isProbeComplete() {
        return probeComplete;
    }

    /**
     * Whether the vertex layout the mod wants fits. The packed format needs four attributes; a
     * context reporting fewer cannot use it and must fall back to a wider vanilla-compatible layout.
     */
    public boolean canUsePackedVertexFormat() {
        return maxVertexAttribs >= 4;
    }

    public String describe() {
        return "backend=" + backend.displayName()
                + " gl=" + glVersion
                + " renderer=" + glRenderer
                + " vendor=" + glVendor
                + " maxTex=" + maxTextureSize
                + " instancing=" + supportsInstancing
                + " clipControl=" + supportsClipControl
                + " compute=" + supportsCompute;
    }

    @Override
    public String toString() {
        return describe();
    }

    /** Mutable collector used by the GL probe once a context exists. */
    public static final class Builder {

        private RendererBackend backend;
        private String glVersion = "unknown";
        private String glRenderer = "unknown";
        private String glVendor = "unknown";
        private int maxTextureSize = 2048;
        private boolean supportsInstancing;
        private boolean supportsAnisotropic;
        private boolean supportsClipControl;
        private boolean supportsPersistentMapping;
        private boolean supportsCompute;
        private int maxVertexAttribs = 4;
        private boolean probeComplete;

        public Builder(RendererBackend backend) {
            this.backend = backend;
        }

        public Builder glVersion(String value) {
            this.glVersion = nullToUnknown(value);
            return this;
        }

        public Builder glRenderer(String value) {
            this.glRenderer = nullToUnknown(value);
            return this;
        }

        public Builder glVendor(String value) {
            this.glVendor = nullToUnknown(value);
            return this;
        }

        public Builder maxTextureSize(int value) {
            this.maxTextureSize = Math.max(256, value);
            return this;
        }

        public Builder supportsInstancing(boolean value) {
            this.supportsInstancing = value;
            return this;
        }

        public Builder supportsAnisotropic(boolean value) {
            this.supportsAnisotropic = value;
            return this;
        }

        public Builder supportsClipControl(boolean value) {
            this.supportsClipControl = value;
            return this;
        }

        public Builder supportsPersistentMapping(boolean value) {
            this.supportsPersistentMapping = value;
            return this;
        }

        public Builder supportsCompute(boolean value) {
            this.supportsCompute = value;
            return this;
        }

        public Builder maxVertexAttribs(int value) {
            this.maxVertexAttribs = Math.max(0, value);
            return this;
        }

        public Builder probeComplete(boolean value) {
            this.probeComplete = value;
            return this;
        }

        public GpuCaps build() {
            if (Debug.isVerbose()) {
                PJO.debug("GpuCaps snapshot: backend={} instancing={} clipControl={}",
                        backend.displayName(), supportsInstancing, supportsClipControl);
            }
            return new GpuCaps(this);
        }

        private static String nullToUnknown(String value) {
            return value == null || value.isEmpty() ? "unknown" : value;
        }
    }
}
