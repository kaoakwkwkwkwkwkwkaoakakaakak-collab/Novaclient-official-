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

/**
 * Draws the debug overlay on top of the vanilla HUD.
 *
 * <p>Injected at the tail of {@code Gui.render} so the overlay sits above everything vanilla drew,
 * and adds no instructions of its own beyond a guarded call. A mod that replaces the HUD entirely
 * simply never enters this method, which degrades to "no overlay" rather than to a crash.
 *
 * <p>Render failures are caught and logged once. A debug overlay that can take the HUD down would be
 * worse than having no overlay, and this code path only runs when the user has explicitly asked for
 * it.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    public net.minecraft.client.gui.Font getFont() {
        // Shadowed to reach the vanilla font renderer. Mixin discards this body at merge time and
        // binds the call to Gui's real implementation, so it is never executed.
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
