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

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public Options options;

    @Shadow
    private LevelRenderer levelRenderer;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void pjo$onRunTick(boolean tick, CallbackInfo ci) {
        if (!PojavXOptimizer.isReady()) {
            return;
        }
        try {
            pjo$update();
        } catch (RuntimeException updateFailed) {

            if (!pjo$updateErrorLogged) {
                pjo$updateErrorLogged = true;
                PJO.warn("Per-frame update failed; PojavXOptimizer is now idle.", updateFailed);
            }
        }
    }

    @Unique
    private boolean pjo$updateErrorLogged;

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

        double camX = ViewState.cameraX();
        double camY = ViewState.cameraY();
        double camZ = ViewState.cameraZ();

        int cap = mod.framePacer().update(camX, camY, camZ, 0.0f, 0.0f, guiOpen || self.isPaused(),
                mod.thermalGovernor().pressure());

        mod.frameRateLimiter().apply(self.options, cap);

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
