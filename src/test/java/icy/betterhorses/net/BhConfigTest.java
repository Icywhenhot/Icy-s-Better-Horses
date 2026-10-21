package icy.betterhorses.net;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BhConfigTest {

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void tuningDefaultsAreStable() {
        BhTuning tuning = BhConfig.tuning();
        assertNotNull(tuning, "tuning must never be null before load()");

        assertEquals(1, tuning.bondAmount());
        assertEquals(1, tuning.bondMinutes());
        assertEquals(5, tuning.spawnWeight());
        assertEquals(2, tuning.groupMin());
        assertEquals(6, tuning.groupMax());
        assertEquals(0.10D, tuning.spawnFloor());
        assertEquals(10, tuning.reviveLevels());
        assertEquals(20, tuning.reviveBondLoss());
    }

    @Test
    void constructorArgumentsMapToFieldsInDeclaredOrder() {
        BhTuning tuning = new BhTuning(2, 3, 4, 5, 6, 0.25D, 7, 8, 96, BhTuning.Dismount.STAY);

        assertEquals(2, tuning.bondAmount());
        assertEquals(3, tuning.bondMinutes());
        assertEquals(4, tuning.spawnWeight());
        assertEquals(5, tuning.groupMin());
        assertEquals(6, tuning.groupMax());
        assertEquals(0.25D, tuning.spawnFloor());
        assertEquals(7, tuning.reviveLevels());
        assertEquals(8, tuning.reviveBondLoss());
        assertEquals(96, tuning.whistleRange());
        assertEquals(BhTuning.Dismount.STAY, tuning.dismount());
        assertEquals(3600, tuning.bondIntervalTicks());
    }

    @Test
    void defaultsMatchBhTuningDefaults() {
        BhTuning defaults = BhTuning.defaults();

        assertEquals(1, defaults.bondAmount());
        assertEquals(1, defaults.bondMinutes());
        assertEquals(5, defaults.spawnWeight());
        assertEquals(2, defaults.groupMin());
        assertEquals(6, defaults.groupMax());
        assertEquals(0.10D, defaults.spawnFloor());
        assertEquals(BhTuning.Dismount.WANDER, defaults.dismount());
    }

    @Test
    void defaultsAreAlreadyWithinClampRanges() {
        assertEquals(BhTuning.defaults(), BhTuning.defaults().clamped());
    }

    @Test
    void defaultBondIntervalIsOneMinuteInTicks() {
        assertEquals(1200, BhTuning.defaults().bondIntervalTicks());
    }
}
