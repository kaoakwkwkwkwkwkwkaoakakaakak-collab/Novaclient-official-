package dev.novaclient.mixin;

import dev.novaclient.NovaClient;
import dev.novaclient.state.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftAttackMixin {

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void novaSelectBestTool(CallbackInfoReturnable<Boolean> info) {
        if (!NovaClient.isReady() || !PlayerState.autoTool()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            return;
        }
        LocalPlayer player = minecraft.player;
        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHit)) {
            return;
        }
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        if (state == null || state.isAir()) {
            return;
        }
        int best = player.getInventory().selected;
        float bestSpeed = speedFor(player.getInventory().getItem(best), state, player);
        for (int slot = 0; slot < 9; slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.isEmpty()) {
                continue;
            }
            float speed = speedFor(candidate, state, player);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                best = slot;
            }
        }
        if (best != player.getInventory().selected) {
            player.getInventory().selected = best;
        }
    }

    private static float speedFor(ItemStack stack, BlockState state, LocalPlayer player) {
        try {
            return stack.getDestroySpeed(state);
        } catch (RuntimeException unavailable) {
            return 1.0f;
        }
    }
}
