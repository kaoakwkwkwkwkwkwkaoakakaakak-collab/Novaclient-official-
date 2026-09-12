package dev.novaclient.state;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class RenderState {

    private static final AtomicBoolean ENTITY_CULLING = new AtomicBoolean();
    private static final AtomicInteger ENTITY_CULL_DISTANCE = new AtomicInteger(64);
    private static final AtomicInteger PARTICLE_BUDGET = new AtomicInteger(-1);
    private static final AtomicBoolean CHUNK_CULLING = new AtomicBoolean(true);
    private static final AtomicReference<Float> BUILD_BUDGET = new AtomicReference<>(-1.0f);
    private static final AtomicBoolean WEATHER = new AtomicBoolean(true);
    private static final AtomicReference<String> CLOUDS = new AtomicReference<>("Vanilla");
    private static final AtomicBoolean FOG = new AtomicBoolean(true);
    private static final AtomicInteger HEAP_PERCENT = new AtomicInteger();
    private static final AtomicBoolean EVICTION_REQUESTED = new AtomicBoolean();
    private static final AtomicBoolean MOTION_BLUR = new AtomicBoolean();
    private static final AtomicInteger MOTION_BLUR_AMOUNT = new AtomicInteger(40);
    private static final AtomicBoolean FULL_BRIGHT = new AtomicBoolean();
    private static final AtomicBoolean HIT_BOXES = new AtomicBoolean();
    private static final AtomicBoolean LOW_FIRE = new AtomicBoolean();
    private static final AtomicInteger LOW_FIRE_HEIGHT = new AtomicInteger(50);
    private static final AtomicInteger TIME_OF_DAY = new AtomicInteger(-1);
    private static final AtomicBoolean ITEM_PHYSICS = new AtomicBoolean();
    private static final AtomicInteger HAND_SCALE = new AtomicInteger(100);
    private static final AtomicInteger RESOLUTION_SCALE = new AtomicInteger(100);
    private static final AtomicInteger ACCENT_COLOR = new AtomicInteger(0xFF7C9CFF);
    private static final AtomicInteger NAME_TAG_RANGE = new AtomicInteger(64);
    private static final AtomicBoolean CHUNK_BORDERS = new AtomicBoolean();
    private static final AtomicBoolean WATER_OVERLAY = new AtomicBoolean(true);
    private static final AtomicBoolean PUMPKIN_OVERLAY = new AtomicBoolean(true);
    private static final AtomicBoolean PORTAL_OVERLAY = new AtomicBoolean(true);
    private static final AtomicBoolean HAND_SCALED = new AtomicBoolean();
    private static final java.util.concurrent.atomic.AtomicReference<String> FOG_MODE =
            new java.util.concurrent.atomic.AtomicReference<>("Vanilla");
    private static final AtomicBoolean CUSTOM_SKY = new AtomicBoolean();
    private static final java.util.concurrent.atomic.AtomicReference<String> MOTION_BLUR_FAULT =
            new java.util.concurrent.atomic.AtomicReference<>();

    private RenderState() {
    }

    public static void entityCulling(boolean enabled, int distance) {
        ENTITY_CULLING.set(enabled);
        ENTITY_CULL_DISTANCE.set(distance);
    }

    public static boolean entityCulling() {
        return ENTITY_CULLING.get();
    }

    public static int entityCullDistance() {
        return ENTITY_CULL_DISTANCE.get();
    }

    public static void particleBudget(int budget) {
        PARTICLE_BUDGET.set(budget);
    }

    public static int particleBudget() {
        return PARTICLE_BUDGET.get();
    }

    public static void chunkCulling(boolean enabled) {
        CHUNK_CULLING.set(enabled);
    }

    public static boolean chunkCulling() {
        return CHUNK_CULLING.get();
    }

    public static void buildBudgetMillis(float millis) {
        BUILD_BUDGET.set(millis);
    }

    public static float buildBudgetMillis() {
        return BUILD_BUDGET.get();
    }

    public static void weather(boolean enabled) {
        WEATHER.set(enabled);
    }

    public static boolean weather() {
        return WEATHER.get();
    }

    public static void clouds(String mode) {
        CLOUDS.set(mode == null ? "Vanilla" : mode);
    }

    public static String clouds() {
        return CLOUDS.get();
    }

    public static void fog(boolean enabled) {
        FOG.set(enabled);
    }

    public static boolean fog() {
        return FOG.get();
    }

    public static void heapPercent(int percent) {
        HEAP_PERCENT.set(percent);
    }

    public static int heapPercent() {
        return HEAP_PERCENT.get();
    }

    public static void requestEviction() {
        EVICTION_REQUESTED.set(true);
    }

    public static boolean consumeEvictionRequest() {
        return EVICTION_REQUESTED.getAndSet(false);
    }

    public static void motionBlur(boolean enabled, int amount) {
        MOTION_BLUR.set(enabled);
        MOTION_BLUR_AMOUNT.set(amount);
    }

    public static boolean motionBlur() {
        return MOTION_BLUR.get();
    }

    public static int motionBlurAmount() {
        return MOTION_BLUR_AMOUNT.get();
    }

    public static void fullBright(boolean enabled) {
        FULL_BRIGHT.set(enabled);
    }

    public static boolean fullBright() {
        return FULL_BRIGHT.get();
    }

    public static void hitBoxes(boolean enabled) {
        HIT_BOXES.set(enabled);
    }

    public static boolean hitBoxes() {
        return HIT_BOXES.get();
    }

    public static void motionBlurFault(String message) {
        MOTION_BLUR_FAULT.set(message);
    }

    public static String motionBlurFault() {
        return MOTION_BLUR_FAULT.get();
    }

    public static void fogMode(String mode) {
        FOG_MODE.set(mode == null ? "Vanilla" : mode);
    }

    public static String fogMode() {
        return FOG_MODE.get();
    }

    public static void customSky(boolean enabled) {
        CUSTOM_SKY.set(enabled);
    }

    public static boolean customSky() {
        return CUSTOM_SKY.get();
    }

    public static void waterOverlay(boolean enabled) {
        WATER_OVERLAY.set(enabled);
    }

    public static boolean waterOverlay() {
        return WATER_OVERLAY.get();
    }

    public static void handScaled(boolean enabled) {
        HAND_SCALED.set(enabled);
    }

    public static boolean handScaled() {
        return HAND_SCALED.get();
    }

    public static void pumpkinOverlay(boolean enabled) {
        PUMPKIN_OVERLAY.set(enabled);
    }

    public static boolean pumpkinOverlay() {
        return PUMPKIN_OVERLAY.get();
    }

    public static void portalOverlay(boolean enabled) {
        PORTAL_OVERLAY.set(enabled);
    }

    public static boolean portalOverlay() {
        return PORTAL_OVERLAY.get();
    }

    public static void chunkBorders(boolean enabled) {
        CHUNK_BORDERS.set(enabled);
    }

    public static boolean chunkBorders() {
        return CHUNK_BORDERS.get();
    }

    public static void nameTagRange(int range) {
        NAME_TAG_RANGE.set(range);
    }

    public static int nameTagRange() {
        return NAME_TAG_RANGE.get();
    }

    public static void lowFire(boolean enabled, int height) {
        LOW_FIRE.set(enabled);
        LOW_FIRE_HEIGHT.set(height);
    }

    public static boolean lowFire() {
        return LOW_FIRE.get();
    }

    public static int lowFireHeight() {
        return LOW_FIRE_HEIGHT.get();
    }

    public static void timeOfDay(int hour) {
        TIME_OF_DAY.set(hour);
    }

    public static int timeOfDay() {
        return TIME_OF_DAY.get();
    }

    public static void itemPhysics(boolean enabled) {
        ITEM_PHYSICS.set(enabled);
    }

    public static boolean itemPhysics() {
        return ITEM_PHYSICS.get();
    }

    public static void handScale(int percent) {
        HAND_SCALE.set(percent);
    }

    public static int handScale() {
        return HAND_SCALE.get();
    }

    public static void resolutionScale(int percent) {
        RESOLUTION_SCALE.set(percent);
    }

    public static int resolutionScale() {
        return RESOLUTION_SCALE.get();
    }

    public static void accentColor(int argb) {
        ACCENT_COLOR.set(argb);
    }

    public static int accentColor() {
        return ACCENT_COLOR.get();
    }

    public static void reset() {
        ENTITY_CULLING.set(false);
        ENTITY_CULL_DISTANCE.set(64);
        PARTICLE_BUDGET.set(-1);
        CHUNK_CULLING.set(true);
        BUILD_BUDGET.set(-1.0f);
        WEATHER.set(true);
        CLOUDS.set("Vanilla");
        FOG.set(true);
        MOTION_BLUR.set(false);
        MOTION_BLUR_AMOUNT.set(40);
        FULL_BRIGHT.set(false);
        HIT_BOXES.set(false);
        LOW_FIRE.set(false);
        LOW_FIRE_HEIGHT.set(50);
        TIME_OF_DAY.set(-1);
        ITEM_PHYSICS.set(false);
        HAND_SCALE.set(100);
        RESOLUTION_SCALE.set(100);
        EVICTION_REQUESTED.set(false);
        CHUNK_BORDERS.set(false);
        NAME_TAG_RANGE.set(64);
    }
}
