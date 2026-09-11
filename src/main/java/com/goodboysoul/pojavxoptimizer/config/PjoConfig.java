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

public final class PjoConfig {

    public static final int SCHEMA_VERSION = 1;
    public static final String FILE_NAME = "pojavxoptimizer.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private int schemaVersion = SCHEMA_VERSION;
    private DeviceProfile profile = DeviceProfile.LOW;

    private boolean dynamicResolution = true;
    private float resolutionScaleMin = 0.60f;
    private float resolutionScaleMax = 1.00f;
    private int visibilitySectionCap = 4096;
    private boolean instancedSectionDrawing = true;
    private boolean packedVertexFormat = true;
    private boolean shaderPrecompile = true;

    private boolean packedMeshUpload = false;

    private boolean heapGuard = true;
    private float heapPressureThreshold = 0.82f;
    private boolean offHeapMeshes = true;
    private boolean aggressiveMeshEviction = true;

    private int buildThreads = 0;
    private double buildTimeBudgetMs = 6.0;
    private boolean viewWeightedChunkPriority = true;

    private boolean thermalGovernor = true;
    private double thermalBackoffThreshold = 0.30;
    private int thermalRecoverSeconds = 20;

    private boolean batterySaver = true;
    private int idleFpsCap = 20;
    private int guiFpsCap = 15;
    private boolean capToRefreshRate = true;
    private boolean maxFpsMode = false;

    private boolean inputSmoothing = false;
    private double inputSmoothingStrength = 0.35;

    private boolean entityCulling = true;
    private double entityCullDistance = 64.0;
    private double entityCullPixelSize = 3.0;
    private boolean particleBudgeting = true;
    private int particleBudget = 400;

    private boolean debugOverlay = false;

    private PjoConfig() {
    }

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

    public void applyProfile(DeviceProfile newProfile) {
        this.profile = newProfile;
        this.buildThreads = newProfile.buildThreads();
        this.particleBudget = newProfile.particleBudget();
        this.idleFpsCap = newProfile.idleFpsCap();
        this.heapPressureThreshold = newProfile.heapPressureThreshold();
        this.aggressiveMeshEviction = newProfile.aggressiveMemoryReclaim();
        this.thermalGovernor = newProfile.thermalGovernorEnabled();
        if (maxFpsMode) {
            this.batterySaver = false;
            this.capToRefreshRate = false;
            this.thermalGovernor = false;
        }
    }

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
        idleFpsCap = clampInt(idleFpsCap, 5, UNLIMITED_FPS);
        guiFpsCap = clampInt(guiFpsCap, 5, UNLIMITED_FPS);
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

    public int resolveBuildThreads() {
        int cores = Runtime.getRuntime().availableProcessors();
        int requested = buildThreads > 0 ? buildThreads : profile.buildThreads();
        return Math.max(1, Math.min(requested, Math.max(1, cores - 1)));
    }

    public boolean useInstancedDrawing(GpuCaps caps) {
        return instancedSectionDrawing && caps.supportsInstancing();
    }

    public boolean usePackedVertexFormat(GpuCaps caps) {
        return packedVertexFormat && caps.canUsePackedVertexFormat();
    }

    public int renderDistanceCap() {
        return profile.renderDistanceCap();
    }

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

    public static final int UNLIMITED_FPS = 260;

    public boolean maxFpsMode() { return maxFpsMode; }

    public void maxFpsMode(boolean value) {
        this.maxFpsMode = value;
        if (value) {
            this.batterySaver = false;
            this.capToRefreshRate = false;
            this.thermalGovernor = false;
        }
    }

    public int effectiveIdleFpsCap() {
        return maxFpsMode ? UNLIMITED_FPS : idleFpsCap;
    }

    public int effectiveGuiFpsCap() {
        return maxFpsMode ? UNLIMITED_FPS : guiFpsCap;
    }

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
