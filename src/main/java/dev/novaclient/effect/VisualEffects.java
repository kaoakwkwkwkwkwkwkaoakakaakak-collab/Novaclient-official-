package dev.novaclient.effect;

import dev.novaclient.state.RenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.OptionInstance;

public final class VisualEffects {

    private static int lastRetentionAmount = -1;
    private static boolean blurActive;
    private static PostChain activeChain;

    private static final ResourceLocation BLUR_EFFECT =
            ResourceLocation.fromNamespaceAndPath("novaclient", "shaders/post/nova_blur.json");

    private VisualEffects() {
    }

    public static void motionBlur(boolean enabled, int amount) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gameRenderer == null) {
            return;
        }
        GameRenderer gameRenderer = minecraft.gameRenderer;
        if (!enabled) {
            gameRenderer.shutdownShaders();
            blurActive = false;
            activeChain = null;
            RenderState.motionBlur(false, amount);
            return;
        }
        try {
            gameRenderer.loadEffect(BLUR_EFFECT);
        } catch (RuntimeException failed) {
            RenderState.motionBlurFault("shader failed to load: " + failed.getMessage());
            return;
        }
        PostChain chain = gameRenderer.currentEffect();
        if (chain != null) {
            chain.setUniform("Retention", retentionFor(amount));
        }
        activeChain = chain;
        blurActive = true;
        RenderState.motionBlur(true, amount);
    }

    public static void motionBlurTick(int amount, boolean menusOnly) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        boolean screenOpen = minecraft.screen != null;
        if (menusOnly && !screenOpen) {
            if (blurActive) {
                motionBlur(false, amount);
            }
            return;
        }
        if (menusOnly && screenOpen && !blurActive) {
            motionBlur(true, amount);
            return;
        }
        if (blurActive && chainWasReplaced(minecraft)) {
            motionBlur(true, amount);
            return;
        }
        motionBlurRetention(amount);
    }

    private static boolean chainWasReplaced(Minecraft minecraft) {
        if (minecraft.gameRenderer == null) {
            return false;
        }
        PostChain current = minecraft.gameRenderer.currentEffect();
        return current == null || current != activeChain;
    }

    public static void motionBlurRetention(int amount) {
        if (amount == lastRetentionAmount) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gameRenderer == null) {
            return;
        }
        PostChain chain = minecraft.gameRenderer.currentEffect();
        if (chain == null) {
            return;
        }
        chain.setUniform("Retention", retentionFor(amount));
        lastRetentionAmount = amount;
    }

    private static float retentionFor(int amount) {
        int clamped = Math.max(0, Math.min(amount, 95));
        return clamped / 100.0f;
    }

    public static void fullBright(boolean enabled) {
        RenderState.fullBright(enabled);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.levelRenderer != null) {
            minecraft.levelRenderer.allChanged();
        }
    }

    public static void nameTags(boolean enabled, int range) {
        OptionInstance<Boolean> option = OptionAccess.hideMatchedNames();
        if (option == null) {
            return;
        }
        if (!enabled) {
            OptionAccess.restore(option, "nameTags");
        } else {
            OptionAccess.override(option, Boolean.FALSE, "nameTags");
        }
        RenderState.nameTagRange(range);
    }

    public static void itemPhysics(boolean enabled) {
        RenderState.itemPhysics(enabled);
    }

    public static void hitBoxes(boolean enabled) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        if (dispatcher == null) {
            return;
        }
        dispatcher.setRenderHitBoxes(enabled);
        RenderState.hitBoxes(enabled);
    }

    public static void chunkBorders(boolean enabled) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.debugRenderer == null) {
            return;
        }
        DebugRenderer debugRenderer = minecraft.debugRenderer;
        if (debugRenderer.switchRenderChunkborder() != enabled) {
            debugRenderer.switchRenderChunkborder();
        }
        RenderState.chunkBorders(enabled);
    }

    public static void lowFire(boolean enabled, int height) {
        RenderState.lowFire(enabled, height);
    }

    public static void handScale(boolean enabled, int percent) {
        RenderState.handScaled(enabled);
        RenderState.handScale(percent);
    }

    public static void hurtCam(boolean enabled) {
        OptionInstance<Double> option = OptionAccess.screenEffectScale();
        if (option == null) {
            return;
        }
        if (!enabled) {
            OptionAccess.restore(option, "hurtCam");
        } else {
            OptionAccess.override(option, Double.valueOf(0.0D), "hurtCam");
        }
    }

    public static void pumpkinOverlay(boolean enabled) {
        RenderState.resolutionScale(enabled ? 100 : 100);
    }

    public static void waterOverlay(boolean enabled) {
        RenderState.resolutionScale(100);
    }

    public static void portalOverlay(boolean enabled) {
        RenderState.resolutionScale(100);
    }

    public static void timeOfDay(int hour) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        if (hour < 0) {
            RenderState.timeOfDay(-1);
            return;
        }
        int clamped = Math.max(0, Math.min(hour, 23));
        minecraft.level.setDayTime((long) clamped * 1000L);
        RenderState.timeOfDay(clamped);
    }

    public static void handScale(int percent) {
        RenderState.handScale(percent);
    }

    public static void resolutionScale(int percent) {
        RenderState.resolutionScale(percent);
    }

    public static void chroma(boolean enabled, int speed, int saturation) {
        if (!enabled) {
            RenderState.accentColor(0xFF7C9CFF);
            return;
        }
        float hue = (System.currentTimeMillis() % (long) (10000L / Math.max(1, speed)))
                / (float) (10000L / Math.max(1, speed));
        RenderState.accentColor(java.awt.Color.HSBtoRGB(hue, saturation / 100.0f, 1.0f) | 0xFF000000);
    }
}
