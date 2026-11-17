package icy.betterhorses.net.registry;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchetypeTypeTest {

    private static ArchetypeType race() {
        return ArchetypeType.builder()
                .speed(0.2546D, 0.3472D).health(15.0D, 20.0D).jump(0.72D, 1.02D)
                .bashDamage(0.6D).bashKnockback(0.8D)
                .build();
    }

    private static ArchetypeType western() {
        return ArchetypeType.builder()
                .speed(0.1852D, 0.3472D).health(15.0D, 30.0D).jump(0.57D, 0.84D)
                .pathSpeedBonus(0.10D, 0.20D, 0.50D)
                .build();
    }

    @Test
    void spookChanceDropsWithBondTier() {
        ArchetypeType arch = race();
        assertEquals(0.10D, arch.spookChance(0), 1e-9);
        assertEquals(0.07D, arch.spookChance(1), 1e-9);
        assertEquals(0.05D, arch.spookChance(2), 1e-9);
        assertEquals(0.05D, arch.spookChance(7), 1e-9);
    }

    @Test
    void draftSpookIsHalved() {
        ArchetypeType draft = ArchetypeType.builder().speed(0.1D, 0.2D).health(35, 50).jump(0.38D, 0.72D)
                .baseSpookChance(0.05D).build();
        assertEquals(0.05D, draft.spookChance(0), 1e-9);
        assertEquals(0.035D, draft.spookChance(1), 1e-9);
        assertEquals(0.025D, draft.spookChance(2), 1e-9);
    }

    @Test
    void roadBonusFollowsTier() {
        ArchetypeType arch = western();
        assertEquals(0.10D, arch.pathSpeedBonus(0), 1e-9);
        assertEquals(0.20D, arch.pathSpeedBonus(1), 1e-9);
        assertEquals(0.50D, arch.pathSpeedBonus(2), 1e-9);
        assertEquals(0.50D, arch.pathSpeedBonus(3), 1e-9);
    }

    @Test
    void builderDefaultsMatchPlainHorse() {
        ArchetypeType arch = race();
        assertEquals(1.125D, arch.stepHeight(), 1e-9);
        assertEquals(3, arch.defaultChestRows());
        assertEquals(1, arch.medkitMultiplier());
        assertEquals(0, arch.passiveHealInterval());
        assertEquals(0.0D, arch.fallDamageWaiver(), 1e-9);
        assertEquals(0.0D, arch.knockbackResistance(), 1e-9);
        assertEquals(0.6D, arch.kickDamage(), 1e-9);
    }

    @Test
    void rollsStayInsideTheRange() {
        ArchetypeType arch = race();
        RandomSource rand = RandomSource.create(42L);
        for (int i = 0; i < 5000; i++) {
            double s = arch.rollSpeed(rand);
            double h = arch.rollHealth(rand);
            double j = arch.rollJump(rand);
            assertTrue(s >= 0.2546D && s <= 0.3472D, "speed " + s);
            assertTrue(h >= 15.0D && h <= 20.0D && h == Math.rint(h), "health " + h);
            assertTrue(j >= 0.72D && j <= 1.02D, "jump " + j);
        }
    }

    @Test
    void clampsAndMidpoints() {
        ArchetypeType arch = race();
        assertEquals(0.3472D, arch.clampSpeed(9.0D), 1e-9);
        assertEquals(0.2546D, arch.clampSpeed(-1.0D), 1e-9);
        assertEquals(20.0D, arch.clampHealth(25.4D), 1e-9);
        assertEquals(16.0D, arch.clampHealth(15.6D), 1e-9);
        assertEquals(0.87D, arch.midJump(), 1e-9);
        assertEquals(18.0D, arch.midHealth(), 1e-9);
    }
}
