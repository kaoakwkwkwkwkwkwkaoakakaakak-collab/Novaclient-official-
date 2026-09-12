package dev.novaclient.hud;

import dev.novaclient.NovaClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HudRenderer {

    private static final int TEXT_COLOR = 0xFFE8E8E8;
    private static final int BACKGROUND = 0x66000000;
    private static final int PADDING = 3;
    private static final int MARGIN = 4;
    private static final int LINE_HEIGHT = 11;

    private HudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        if (!NovaClient.isReady()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return;
        }
        if (graphics == null) {
            return;
        }
        if (minecraft.screen != null) {
            return;
        }

        List<String> topLeft = new ArrayList<>();
        List<String> bottomLeft = new ArrayList<>();
        List<String> topRight = new ArrayList<>();
        List<String> bottomRight = new ArrayList<>();

        collect(minecraft, HudElement.COORDS, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.FPS, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.CPS, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.PING, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.DIRECTION, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.BIOME, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.CLOCK, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.MEMORY, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.SERVER_IP, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.REACH, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.WATERMARK, topLeft, bottomLeft, topRight, bottomRight);
        collect(minecraft, HudElement.NOTIFICATIONS, topLeft, bottomLeft, topRight, bottomRight);

        drawStack(graphics, minecraft, Anchor.TOP_LEFT, topLeft);
        drawStack(graphics, minecraft, Anchor.BOTTOM_LEFT, bottomLeft);
        drawStack(graphics, minecraft, Anchor.TOP_RIGHT, topRight);
        drawStack(graphics, minecraft, Anchor.BOTTOM_RIGHT, bottomRight);

        if (HudState.isVisible(HudElement.ARRAYLIST)) {
            drawArrayList(graphics, minecraft);
        }
    }

    private static void collect(Minecraft minecraft, HudElement element,
                                List<String> topLeft, List<String> bottomLeft,
                                List<String> topRight, List<String> bottomRight) {
        if (!HudState.isVisible(element)) {
            return;
        }
        String text = textFor(minecraft, element);
        if (text == null || text.isEmpty()) {
            return;
        }
        switch (HudState.anchor(element)) {
            case TOP_LEFT -> topLeft.add(text);
            case BOTTOM_LEFT -> bottomLeft.add(text);
            case TOP_RIGHT -> topRight.add(text);
            case BOTTOM_RIGHT -> bottomRight.add(text);
        }
    }

    private static String textFor(Minecraft minecraft, HudElement element) {
        LocalPlayer player = minecraft.player;
        return switch (element) {
            case FPS -> minecraft.getFps() + " fps";
            case CPS -> HudState.leftCps() + " | " + HudState.rightCps() + " cps";
            case PING -> pingText(minecraft);
            case REACH -> reachText(minecraft);
            case WATERMARK -> NovaClient.NAME + " " + NovaClient.VERSION;
            case NOTIFICATIONS -> dev.novaclient.state.MiscState.latestNotification();
            case COORDS -> coordsText(player);
            case DIRECTION -> directionText(player);
            case BIOME -> biomeText(player);
            case CLOCK -> clockText(minecraft);
            case MEMORY -> memoryText();
            case SERVER_IP -> serverText(minecraft);
            default -> null;
        };
    }

    private static String coordsText(LocalPlayer player) {
        if (player == null) {
            return null;
        }
        return String.format("XYZ %.1f / %.1f / %.1f",
                player.getX(), player.getY(), player.getZ());
    }

    private static String directionText(LocalPlayer player) {
        if (player == null) {
            return null;
        }
        float yaw = player.getYRot();
        String[] names = {"South", "South West", "West", "North West",
                "North", "North East", "East", "South East"};
        int index = (int) Math.floor((yaw - 22.5) / 45.0 + 3.0) & 7;
        String facing = names[Math.floorMod(index, names.length)];
        BlockPos pos = player.blockPosition();
        return facing + " (" + pos.getX() + ", " + pos.getZ() + ")";
    }

    private static String biomeText(LocalPlayer player) {
        if (player == null || player.level() == null) {
            return null;
        }
        try {
            var holder = player.level().getBiome(player.blockPosition());
            if (holder == null) {
                return null;
            }
            var key = holder.unwrapKey();
            if (key.isEmpty()) {
                return null;
            }
            String path = key.get().location().getPath();
            return "Biome " + path.replace('_', ' ');
        } catch (RuntimeException unavailable) {
            return null;
        }
    }

    private static String reachText(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.hitResult == null) {
            return null;
        }
        net.minecraft.world.phys.Vec3 eyes = player.getEyePosition();
        net.minecraft.world.phys.Vec3 hit = minecraft.hitResult.getLocation();
        double distance = eyes.distanceTo(hit);
        if (distance <= 0.0 || distance > 12.0) {
            return null;
        }
        return String.format(java.util.Locale.ROOT, "%.2f m", distance);
    }

    private static String pingText(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return null;
        }
        try {
            var info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
            if (info == null) {
                return null;
            }
            return info.getLatency() + " ms";
        } catch (RuntimeException unavailable) {
            return null;
        }
    }

    private static String clockText(Minecraft minecraft) {
        if (minecraft.level == null) {
            return null;
        }
        long dayTime = minecraft.level.getDayTime();
        long minutes = (dayTime / 1000L + 6L) % 24L;
        long seconds = (dayTime % 1000L) * 60L / 1000L;
        return String.format("Time %02d:%02d", minutes, seconds);
    }

    private static String memoryText() {
        Runtime runtime = Runtime.getRuntime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
        long maxMb = runtime.maxMemory() / (1024L * 1024L);
        return "Mem " + usedMb + "/" + maxMb + " MB";
    }

    private static String serverText(Minecraft minecraft) {
        if (minecraft.getCurrentServer() == null) {
            return minecraft.isLocalServer() ? "Singleplayer" : null;
        }
        return minecraft.getCurrentServer().ip;
    }

    private static void drawStack(GuiGraphics graphics, Minecraft minecraft, Anchor anchor,
                                  List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int width = 0;
        for (String line : lines) {
            width = Math.max(width, font.width(line));
        }
        int boxWidth = width + PADDING * 2;
        int boxHeight = lines.size() * LINE_HEIGHT + PADDING;

        int x = anchor.isRight() ? screenWidth - MARGIN - boxWidth : MARGIN;
        int y;
        if (anchor.isBottom()) {
            y = screenHeight - MARGIN - boxHeight;
        } else {
            y = MARGIN;
        }

        graphics.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND);

        int textY = y + PADDING;
        for (String line : lines) {
            int textX = anchor.isRight() ? x + boxWidth - PADDING - font.width(line) : x + PADDING;
            graphics.drawString(font, line, textX, textY, TEXT_COLOR, true);
            textY += LINE_HEIGHT;
        }
    }

    private static void drawArrayList(GuiGraphics graphics, Minecraft minecraft) {
        if (!NovaClient.isReady()) {
            return;
        }
        Font font = minecraft.font;
        List<String> names = new ArrayList<>();
        for (var module : NovaClient.get().modules().enabled()) {
            if (module.category() != dev.novaclient.module.Category.HUD) {
                names.add(module.name());
            }
        }
        if (names.isEmpty()) {
            return;
        }
        names.sort((a, b) -> Integer.compare(font.width(b), font.width(a)));

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int widest = font.width(names.get(0));
        int boxWidth = widest + PADDING * 2 + 2;
        int boxHeight = names.size() * LINE_HEIGHT + PADDING;
        int x = screenWidth - MARGIN - boxWidth;
        int y = MARGIN;

        graphics.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND);

        int textY = y + PADDING;
        int index = 0;
        for (String name : names) {
            int shade = blend(dev.novaclient.gui.Theme.accent(), TEXT_COLOR, names.size() == 1
                    ? 0.0
                    : (double) index / (names.size() - 1));
            graphics.drawString(font, name, x + PADDING, textY, shade, true);
            graphics.fill(x + boxWidth - 2, textY - 1, x + boxWidth, textY + 9, shade);
            textY += LINE_HEIGHT;
            index++;
        }
    }

    private static int blend(int from, int to, double t) {
        double clamped = t < 0.0 ? 0.0 : Math.min(t, 1.0);
        int a = (int) (((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * clamped);
        int r = (int) (((from >>> 16) & 0xFF) + (((to >>> 16) & 0xFF) - ((from >>> 16) & 0xFF)) * clamped);
        int g = (int) (((from >>> 8) & 0xFF) + (((to >>> 8) & 0xFF) - ((from >>> 8) & 0xFF)) * clamped);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * clamped);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
