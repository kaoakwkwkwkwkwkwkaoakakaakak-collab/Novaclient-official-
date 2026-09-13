package dev.novaclient.module;

public enum Category {

    PERFORMANCE("Performance"),
    RENDER("Render"),
    VISUAL("Visual"),
    HUD("HUD"),
    PLAYER("Player"),
    MISC("Misc");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static Category[] tabs() {
        return values();
    }
}
