package com.goodboysoul.pojavxoptimizer.core;

public final class Debug {

    private static final boolean VERBOSE =
            Boolean.parseBoolean(System.getProperty("pojavxoptimizer.debug", "false"));

    private Debug() {
    }

    public static boolean isVerbose() {
        return VERBOSE;
    }
}
