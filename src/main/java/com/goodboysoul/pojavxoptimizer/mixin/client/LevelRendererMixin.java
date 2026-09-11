package com.goodboysoul.pojavxoptimizer.mixin.client;

import com.goodboysoul.pojavxoptimizer.PojavXOptimizer;
import com.goodboysoul.pojavxoptimizer.core.ViewState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "setupRender", at = @At("HEAD"))
    private void pjo$captureView(Camera camera, Frustum frustum,
                                 boolean captureFrustum, boolean isSpectator, CallbackInfo ci) {
        if (!PojavXOptimizer.isReady() || camera == null) {
            return;
        }
        try {
            PojavXOptimizer mod = PojavXOptimizer.get();
            double camX = camera.getPosition().x;
            double camY = camera.getPosition().y;
            double camZ = camera.getPosition().z;
            float lookX = camera.getLookVector().x();
            float lookY = camera.getLookVector().y();
            float lookZ = camera.getLookVector().z();

            ViewState.update(camX, camY, camZ, lookX, lookY, lookZ);
            mod.chunkPrioritizer().setView(camX, camY, camZ, lookX, lookY, lookZ);
        } catch (RuntimeException captureFailed) {

            if (!pjo$captureErrorLogged) {
                pjo$captureErrorLogged = true;
                com.goodboysoul.pojavxoptimizer.core.PJO.warn(
                        "Could not capture the view direction; chunk prioritisation will use "
                        + "plain distance ordering.", captureFailed);
            }
        }
    }

    @Unique
    private boolean pjo$captureErrorLogged;
}
