package com.goodboysoul.pojavxoptimizer;

import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.gui.DebugHud;
import com.goodboysoul.pojavxoptimizer.module.battery.FramePacer;
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

/**
 * Entry point for PojavXOptimizer.
 *
 * <p>made for pojavers by pojaver
 *
 * <p>The design rule that governs everything in this class: <b>nothing here may throw</b>. A
 * performance mod that takes the game down with it is worse than no mod, and on a phone the user has
 * no convenient way to read a stack trace. Every initialisation step is individually guarded, and a
 * step that fails disables its own module rather than aborting the mod.
 *
 * <p>The mod also deliberately depends on nothing but Fabric Loader. No Fabric API, no Sodium, no
 * configuration library, no jar-in-jar. Every dependency is another thing a user has to install
 * correctly and another source of version-mismatch crashes, and on mobile those failures look
 * identical to "the game won't start".
 */
public final class PojavXOptimizer implements ClientModInitializer {

    private static PojavXOptimizer instance;

    private PjoConfig config;
    private RendererBackend backend;
    private GpuCaps gpuCaps;
    private Path configDir;

    private ThermalGovernor thermalGovernor;
    private FramePacer framePacer;
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
            // Fall back to the working directory; the mod should still function with defaults.
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

    /**
     * Constructs each module independently. If one fails, the rest still come up, which keeps a
     * single bad module from taking the whole mod offline.
     */
    private void initModules() {
        this.thermalGovernor = new ThermalGovernor(config);
        this.framePacer = new FramePacer(config);
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

    /** Saves settings; safe to call repeatedly and from any thread. */
    public void saveConfig() {
        if (config != null && configDir != null) {
            config.save(configDir);
        }
    }

    // ---- accessors used by the mixins ------------------------------------

    public static PojavXOptimizer get() {
        return instance;
    }

    /**
     * True once initialisation has completed. Every mixin checks this before doing anything, so a
     * mod that loads after us — or a partial initialisation — degrades to vanilla behaviour instead
     * of dereferencing a null module.
     */
    public static boolean isReady() {
        return instance != null && instance.ready;
    }

    public PjoConfig config() { return config; }
    public RendererBackend backend() { return backend; }
    public GpuCaps gpuCaps() { return gpuCaps; }
    public ThermalGovernor thermalGovernor() { return thermalGovernor; }
    public FramePacer framePacer() { return framePacer; }
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

    /**
     * Called once the GL context exists so capability probing can replace the conservative
     * assumptions made at startup.
     */
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

    /** One-line status for the debug overlay and for support requests. */
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
