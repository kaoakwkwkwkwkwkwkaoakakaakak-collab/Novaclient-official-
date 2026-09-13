package dev.novaclient.mixin;

import dev.novaclient.NovaClient;
import dev.novaclient.state.PlayerState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    private Vec3 nova$motionBeforeUse;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void novaRememberMotion(CallbackInfo info) {
        Player self = (Player) (Object) this;
        nova$motionBeforeUse = self.getDeltaMovement();
    }

    @Inject(method = "aiStep", at = @At("RETURN"))
    private void novaCancelUseSlowdown(CallbackInfo info) {
        if (!NovaClient.isReady() || !PlayerState.noSlow()) {
            return;
        }
        Player self = (Player) (Object) this;
        if (!self.isUsingItem()) {
            return;
        }
        Vec3 before = nova$motionBeforeUse;
        Vec3 after = self.getDeltaMovement();
        if (before == null) {
            return;
        }
        double beforeHorizontal = Math.sqrt(before.x * before.x + before.z * before.z);
        if (beforeHorizontal < 1.0E-4) {
            return;
        }
        double afterHorizontal = Math.sqrt(after.x * after.x + after.z * after.z);
        if (afterHorizontal < 1.0E-4 || afterHorizontal >= beforeHorizontal) {
            return;
        }
        double scale = beforeHorizontal / afterHorizontal;
        self.setDeltaMovement(after.x * scale, after.y, after.z * scale);
    }
}
