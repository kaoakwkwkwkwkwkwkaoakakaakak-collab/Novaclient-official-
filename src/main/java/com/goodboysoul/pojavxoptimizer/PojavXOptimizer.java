package com.goodboysoul.pojavxoptimizer;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.gui.DebugHud;
import com.goodboysoul.pojavxoptimizer.module.battery.FramePacer;
import com.goodboysoul.pojavxoptimizer.module.battery.FrameRateLimiter;
import com.goodboysoul.pojavxoptimizer.module.chunk.BuildBudget;
import com.goodboysoul.pojavxoptimizer.module.chunk.ChunkPrioritizer;
import com.goodboysoul.pojavxoptimizer.module.culling.EntityCullPolicy;
import com.goodboysoul.pojavxoptimizer.module.culling.ParticleBudget;
import com.goodboysoul.pojavxoptimizer.module.input.InputSmoother;
import com.goodboysoul.pojavxoptimizer.module.memory.BufferArena;
import com.goodboysoul.pojavxoptimizer.module.memory.HeapGuard;
import com.goodboysoul.pojavxoptimizer.module.render.DynamicResolution;
import com.goodboysoul.pojavxoptimizer.module.render.VisibilityBudget;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;
import com.goodboysoul.pojavxoptimizer.platform.GpuCaps;
import com.goodboysoul.pojavxoptimizer.platform.RendererBackend;
import com.goodboysoul.pojavxoptimizer.platform.RendererDetector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class PojavXOptimizer implements ClientModInitializer {

    private static PojavXOptimizer instance;

    private PjoConfig config;
    private RendererBackend backend;
    private GpuCaps gpuCaps;
    private Path configDir;

    private ThermalGovernor thermalGovernor;
    private FramePacer framePacer;
    private FrameRateLimiter frameRateLimiter;
    private HeapGuard heapGuard;
    private BufferArena bufferArena;
    private ChunkPrioritizer chunkPrioritizer;
    private BuildBudget buildBudget;
    private VisibilityBudget visibilityBudget;
    private DynamicResolution dynamicResolution;
    private InputSmoother inputSmoother;
    private EntityCullPolicy entityCullPolicy;
    private ParticleBudget particleBudget;
    private DebugHud debugHud;

    private boolean ready;

    @Override
    public void onInitializeClient() {
        instance = this;
        PJO.info("{} {} initialising", PJO.MOD_NAME, PJO.VERSION);

        try {
            this.configDir = FabricLoader.getInstance().getConfigDir();
        } catch (RuntimeException loaderUnavailable) {

            this.configDir = Path.of("config");
            PJO.warn("Could not resolve the loader config directory; using ./config instead.",
                    loaderUnavailable);
        }

        initConfig();
        initPlatform();
        initModules();

        PJO.info("{} ready: profile={}, renderer={}", PJO.MOD_NAME,
                config.profile().displayName(), backend.displayName());
        ready = true;
    }

    private void initConfig() {
        try {
            this.config = PjoConfig.load(configDir);
        } catch (RuntimeException configFailed) {
            PJO.error("Config initialisation failed; using hardcoded defaults.", configFailed);
            this.config = PjoConfig.defaults(
                    com.goodboysoul.pojavxoptimizer.config.DeviceProfile.LOW);
        }
    }

    private void initPlatform() {
        try {
            this.backend = RendererDetector.detect();
        } catch (RuntimeException detectionFailed) {
            PJO.warn("Renderer detection threw; assuming unknown backend.", detectionFailed);
            this.backend = RendererBackend.UNKNOWN;
        }
        this.gpuCaps = GpuCaps.unknown(backend);

        if (backend == RendererBackend.UNKNOWN) {
            PJO.warn("Running with an unidentified renderer. Only the safe optimisations are on: "
                    + "frame pacing, heap guarding, particle budgeting and entity culling.");
        }
    }

    private void initModules() {
        this.thermalGovernor = new ThermalGovernor(config);
        this.framePacer = new FramePacer(config);
        this.frameRateLimiter = new FrameRateLimiter(config);
        this.chunkPrioritizer = new ChunkPrioritizer(config);
        this.buildBudget = new BuildBudget(config);
        this.visibilityBudget = new VisibilityBudget(config);
        this.dynamicResolution = new DynamicResolution(config);
        this.inputSmoother = new InputSmoother(config);
        this.entityCullPolicy = new EntityCullPolicy(config);
        this.particleBudget = new ParticleBudget(config);
        this.debugHud = new DebugHud();

        try {
            this.heapGuard = new HeapGuard(config);
            this.bufferArena = new BufferArena(64);
            this.heapGuard.register(bufferArena);
            this.heapGuard.reportStartup();
        } catch (RuntimeException memoryModulesFailed) {
            PJO.warn("Memory modules could not start; continuing without them.",
                    memoryModulesFailed);
            this.heapGuard = null;
            this.bufferArena = null;
        }
    }

    public void saveConfig() {
        if (config != null && configDir != null) {
            config.save(configDir);
        }
    }

    public static PojavXOptimizer get() {
        return instance;
    }

    public static boolean isReady() {
        return instance != null && instance.ready;
    }

    public PjoConfig config() { return config; }
    public RendererBackend backend() { return backend; }
    public GpuCaps gpuCaps() { return gpuCaps; }
    public ThermalGovernor thermalGovernor() { return thermalGovernor; }
    public FramePacer framePacer() { return framePacer; }
    public FrameRateLimiter frameRateLimiter() { return frameRateLimiter; }
    public HeapGuard heapGuard() { return heapGuard; }
    public BufferArena bufferArena() { return bufferArena; }
    public ChunkPrioritizer chunkPrioritizer() { return chunkPrioritizer; }
    public BuildBudget buildBudget() { return buildBudget; }
    public VisibilityBudget visibilityBudget() { return visibilityBudget; }
    public DynamicResolution dynamicResolution() { return dynamicResolution; }
    public InputSmoother inputSmoother() { return inputSmoother; }
    public EntityCullPolicy entityCullPolicy() { return entityCullPolicy; }
    public ParticleBudget particleBudget() { return particleBudget; }
    public DebugHud debugHud() { return debugHud; }
    public Path configDir() { return configDir; }

    public void onGlContextReady(String glVersion, String glRenderer, String glVendor,
                                 int maxTextureSize, boolean instancing, boolean clipControl,
                                 boolean compute) {
        RendererBackend refined = RendererDetector.refine(glRenderer, glVendor);
        if (refined != null) {
            this.backend = refined;
        }
        this.gpuCaps = new GpuCaps.Builder(backend)
                .glVersion(glVersion)
                .glRenderer(glRenderer)
                .glVendor(glVendor)
                .maxTextureSize(maxTextureSize)
                .supportsInstancing(instancing)
                .supportsClipControl(clipControl)
                .supportsCompute(compute)
                .probeComplete(true)
                .build();
        PJO.info("GPU probe complete: {}", gpuCaps.describe());
    }

    public String statusLine() {
        if (!ready) {
            return "PJO: not ready";
        }
        return "PJO " + backend.displayName()
                + " | " + thermalGovernor.pressure().label()
                + " | cap " + framePacer.effectiveCap() + " fps"
                + (dynamicResolution.isEngaged()
                        ? " | scale " + String.format("%.2f", dynamicResolution.scale()) : "")
                + (visibilityBudget.isCapEngaged()
                        ? " | vis " + visibilityBudget.currentCap() : "");
    }
}
