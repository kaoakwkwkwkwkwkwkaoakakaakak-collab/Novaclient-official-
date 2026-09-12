package dev.novaclient.module;

import dev.novaclient.setting.ModeSetting;
import dev.novaclient.setting.SliderSetting;
import dev.novaclient.setting.ToggleSetting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleManagerTest {

    private static ModuleManager fresh() {
        ModuleManager manager = new ModuleManager();
        manager.register();
        return manager;
    }

    @Test
    @DisplayName("every module name is unique")
    void everyModuleNameIsUnique() {
        ModuleManager manager = fresh();
        Set<String> names = new HashSet<>();
        for (Module module : manager.all()) {
            assertTrue(names.add(module.name()), "duplicate module name: " + module.name());
        }
        assertEquals(names.size(), manager.all().size());
    }

    @Test
    @DisplayName("at least 40 modules are registered across 6 tabs")
    void theModuleCountMeetsTheTarget() {
        ModuleManager manager = fresh();
        assertTrue(manager.all().size() >= 40,
                "expected at least 40 modules, found " + manager.all().size());
        assertEquals(6, manager.categories().size());
        for (Category category : Category.tabs()) {
            assertFalse(manager.inCategory(category).isEmpty(),
                    category.displayName() + " has no modules");
        }
    }

    @Test
    @DisplayName("modules are grouped into the category they declare")
    void modulesLandInTheRightCategory() {
        ModuleManager manager = fresh();
        for (Module module : manager.all()) {
            assertTrue(manager.inCategory(module.category()).contains(module),
                    module.name() + " is missing from its own category listing");
        }
    }

    @Test
    @DisplayName("lookup by name ignores case")
    void lookupIgnoresCase() {
        ModuleManager manager = fresh();
        Module module = manager.all().get(0);
        assertEquals(module, manager.byName(module.name().toUpperCase()));
        assertEquals(module, manager.byName(module.name().toLowerCase()));
    }

    @Test
    @DisplayName("toggling a module runs its effects in both directions")
    void togglingRunsEffectsBothWays() {
        int[] calls = new int[2];
        ActiveModule module = new ActiveModule("Test", "desc", Category.MISC) {
        };
        module.effect(enabled -> {
            if (enabled) {
                calls[0]++;
            } else {
                calls[1]++;
            }
        });

        module.setEnabled(true);
        assertEquals(1, calls[0]);
        assertEquals(0, calls[1]);

        module.setEnabled(false);
        assertEquals(1, calls[0]);
        assertEquals(1, calls[1]);
    }

    @Test
    @DisplayName("a module whose effect throws is disabled rather than left half-on")
    void aFailingEffectDisablesTheModule() {
        ActiveModule module = new ActiveModule("Broken", "desc", Category.MISC) {
        };
        module.effect(enabled -> {
            throw new IllegalStateException("simulated renderer failure");
        });

        module.setEnabled(true);

        assertFalse(module.isEnabled(), "a module that failed to apply must not report itself on");
        assertTrue(module.isFaulty());
        assertTrue(module.faultMessage().contains("simulated renderer failure"));
    }

    @Test
    @DisplayName("keybind lookup finds every module bound to a key")
    void keybindLookupFindsBoundModules() {
        ModuleManager manager = fresh();
        Module first = manager.all().get(0);
        first.keyCode(75);

        List<Module> bound = manager.boundTo(75);
        assertEquals(1, bound.size());
        assertEquals(first, bound.get(0));
        assertTrue(manager.boundTo(-1).isEmpty());
    }

    @Test
    @DisplayName("search matches name, description and category")
    void searchMatchesEveryField() {
        ModuleManager manager = fresh();
        assertFalse(manager.search("motion").isEmpty(), "motion blur must be findable");
        assertFalse(manager.search("HUD").isEmpty(), "searching a category name must work");
        assertEquals(manager.all().size(), manager.search("").size());
        assertEquals(manager.all().size(), manager.search(null).size());
    }

    @Test
    @DisplayName("disableAll turns everything off")
    void disableAllTurnsEverythingOff() {
        ModuleManager manager = fresh();
        for (Module module : manager.all()) {
            module.setEnabled(true);
        }
        assertTrue(manager.enabledCount() > 0);

        manager.disableAll();

        assertEquals(0, manager.enabledCount());
    }

    @Test
    @DisplayName("slider settings snap to their step and clamp to their range")
    void slidersClampAndSnap() {
        SliderSetting slider = new SliderSetting("Test", 50, 0, 100, 10);
        assertEquals(50.0, slider.get());

        slider.set(999.0);
        assertEquals(100.0, slider.get(), "must clamp to the maximum");

        slider.set(-50.0);
        assertEquals(0.0, slider.get(), "must clamp to the minimum");

        slider.set(53.0);
        assertEquals(50.0, slider.get(), "must snap to the nearest step");

        slider.set(Double.NaN);
        assertEquals(0.0, slider.get(), "NaN must not corrupt the value");
    }

    @Test
    @DisplayName("mode settings cycle and reject unknown values")
    void modesCycleSafely() {
        ModeSetting mode = new ModeSetting("Test", "A", "A", "B", "C");
        assertEquals("A", mode.get());

        mode.cycle();
        assertEquals("B", mode.get());
        mode.cycle();
        mode.cycle();
        assertEquals("A", mode.get(), "cycling past the end must wrap");

        mode.cycleBack();
        assertEquals("C", mode.get(), "cycling back from the start must wrap");

        mode.set("nonsense");
        assertEquals("C", mode.get(), "an unknown mode must not replace the current one");
    }

    @Test
    @DisplayName("settings serialise and deserialise without loss")
    void settingsRoundTrip() {
        SliderSetting slider = new SliderSetting("S", 42, 0, 100, 1);
        ToggleSetting toggle = new ToggleSetting("T", true);
        ModeSetting mode = new ModeSetting("M", "B", "A", "B", "C");

        assertTrue(slider.deserialise(slider.serialise()));
        assertTrue(toggle.deserialise(toggle.serialise()));
        assertTrue(mode.deserialise(mode.serialise()));

        assertEquals(42.0, slider.get());
        assertTrue(toggle.get());
        assertEquals("B", mode.get());

        assertFalse(toggle.deserialise("maybe"), "a non-boolean must be rejected");
        assertFalse(slider.deserialise("abc"), "a non-number must be rejected");
        assertFalse(mode.deserialise("Z"), "an unknown mode must be rejected");
    }

    @Test
    @DisplayName("a mode setting refuses a default that is not one of its modes")
    void modeSettingRejectsABadDefault() {
        assertThrows(IllegalArgumentException.class,
                () -> new ModeSetting("Bad", "Z", "A", "B"));
        assertThrows(IllegalArgumentException.class,
                () -> new ModeSetting("Empty", "A"));
    }

    @Test
    @DisplayName("a slider refuses an inverted or non-positive range")
    void sliderRejectsABadRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new SliderSetting("Bad", 5, 100, 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new SliderSetting("Bad", 5, 0, 100, 0));
    }

    @Test
    @DisplayName("expanding a module with no settings does nothing")
    void expandingWithoutSettingsIsANoOp() {
        ActiveModule module = new ActiveModule("Plain", "desc", Category.MISC) {
        };
        assertFalse(module.hasSettings());
        module.setExpanded(true);
        assertFalse(module.isExpanded());
    }
}
