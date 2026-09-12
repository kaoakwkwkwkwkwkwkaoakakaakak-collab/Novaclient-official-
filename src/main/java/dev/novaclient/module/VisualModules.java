package dev.novaclient.module;

import dev.novaclient.effect.VisualEffects;
import dev.novaclient.setting.ModeSetting;
import dev.novaclient.setting.SliderSetting;
import dev.novaclient.setting.ToggleSetting;

final class VisualModules {

    private VisualModules() {
    }

    static void register(ModuleManager manager) {
        ActiveModule motionBlur = new ActiveModule("Motion Blur",
                "Blends previous frames so fast movement looks smooth.", Category.VISUAL);
        SliderSetting amount = motionBlur.register(new SliderSetting("Amount", 40, 5, 90, 5, "%"));
        ToggleSetting menusOnly = motionBlur.register(new ToggleSetting("In Menus Only", false));
        motionBlur.effect(enabled -> VisualEffects.motionBlur(enabled, amount.intValue()));
        motionBlur.onTick(() -> VisualEffects.motionBlurTick(amount.intValue(), menusOnly.get()));
        manager.registerInternal(motionBlur);

        ActiveModule fullBright = new ActiveModule("Full Bright",
                "Lights every block as if it were daylight.", Category.VISUAL);
        fullBright.effect(enabled -> VisualEffects.fullBright(enabled));
        manager.registerInternal(fullBright);

        ActiveModule nameTags = new ActiveModule("Name Tags", "Redraws player name plates.",
                Category.VISUAL);
        SliderSetting range = nameTags.register(new SliderSetting("Range", 64, 8, 128, 4, "m"));
        nameTags.register(new ToggleSetting("Show Health", true));
        nameTags.register(new ToggleSetting("Show Ping", false));
        nameTags.register(new ToggleSetting("Background", true));
        nameTags.effect(enabled -> VisualEffects.nameTags(enabled, range.intValue()));
        manager.registerInternal(nameTags);

        ActiveModule itemPhysics = new ActiveModule("Item Physics",
                "Makes dropped items lie flat on the ground.", Category.VISUAL);
        itemPhysics.register(new SliderSetting("Rotation", 90, 0, 90, 5, "deg"));
        itemPhysics.effect(enabled -> VisualEffects.itemPhysics(enabled));
        manager.registerInternal(itemPhysics);

        ActiveModule hitBoxes = new ActiveModule("Hit Boxes", "Draws entity bounding boxes.",
                Category.VISUAL);
        hitBoxes.register(new ToggleSetting("Show Eyes", true));
        hitBoxes.effect(enabled -> VisualEffects.hitBoxes(enabled));
        manager.registerInternal(hitBoxes);

        ActiveModule chunkBorders = new ActiveModule("Chunk Borders",
                "Draws the section outlines.", Category.VISUAL);
        chunkBorders.effect(enabled -> VisualEffects.chunkBorders(enabled));
        manager.registerInternal(chunkBorders);

        ActiveModule lowFire = new ActiveModule("Low Fire",
                "Lowers the fire overlay so you can see.", Category.VISUAL);
        SliderSetting height = lowFire.register(new SliderSetting("Height", 50, 10, 100, 5, "%"));
        lowFire.effect(enabled -> VisualEffects.lowFire(enabled, height.intValue()));
        manager.registerInternal(lowFire);

        ActiveModule noHurtCam = new ActiveModule("No Hurt Cam",
                "Removes the red flash when you take damage.", Category.VISUAL);
        noHurtCam.effect(enabled -> VisualEffects.hurtCam(!enabled));
        manager.registerInternal(noHurtCam);

        ActiveModule noPumpkin = new ActiveModule("No Pumpkin Overlay",
                "Removes the pumpkin blur.", Category.VISUAL);
        noPumpkin.effect(enabled -> VisualEffects.pumpkinOverlay(!enabled));
        manager.registerInternal(noPumpkin);

        ActiveModule noWater = new ActiveModule("No Water Overlay",
                "Removes the blue water tint.", Category.VISUAL);
        noWater.effect(enabled -> VisualEffects.waterOverlay(!enabled));
        manager.registerInternal(noWater);

        ActiveModule noPortal = new ActiveModule("No Portal Overlay",
                "Removes the purple portal swirl.", Category.VISUAL);
        noPortal.effect(enabled -> VisualEffects.portalOverlay(!enabled));
        manager.registerInternal(noPortal);

        ActiveModule timeChanger = new ActiveModule("Time Changer",
                "Draws the world at a fixed time of day.", Category.VISUAL);
        SliderSetting hour = timeChanger.register(new SliderSetting("Hour", 6, 0, 23, 1, "h"));
        timeChanger.effect(enabled -> VisualEffects.timeOfDay(enabled ? hour.intValue() : -1));
        manager.registerInternal(timeChanger);

        ActiveModule handFov = new ActiveModule("Hand FOV",
                "Scales how large the held item appears.", Category.VISUAL);
        SliderSetting scale = handFov.register(new SliderSetting("Scale", 100, 50, 200, 5, "%"));
        handFov.effect(enabled -> VisualEffects.handScale(enabled, scale.intValue()));
        manager.registerInternal(handFov);

        ActiveModule screenScale = new ActiveModule("Resolution Scale",
                "Renders at a lower resolution and upscales.", Category.VISUAL);
        SliderSetting scaleDown = screenScale.register(new SliderSetting("Scale", 100, 50, 100, 5, "%"));
        screenScale.effect(enabled -> VisualEffects.resolutionScale(enabled ? scaleDown.intValue() : 100));
        manager.registerInternal(screenScale);

        ActiveModule chroma = new ActiveModule("Chroma", "Cycles the accent colour.",
                Category.VISUAL);
        SliderSetting speed = chroma.register(new SliderSetting("Speed", 50, 5, 200, 5, "%"));
        SliderSetting saturation = chroma.register(new SliderSetting("Saturation", 80, 10, 100, 5, "%"));
        chroma.effect(enabled -> VisualEffects.chroma(enabled, speed.intValue(), saturation.intValue()));
        chroma.onTick(() -> VisualEffects.chroma(true, speed.intValue(), saturation.intValue()));
        manager.registerInternal(chroma);
    }
}
