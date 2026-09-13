package dev.novaclient.mixin;

import dev.novaclient.NovaClient;
import dev.novaclient.state.RenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void novaSuppressFog(Camera camera, FogRenderer.FogMode mode, float partialTicks,
                                 boolean thickFog, float fov, CallbackInfo info) {
        if (!NovaClient.isReady()) {
            return;
        }
        String requested = RenderState.fogMode();
        if (requested == null || "Vanilla".equalsIgnoreCase(requested)) {
            return;
        }
        FogRenderer.setupNoFog();
        info.cancel();
    }
}
