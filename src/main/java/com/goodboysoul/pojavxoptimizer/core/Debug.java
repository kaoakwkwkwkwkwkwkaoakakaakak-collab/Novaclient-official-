package com.goodboysoul.pojavxoptimizer.core;

/**
 * Verbose diagnostics gate.
 *
 * <p>Enabled with {@code -Dpojavxoptimizer.debug=true} in the launcher's JVM arguments. Kept as a
 * system property rather than a config value on purpose: it must be readable before the config file
 * has been located, and it must survive a config file that fails to parse.
 */
public final class Debug {

    private static final boolean VERBOSE =
            Boolean.parseBoolean(System.getProperty("pojavxoptimizer.debug", "false"));

    private Debug() {
    }

    public static boolean isVerbose() {
        return VERBOSE;
    }
}
