package dev.novaclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.novaclient.NovaClient;
import dev.novaclient.state.RenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void novaHideWaterOverlay(Minecraft minecraft, PoseStack poseStack, CallbackInfo info) {
        if (NovaClient.isReady() && !RenderState.waterOverlay()) {
            info.cancel();
        }
    }

    @Inject(method = "renderFire", at = @At("HEAD"))
    private static void novaLowerFire(Minecraft minecraft, PoseStack poseStack, CallbackInfo info) {
        if (!NovaClient.isReady() || !RenderState.lowFire()) {
            return;
        }
        int percent = Math.max(1, Math.min(RenderState.lowFireHeight(), 100));
        poseStack.pushPose();
        poseStack.scale(1.0f, percent / 100.0f, 1.0f);
    }

    @Inject(method = "renderFire", at = @At("RETURN"))
    private static void novaRestoreFire(Minecraft minecraft, PoseStack poseStack, CallbackInfo info) {
        if (NovaClient.isReady() && RenderState.lowFire()) {
            poseStack.popPose();
        }
    }
}
