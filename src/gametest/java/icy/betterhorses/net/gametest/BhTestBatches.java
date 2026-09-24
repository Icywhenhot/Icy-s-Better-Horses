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

    // Mirrors Jake's server: tamed vanilla horses aren't converted, RETURN_HOME never teleports.
    public static final String JAKE_SERVER = "jake_server";

    @BeforeBatch(batch = JAKE_SERVER)
    public void setUpJakeServer(ServerLevel level) {
        BhConfig.apply(Map.of(
                BhFeature.CONVERT_TAMED_HORSES, false,
                BhFeature.HORSE_TELEPORT, false,
                BhFeature.SMOOTH_HORSE_CLIMBING, true
        ), BhConfig.tuning());
    }

    @AfterBatch(batch = JAKE_SERVER)
    public void tearDownJakeServer(ServerLevel level) {
        BhConfig.apply(Map.of(
                BhFeature.CONVERT_TAMED_HORSES, true,
                BhFeature.HORSE_TELEPORT, true,
                BhFeature.SMOOTH_HORSE_CLIMBING, true
        ), BhConfig.tuning());
    }
}
