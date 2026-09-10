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

/**
 * Adds distance and projected-size tests to the existing entity visibility decision.
 *
 * <p>Vanilla's {@code shouldRender} already handles frustum culling correctly, so this does not
 * replace it — it only adds a refusal on top. When PojavXOptimizer is not ready, not configured for
 * entity culling, or the entity is the one the player is riding, the original result stands.
 *
 * <p>The rider exception is not cosmetic. Culling the entity you are sitting on removes your own
 * mount from the world, which reads as a serious bug rather than as an optimisation.
 */
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
            // Some entity types override bounding-box access and can throw before they are fully
            // initialised. A default width keeps culling conservative instead of crashing.
            return 0.6;
        }
    }
}
