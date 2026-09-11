package com.goodboysoul.pojavxoptimizer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.platform.GpuCaps;
import com.goodboysoul.pojavxoptimizer.platform.RendererBackend;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * The on-disk settings for PojavXOptimizer, stored as {@code config/pojavxoptimizer.json}.
 *
 * <p>Three behaviours here are deliberate and matter more than any individual setting:
 *
 * <ul>
 *   <li><b>Loading never throws.</b> A malformed file is logged, quarantined to
 *       {@code .corrupt}, and replaced with defaults. A performance mod that refuses to start the
 *       game because of its own config file is worse than no mod at all, and on mobile the user has
 *       no easy way to inspect a stack trace.</li>
 *   <li><b>Writing is atomic.</b> Settings are written to a temporary file and then moved into
 *       place. Android kills processes without warning, and a half-written JSON file is exactly the
 *       failure that the first rule then has to recover from.</li>
 *   <li><b>Schema version is recorded.</b> An older client reading a newer file, or the reverse,
 *       falls back to defaults rather than silently misinterpreting a field whose meaning changed.</li>
 * </ul>
 */
public final class PjoConfig {

    public static final int SCHEMA_VERSION = 1;
    public static final String FILE_NAME = "pojavxoptimizer.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ---- persisted state -------------------------------------------------

    private int schemaVersion = SCHEMA_VERSION;
    private DeviceProfile profile = DeviceProfile.LOW;

    // Rendering
    private boolean dynamicResolution = true;
    private float resolutionScaleMin = 0.60f;
    private float resolutionScaleMax = 1.00f;
    private int visibilitySectionCap = 4096;
    private boolean instancedSectionDrawing = true;
    private boolean packedVertexFormat = true;
    private boolean shaderPrecompile = true;

    /**
     * Uploads section meshes in the packed 16-byte format instead of vanilla's 32-byte one.
     *
     * <p>Default OFF. The compaction itself is unit-tested and provably correct, but the custom
     * vertex format and shader that consume it have not been run on a real device. Shipping it
     * enabled would mean a black screen or corrupted colours for anyone who installs without
     * knowing that. It turns on once it has been verified on each renderer.
     */
    private boolean packedMeshUpload = false;

    // Memory
    private boolean heapGuard = true;
    private float heapPressureThreshold = 0.82f;
    private boolean offHeapMeshes = true;
    private boolean aggressiveMeshEviction = true;

    // Chunk building
    private int buildThreads = 0; // 0 = derive from the device profile
    private double buildTimeBudgetMs = 6.0;
    private boolean viewWeightedChunkPriority = true;

    // Thermal
    private boolean thermalGovernor = true;
    private double thermalBackoffThreshold = 0.30;
    private int thermalRecoverSeconds = 20;

    // Battery
    private boolean batterySaver = true;
    private int idleFpsCap = 20;
    private int guiFpsCap = 15;
    private boolean capToRefreshRate = true;

    // Input
    private boolean inputSmoothing = false;
    private double inputSmoothingStrength = 0.35;

    // Culling
    private boolean entityCulling = true;
    private double entityCullDistance = 64.0;
    private double entityCullPixelSize = 3.0;
    private boolean particleBudgeting = true;
    private int particleBudget = 400;

    // Diagnostics
    private boolean debugOverlay = false;

    private PjoConfig() {
    }

    // ---- lifecycle -------------------------------------------------------

    /**
     * Reads the config, or produces profile-derived defaults when it is absent, unreadable, or from
     * an incompatible schema. Always returns a usable object.
     */
    public static PjoConfig load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        DeviceProfile profile = DeviceProfile.detect();

