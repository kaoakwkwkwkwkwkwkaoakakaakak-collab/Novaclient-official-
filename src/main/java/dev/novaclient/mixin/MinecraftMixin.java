package dev.novaclient.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.novaclient.NovaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    private long nova$lastTogglePoll;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void nova$tick(boolean render, CallbackInfo ci) {
        if (!NovaClient.isReady()) {
            return;
        }
        try {
            NovaClient.get().onTick();
            nova$pollToggleKey();
        } catch (RuntimeException failure) {

        }
    }

    private void nova$pollToggleKey() {
        long now = System.currentTimeMillis();
        if (now - nova$lastTogglePoll < 40L) {
            return;
        }
        nova$lastTogglePoll = now;

        Minecraft self = (Minecraft) (Object) this;
        if (self.getWindow() == null) {
            return;
        }
        long window = self.getWindow().getWindow();
        boolean down = InputConstants.isKeyDown(window, NovaClient.TOGGLE_KEY);
        if (!down) {
            nova$toggleWasDown = false;
            return;
        }
        if (nova$toggleWasDown) {
            return;
        }
        nova$toggleWasDown = true;

        Screen current = self.screen;
        if (current instanceof dev.novaclient.gui.ClickGuiScreen) {
            self.setScreen(null);
        } else {
            NovaClient.get().openGui();
        }
    }

    private boolean nova$toggleWasDown;
}
