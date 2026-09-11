package com.goodboysoul.pojavxoptimizer.gui;

import com.goodboysoul.pojavxoptimizer.PojavXOptimizer;
import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * The on-screen diagnostic overlay.
 *
 * <p>Toggled from the options screen. Off by default: it costs draw calls, and on a phone every
 * draw call goes through the translation layer, so leaving a debug overlay permanently enabled would
 * quietly undo part of what the mod is doing.
 *
 * <p>The reason this exists at all is that "my phone runs it badly" is not actionable. This overlay
 * turns that into a screenshot containing the backend, the thermal state, the cap in force, the
 * resolution scale, and the section count — which is enough to tell apart a thermal problem, a
 * memory problem, and a backend problem without asking the user to run anything.
 */
public final class DebugHud {

    private static final int MARGIN = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int PANEL_PADDING = 3;
    private static final int BACKGROUND = 0xB0101014;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int LABEL = 0xFF9AA0AA;
    private static final int GOOD = 0xFF7CE07C;
    private static final int WARN = 0xFFE0C860;
    private static final int BAD = 0xFFE07060;

    private final List<String> lines = new ArrayList<>(12);
    private long lastRefreshMillis;

    /**
     * Rebuilds the line cache. Called at most four times a second; building the strings every frame
     * would allocate more per second than the overlay is worth.
     */
    public void tick() {
        long now = System.currentTimeMillis();
        if (now - lastRefreshMillis < 250L) {
            return;
        }
        lastRefreshMillis = now;

        lines.clear();
        if (!PojavXOptimizer.isReady()) {
            lines.add("PJO: not initialised");
            return;
        }

        PojavXOptimizer mod = PojavXOptimizer.get();
        PjoConfig config = mod.config();
        ThermalGovernor.Pressure pressure = mod.thermalGovernor().pressure();

        lines.add(PJO.MOD_NAME + " " + PJO.VERSION);
        lines.add("renderer: " + mod.backend().displayName());
        lines.add("profile: " + config.profile().displayName()
                + "  heap: " + (mod.heapGuard() != null ? mod.heapGuard().maxHeapMb() + " MB" : "n/a"));
        lines.add("cap: " + mod.framePacer().effectiveCap() + " fps @"
                + mod.framePacer().refreshRate() + " Hz"
                + (mod.framePacer().isIdle() ? " (idle)" : ""));
        lines.add("frame: " + one(mod.thermalGovernor().smoothedFrameMs()) + " ms (baseline "
                + one(mod.thermalGovernor().baselineFrameMs()) + ")");
        lines.add("thermal: " + pressure.label());
        lines.add("sections: " + mod.visibilityBudget().lastVisibleSections()
                + (mod.visibilityBudget().isCapEngaged()
                        ? " / cap " + mod.visibilityBudget().currentCap() : ""));
        if (config.dynamicResolution()) {
            lines.add("resolution: " + percent(mod.dynamicResolution().scale())
                    + (mod.dynamicResolution().isEngaged() ? " (scaled)" : ""));
        }
        lines.add(mod.entityCullPolicy().describe());
        lines.add(mod.particleBudget().describe());
        if (mod.bufferArena() != null) {
            lines.add(mod.bufferArena().describe());
        }
        lines.add("builds: " + mod.buildBudget().describe());
    }

    /**
     * Draws the overlay in the top-left corner.
     *
     * @param graphics the vanilla GUI graphics context, supplied by the {@code Gui.render} hook
     * @param font     the vanilla font renderer
     */
    public void render(GuiGraphics graphics, Font font) {
        if (lines.isEmpty() || graphics == null || font == null) {
            return;
        }

        int widest = 0;
        for (int i = 0; i < lines.size(); i++) {
            widest = Math.max(widest, font.width(lines.get(i)));
        }

        int panelWidth = widest + PANEL_PADDING * 2;
        int panelHeight = lines.size() * LINE_HEIGHT + PANEL_PADDING * 2;

        graphics.fill(MARGIN, MARGIN, MARGIN + panelWidth, MARGIN + panelHeight, BACKGROUND);

        int y = MARGIN + PANEL_PADDING;
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), MARGIN + PANEL_PADDING, y, colourFor(i));
            y += LINE_HEIGHT;
        }
    }

    /**
     * Colours the thermal line by state so a throttling device is obvious at a glance rather than
     * requiring the user to read the word.
     */
    private int colourFor(int index) {
        if (index != 5 || !PojavXOptimizer.isReady()) {
            return index == 0 ? TEXT : LABEL;
        }
        return switch (PojavXOptimizer.get().thermalGovernor().pressure()) {
            case NONE -> GOOD;
            case MILD -> GOOD;
            case MODERATE -> WARN;
            case SEVERE -> BAD;
        };
    }

    private static String one(double value) {
        return value < 0.0 ? "-" : String.format("%.1f", value);
    }

    private static String percent(double fraction) {
        return Math.round(fraction * 100.0) + "%";
    }

    public void reset() {
        lines.clear();
        lastRefreshMillis = 0L;
    }
}
