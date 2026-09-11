package com.goodboysoul.pojavxoptimizer.platform;

import com.goodboysoul.pojavxoptimizer.core.Debug;
import com.goodboysoul.pojavxoptimizer.core.PJO;

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

    public boolean supportsInstancing() {
        return supportsInstancing;
    }

    public boolean supportsAnisotropic() {
        return supportsAnisotropic;
    }

    public boolean supportsClipControl() {
        return supportsClipControl;
    }

    public boolean supportsPersistentMapping() {
        return supportsPersistentMapping;
    }

    public boolean supportsCompute() {
        return supportsCompute;
    }

    public int maxVertexAttribs() {
        return maxVertexAttribs;
    }

    public boolean isProbeComplete() {
        return probeComplete;
    }

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