        if (!Files.isRegularFile(file)) {
            PJO.info("No config found at {}; writing defaults for profile {}",
                    file, profile.displayName());
            PjoConfig fresh = defaults(profile);
            fresh.save(configDir);
            return fresh;
        }

        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            PjoConfig loaded = GSON.fromJson(json, PjoConfig.class);
            if (loaded == null) {
                throw new JsonSyntaxException("config parsed to null");
            }
            if (loaded.schemaVersion != SCHEMA_VERSION) {
                PJO.warn("Config schema is {} but this build expects {}; using defaults and "
                        + "preserving the old file.", loaded.schemaVersion, SCHEMA_VERSION);
                return quarantineAndDefault(configDir, file, profile);
            }
            loaded.clamp();
            PJO.info("Loaded config for profile {}", loaded.profile.displayName());
            return loaded;
        } catch (IOException | RuntimeException exception) {
            PJO.warn("Config at " + file + " could not be read; falling back to defaults.",
                    exception);
            return quarantineAndDefault(configDir, file, profile);
        }
    }

    public static PjoConfig defaults(DeviceProfile profile) {
        PjoConfig config = new PjoConfig();
        config.applyProfile(profile);
        return config;
    }

    /**
     * Writes settings atomically. Failure is logged and swallowed: losing a settings change is
     * recoverable, but propagating an IOException from a config save into the render loop is not.
     */
    public void save(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        Path temp = configDir.resolve(FILE_NAME + ".tmp");
        try {
            Files.createDirectories(configDir);
            this.schemaVersion = SCHEMA_VERSION;
            Files.writeString(temp, GSON.toJson(this), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicFailed) {
            // ATOMIC_MOVE is unsupported on some Android filesystems; retry without it before
            // giving up, since a non-atomic write is still better than no write at all.
            try {
                Files.writeString(file, GSON.toJson(this), StandardCharsets.UTF_8);
                Files.deleteIfExists(temp);
            } catch (IOException fallbackFailed) {
                PJO.warn("Could not save config to " + file, fallbackFailed);
            }
        }
    }

    private static PjoConfig quarantineAndDefault(Path configDir, Path file, DeviceProfile profile) {
        try {
            Path quarantine = configDir.resolve(FILE_NAME + ".corrupt");
            Files.move(file, quarantine, StandardCopyOption.REPLACE_EXISTING);
            PJO.warn("Previous config preserved at {}", quarantine);
        } catch (IOException moveFailed) {
            PJO.warn("Could not quarantine the unreadable config.", moveFailed);
        }
        PjoConfig fresh = defaults(profile);
        fresh.save(configDir);
        return fresh;
    }

    /**
     * Copies profile defaults into the settings. Used both for a first run and when the user asks
     * to reset, so the two paths cannot drift apart.
     */
    public void applyProfile(DeviceProfile newProfile) {
        this.profile = newProfile;
        this.buildThreads = newProfile.buildThreads();
        this.particleBudget = newProfile.particleBudget();
        this.idleFpsCap = newProfile.idleFpsCap();
        this.heapPressureThreshold = newProfile.heapPressureThreshold();
        this.aggressiveMeshEviction = newProfile.aggressiveMemoryReclaim();
        this.thermalGovernor = newProfile.thermalGovernorEnabled();
    }

    /**
     * Constrains every value to a range that cannot break the game. A user editing the JSON by hand
     * on a phone keyboard is expected to produce nonsense eventually, and nonsense here means a
     * black screen rather than a validation error.
     */
    private void clamp() {
        resolutionScaleMin = clampFloat(resolutionScaleMin, 0.25f, 1.0f);
        resolutionScaleMax = clampFloat(resolutionScaleMax, 0.25f, 1.0f);
        if (resolutionScaleMin > resolutionScaleMax) {
            resolutionScaleMin = resolutionScaleMax;
        }
        visibilitySectionCap = clampInt(visibilitySectionCap, 256, 65536);
        heapPressureThreshold = clampFloat(heapPressureThreshold, 0.50f, 0.95f);
        buildThreads = clampInt(buildThreads, 0, 16);
        buildTimeBudgetMs = clampDouble(buildTimeBudgetMs, 1.0, 50.0);
        thermalBackoffThreshold = clampDouble(thermalBackoffThreshold, 0.10, 0.80);
        thermalRecoverSeconds = clampInt(thermalRecoverSeconds, 5, 300);
        idleFpsCap = clampInt(idleFpsCap, 5, 120);
        guiFpsCap = clampInt(guiFpsCap, 5, 120);
        inputSmoothingStrength = clampDouble(inputSmoothingStrength, 0.0, 1.0);
        entityCullDistance = clampDouble(entityCullDistance, 8.0, 256.0);
        entityCullPixelSize = clampDouble(entityCullPixelSize, 0.0, 64.0);
        particleBudget = clampInt(particleBudget, 0, 10000);
        if (profile == null) {
            profile = DeviceProfile.LOW;
        }
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ---- derived decisions -----------------------------------------------

    /**
     * Resolves the thread count for chunk building, falling back to the profile when the user left
     * it on auto. Never returns 0, and never exceeds physical cores minus one so that the render
     * thread always has a core available.
     */
    public int resolveBuildThreads() {
        int cores = Runtime.getRuntime().availableProcessors();
        int requested = buildThreads > 0 ? buildThreads : profile.buildThreads();
        return Math.max(1, Math.min(requested, Math.max(1, cores - 1)));
    }

    /**
     * Instanced drawing needs both user consent and hardware support. Checking only the config
     * would emit instanced draw calls into a backend that cannot execute them.
     */
    public boolean useInstancedDrawing(GpuCaps caps) {
        return instancedSectionDrawing && caps.supportsInstancing();
    }

    public boolean usePackedVertexFormat(GpuCaps caps) {
        return packedVertexFormat && caps.canUsePackedVertexFormat();
    }

    /** Render distance cap for the current profile, honoured even if the user asks for more. */
    public int renderDistanceCap() {
        return profile.renderDistanceCap();
    }

    // ---- accessors -------------------------------------------------------

    public int schemaVersion() { return schemaVersion; }
    public DeviceProfile profile() { return profile; }

    public boolean dynamicResolution() { return dynamicResolution; }
    public void dynamicResolution(boolean value) { this.dynamicResolution = value; }

    public float resolutionScaleMin() { return resolutionScaleMin; }
    public void resolutionScaleMin(float value) { this.resolutionScaleMin = value; }

    public float resolutionScaleMax() { return resolutionScaleMax; }
    public void resolutionScaleMax(float value) { this.resolutionScaleMax = value; }

    public int visibilitySectionCap() { return visibilitySectionCap; }
    public void visibilitySectionCap(int value) { this.visibilitySectionCap = value; }

    public boolean instancedSectionDrawing() { return instancedSectionDrawing; }
    public void instancedSectionDrawing(boolean value) { this.instancedSectionDrawing = value; }

    public boolean packedVertexFormat() { return packedVertexFormat; }
    public void packedVertexFormat(boolean value) { this.packedVertexFormat = value; }

    public boolean packedMeshUpload() { return packedMeshUpload; }
    public void packedMeshUpload(boolean value) { this.packedMeshUpload = value; }

    public boolean shaderPrecompile() { return shaderPrecompile; }
    public void shaderPrecompile(boolean value) { this.shaderPrecompile = value; }

    public boolean heapGuard() { return heapGuard; }
    public void heapGuard(boolean value) { this.heapGuard = value; }

    public float heapPressureThreshold() { return heapPressureThreshold; }
    public void heapPressureThreshold(float value) { this.heapPressureThreshold = value; }

    public boolean offHeapMeshes() { return offHeapMeshes; }
    public void offHeapMeshes(boolean value) { this.offHeapMeshes = value; }

    public boolean aggressiveMeshEviction() { return aggressiveMeshEviction; }
    public void aggressiveMeshEviction(boolean value) { this.aggressiveMeshEviction = value; }

    public int buildThreads() { return buildThreads; }
    public void buildThreads(int value) { this.buildThreads = value; }

    public double buildTimeBudgetMs() { return buildTimeBudgetMs; }
    public void buildTimeBudgetMs(double value) { this.buildTimeBudgetMs = value; }

    public boolean viewWeightedChunkPriority() { return viewWeightedChunkPriority; }
    public void viewWeightedChunkPriority(boolean value) { this.viewWeightedChunkPriority = value; }

    public boolean thermalGovernor() { return thermalGovernor; }
    public void thermalGovernor(boolean value) { this.thermalGovernor = value; }

    public double thermalBackoffThreshold() { return thermalBackoffThreshold; }
    public void thermalBackoffThreshold(double value) { this.thermalBackoffThreshold = value; }

    public int thermalRecoverSeconds() { return thermalRecoverSeconds; }
    public void thermalRecoverSeconds(int value) { this.thermalRecoverSeconds = value; }

    public boolean batterySaver() { return batterySaver; }
    public void batterySaver(boolean value) { this.batterySaver = value; }

    public int idleFpsCap() { return idleFpsCap; }
    public void idleFpsCap(int value) { this.idleFpsCap = value; }

    public int guiFpsCap() { return guiFpsCap; }
    public void guiFpsCap(int value) { this.guiFpsCap = value; }

    public boolean capToRefreshRate() { return capToRefreshRate; }
    public void capToRefreshRate(boolean value) { this.capToRefreshRate = value; }

    public boolean inputSmoothing() { return inputSmoothing; }
    public void inputSmoothing(boolean value) { this.inputSmoothing = value; }

    public double inputSmoothingStrength() { return inputSmoothingStrength; }
    public void inputSmoothingStrength(double value) { this.inputSmoothingStrength = value; }

    public boolean entityCulling() { return entityCulling; }
    public void entityCulling(boolean value) { this.entityCulling = value; }

    public double entityCullDistance() { return entityCullDistance; }
    public void entityCullDistance(double value) { this.entityCullDistance = value; }

    public double entityCullPixelSize() { return entityCullPixelSize; }
    public void entityCullPixelSize(double value) { this.entityCullPixelSize = value; }

    public boolean particleBudgeting() { return particleBudgeting; }
    public void particleBudgeting(boolean value) { this.particleBudgeting = value; }

    public int particleBudget() { return particleBudget; }
    public void particleBudget(int value) { this.particleBudget = value; }

    public boolean debugOverlay() { return debugOverlay; }
    public void debugOverlay(boolean value) { this.debugOverlay = value; }

    public RendererBackend backendHint() {
        return RendererBackend.UNKNOWN;
    }
}
