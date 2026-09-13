package dev.novaclient.module;

import dev.novaclient.effect.RenderEffects;
import dev.novaclient.setting.ModeSetting;
import dev.novaclient.setting.SliderSetting;
import dev.novaclient.setting.ToggleSetting;

final class RenderModules {

    private RenderModules() {
    }

    static void register(ModuleManager manager) {
        ActiveModule fog = new ActiveModule("Fog Control", "Adjusts or removes distance fog.",
                Category.RENDER);
        ModeSetting fogMode = fog.register(new ModeSetting("Mode", "Off", "Off", "Reduced", "Vanilla"));
        fog.effect(enabled -> RenderEffects.fog(enabled ? fogMode.get() : "Vanilla"));
        manager.registerInternal(fog);

        ActiveModule clouds = new ActiveModule("Clouds", "Chooses how clouds are drawn.",
                Category.RENDER);
        ModeSetting cloudMode = clouds.register(new ModeSetting("Quality", "Fast",
                "Off", "Fast", "Fancy"));
        clouds.effect(enabled -> RenderEffects.clouds(cloudMode.get()));
        manager.registerInternal(clouds);

        ActiveModule sky = new ActiveModule("Hide Sky",
                "Hides the skybox, sun, moon and stars.", Category.RENDER);
        sky.effect(enabled -> RenderEffects.sky(enabled));
        manager.registerInternal(sky);

        ActiveModule weather = new ActiveModule("Weather", "Controls rain and snow rendering.",
                Category.RENDER);
        ModeSetting weatherMode = weather.register(new ModeSetting("Mode", "Clear",
                "Clear", "Reduced", "Vanilla"));
        weather.effect(enabled -> RenderEffects.weather(weatherMode.get()));
        manager.registerInternal(weather);

        ActiveModule shadows = new ActiveModule("Entity Shadows", "Draws shadows under entities.",
                Category.RENDER);
        shadows.effect(enabled -> RenderEffects.entityShadows(enabled));
        manager.registerInternal(shadows);

        ActiveModule particles = new ActiveModule("Particles", "Chooses the particle quality.",
                Category.RENDER);
        ModeSetting particleMode = particles.register(new ModeSetting("Quality", "All",
                "All", "Decreased", "Minimal"));
        particles.effect(enabled -> RenderEffects.particles(particleMode.get()));
        manager.registerInternal(particles);

        ActiveModule graphics = new ActiveModule("Graphics Mode", "Switches fast and fancy.",
                Category.RENDER);
        ModeSetting graphicsMode = graphics.register(new ModeSetting("Mode", "Fast",
                "Fast", "Fancy", "Fabulous"));
        graphics.effect(enabled -> RenderEffects.graphicsMode(graphicsMode.get()));
        manager.registerInternal(graphics);

        ActiveModule light = new ActiveModule("Smooth Lighting", "Chooses the lighting quality.",
                Category.RENDER);
        ModeSetting lightMode = light.register(new ModeSetting("Mode", "Off",
                "Off", "Minimum", "Maximum"));
        light.effect(enabled -> RenderEffects.smoothLighting(lightMode.get()));
        manager.registerInternal(light);

        ActiveModule viewDistance = new ActiveModule("View Distance",
                "Overrides the render distance.", Category.RENDER);
        SliderSetting chunks = viewDistance.register(new SliderSetting("Chunks", 8, 2, 32, 1));
        viewDistance.effect(enabled -> RenderEffects.renderDistance(enabled ? chunks.intValue() : -1));
        manager.registerInternal(viewDistance);

        ActiveModule simDistance = new ActiveModule("Simulation Distance",
                "Overrides how far the world ticks.", Category.RENDER);
        SliderSetting sim = simDistance.register(new SliderSetting("Chunks", 6, 2, 32, 1));
        simDistance.effect(enabled -> RenderEffects.simulationDistance(enabled ? sim.intValue() : -1));
        manager.registerInternal(simDistance);

        ActiveModule bob = new ActiveModule("View Bobbing", "Toggles the walking sway.",
                Category.RENDER);
        bob.effect(enabled -> RenderEffects.viewBobbing(enabled));
        manager.registerInternal(bob);

        ActiveModule vignette = new ActiveModule("Vignette", "Toggles the dark screen edges.",
                Category.RENDER);
        vignette.effect(enabled -> RenderEffects.vignette(enabled));
        manager.registerInternal(vignette);

        ActiveModule distortion = new ActiveModule("Distortion Effects",
                "Toggles the nausea and portal wobble.", Category.RENDER);
        SliderSetting amount = distortion.register(new SliderSetting("Amount", 100, 0, 100, 5, "%"));
        distortion.effect(enabled -> RenderEffects.distortion(enabled ? amount.intValue() : 100));
        manager.registerInternal(distortion);

        ActiveModule damageTilt = new ActiveModule("Damage Tilt", "Toggles the hit shake.",
                Category.RENDER);
        damageTilt.effect(enabled -> RenderEffects.damageTilt(enabled));
        manager.registerInternal(damageTilt);

        ActiveModule darkScreen = new ActiveModule("Dark Screen", "Toggles the darkness overlay.",
                Category.RENDER);
        darkScreen.effect(enabled -> RenderEffects.darkness(enabled));
        manager.registerInternal(darkScreen);
    }
}
