package dev.novaclient.mixin;

import dev.novaclient.NovaClient;
import dev.novaclient.hud.HudRenderer;
import dev.novaclient.state.RenderState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    private boolean nova$hudFailed;

    @Inject(method = "render", at = @At("TAIL"))
    private void nova$renderHud(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!NovaClient.isReady() || nova$hudFailed) {
            return;
        }
        try {
            HudRenderer.render(graphics);
        } catch (RuntimeException failure) {
            nova$hudFailed = true;
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void nova$hidePortalOverlay(GuiGraphics graphics, float alpha, CallbackInfo ci) {
        if (NovaClient.isReady() && !RenderState.portalOverlay()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCameraOverlays", at = @At("HEAD"), cancellable = true)
    private void nova$hidePumpkinOverlay(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (NovaClient.isReady() && !RenderState.pumpkinOverlay()) {
            ci.cancel();
        }
    }
}
