package com.goodboysoul.pojavxoptimizer.mixin.client;

import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.gui.PjoKeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void pjo$registerKeybind(CallbackInfo ci) {
        if (PjoKeyBindings.isRegistered()) {
            return;
        }
        try {
            KeyMapping binding = new KeyMapping(
                    PjoKeyBindings.OPTIONS_KEY_NAME,
                    PjoKeyBindings.OPTIONS_DEFAULT_KEY,
                    PjoKeyBindings.OPTIONS_CATEGORY);
            PjoKeyBindings.setOptionsKey(binding);

            if (this.keyMappings == null) {
                this.keyMappings = new KeyMapping[] { binding };
            } else {
                KeyMapping[] extended = Arrays.copyOf(this.keyMappings,
                        this.keyMappings.length + 1);
                extended[this.keyMappings.length] = binding;
                this.keyMappings = extended;
            }
            PJO.info("Registered keybind 'Open PojavXOptimizer settings' (default: O)");
        } catch (RuntimeException registrationFailed) {
            PJO.warn("Could not register the settings keybind. Edit config/pojavxoptimizer.json "
                    + "directly instead.", registrationFailed);
        }
    }
}
