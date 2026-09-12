package dev.novaclient;

import dev.novaclient.module.Category;
import dev.novaclient.module.Module;
import dev.novaclient.module.ModuleManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CensusTest {

    private static ModuleManager fresh() {
        ModuleManager manager = new ModuleManager();
        manager.register();
        return manager;
    }

    @Test
    @DisplayName("the registry holds the advertised 72 modules across 6 tabs")
    void advertisedCountsMatchTheRegistry() {
        ModuleManager manager = fresh();
        assertEquals(14, manager.inCategory(Category.PERFORMANCE).size(), "performance tab");
        assertEquals(15, manager.inCategory(Category.RENDER).size(), "render tab");
        assertEquals(15, manager.inCategory(Category.VISUAL).size(), "visual tab");
        assertEquals(13, manager.inCategory(Category.HUD).size(), "hud tab");
        assertEquals(8, manager.inCategory(Category.PLAYER).size(), "player tab");
        assertEquals(7, manager.inCategory(Category.MISC).size(), "misc tab");
        assertEquals(72, manager.all().size(), "total modules");
        assertEquals(6, Category.tabs().length, "tab count");
    }

    @Test
    @DisplayName("more than half of the modules expose a setting")
    void settingsAreWidelyAvailable() {
        ModuleManager manager = fresh();
        long withSettings = manager.all().stream().filter(Module::hasSettings).count();
        assertTrue(withSettings >= 40, "expected at least 40 modules with settings, got " + withSettings);
    }
}
