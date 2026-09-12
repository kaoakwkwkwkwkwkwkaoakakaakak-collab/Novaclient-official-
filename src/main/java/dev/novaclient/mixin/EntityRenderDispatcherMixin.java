package dev.novaclient.mixin;

import dev.novaclient.NovaClient;
import dev.novaclient.state.RenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void novaCullDistantEntities(Entity entity, Frustum frustum, double x, double y, double z,
                                         CallbackInfoReturnable<Boolean> info) {
        if (!NovaClient.isReady() || !RenderState.entityCulling() || entity == null) {
            return;
        }
        int limit = RenderState.entityCullDistance();
        if (limit <= 0) {
            return;
        }
        double squared = x * x + y * y + z * z;
        if (squared > (double) limit * limit) {
            info.setReturnValue(false);
        }
    }
}
