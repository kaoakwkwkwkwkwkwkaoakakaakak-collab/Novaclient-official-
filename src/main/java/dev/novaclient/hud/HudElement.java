package dev.novaclient.hud;

public enum HudElement {

    FPS("FPS"),
    CPS("CPS"),
    PING("Ping"),
    COORDS("Coords"),
    DIRECTION("Direction"),
    BIOME("Biome"),
    KEYSTROKES("Keys"),
    ARMOR("Armor"),
    POTIONS("Potions"),
    CLOCK("Clock"),
    MEMORY("Memory"),
    SERVER_IP("IP"),
    WATERMARK("Nova"),
    NOTIFICATIONS("Notice"),
    REACH("Reach"),
    ARRAYLIST("Modules");

    private final String label;

    HudElement(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
