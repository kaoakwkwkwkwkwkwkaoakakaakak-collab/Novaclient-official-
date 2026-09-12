package dev.novaclient.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

import java.util.HashMap;
import java.util.Map;

public final class OptionAccess {

    private static final Map<String, Object> ORIGINAL_VALUES = new HashMap<>();

    private OptionAccess() {
    }

    public static Options options() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.options;
    }

    public static OptionInstance<Integer> framerateLimit() {
        Options options = options();
        return options == null ? null : options.framerateLimit();
    }

    public static OptionInstance<Integer> renderDistance() {
        Options options = options();
        return options == null ? null : options.renderDistance();
    }

    public static OptionInstance<Integer> simulationDistance() {
        Options options = options();
        return options == null ? null : options.simulationDistance();
    }

    public static OptionInstance<Integer> biomeBlendRadius() {
        Options options = options();
        return options == null ? null : options.biomeBlendRadius();
    }

    public static OptionInstance<Boolean> entityShadows() {
        Options options = options();
        return options == null ? null : options.entityShadows();
    }

    public static OptionInstance<Boolean> bobView() {
        Options options = options();
        return options == null ? null : options.bobView();
    }

    public static OptionInstance<Boolean> hideLightningFlash() {
        Options options = options();
        return options == null ? null : options.hideLightningFlash();
    }

    public static OptionInstance<Boolean> hideSplashTexts() {
        Options options = options();
        return options == null ? null : options.hideSplashTexts();
    }

    public static OptionInstance<Boolean> hideMatchedNames() {
        Options options = options();
        return options == null ? null : options.hideMatchedNames();
    }

    public static OptionInstance<net.minecraft.client.ParticleStatus> particles() {
        Options options = options();
        return options == null ? null : options.particles();
    }

    public static OptionInstance<net.minecraft.client.GraphicsStatus> graphicsMode() {
        Options options = options();
        return options == null ? null : options.graphicsMode();
    }

    public static OptionInstance<Boolean> toggleCrouch() {
        Options options = options();
        return options == null ? null : options.toggleCrouch();
    }

    public static OptionInstance<Boolean> toggleSprint() {
        Options options = options();
        return options == null ? null : options.toggleSprint();
    }

    public static OptionInstance<net.minecraft.client.PrioritizeChunkUpdates> prioritizeChunkUpdates() {
        Options options = options();
        return options == null ? null : options.prioritizeChunkUpdates();
    }

    public static OptionInstance<net.minecraft.client.CloudStatus> cloudStatus() {
        Options options = options();
        return options == null ? null : options.cloudStatus();
    }

    public static OptionInstance<Boolean> showSubtitles() {
        Options options = options();
        return options == null ? null : options.showSubtitles();
    }

    public static OptionInstance<Boolean> darkMojangStudiosBackground() {
        Options options = options();
        return options == null ? null : options.darkMojangStudiosBackground();
    }

    public static OptionInstance<Double> fovEffectScale() {
        Options options = options();
        return options == null ? null : options.fovEffectScale();
    }

    public static OptionInstance<Double> screenEffectScale() {
        Options options = options();
        return options == null ? null : options.screenEffectScale();
    }

    public static OptionInstance<Double> darknessEffectScale() {
        Options options = options();
        return options == null ? null : options.darknessEffectScale();
    }

    public static OptionInstance<Double> notificationDisplayTime() {
        Options options = options();
        return options == null ? null : options.notificationDisplayTime();
    }

    public static <T> void override(OptionInstance<T> option, T value, String key) {
        if (option == null) {
            return;
        }
        if (!ORIGINAL_VALUES.containsKey(key)) {
            T current = option.get();
            if (current != null) {
                ORIGINAL_VALUES.put(key, current);
            }
        }
        option.set(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> void restore(OptionInstance<T> option, String key) {
        if (option == null) {
            return;
        }
        Object original = ORIGINAL_VALUES.remove(key);
        if (original == null) {
            return;
        }
        try {
            option.set((T) original);
        } catch (RuntimeException rejected) {

        }
    }

    public static void restoreAll() {
        ORIGINAL_VALUES.clear();
    }

    public static int overrideCount() {
        return ORIGINAL_VALUES.size();
    }
}
