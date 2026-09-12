package dev.novaclient.effect;

import dev.novaclient.NovaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

public final class OptionEffects {

    public static final int UNLIMITED_FPS = 260;

    private static long lastMovementMillis;
    private static double lastX = Double.NaN;
    private static double lastY;
    private static double lastZ;
    private static int idleCap = -1;

    private OptionEffects() {
    }

    public static void frameRateLimit(int cap) {
        OptionInstance<Integer> option = OptionAccess.framerateLimit();
        if (option == null) {
            return;
        }
        if (cap >= UNLIMITED_FPS) {
            OptionAccess.restore(option, "framerateLimit");
        } else {
            OptionAccess.override(option, Math.max(5, cap), "framerateLimit");
        }
    }

    public static void idleFrameTick(int cap) {
        idleCap = cap;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        double x = minecraft.player.getX();
        double y = minecraft.player.getY();
        double z = minecraft.player.getZ();
        long now = System.currentTimeMillis();
        if (Double.isNaN(lastX)
                || Math.abs(x - lastX) > 0.01
                || Math.abs(y - lastY) > 0.01
                || Math.abs(z - lastZ) > 0.01) {
            lastMovementMillis = now;
        }
        lastX = x;
        lastY = y;
        lastZ = z;

        OptionInstance<Integer> option = OptionAccess.framerateLimit();
        if (option == null) {
            return;
        }
        boolean idle = now - lastMovementMillis > 1500L && minecraft.screen == null;
        if (idle) {
            OptionAccess.override(option, Math.max(5, cap), "framerateLimit");
        } else {
            OptionAccess.restore(option, "framerateLimit");
        }
    }

    public static void clearIdle() {
        idleCap = -1;
        OptionAccess.restore(OptionAccess.framerateLimit(), "framerateLimit");
    }

    public static int idleCap() {
        return idleCap;
    }

    public static void entityCulling(boolean enabled, int distance) {
        dev.novaclient.state.RenderState.entityCulling(enabled, distance);
    }

    public static void particleBudget(int budget) {
        dev.novaclient.state.RenderState.particleBudget(budget);
        RenderEffects.particleLimit(budget);
    }

    public static void chunkCulling(boolean enabled) {
        RenderEffects.chunkCulling(enabled);
    }

    public static void renderDistance(int chunks) {
        OptionInstance<Integer> option = OptionAccess.renderDistance();
        if (option == null) {
            return;
        }
        if (chunks < 0) {
            OptionAccess.restore(option, "renderDistance");
        } else {
            OptionAccess.override(option, Math.max(2, Math.min(chunks, 32)), "renderDistance");
        }
    }

    public static void buildBudget(float millis) {
        dev.novaclient.state.RenderState.buildBudgetMillis(millis);
        RenderEffects.lazyChunkLoading(millis < 0.0f ? 2 : millis < 4.0f ? 1 : 0);
    }

    public static void smoothCamera(boolean enabled) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.options != null) {
            minecraft.options.smoothCamera = enabled;
        }
    }

    public static void weather(boolean enabled) {
        RenderEffects.noWeather(!enabled);
    }

    public static void clouds(String mode) {
        RenderEffects.clouds(mode);
    }

    public static void fog(boolean enabled) {
        dev.novaclient.state.RenderState.fog(enabled);
        RenderEffects.fog(enabled ? "Vanilla" : "Off");
    }

    public static void biomeBlend(int radius) {
        OptionInstance<Integer> option = OptionAccess.biomeBlendRadius();
        if (option == null) {
            return;
        }
        OptionAccess.override(option, Math.max(0, Math.min(radius, 7)), "biomeBlendRadius");
    }

    public static void entityShadows(boolean enabled) {
        OptionInstance<Boolean> option = OptionAccess.entityShadows();
        if (option == null) {
            return;
        }
        if (enabled) {
            OptionAccess.restore(option, "entityShadows");
        } else {
            OptionAccess.override(option, Boolean.FALSE, "entityShadows");
        }
    }

    public static void memoryTick(int thresholdPercent) {
        Runtime runtime = Runtime.getRuntime();
        long usedPercent = 100L * (runtime.totalMemory() - runtime.freeMemory())
                / Math.max(1L, runtime.maxMemory());
        if (usedPercent >= thresholdPercent) {
            dev.novaclient.state.RenderState.requestEviction();
            System.gc();
        }
        long max = runtime.maxMemory();
        if (max <= 0) {
            return;
        }
        long used = max - runtime.freeMemory();
        int percent = (int) (used * 100L / max);
        dev.novaclient.state.RenderState.heapPercent(percent);
        if (percent >= thresholdPercent) {
            dev.novaclient.state.RenderState.requestEviction();
        }
    }

    public static boolean isReady() {
        return NovaClient.isReady();
    }
}
