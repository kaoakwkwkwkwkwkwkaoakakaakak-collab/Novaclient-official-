package dev.novaclient.module;

import dev.novaclient.effect.PlayerEffects;
import dev.novaclient.setting.ModeSetting;
import dev.novaclient.setting.SliderSetting;
import dev.novaclient.setting.ToggleSetting;

final class PlayerModules {

    private PlayerModules() {
    }

    static void register(ModuleManager manager) {
        ActiveModule sprint = new ActiveModule("Auto Sprint", "Keeps you sprinting.",
                Category.PLAYER);
        sprint.register(new ToggleSetting("Only Moving Forward", true));
        sprint.onTick(() -> PlayerEffects.autoSprint());
        manager.registerInternal(sprint);

        ActiveModule sneak = new ActiveModule("Sneak Toggle", "Makes sneak a toggle.",
                Category.PLAYER);
        sneak.register(new ModeSetting("Mode", "Toggle", "Toggle", "Hold"));
        sneak.effect(enabled -> PlayerEffects.sneakToggle(enabled));
        manager.registerInternal(sneak);

        ActiveModule noSlow = new ActiveModule("No Slowdown",
                "Keeps full speed while eating or blocking.", Category.PLAYER);
        noSlow.effect(enabled -> PlayerEffects.noSlow(enabled));
        manager.registerInternal(noSlow);

        ActiveModule autoTool = new ActiveModule("Auto Tool",
                "Switches to the best tool for the block.", Category.PLAYER);
        autoTool.effect(enabled -> PlayerEffects.autoTool(enabled));
        manager.registerInternal(autoTool);

        ActiveModule perspective = new ActiveModule("Perspective Hold",
                "Keeps the camera behind you in third person.", Category.PLAYER);
        perspective.register(new ToggleSetting("Reset On Release", true));
        perspective.effect(enabled -> PlayerEffects.perspectiveHold(enabled));
        manager.registerInternal(perspective);

        ActiveModule fastPlace = new ActiveModule("Fast Placement",
                "Removes the delay between placing blocks.", Category.PLAYER);
        SliderSetting delay = fastPlace.register(new SliderSetting("Delay", 0, 0, 4, 1, "t"));
        fastPlace.effect(enabled -> PlayerEffects.placeDelay(enabled ? delay.intValue() : 4));
        manager.registerInternal(fastPlace);

        ActiveModule reach = new ActiveModule("Reach Display",
                "Shows your block reach distance.", Category.PLAYER);
        reach.effect(enabled -> PlayerEffects.reachDisplay(enabled));
        manager.registerInternal(reach);

        ActiveModule inventoryMove = new ActiveModule("Inventory Move",
                "Lets you walk while an inventory is open.", Category.PLAYER);
        inventoryMove.effect(enabled -> PlayerEffects.inventoryMove(enabled));
        manager.registerInternal(inventoryMove);
    }
}
