package com.goodboysoul.pojavxoptimizer.mixin.client;

import com.goodboysoul.pojavxoptimizer.PojavXOptimizer;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    public net.minecraft.client.gui.Font getFont() {

        throw new AssertionError("mixin stub");
    }

    @Unique
    private boolean pjo$hudErrorLogged;

    @Inject(method = "render", at = @At("TAIL"))
    private void pjo$renderDebugOverlay(GuiGraphics graphics, DeltaTracker deltaTracker,
                                        CallbackInfo ci) {
        if (!PojavXOptimizer.isReady() || graphics == null) {
            return;
        }
        PojavXOptimizer mod = PojavXOptimizer.get();
        if (!mod.config().debugOverlay()) {
            return;
        }
        try {
            mod.debugHud().tick();
            mod.debugHud().render(graphics, getFont());
        } catch (RuntimeException renderFailed) {
            if (!pjo$hudErrorLogged) {
                pjo$hudErrorLogged = true;
                PJO.warn("Debug overlay failed to render; disabling it for this session.",
                        renderFailed);
                mod.config().debugOverlay(false);
            }
        }
    }
}
