package com.goodboysoul.pojavxoptimizer.mixin.client;

import com.goodboysoul.pojavxoptimizer.PojavXOptimizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void pjo$extraCullChecks(Entity entity, Frustum frustum,
                                     double cameraX, double cameraY, double cameraZ,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (!PojavXOptimizer.isReady() || entity == null) {
            return;
        }
        PojavXOptimizer mod = PojavXOptimizer.get();
        if (!mod.config().entityCulling()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getCameraEntity() == entity) {
            return;
        }

        double distanceSquared = pjo$distanceSquared(entity, cameraX, cameraY, cameraZ);
        double width = pjo$entityWidth(entity);

        float fov = client.options != null && client.options.fov() != null
                ? client.options.fov().get().floatValue()
                : 70.0f;
        int viewportHeight = client.getWindow() != null
                ? client.getWindow().getScreenHeight()
                : 1080;

        if (mod.entityCullPolicy().shouldCull(distanceSquared, width, fov, viewportHeight)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private double pjo$distanceSquared(Entity entity, double cameraX, double cameraY, double cameraZ) {
        double dx = entity.getX() - cameraX;
        double dy = entity.getY() - cameraY;
        double dz = entity.getZ() - cameraZ;
        return dx * dx + dy * dy + dz * dz;
    }

    @Unique
    private double pjo$entityWidth(Entity entity) {
        try {
            AABB box = entity.getBoundingBox();
            return box == null ? 0.6 : box.getXsize();
        } catch (RuntimeException boxUnavailable) {

            return 0.6;
        }
    }
}
