package icy.betterhorses.net.tuning;

import com.google.gson.JsonParser;
import icy.betterhorses.net.registry.ArchetypeType;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RangesTest {

    private static final ArchetypeType.Stats CLS = ArchetypeType.builder()
            .speed(11 / 43.2D, 15 / 43.2D).health(15.0D, 20.0D).jump(0.72D, 1.02D).stats();

    private final List<String> warns = new ArrayList<>();

    private Ranges read(String json) {
        return Ranges.read(JsonParser.parseString(json).getAsJsonObject(), warns::add);
    }

    @Test
    void breedWithoutRangesUsesTheClass() {
        Ranges r = read("{\"class\": \"race\", \"chest_rows\": 3}");
        assertTrue(r.empty());
        assertArrayEquals(new double[]{CLS.lowSpeed(), CLS.highSpeed()}, r.speed(CLS), 1e-12);
        assertTrue(warns.isEmpty());
    }

    @Test
    void halfARangeBorrowsTheOtherHalfFromTheClass() {
        Ranges r = read("{\"max_health\": 26}");
        assertArrayEquals(new double[]{15.0D, 26.0D}, r.health(CLS), 1e-12);
    }

    @Test
    void halfRangeThatCrossesTheClassFallsBack() {
        Ranges r = read("{\"max_health\": 10}");
        assertArrayEquals(new double[]{15.0D, 20.0D}, r.health(CLS), 1e-12);
    }

    @Test
    void backwardsRangeIsDroppedWithAWarning() {
        Ranges r = read("{\"min_speed\": 16, \"max_speed\": 12}");
        assertTrue(Double.isNaN(r.lowSpeed()) && Double.isNaN(r.highSpeed()));
        assertTrue(warns.stream().anyMatch(w -> w.contains("speed")));
    }

    @Test
    void rollsAndClampsUseTheBreedRange() {
        Ranges r = read("{\"min_speed\": 16, \"max_speed\": 18}");
        RandomSource rand = RandomSource.create(5L);
        for (int i = 0; i < 1000; i++) {
            double s = r.rollSpeed(CLS, rand);
            assertTrue(s >= 16 / 43.2D - 1e-12 && s <= 18 / 43.2D + 1e-12);
        }
        assertEquals(18 / 43.2D, r.clampSpeed(CLS, 1.0D), 1e-12);
        assertEquals(15.0D, r.clampHealth(CLS, 2.0D), 1e-12);
    }

    @Test
    void noneEqualsNoneDespiteNaN() {
        assertEquals(Ranges.NONE, new Ranges(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN));
    }
}
