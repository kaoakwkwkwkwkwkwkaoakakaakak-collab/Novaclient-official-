package dev.novaclient.mixin;

import dev.novaclient.NovaClient;
import dev.novaclient.state.PlayerState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void novaAllowMovementInInventory(int keyCode, int scanCode, int modifiers,
                                              CallbackInfoReturnable<Boolean> info) {
        if (!NovaClient.isReady() || !PlayerState.inventoryMove()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return;
        }
        KeyMapping[] movement = {
                minecraft.options.keyUp, minecraft.options.keyDown,
                minecraft.options.keyLeft, minecraft.options.keyRight,
                minecraft.options.keyJump, minecraft.options.keySprint,
                minecraft.options.keyShift
        };
        for (KeyMapping mapping : movement) {
            if (mapping == null || !mapping.matches(keyCode, scanCode)) {
                continue;
            }
            mapping.setDown(true);
            info.setReturnValue(true);
            return;
        }
    }
}
