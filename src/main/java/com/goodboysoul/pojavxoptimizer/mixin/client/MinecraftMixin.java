package com.goodboysoul.pojavxoptimizer.mixin.client;

import com.goodboysoul.pojavxoptimizer.PojavXOptimizer;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.core.ViewState;
import com.goodboysoul.pojavxoptimizer.gui.PjoOptionsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives PojavXOptimizer once per client tick.
 *
 * <p>This is the mod's only required hook, and it is deliberately the smallest possible one: a
 * single {@code HEAD} injection into the game loop that forwards to {@link #pjo$update()}. All
 * actual logic lives in ordinary classes outside Minecraft's bytecode.
 *
 * <p>Keeping the injected code to a forwarding call is the core of the mod's compatibility strategy.
 * When two mods inject at the same point their callbacks simply both run; there is nothing here that
 * another mod's injection could invalidate, no locals captured, no control flow altered. Contrast
 * this with a redirect or an overwrite, which replaces the original instruction outright and so
 * cannot coexist with a second mod doing the same thing.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public Options options;

    @Shadow
    private LevelRenderer levelRenderer;

    /**
     * Runs before the tick body so that a cap computed here applies to the frame about to be
     * rendered rather than to the one after it.
     */
    @Inject(method = "runTick", at = @At("HEAD"))
    private void pjo$onRunTick(boolean tick, CallbackInfo ci) {
        if (!PojavXOptimizer.isReady()) {
            return;
        }
        try {
            pjo$update();
        } catch (RuntimeException updateFailed) {
            // A failure in our own update must never break the game loop. Logged once rather than
            // per frame, since this runs 20-120 times a second.
            if (!pjo$updateErrorLogged) {
                pjo$updateErrorLogged = true;
                PJO.warn("Per-frame update failed; PojavXOptimizer is now idle.", updateFailed);
            }
        }
    }

    @Unique
    private boolean pjo$updateErrorLogged;

    /**
     * Opens the settings screen when the mod's keybind fires.
     *
     * <p>Deliberately ignored while another screen is open, so that pressing the key inside a menu
     * cannot stack a second screen on top of the first and leave the user unable to get back.
     */
    @Unique
    private void pjo$handleSettingsKey(Minecraft self) {
        KeyMapping binding = OptionsMixin.pjo$keyMapping();
        if (binding == null || self.screen != null) {
            return;
        }
        while (binding.consumeClick()) {
            self.setScreen(new PjoOptionsScreen(self.screen));
            return;
        }
    }

    @Unique
    private long pjo$lastFrameNanos;

    @Unique
    private long pjo$lastThermalCheckMillis;

    @Unique
    private void pjo$update() {
        PojavXOptimizer mod = PojavXOptimizer.get();
        Minecraft self = (Minecraft) (Object) this;

        long nowNanos = System.nanoTime();
        double frameMs = 0.0;
        if (pjo$lastFrameNanos != 0L) {
            frameMs = (nowNanos - pjo$lastFrameNanos) / 1_000_000.0;
        }
        pjo$lastFrameNanos = nowNanos;

        boolean guiOpen = self.screen != null;
        double targetFrameMs = 1000.0 / Math.max(20, mod.framePacer().refreshRate());

        mod.thermalGovernor().recordFrame(frameMs);

        long nowMillis = System.currentTimeMillis();
        if (nowMillis - pjo$lastThermalCheckMillis > 1000L) {
            pjo$lastThermalCheckMillis = nowMillis;
            mod.thermalGovernor().update();
            if (mod.heapGuard() != null) {
                mod.heapGuard().tick();
            }
        }

        // Minecraft has no public camera accessor in 1.21.1, so the position comes from the
        // one place it can be read without shadowing internals: setupRender's own parameter.
        double camX = ViewState.cameraX();
        double camY = ViewState.cameraY();
        double camZ = ViewState.cameraZ();

        // A paused single-player world is already idle, so it does not need the idle detector.
        mod.framePacer().update(camX, camY, camZ, 0.0f, 0.0f, guiOpen || self.isPaused(),
                mod.thermalGovernor().pressure());

        mod.dynamicResolution().update(frameMs, targetFrameMs, mod.thermalGovernor().pressure());

        if (levelRenderer != null) {
            int visible = levelRenderer.countRenderedSections();
            mod.visibilityBudget().update(visible, frameMs, targetFrameMs,
                    mod.thermalGovernor().pressure());
        }

        mod.entityCullPolicy().setThermal(mod.thermalGovernor().pressure());
        mod.particleBudget().setThermal(mod.thermalGovernor().pressure());

        pjo$handleSettingsKey(self);
    }
}
