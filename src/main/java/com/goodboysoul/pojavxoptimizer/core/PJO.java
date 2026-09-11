package com.goodboysoul.pojavxoptimizer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PJO {

    public static final String MOD_ID = "pojavxoptimizer";
    public static final String MOD_NAME = "PojavXOptimizer";
    public static final String VERSION = "1.0.2";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private PJO() {
    }

    public static Logger logger() {
        return LOGGER;
    }

    public static void info(String message) {
        LOGGER.info("[{}] {}", MOD_NAME, message);
    }

    public static void info(String format, Object... args) {
        LOGGER.info("[{}] " + format, prepend(args));
    }

    public static void warn(String message) {
        LOGGER.warn("[{}] {}", MOD_NAME, message);
    }

    public static void warn(String format, Object... args) {
        LOGGER.warn("[{}] " + format, prepend(args));
    }

    public static void warn(String message, Throwable cause) {
        LOGGER.warn("[{}] {}", MOD_NAME, message, cause);
    }

    public static void error(String message, Throwable cause) {
        LOGGER.error("[{}] {}", MOD_NAME, message, cause);
    }

    public static void debug(String format, Object... args) {
        if (Debug.isVerbose()) {
            LOGGER.info("[{}][debug] " + format, prepend(args));
        }
    }

    private static Object[] prepend(Object[] args) {
        Object[] out = new Object[args.length + 1];
        out[0] = MOD_NAME;
        System.arraycopy(args, 0, out, 1, args.length);
        return out;
    }
}
