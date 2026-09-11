package com.goodboysoul.pojavxoptimizer.gui;

import net.minecraft.client.KeyMapping;

public final class PjoKeyBindings {

    public static final String OPTIONS_KEY_NAME = "key.pojavxoptimizer.options";
    public static final String OPTIONS_CATEGORY = "key.categories.pojavxoptimizer";
    public static final int OPTIONS_DEFAULT_KEY = 79;

    private static KeyMapping optionsKey;

    private PjoKeyBindings() {
    }

    public static void setOptionsKey(KeyMapping binding) {
        optionsKey = binding;
    }

    public static KeyMapping optionsKey() {
        return optionsKey;
    }

    public static boolean isRegistered() {
        return optionsKey != null;
    }
}
