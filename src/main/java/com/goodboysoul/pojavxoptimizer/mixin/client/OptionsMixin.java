package com.goodboysoul.pojavxoptimizer.mixin.client;

import com.goodboysoul.pojavxoptimizer.core.PJO;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Registers the mod's keybind by appending it to vanilla's own key map array.
 *
 * <p>This is the one place where the mod writes to a Minecraft field, so it is worth being explicit
 * about why it is safe. The array is copied, one element is added, and the new array is assigned
 * back. Nothing is removed and no existing element is modified, so every other mod that has already
 * appended its own keybind keeps it, and every mod that appends after us still sees a valid array.
 * The alternative — injecting into the key-press handler and polling GLFW directly — would need the
 * window handle and a hardcoded key code, and would silently break for anyone who remapped keys.
 *
 * <p>A side benefit of going through the vanilla array is that the binding appears in the game's own
 * Controls screen and can be rebound there, with no extra UI from this mod.
 *
 * <p>The injection targets the {@code RETURN} of the constructor rather than {@code HEAD} because
 * {@code keyMappings} is populated during construction; at HEAD it would still be empty or null.
 */
@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow
    public KeyMapping[] keyMappings;

    /** The mod's own binding, published so the frame hook can poll it. */
    @Unique
    private static KeyMapping pjo$optionsKey;

    /**
     * The binding that opens the settings screen, or {@code null} if registration did not happen.
     * Callers must treat {@code null} as "no keybind", never as an error.
     */
    public static KeyMapping pjo$keyMapping() {
        return pjo$optionsKey;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void pjo$registerKeybind(CallbackInfo ci) {
        if (pjo$optionsKey != null) {
            // Options can be constructed more than once in a session; the binding only needs to
            // exist once, and adding it twice would show a duplicate row in the Controls screen.
            return;
        }
        try {
            KeyMapping binding = new KeyMapping(
                    "key.pojavxoptimizer.options",
                    org.lwjgl.glfw.GLFW.GLFW_KEY_O,
                    "key.categories.pojavxoptimizer");
            pjo$optionsKey = binding;

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
            // Without the binding the mod still works; only the settings screen becomes
            // unreachable from a key, and it remains reachable from the config file.
            PJO.warn("Could not register the settings keybind. Edit config/pojavxoptimizer.json "
                    + "directly instead.", registrationFailed);
        }
    }
}
