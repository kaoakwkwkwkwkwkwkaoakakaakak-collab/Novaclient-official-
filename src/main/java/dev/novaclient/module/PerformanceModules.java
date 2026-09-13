package dev.novaclient.module;

import dev.novaclient.effect.OptionEffects;
import dev.novaclient.setting.ModeSetting;
import dev.novaclient.setting.SliderSetting;
import dev.novaclient.setting.ToggleSetting;

final class PerformanceModules {

    private PerformanceModules() {
    }

    static void register(ModuleManager manager) {
        manager.registerInternal(frameLimiter());
        manager.registerInternal(idleFrameLimit());
        manager.registerInternal(entityCulling());
        manager.registerInternal(particleLimiter());
        manager.registerInternal(chunkCulling());
        manager.registerInternal(renderDistance());
        manager.registerInternal(lazyChunkLoading());
        manager.registerInternal(disableSmoothCamera());
        manager.registerInternal(noWeather());
        manager.registerInternal(noClouds());
        manager.registerInternal(noFog());
        manager.registerInternal(biomeBlend());
        manager.registerInternal(entityShadows());
        manager.registerInternal(memoryGuard());
    }

    private static Module frameLimiter() {
        ActiveModule module = new ActiveModule("Frame Limiter",
                "Caps the frame rate so the GPU stops running flat out.", Category.PERFORMANCE);
        SliderSetting cap = module.register(new SliderSetting("Max FPS", 120, 10, 260, 5));
        module.effect(enabled -> OptionEffects.frameRateLimit(enabled ? cap.intValue() : 260));
        return module;
    }

    private static Module idleFrameLimit() {
        ActiveModule module = new ActiveModule("Idle Frame Limit",
                "Drops the frame rate while you are not moving.", Category.PERFORMANCE);
        SliderSetting cap = module.register(new SliderSetting("Idle FPS", 30, 5, 120, 5));
        module.onTick(() -> OptionEffects.idleFrameTick(cap.intValue()));
        module.effect(enabled -> {
            if (!enabled) {
                OptionEffects.clearIdle();
            }
        });
        return module;
    }

    private static Module entityCulling() {
        ActiveModule module = new ActiveModule("Entity Culling",
                "Skips drawing entities that are not on screen.", Category.PERFORMANCE);
        SliderSetting distance = module.register(new SliderSetting("Distance", 64, 8, 256, 8, "m"));
        module.register(new ToggleSetting("Cull Behind Walls", true));
        module.effect(enabled -> OptionEffects.entityCulling(enabled, distance.intValue()));
        return module;
    }

    private static Module particleLimiter() {
        ActiveModule module = new ActiveModule("Particle Limiter",
                "Caps how many particles can exist at once.", Category.PERFORMANCE);
        SliderSetting budget = module.register(new SliderSetting("Budget", 300, 0, 4000, 50));
        module.effect(enabled -> OptionEffects.particleBudget(enabled ? budget.intValue() : -1));
        return module;
    }

    private static Module chunkCulling() {
        ActiveModule module = new ActiveModule("Chunk Culling",
                "Skips chunk sections hidden behind other geometry.", Category.PERFORMANCE);
        module.effect(enabled -> OptionEffects.chunkCulling(enabled));
        return module;
    }

    private static Module renderDistance() {
        ActiveModule module = new ActiveModule("Render Distance",
                "Overrides the render distance without touching your options menu.",
                Category.PERFORMANCE);
        SliderSetting chunks = module.register(new SliderSetting("Chunks", 8, 2, 32, 1));
        module.effect(enabled -> OptionEffects.renderDistance(enabled ? chunks.intValue() : -1));
        return module;
    }

    private static Module lazyChunkLoading() {
        ActiveModule module = new ActiveModule("Lazy Chunk Loading",
                "Spreads chunk builds over more frames to reduce stutter.", Category.PERFORMANCE);
        SliderSetting budget = module.register(new SliderSetting("Budget", 4.0, 1.0, 20.0, 0.5, "ms"));
        module.effect(enabled -> OptionEffects.buildBudget((float) (enabled ? budget.floatValue() : -1.0)));
        return module;
    }

    private static Module disableSmoothCamera() {
        ActiveModule module = new ActiveModule("Disable Cinematic Camera",
                "Turns off the smooth camera, which costs frames.", Category.PERFORMANCE);
        module.effect(enabled -> OptionEffects.smoothCamera(!enabled));
        return module;
    }

    private static Module noWeather() {
        ActiveModule module = new ActiveModule("No Weather",
                "Stops drawing rain and snow.", Category.PERFORMANCE);
        module.effect(enabled -> OptionEffects.weather(!enabled));
        return module;
    }

    private static Module noClouds() {
        ActiveModule module = new ActiveModule("No Clouds",
                "Stops drawing clouds.", Category.PERFORMANCE);
        module.effect(enabled -> OptionEffects.clouds(enabled ? "OFF" : "FANCY"));
        return module;
    }

    private static Module noFog() {
        ActiveModule module = new ActiveModule("No Fog",
                "Removes distance fog.", Category.PERFORMANCE);
        module.register(new ModeSetting("Mode", "Off", "Off", "Reduced"));
        module.effect(enabled -> OptionEffects.fog(!enabled));
        return module;
    }

    private static Module biomeBlend() {
        ActiveModule module = new ActiveModule("Biome Blend",
                "Lowers biome blending, which is expensive on chunk build.", Category.PERFORMANCE);
        SliderSetting radius = module.register(new SliderSetting("Radius", 0, 0, 7, 1));
        module.effect(enabled -> OptionEffects.biomeBlend(enabled ? radius.intValue() : 5));
        return module;
    }

    private static Module entityShadows() {
        ActiveModule module = new ActiveModule("No Entity Shadows",
                "Stops drawing the shadow disc under entities.", Category.PERFORMANCE);
        module.effect(enabled -> OptionEffects.entityShadows(!enabled));
        return module;
    }

    private static Module memoryGuard() {
        ActiveModule module = new ActiveModule("Memory Guard",
                "Releases cached buffers before the heap runs out.", Category.PERFORMANCE);
        SliderSetting threshold = module.register(new SliderSetting("Trigger", 85, 50, 95, 1, "%"));
        module.register(new ToggleSetting("On World Change", true));
        module.onTick(() -> OptionEffects.memoryTick(threshold.intValue()));
        return module;
    }
}
