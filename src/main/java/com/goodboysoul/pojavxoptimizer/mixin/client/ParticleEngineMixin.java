package com.goodboysoul.pojavxoptimizer.mixin.client;

import com.goodboysoul.pojavxoptimizer.PojavXOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the particle population budget.
 *
 * <p>Two injections, both additive. The one on {@code createParticle} refuses new particles before
 * they are allocated, which is where the saving actually is: a particle that is never constructed
 * costs no allocation, no tick, and no draw call. The one on {@code add} maintains the live count.
 *
 * <p>Refusing at creation rather than culling from the live set matters. Removing an existing
 * particle mid-animation is visible as a particle winking out; declining to spawn a new one is not,
 * because nothing was ever on screen to miss.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void pjo$maybeRefuseSpawn(
            net.minecraft.core.particles.ParticleOptions options,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            CallbackInfoReturnable<Particle> cir) {
        PojavXOptimizer mod = PojavXOptimizer.get();
        if (!PojavXOptimizer.isReady()) {
            return;
        }
        if (mod.particleBudget().shouldSuppressSpawn()) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "add", at = @At("HEAD"))
    private void pjo$onParticleAdded(Particle particle, CallbackInfo ci) {
        if (!PojavXOptimizer.isReady() || particle == null) {
            return;
        }
        PojavXOptimizer.get().particleBudget().onSpawned();
    }
}
