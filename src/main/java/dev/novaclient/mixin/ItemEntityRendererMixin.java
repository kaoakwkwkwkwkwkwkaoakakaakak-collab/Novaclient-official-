package dev.novaclient.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.novaclient.NovaClient;
import dev.novaclient.state.RenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Inject(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void novaLayItemFlat(ItemEntity entity, float yaw, float partialTicks, PoseStack poseStack,
                                 MultiBufferSource buffer, int light, CallbackInfo info) {
        if (!NovaClient.isReady() || !RenderState.itemPhysics() || entity == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0, 0.12, 0.0);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
        nova$flattened = true;
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void novaRestoreItem(ItemEntity entity, float yaw, float partialTicks, PoseStack poseStack,
                                 MultiBufferSource buffer, int light, CallbackInfo info) {
        if (nova$flattened) {
            poseStack.popPose();
            nova$flattened = false;
        }
    }

    private boolean nova$flattened;
}
