package com.goodboysoul.pojavxoptimizer.module.battery;

import com.goodboysoul.pojavxoptimizer.config.DeviceProfile;
import com.goodboysoul.pojavxoptimizer.config.PjoConfig;
import com.goodboysoul.pojavxoptimizer.module.thermal.ThermalGovernor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FramePacerTest {

    private static FramePacer pacerOn60Hz(PjoConfig config) {
        FramePacer pacer = new FramePacer(config);
        pacer.setRefreshRate(60);
        return pacer;
    }

    private static int sample(FramePacer pacer, boolean guiOpen) {
        return pacer.update(0, 0, 0, 0, 0, guiOpen, ThermalGovernor.Pressure.NONE);
    }

    @Test
    @DisplayName("without max FPS mode the cap never exceeds the panel refresh rate")
    void theDefaultPathIsBoundedByTheRefreshRate() {
        PjoConfig config = PjoConfig.defaults(DeviceProfile.FLAGSHIP);
        config.batterySaver(false);
        config.maxFpsMode(false);
        config.capToRefreshRate(false);

        FramePacer pacer = pacerOn60Hz(config);
        assertEquals(60, sample(pacer, false),
                "with battery saver off the cap falls back to the refresh rate");
    }

    @Test
    @DisplayName("max FPS mode lifts the cap above a 60Hz panel")
    void maxFpsModeLiftsTheRefreshRateCeiling() {
        PjoConfig config = PjoConfig.defaults(DeviceProfile.FLAGSHIP);
        config.maxFpsMode(true);

        FramePacer pacer = pacerOn60Hz(config);
        assertEquals(PjoConfig.UNLIMITED_FPS, sample(pacer, false),
                "max FPS mode must not be bounded by a 60Hz panel");
        assertEquals(PjoConfig.UNLIMITED_FPS, sample(pacer, true),
                "max FPS mode must also lift the menu cap");
    }

    @Test
    @DisplayName("max FPS mode works the same on a 120Hz panel")
    void maxFpsModeIsIndependentOfPanelRefreshRate() {
        PjoConfig config = PjoConfig.defaults(DeviceProfile.FLAGSHIP);
        config.maxFpsMode(true);

        FramePacer pacer = new FramePacer(config);
        pacer.setRefreshRate(120);
        assertEquals(120, pacer.refreshRate());
        assertEquals(PjoConfig.UNLIMITED_FPS, sample(pacer, false));
    }

    @Test
    @DisplayName("turning on max FPS mode switches off everything that caps frames")
    void maxFpsModeDisablesTheOtherLimiters() {
        PjoConfig config = PjoConfig.defaults(DeviceProfile.LOW);
        config.thermalGovernor(true);
        config.batterySaver(true);
        config.capToRefreshRate(true);
        assertTrue(config.batterySaver());
        assertTrue(config.thermalGovernor(),
                "the thermal governor must be on before this test can show max FPS mode turning it off");
        assertTrue(config.capToRefreshRate());

        config.maxFpsMode(true);

        assertFalse(config.batterySaver(), "battery saver caps idle frames, so it must go");
        assertFalse(config.capToRefreshRate(), "capping to the panel is the whole problem");
        assertFalse(config.thermalGovernor(), "the governor lowers the cap when hot");
    }

    @Test
    @DisplayName("battery saver still caps idle and menu frames when max FPS mode is off")
    void batterySaverStillWorksWhenMaxFpsModeIsOff() {
        PjoConfig config = PjoConfig.defaults(DeviceProfile.LOW);
        config.batterySaver(true);
        config.maxFpsMode(false);
        config.guiFpsCap(15);

        FramePacer pacer = pacerOn60Hz(config);
        assertEquals(15, sample(pacer, true),
                "a menu with battery saver on should drop to the gui cap");
    }

    @Test
    @DisplayName("the fps clamp ceiling allows unlimited rather than stopping at 120")
    void theClampCeilingAllowsUnlimited() {
        assertEquals(260, PjoConfig.UNLIMITED_FPS,
                "260 is Minecraft's own sentinel for an uncapped frame rate");

        PjoConfig config = PjoConfig.defaults(DeviceProfile.POTATO);
        config.idleFpsCap(240);
        config.guiFpsCap(240);
        assertEquals(240, config.idleFpsCap(),
                "a cap above 120 must survive validation instead of being clamped down");
        assertEquals(240, config.guiFpsCap());
    }

    @Test
    @DisplayName("the effective caps report unlimited in max FPS mode")
    void theEffectiveCapsReflectMaxFpsMode() {
        PjoConfig config = PjoConfig.defaults(DeviceProfile.LOW);
        config.idleFpsCap(20);
        config.guiFpsCap(15);

        assertEquals(20, config.effectiveIdleFpsCap());
        assertEquals(15, config.effectiveGuiFpsCap());

        config.maxFpsMode(true);

        assertEquals(PjoConfig.UNLIMITED_FPS, config.effectiveIdleFpsCap());
        assertEquals(PjoConfig.UNLIMITED_FPS, config.effectiveGuiFpsCap());
    }

    @Test
    @DisplayName("reapplying a device profile does not silently re-enable the caps")
    void reapplyingAProfileRespectsMaxFpsMode() {
        PjoConfig config = PjoConfig.defaults(DeviceProfile.LOW);
        config.maxFpsMode(true);

        config.applyProfile(DeviceProfile.POTATO);

        assertTrue(config.maxFpsMode());
        assertFalse(config.batterySaver(),
                "switching profile must not quietly turn the idle cap back on");
        assertFalse(config.capToRefreshRate());
    }
}
