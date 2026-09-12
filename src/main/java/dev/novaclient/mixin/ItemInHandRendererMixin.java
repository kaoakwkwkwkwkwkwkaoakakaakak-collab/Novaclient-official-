package dev.novaclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.novaclient.NovaClient;
import dev.novaclient.state.RenderState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void novaScaleHand(AbstractClientPlayer player, float partialTicks, float pitch,
                               InteractionHand hand, float swingProgress, ItemStack stack,
                               float equipProgress, PoseStack poseStack,
                               MultiBufferSource buffer, int light, CallbackInfo info) {
        if (!NovaClient.isReady() || !RenderState.handScaled()) {
            return;
        }
        int percent = Math.max(10, Math.min(RenderState.handScale(), 200));
        float factor = percent / 100.0f;
        poseStack.pushPose();
        poseStack.scale(factor, factor, factor);
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void novaRestoreHand(AbstractClientPlayer player, float partialTicks, float pitch,
                                 InteractionHand hand, float swingProgress, ItemStack stack,
                                 float equipProgress, PoseStack poseStack,
                                 MultiBufferSource buffer, int light, CallbackInfo info) {
        if (NovaClient.isReady() && RenderState.handScaled()) {
            poseStack.popPose();
        }
    }
}
