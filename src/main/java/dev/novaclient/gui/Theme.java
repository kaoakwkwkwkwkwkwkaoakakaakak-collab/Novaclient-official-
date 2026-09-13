package dev.novaclient.gui;

public final class Theme {

    public static final int PANEL_WIDTH = 372;
    public static final int PANEL_HEIGHT = 246;
    public static final int TAB_WIDTH = 96;
    public static final int ROW_HEIGHT = 19;
    public static final int SETTING_HEIGHT = 14;
    public static final int HEADER_HEIGHT = 26;
    public static final int FOOTER_HEIGHT = 16;

    public static final int BACKDROP = 0x9E000000;
    public static final int PANEL = 0xF7131318;
    public static final int PANEL_BORDER = 0xFF1E1E26;
    public static final int SHADOW = 0x40000000;
    public static final int SEPARATOR = 0xFF1C1C23;
    public static final int TAB_BAR = 0xFF141419;
    public static final int TAB_SELECTED = 0xFF1D1D25;
    public static final int ROW = 0xFF17171D;
    public static final int ROW_HOVER = 0xFF20202A;
    public static final int TEXT = 0xFFE6E6EA;
    public static final int TEXT_DIM = 0xFF8A8A96;
    public static final int ACCENT = 0xFF7C9CFF;
    public static final int ACCENT_DIM = 0x447C9CFF;

    public static int accent() {
        return dev.novaclient.state.RenderState.accentColor();
    }
    public static final int TOGGLE_OFF = 0xFF2A2A33;

    private Theme() {
    }

    public static int withAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int darken(int argb, double amount) {
        double keep = 1.0 - (amount < 0.0 ? 0.0 : Math.min(amount, 1.0));
        int a = (argb >>> 24) & 0xFF;
        int r = (int) (((argb >>> 16) & 0xFF) * keep);
        int g = (int) (((argb >>> 8) & 0xFF) * keep);
        int b = (int) ((argb & 0xFF) * keep);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
