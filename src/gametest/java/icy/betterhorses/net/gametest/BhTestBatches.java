package icy.betterhorses.net.gametest;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhFeature;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;

// Batch names are global across every registered test class, so the before/after functions for
// each named batch live here once, not per test class.
public final class BhTestBatches {

    // Teleport and tamed-horse conversion off: tamed vanilla horses stay vanilla, RETURN_HOME never teleports.
    public static final String TOGGLES_OFF = "toggles_off";

    @BeforeBatch(batch = TOGGLES_OFF)
    public void setUpTogglesOff(ServerLevel level) {
        BhConfig.apply(Map.of(
                BhFeature.CONVERT_TAMED_HORSES, false,
                BhFeature.HORSE_TELEPORT, false
        ), BhConfig.tuning());
    }

    @AfterBatch(batch = TOGGLES_OFF)
    public void tearDownTogglesOff(ServerLevel level) {
        BhConfig.apply(Map.of(
                BhFeature.CONVERT_TAMED_HORSES, true,
                BhFeature.HORSE_TELEPORT, true
        ), BhConfig.tuning());
    }
}
