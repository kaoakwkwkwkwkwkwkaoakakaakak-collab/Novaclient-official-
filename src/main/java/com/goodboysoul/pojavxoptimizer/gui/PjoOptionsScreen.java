package com.goodboysoul.pojavxoptimizer.gui;

import com.goodboysoul.pojavxoptimizer.PojavXOptimizer;
import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.core.PJO;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class PjoOptionsScreen extends Screen {

    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ROW_GAP = 6;

    private enum Page {
        MAIN("Performance"),
        VISUAL("Visual"),
        ADVANCED("Advanced");

        private final String title;

        Page(String title) {
            this.title = title;
        }
    }

    private final Screen parent;
    private final PjoConfig config;
    private final List<Button> pageButtons = new ArrayList<>();
    private Page page = Page.MAIN;
    private Component statusLine = Component.empty();

    public PjoOptionsScreen(Screen parent) {
        super(Component.literal(PJO.MOD_NAME));
        this.parent = parent;
        this.config = PojavXOptimizer.isReady()
                ? PojavXOptimizer.get().config()
                : null;
    }

    @Override
    protected void init() {
        super.init();
        pageButtons.clear();

        if (config == null) {
            addRenderableWidget(Button.builder(Component.literal("Mod not loaded"), button -> { })
                    .bounds(centreX() - BUTTON_WIDTH / 2, 60, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
            return;
        }

        int y = 40;
        for (Page target : Page.values()) {
            Button tab = Button.builder(Component.literal(target.title), button -> {
                page = target;
                rebuild();
            }).bounds(centreX() - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();
            pageButtons.add(tab);
            addRenderableWidget(tab);
            y += BUTTON_HEIGHT + ROW_GAP;
        }

        y += 8;
        buildPage(y);

        int bottomY = this.height - 28;
        addRenderableWidget(Button.builder(Component.literal("Reset to profile defaults"),
                button -> {
                    config.applyProfile(config.profile());
                    status("Reset to " + config.profile().displayName() + " defaults");
                    rebuild();
                }).bounds(centreX() - BUTTON_WIDTH / 2, bottomY - BUTTON_HEIGHT - ROW_GAP,
                BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.literal("Save & close"), button -> pjo$dismiss())
                .bounds(centreX() - BUTTON_WIDTH / 2, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private void buildPage(int startY) {
        int y = startY;
        switch (page) {
            case MAIN -> {
                y = addToggle(y, "Battery saver (idle fps cap)",
                        () -> config.batterySaver(), value -> config.batterySaver(value));
                y = addToggle(y, "Thermal governor",
                        () -> config.thermalGovernor(), value -> config.thermalGovernor(value));
                y = addToggle(y, "Heap guard (prevents crashes)",
                        () -> config.heapGuard(), value -> config.heapGuard(value));
                y = addToggle(y, "Entity culling",
                        () -> config.entityCulling(), value -> config.entityCulling(value));
                y = addToggle(y, "Particle budget",
                        () -> config.particleBudgeting(), value -> config.particleBudgeting(value));
                y = addCycle(y, "Particle limit", config.particleBudget(),
                        new int[] {100, 200, 400, 800, 1600}, value -> config.particleBudget(value));
            }
            case VISUAL -> {
                y = addToggle(y, "Dynamic resolution",
                        () -> config.dynamicResolution(), value -> config.dynamicResolution(value));
                y = addCycle(y, "Minimum resolution scale", Math.round(config.resolutionScaleMin() * 100),
                        new int[] {50, 60, 70, 80, 90, 100},
                        value -> config.resolutionScaleMin(value / 100.0f));
                y = addCycle(y, "Visibility section cap", config.visibilitySectionCap(),
                        new int[] {1024, 2048, 4096, 8192, 16384}, value -> config.visibilitySectionCap(value));
                y = addToggle(y, "Build what I look at first",
                        () -> config.viewWeightedChunkPriority(), value -> config.viewWeightedChunkPriority(value));
            }
            case ADVANCED -> {
                y = addToggle(y, "Instanced section drawing",
                        () -> config.instancedSectionDrawing(), value -> config.instancedSectionDrawing(value));
                y = addToggle(y, "Packed vertex format",
                        () -> config.packedVertexFormat(), value -> config.packedVertexFormat(value));
                y = addToggle(y, "Off-heap mesh buffers",
                        () -> config.offHeapMeshes(), value -> config.offHeapMeshes(value));
                y = addCycle(y, "Build threads (0 = auto)", config.buildThreads(),
                        new int[] {0, 1, 2, 3, 4, 6, 8}, value -> config.buildThreads(value));
                y = addToggle(y, "Look input smoothing (adds latency)",
                        () -> config.inputSmoothing(), value -> config.inputSmoothing(value));
                y = addToggle(y, "Debug overlay",
                        () -> config.debugOverlay(), value -> config.debugOverlay(value));
                y = addCycle(y, "Device profile", config.profile().ordinal(),
                        new int[] {0, 1, 2, 3, 4}, value ->
                                config.applyProfile(
                                        com.goodboysoul.pojavxoptimizer.config.DeviceProfile
                                                .values()[value]));
            }
            default -> {
            }
        }
    }

    private int addToggle(int y, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
        Component text = Component.literal(label + ": " + (getter.getAsBoolean() ? "ON" : "OFF"));
        addRenderableWidget(Button.builder(text, button -> {
            setter.accept(!getter.getAsBoolean());
            status(label + " -> " + (getter.getAsBoolean() ? "ON" : "OFF"));
            rebuild();
        }).bounds(centreX() - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        return y + BUTTON_HEIGHT + ROW_GAP;
    }

    private int addCycle(int y, String label, int current, int[] options,
                         java.util.function.IntConsumer setter) {
        Component text = Component.literal(label + ": " + current);
        addRenderableWidget(Button.builder(text, button -> {
            int next = options[0];
            for (int i = 0; i < options.length; i++) {
                if (options[i] == current) {
                    next = options[(i + 1) % options.length];
                    break;
                }
            }
            setter.accept(next);
            status(label + " -> " + next);
            rebuild();
        }).bounds(centreX() - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        return y + BUTTON_HEIGHT + ROW_GAP;
    }

    private void rebuild() {
        clearWidgets();
        init(this.minecraft, this.width, this.height);
    }

    private void status(String message) {
        this.statusLine = Component.literal(message);
    }

    private int centreX() {
        return this.width / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.font != null) {
            graphics.drawCenteredString(this.font, PJO.MOD_NAME, centreX(), 16, 0xFFFFFFFF);
            if (config != null) {
                graphics.drawCenteredString(this.font,
                        "Profile: " + config.profile().displayName()
                                + "  |  " + page.title,
                        centreX(), 26, 0xFF9AA0AA);
            }
            if (statusLine != null && !statusLine.getString().isEmpty()) {
                graphics.drawCenteredString(this.font, statusLine, centreX(),
                        this.height - 44, 0xFF7CE07C);
            }
        }
    }

    private void pjo$save() {
        if (PojavXOptimizer.isReady()) {
            PojavXOptimizer.get().saveConfig();
        }
    }

    private void pjo$dismiss() {
        pjo$save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {

        return false;
    }
}
