package dev.novaclient.hud;

public enum Anchor {

    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    public static Anchor parse(String value, Anchor fallback) {
        if (value == null) {
            return fallback;
        }
        switch (value.trim().toLowerCase()) {
            case "topleft":
                return TOP_LEFT;
            case "topright":
                return TOP_RIGHT;
            case "bottomleft":
                return BOTTOM_LEFT;
            case "bottomright":
                return BOTTOM_RIGHT;
            default:
                return fallback;
        }
    }

    public boolean isRight() {
        return this == TOP_RIGHT || this == BOTTOM_RIGHT;
    }

    public boolean isBottom() {
        return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
    }
}
