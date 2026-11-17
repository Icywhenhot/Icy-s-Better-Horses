package icy.betterhorses.net.tuning;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BondRulesTest {

    private final List<String> warns = new ArrayList<>();

    private BondRules read(String json) {
        return BondRules.read(JsonParser.parseString(json).getAsJsonObject(), warns::add);
    }

    private static double legacyGrowth(int bond) {
        return Math.min(bond / 20, 5) * 0.15D;
    }

    @Test
    void defaultsMatchTheOldHardcodedCurve() {
        BondRules r = BondRules.DEFAULTS;
        for (int bond = 0; bond <= 150; bond++) {
            assertEquals(legacyGrowth(bond), r.speedBonus(bond), 1e-12, "bond " + bond);
            assertEquals(legacyGrowth(bond), r.jumpBonus(bond), 1e-12, "bond " + bond);
            assertEquals(0.0D, r.healthBonus(bond), 1e-12);
        }
    }

    @Test
    void defaultsMatchTheOldTiers() {
        BondRules r = BondRules.DEFAULTS;
        assertEquals(0, r.tier(0));
        assertEquals(0, r.tier(39));
        assertEquals(1, r.tier(40));
        assertEquals(1, r.tier(99));
        assertEquals(2, r.tier(100));
        assertEquals(0, r.tier(-3));
        assertEquals(100, r.maxBond());
    }

    @Test
    void defaultsMatchTheOldSources() {
        BondRules r = BondRules.DEFAULTS;
        assertEquals(1000.0D, r.rideBlocks(), 1e-9);
        assertEquals(1, r.rideBond());
        assertEquals(10, r.jumpsPerBond());
        assertEquals(2, r.jumpDailyCap());
        assertEquals(1, r.killBond());
        assertEquals(3, r.killDailyCap());
        assertEquals(2, r.saveBond());
        assertEquals(2, r.saveDailyCap());
        assertEquals(2, r.hitLoss());
        assertEquals(1, r.treatBond());
        assertEquals(6000, r.treatCooldown());
        assertEquals(2, r.gappleBond());
        assertEquals(6000, r.gappleCooldown());
        assertEquals(50, r.foalCap());
    }

    @Test
    void emptyFileIsDefaults() {
        assertEquals(BondRules.DEFAULTS, read("{}"));
        assertTrue(warns.isEmpty());
    }

    @Test
    void customCurve() {
        BondRules r = read("""
                {"growth_every": 10, "growth_steps": 10, "growth_speed": 0.05, "growth_jump": 0.02, "growth_health": 0.1}
                """);
        assertEquals(0.25D, r.speedBonus(55), 1e-12);
        assertEquals(0.10D, r.jumpBonus(55), 1e-12);
        assertEquals(0.5D, r.healthBonus(55), 1e-12);
        assertEquals(0.5D, r.speedBonus(250), 1e-12);
    }

    @Test
    void zeroStepIsClampedNotDividedBy() {
        BondRules r = read("{\"growth_every\": 0}");
        assertEquals(1, r.growthEvery());
        assertEquals(0.75D, r.speedBonus(100), 1e-12);
        assertTrue(warns.stream().anyMatch(w -> w.contains("growth_every")));
    }

    @Test
    void negativeGrowthIsAllowedButNeverStopsTheHorse() {
        BondRules r = read("{\"growth_speed\": -0.5}");
        assertEquals(-0.5D, r.speedBonus(20), 1e-12);
        assertEquals(-0.95D, r.speedBonus(100), 1e-12);
        assertEquals(-0.95D, read("{\"growth_jump\": -5}").growthJump(), 1e-12);
    }

    @Test
    void tiersOutOfOrderKeepDefaults() {
        BondRules r = read("{\"tier_two\": 80, \"tier_three\": 50}");
        assertEquals(40, r.tierTwo());
        assertEquals(100, r.tierThree());
        assertTrue(warns.stream().anyMatch(w -> w.contains("tier")));
    }

    @Test
    void tiersAboveMaxStillLoadButWarn() {
        BondRules r = read("{\"max_bond\": 50}");
        assertEquals(50, r.maxBond());
        assertEquals(1, r.tier(50));
        assertTrue(warns.stream().anyMatch(w -> w.contains("tier_three")));
    }

    @Test
    void raisedMaxAndTiers() {
        BondRules r = read("{\"max_bond\": 200, \"tier_two\": 80, \"tier_three\": 200}");
        assertEquals(200, r.clamp(500));
        assertEquals(0, r.clamp(-9));
        assertEquals(1, r.tier(199));
        assertEquals(2, r.tier(200));
        assertTrue(warns.isEmpty());
    }

    @Test
    void maxBondHasAFloorAndCeiling() {
        assertEquals(1, read("{\"max_bond\": 0}").maxBond());
        assertEquals(BondRules.CEILING, read("{\"max_bond\": 99999999}").maxBond());
    }

    @Test
    void reachableGrowthStopsAtMaxBond() {
        BondRules r = read("{\"max_bond\": 50}");
        assertEquals(2 * 0.15D, r.topSpeedBonus(), 1e-12);
        assertEquals(5 * 0.15D, BondRules.DEFAULTS.topSpeedBonus(), 1e-12);
    }

    @Test
    void zeroRideBlocksCannotFarmEveryTick() {
        BondRules r = read("{\"ride_blocks\": 0, \"jumps_per_bond\": 0}");
        assertTrue(r.rideBlocks() >= 1.0D);
        assertTrue(r.jumpsPerBond() >= 1);
    }

    @Test
    void negativeCooldownsAndCapsClampToZero() {
        BondRules r = read("{\"treat_cooldown\": -100, \"kill_daily_cap\": -1, \"hit_loss\": -4}");
        assertEquals(0, r.treatCooldown());
        assertEquals(0, r.killDailyCap());
        assertEquals(0, r.hitLoss());
    }
}
