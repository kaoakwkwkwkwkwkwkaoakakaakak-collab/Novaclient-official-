package dev.novaclient.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.novaclient.NovaClient;
import dev.novaclient.state.MiscState;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.util.function.Consumer;

@Mixin(KeyboardHandler.class)
public abstract class ScreenshotMixin {

    @Redirect(method = "keyPress",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Screenshot;grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V"))
    private void novaSilenceScreenshot(File directory, RenderTarget target, Consumer<Component> feedback) {
        if (NovaClient.isReady() && MiscState.silentScreenshots()) {
            Screenshot.grab(directory, target, message -> MiscState.pushNotification("Screenshot saved"));
            return;
        }
        Screenshot.grab(directory, target, feedback);
    }
}
