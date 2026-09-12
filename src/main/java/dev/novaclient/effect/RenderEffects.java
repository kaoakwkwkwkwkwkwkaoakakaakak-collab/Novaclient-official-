package dev.novaclient.effect;

import dev.novaclient.state.RenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

public final class RenderEffects {

    private RenderEffects() {
    }

    public static void fog(String mode) {
        RenderState.fogMode(mode);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void clouds(String mode) {
        OptionInstance option = OptionAccess.cloudStatus();
        if (option == null) {
            return;
        }
        try {
            Class<?> type = net.minecraft.client.CloudStatus.class;
            Object value = switch (mode == null ? "" : mode) {
                case "Off" -> Enum.valueOf((Class) type, "OFF");
                case "Fancy" -> Enum.valueOf((Class) type, "FANCY");
                default -> Enum.valueOf((Class) type, "FAST");
            };
            OptionAccess.override(option, value, "clouds");
        } catch (IllegalArgumentException unknownConstant) {
            OptionAccess.restore(option, "clouds");
        }
    }

    public static void noClouds(boolean enabled) {
        if (!enabled) {
            OptionInstance option = OptionAccess.cloudStatus();
            if (option != null) {
                OptionAccess.restore(option, "clouds");
            }
            return;
        }
        clouds("Off");
    }

    public static void sky(boolean enabled) {
        RenderState.customSky(enabled);
    }

    public static void weather(String mode) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) {
            return;
        }
        float rain = switch (mode == null ? "" : mode) {
            case "Clear" -> 0.0f;
            case "Reduced" -> 0.35f;
            default -> 1.0f;
        };
        minecraft.level.setRainLevel(rain);
        minecraft.level.setThunderLevel("Clear".equals(mode) ? 0.0f : rain);
        RenderState.weather(!"Clear".equalsIgnoreCase(mode));
    }

    public static void noWeather(boolean enabled) {
        weather(enabled ? "Clear" : "Vanilla");
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

    public static void particles(String mode) {
        OptionInstance<?> option = OptionAccess.options() == null
                ? null
                : OptionAccess.options().particles();
        if (option == null) {
            return;
        }
        applyParticleMode(option, mode);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applyParticleMode(OptionInstance option, String mode) {
        try {
            Class<?> type = net.minecraft.client.ParticleStatus.class;
            Object value = switch (mode) {
                case "Decreased" -> Enum.valueOf((Class) type, "DECREASED");
                case "Minimal" -> Enum.valueOf((Class) type, "MINIMAL");
                default -> Enum.valueOf((Class) type, "ALL");
            };
            OptionAccess.override(option, value, "particles");
        } catch (IllegalArgumentException unknownConstant) {
            OptionAccess.restore(option, "particles");
        }
    }

    public static void graphicsMode(String mode) {
        OptionInstance<?> option = OptionAccess.options() == null
                ? null
                : OptionAccess.options().graphicsMode();
        if (option == null) {
            return;
        }
        applyGraphicsMode(option, mode);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applyGraphicsMode(OptionInstance option, String mode) {
        try {
            Class<?> type = net.minecraft.client.GraphicsStatus.class;
            Object value = switch (mode) {
                case "Fancy" -> Enum.valueOf((Class) type, "FANCY");
                case "Fabulous" -> Enum.valueOf((Class) type, "FABULOUS");
                default -> Enum.valueOf((Class) type, "FAST");
            };
            OptionAccess.override(option, value, "graphicsMode");
        } catch (IllegalArgumentException unknownConstant) {
            OptionAccess.restore(option, "graphicsMode");
        }
    }

    public static void smoothLighting(String mode) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return;
        }
        boolean smooth = !"Off".equalsIgnoreCase(mode);
        OptionAccess.override(OptionAccess.options() == null
                ? null : OptionAccess.options().mipmapLevels(), 4, "mipmapLevels");
        minecraft.levelRenderer.allChanged();
    }

    public static void renderDistance(int chunks) {
        OptionEffects.renderDistance(chunks);
    }

    public static void simulationDistance(int chunks) {
        OptionInstance<Integer> option = OptionAccess.simulationDistance();
        if (option == null) {
            return;
        }
        if (chunks < 0) {
            OptionAccess.restore(option, "simulationDistance");
        } else {
            OptionAccess.override(option, Math.max(2, Math.min(chunks, 32)), "simulationDistance");
        }
    }

    public static void viewBobbing(boolean enabled) {
        OptionInstance<Boolean> option = OptionAccess.bobView();
        if (option == null) {
            return;
        }
        if (enabled) {
            OptionAccess.restore(option, "bobView");
        } else {
            OptionAccess.override(option, Boolean.FALSE, "bobView");
        }
    }

    public static void vignette(boolean enabled) {
        OptionInstance<Boolean> option = OptionAccess.darkMojangStudiosBackground();
        if (option == null) {
            return;
        }
        if (enabled) {
            OptionAccess.restore(option, "vignette");
        } else {
            OptionAccess.override(option, Boolean.FALSE, "vignette");
        }
    }

    public static void distortion(int percent) {
        OptionInstance<Double> option = OptionAccess.screenEffectScale();
        if (option == null) {
            return;
        }
        OptionAccess.override(option, Math.max(0.0, percent) / 100.0, "screenEffectScale");
    }

    public static void damageTilt(boolean enabled) {
        OptionInstance<Double> option = OptionAccess.fovEffectScale();
        if (option == null) {
            return;
        }
        OptionAccess.override(option, enabled ? 1.0 : 0.0, "fovEffectScale");
    }

    public static void darkness(boolean enabled) {
        OptionInstance<Double> option = OptionAccess.darknessEffectScale();
        if (option == null) {
            return;
        }
        OptionAccess.override(option, enabled ? 1.0 : 0.0, "darknessEffectScale");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void lazyChunkLoading(int level) {
        OptionInstance option = OptionAccess.prioritizeChunkUpdates();
        if (option == null) {
            return;
        }
        try {
            Class<?> type = net.minecraft.client.PrioritizeChunkUpdates.class;
            Object value = switch (level) {
                case 0 -> Enum.valueOf((Class) type, "NONE");
                case 1 -> Enum.valueOf((Class) type, "PLAYER_AFFECTED");
                default -> Enum.valueOf((Class) type, "NEARBY");
            };
            OptionAccess.override(option, value, "lazyChunkLoading");
        } catch (IllegalArgumentException unknownConstant) {
            OptionAccess.restore(option, "lazyChunkLoading");
        }
    }

    public static void chunkCulling(boolean enabled) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        minecraft.smartCull = enabled;
        RenderState.chunkCulling(enabled);
    }

    public static void particleLimit(int budget) {
        particles(budget <= 0 ? "Minimal" : budget < 500 ? "Decreased" : "All");
    }
}
