package dev.novaclient.gui;

import dev.novaclient.NovaClient;
import dev.novaclient.module.Category;
import dev.novaclient.module.Module;
import dev.novaclient.setting.ModeSetting;
import dev.novaclient.setting.Setting;
import dev.novaclient.setting.SliderSetting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ClickGuiScreen extends Screen {

    private final Screen parent;

    private Category selected = Category.PERFORMANCE;
    private double scroll;
    private double maxScroll;
    private int panelX;
    private int panelY;
    private int hoveredRow = -1;
    private String search = "";

    private final List<Row> rows = new ArrayList<>();

    public ClickGuiScreen(Screen parent) {
        super(Component.literal(NovaClient.NAME));
        this.parent = parent;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (NovaClient.isReady()) {
            NovaClient.get().saveConfig();
        }
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft == null) {
            return;
        }
        int screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = this.minecraft.getWindow().getGuiScaledHeight();

        graphics.fill(0, 0, screenWidth, screenHeight, Theme.BACKDROP);

        panelX = (screenWidth - Theme.PANEL_WIDTH) / 2;
        panelY = (screenHeight - Theme.PANEL_HEIGHT) / 2;

        drawPanel(graphics);
        drawTabs(graphics, mouseX, mouseY);
        drawRows(graphics, mouseX, mouseY);
        drawHeader(graphics);
        drawFooter(graphics, mouseX, mouseY);
    }

    private void drawPanel(GuiGraphics graphics) {
        int right = panelX + Theme.PANEL_WIDTH;
        int bottom = panelY + Theme.PANEL_HEIGHT;
        graphics.fill(panelX + 3, panelY + 4, right + 4, bottom + 5, Theme.SHADOW);
        graphics.fill(panelX, panelY, right, bottom, Theme.PANEL);
        graphics.fill(panelX, panelY, right, panelY + 1, Theme.PANEL_BORDER);
        graphics.fill(panelX, bottom - 1, right, bottom, Theme.PANEL_BORDER);
        graphics.fill(panelX, panelY, panelX + 1, bottom, Theme.PANEL_BORDER);
        graphics.fill(right - 1, panelY, right, bottom, Theme.PANEL_BORDER);
    }

    private void drawHeader(GuiGraphics graphics) {
        graphics.fill(panelX, panelY, panelX + Theme.PANEL_WIDTH,
                panelY + Theme.HEADER_HEIGHT, Theme.TAB_BAR);
        graphics.drawString(this.font, NovaClient.NAME, panelX + 10, panelY + 9, Theme.accent(), false);
        graphics.drawString(this.font, NovaClient.VERSION, panelX + 10 + this.font.width(NovaClient.NAME) + 5,
                panelY + 9, Theme.TEXT_DIM, false);

        String count = NovaClient.isReady()
                ? NovaClient.get().modules().enabledCount() + " active"
                : "inactive";
        graphics.drawString(this.font, count,
                panelX + Theme.PANEL_WIDTH - 10 - this.font.width(count),
                panelY + 9, Theme.TEXT_DIM, false);

        graphics.fill(panelX, panelY + Theme.HEADER_HEIGHT - 1,
                panelX + Theme.PANEL_WIDTH, panelY + Theme.HEADER_HEIGHT, Theme.SEPARATOR);
        graphics.fill(panelX + 1, panelY + Theme.HEADER_HEIGHT - 1,
                panelX + 34, panelY + Theme.HEADER_HEIGHT, Theme.accent());
    }

    private void drawFooter(GuiGraphics graphics, int mouseX, int mouseY) {
        int footerY = panelY + Theme.PANEL_HEIGHT - Theme.FOOTER_HEIGHT;
        graphics.fill(panelX, footerY, panelX + Theme.PANEL_WIDTH,
                panelY + Theme.PANEL_HEIGHT, Theme.TAB_BAR);

        String hint = hoveredRow >= 0 && hoveredRow < rows.size()
                ? rows.get(hoveredRow).module.description()
                : "Right Shift to close";
        if (this.font.width(hint) > Theme.PANEL_WIDTH - 16) {
            hint = this.font.plainSubstrByWidth(hint, Theme.PANEL_WIDTH - 22) + "...";
        }
        graphics.drawString(this.font, hint, panelX + 8, footerY + 3, Theme.TEXT_DIM, false);
    }

    private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = panelX;
        int y = panelY + Theme.HEADER_HEIGHT;
        int height = Theme.PANEL_HEIGHT - Theme.HEADER_HEIGHT - Theme.FOOTER_HEIGHT;

        graphics.fill(x, y, x + Theme.TAB_WIDTH, y + height, Theme.TAB_BAR);

        int tabY = y + 4;
        for (Category category : Category.tabs()) {
            boolean isSelected = category == selected;
            if (isSelected) {
                graphics.fill(x, tabY, x + Theme.TAB_WIDTH, tabY + 14, Theme.TAB_SELECTED);
                graphics.fill(x, tabY, x + 2, tabY + 14, Theme.accent());
            }
            int labelX = x + 8;
            graphics.drawString(this.font, category.displayName(), labelX, tabY + 3,
                    isSelected ? Theme.TEXT : Theme.TEXT_DIM, false);

            if (NovaClient.isReady()) {
                int enabled = countEnabled(category);
                if (enabled > 0) {
                    String text = String.valueOf(enabled);
                    graphics.drawString(this.font, text,
                            x + Theme.TAB_WIDTH - 6 - this.font.width(text), tabY + 3,
                            Theme.accent(), false);
                }
            }
            tabY += 16;
        }
    }

    private int countEnabled(Category category) {
        int count = 0;
        for (Module module : NovaClient.get().modules().inCategory(category)) {
            if (module.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    private void drawRows(GuiGraphics graphics, int mouseX, int mouseY) {
        rows.clear();

        int listX = panelX + Theme.TAB_WIDTH;
        int listY = panelY + Theme.HEADER_HEIGHT;
        int listWidth = Theme.PANEL_WIDTH - Theme.TAB_WIDTH;
        int listHeight = Theme.PANEL_HEIGHT - Theme.HEADER_HEIGHT - Theme.FOOTER_HEIGHT;

        graphics.enableScissor(listX, listY, listX + listWidth, listY + listHeight);

        if (!NovaClient.isReady()) {
            graphics.drawString(this.font, "NovaClient failed to start.",
                    listX + 8, listY + 8, Theme.TEXT_DIM, false);
            graphics.disableScissor();
            return;
        }

        List<Module> modules = search.isEmpty()
                ? NovaClient.get().modules().inCategory(selected)
                : NovaClient.get().modules().search(search);

        int y = listY + 2 - (int) scroll;
        hoveredRow = -1;

        for (Module module : modules) {
            int height = Theme.ROW_HEIGHT + (module.isExpanded()
                    ? module.settings().size() * Theme.SETTING_HEIGHT + 2
                    : 0);

            if (y + height > listY && y < listY + listHeight) {
                boolean hovered = mouseX >= listX && mouseX < listX + listWidth
                        && mouseY >= y && mouseY < y + height && mouseY >= listY
                        && mouseY < listY + listHeight;
                drawModuleRow(graphics, module, listX, y, listWidth, hovered, mouseX, mouseY);
            }

            rows.add(new Row(module, listX, y, listWidth, height));
            if (mouseX >= listX && mouseX < listX + listWidth
                    && mouseY >= y && mouseY < y + height
                    && mouseY >= listY && mouseY < listY + listHeight) {
                hoveredRow = rows.size() - 1;
            }
            y += height;
        }

        int contentHeight = y + (int) scroll - listY;
        maxScroll = Math.max(0, contentHeight - listHeight);
        if (scroll > maxScroll) {
            scroll = maxScroll;
        }
        if (scroll < 0) {
            scroll = 0;
        }

        graphics.disableScissor();
    }

    private void drawModuleRow(GuiGraphics graphics, Module module, int x, int y, int width,
                               boolean hovered, int mouseX, int mouseY) {
        graphics.fill(x + 3, y + 1, x + width - 3, y + Theme.ROW_HEIGHT - 1,
                hovered ? Theme.ROW_HOVER : Theme.ROW);
        if (module.isEnabled()) {
            graphics.fill(x + 3, y + 1, x + 5, y + Theme.ROW_HEIGHT - 1, Theme.accent());
        }

        graphics.drawString(this.font, module.name(), x + 11, y + 6,
                module.isEnabled() ? Theme.TEXT : Theme.TEXT_DIM, false);

        int switchX = x + width - 27;
        int switchY = y + 6;
        int switchWidth = 18;
        int switchHeight = 9;

        int track = module.isEnabled() ? Theme.accent() : Theme.TOGGLE_OFF;
        graphics.fill(switchX + 1, switchY, switchX + switchWidth - 1, switchY + switchHeight, track);
        graphics.fill(switchX, switchY + 1, switchX + switchWidth, switchY + switchHeight - 1, track);
        int knobX = module.isEnabled() ? switchX + switchWidth - 8 : switchX + 2;
        graphics.fill(knobX, switchY + 2, knobX + 6, switchY + switchHeight - 2, 0xFFF2F2F5);

        if (module.hasSettings()) {
            String arrow = module.isExpanded() ? "-" : "+";
            graphics.drawString(this.font, arrow, x + width - 39, y + 6, Theme.TEXT_DIM, false);
        }

        if (module.isExpanded()) {
            int settingY = y + Theme.ROW_HEIGHT;
            for (Setting<?> setting : module.settings()) {
                drawSetting(graphics, setting, x + 12, settingY, width - 16, mouseX, mouseY);
                settingY += Theme.SETTING_HEIGHT;
            }
        }
    }

    private void drawSetting(GuiGraphics graphics, Setting<?> setting, int x, int y, int width,
                             int mouseX, int mouseY) {
        graphics.drawString(this.font, setting.name(), x, y + 2, Theme.TEXT_DIM, false);

        String value = setting.displayValue();
        graphics.drawString(this.font, value, x + width - this.font.width(value), y + 2,
                Theme.accent(), false);

        if (setting instanceof SliderSetting slider) {
            int barX = x;
            int barY = y + Theme.SETTING_HEIGHT - 3;
            int barWidth = width;
            graphics.fill(barX, barY, barX + barWidth, barY + 1, Theme.TOGGLE_OFF);
            int filled = (int) (barWidth * Math.max(0.0, Math.min(1.0, slider.fraction())));
            graphics.fill(barX, barY, barX + filled, barY + 1, Theme.accent());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!NovaClient.isReady()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int tabX = panelX;
        int tabTop = panelY + Theme.HEADER_HEIGHT;
        if (mouseX >= tabX && mouseX < tabX + Theme.TAB_WIDTH
                && mouseY >= tabTop && mouseY < panelY + Theme.PANEL_HEIGHT - Theme.FOOTER_HEIGHT) {
            int index = (int) ((mouseY - tabTop - 4) / 16);
            Category[] categories = Category.tabs();
            if (index >= 0 && index < categories.length) {
                selected = categories[index];
                scroll = 0;
                return true;
            }
        }

        for (Row row : rows) {
            if (!row.contains(mouseX, mouseY)) {
                continue;
            }
            int relativeY = (int) (mouseY - row.y);

            if (relativeY < Theme.ROW_HEIGHT) {
                int switchX = row.x + row.width - 24;
                if (mouseX >= switchX - 4 && mouseX <= switchX + 20) {
                    row.module.toggle();
                announce(row.module);
                    announce(row.module);
                    return true;
                }
                if (row.module.hasSettings() && mouseX >= row.x + row.width - 40) {
                    row.module.setExpanded(!row.module.isExpanded());
                    return true;
                }
                row.module.toggle();
                return true;
            }

            int settingIndex = (relativeY - Theme.ROW_HEIGHT) / Theme.SETTING_HEIGHT;
            if (settingIndex >= 0 && settingIndex < row.module.settings().size()) {
                Setting<?> setting = row.module.settings().get(settingIndex);
                clickSetting(setting, button, mouseX, row);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void clickSetting(Setting<?> setting, int button, double mouseX, Row row) {
        boolean decrement = button == 1;
        if (setting instanceof SliderSetting slider) {
            if (button == 0 || button == 1) {
                if (decrement) {
                    slider.decrement();
                } else {
                    slider.increment();
                }
                applyLive(row.module);
            }
        } else if (setting instanceof ModeSetting mode) {
            if (decrement) {
                mode.cycleBack();
            } else {
                mode.cycle();
            }
            applyLive(row.module);
        } else if (setting instanceof dev.novaclient.setting.ToggleSetting toggle) {
            toggle.set(!toggle.get());
            applyLive(row.module);
        }
    }

    private void applyLive(Module module) {
        if (module.isEnabled()) {
            module.setEnabled(false);
            module.setEnabled(true);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll -= scrollY * 12.0;
        if (scroll < 0) {
            scroll = 0;
        }
        if (scroll > maxScroll) {
            scroll = maxScroll;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == NovaClient.TOGGLE_KEY) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static final class Row {

        final Module module;
        final int x;
        final int y;
        final int width;
        final int height;

        Row(Module module, int x, int y, int width, int height) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    private static void announce(dev.novaclient.module.Module module) {
        if (!dev.novaclient.state.MiscState.notifications()) {
            return;
        }
        dev.novaclient.state.MiscState.pushNotification(
                module.name() + (module.isEnabled() ? " enabled" : " disabled"));
    }
}
