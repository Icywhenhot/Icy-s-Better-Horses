package icy.betterhorses.net.tuning;

import com.google.gson.JsonParser;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoalRulesTest {

    private final List<String> warns = new ArrayList<>();

    private FoalRules read(String json) {
        return FoalRules.read(JsonParser.parseString(json).getAsJsonObject(), warns::add);
    }

    @Test
    void defaultsReplayTheOldRollExactly() {
        for (FoalRules.Stat stat : FoalRules.Stat.values()) {
            RandomSource a = RandomSource.create(7L);
            RandomSource b = RandomSource.create(7L);
            double typical = switch (stat) {
                case SPEED -> 0.25D;
                case JUMP -> 0.8D;
                case HEALTH -> 20.0D;
            };
            for (int i = 0; i < 200; i++) {
                double p1 = typical * (0.8D + i * 0.002D);
                double p2 = typical;
                double old = Math.max(p1, p2) + (a.nextDouble() * 1.5D - 0.5D) / stat.perRaw();
                assertEquals(old, FoalRules.DEFAULTS.inherit(p1, p2, stat, b), 1e-12);
            }
        }
    }

    @Test
    void perRawMatchesTheOldScales() {
        assertEquals(43.2D, FoalRules.Stat.SPEED.perRaw(), 1e-12);
        assertEquals(6.0D, FoalRules.Stat.JUMP.perRaw(), 1e-12);
        assertEquals(1.0D, FoalRules.Stat.HEALTH.perRaw(), 1e-12);
    }

    @Test
    void averageMode() {
        FoalRules r = read("{\"inherit\": \"average\", \"health_variance\": [0, 0]}");
        assertEquals(25.0D, r.inherit(20.0D, 30.0D, FoalRules.Stat.HEALTH, RandomSource.create(1L)), 1e-12);
    }

    @Test
    void worstMode() {
        FoalRules r = read("{\"inherit\": \"worst\", \"health_variance\": [0, 0]}");
        assertEquals(20.0D, r.inherit(20.0D, 30.0D, FoalRules.Stat.HEALTH, RandomSource.create(1L)), 1e-12);
    }

    @Test
    void randomParentModeOnlyEverPicksAParent() {
        FoalRules r = read("{\"inherit\": \"random_parent\", \"speed_variance\": [0, 0]}");
        RandomSource rand = RandomSource.create(3L);
        boolean sawA = false;
        boolean sawB = false;
        for (int i = 0; i < 100; i++) {
            double v = r.inherit(0.2D, 0.3D, FoalRules.Stat.SPEED, rand);
            assertTrue(v == 0.2D || v == 0.3D);
            sawA |= v == 0.2D;
            sawB |= v == 0.3D;
        }
        assertTrue(sawA && sawB);
    }

    @Test
    void modeIsCaseInsensitive() {
        assertEquals(FoalRules.Mode.AVERAGE, read("{\"inherit\": \"AVERAGE\"}").mode());
        assertTrue(warns.isEmpty());
    }

    @Test
    void unknownModeKeepsBest() {
        assertEquals(FoalRules.Mode.BEST, read("{\"inherit\": \"strongest\"}").mode());
        assertTrue(warns.stream().anyMatch(w -> w.contains("inherit")));
    }

    @Test
    void varianceStaysInsideItsBounds() {
        FoalRules r = read("{\"speed_variance\": [-2, 3]}");
        RandomSource rand = RandomSource.create(9L);
        for (int i = 0; i < 2000; i++) {
            double v = r.inherit(0.25D, 0.2D, FoalRules.Stat.SPEED, rand);
            assertTrue(v >= 0.25D - 2.0D / 43.2D - 1e-12 && v <= 0.25D + 3.0D / 43.2D + 1e-12);
        }
    }

    @Test
    void varianceMinAboveMaxKeepsDefault() {
        FoalRules r = read("{\"jump_variance\": [2, 1]}");
        assertEquals(FoalRules.DEFAULTS.jumpMin(), r.jumpMin(), 1e-12);
        assertEquals(FoalRules.DEFAULTS.jumpMax(), r.jumpMax(), 1e-12);
        assertTrue(warns.stream().anyMatch(w -> w.contains("jump_variance")));
    }

    @Test
    void varianceNeedsTwoNumbers() {
        FoalRules r = read("{\"jump_variance\": [2], \"health_variance\": 3}");
        assertEquals(FoalRules.DEFAULTS.jumpMin(), r.jumpMin(), 1e-12);
        assertEquals(FoalRules.DEFAULTS.healthMax(), r.healthMax(), 1e-12);
        assertEquals(2, warns.size());
    }

    @Test
    void neverReturnsBelowZero() {
        FoalRules r = read("{\"inherit\": \"worst\", \"speed_variance\": [-50, -40]}");
        assertEquals(0.0D, r.inherit(0.1D, 0.2D, FoalRules.Stat.SPEED, RandomSource.create(1L)), 1e-12);
    }
}
