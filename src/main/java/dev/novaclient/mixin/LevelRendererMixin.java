package dev.novaclient.mixin;

import dev.novaclient.NovaClient;
import dev.novaclient.state.RenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void novaHideSky(Matrix4f projection, Matrix4f view, float partialTicks, Camera camera,
                             boolean isFoggy, Runnable fogCallback, CallbackInfo info) {
        if (NovaClient.isReady() && RenderState.hideSky()) {
            if (fogCallback != null) {
                fogCallback.run();
            }
            info.cancel();
        }
    }
}
