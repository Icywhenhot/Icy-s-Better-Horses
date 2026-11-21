package icy.betterhorses.net.tuning;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import icy.betterhorses.net.BhJumpHeight;
import icy.betterhorses.net.registry.ArchetypeType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassFileTest {

    private static final ArchetypeType.Stats BASE = ArchetypeType.builder()
            .speed(0.2546D, 0.3472D).health(15.0D, 20.0D).jump(0.72D, 1.02D)
            .bashDamage(0.6D).bashKnockback(0.8D)
            .stats();

    private final List<String> warns = new ArrayList<>();

    private ArchetypeType.Stats read(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        return ClassFile.read(obj, BASE, warns::add);
    }

    private boolean warned(String bit) {
        return warns.stream().anyMatch(w -> w.contains(bit));
    }

    @Test
    void emptyFileKeepsFallback() {
        assertEquals(BASE, read("{}"));
        assertTrue(warns.isEmpty());
    }

    @Test
    void everyKeyIsRead() {
        ArchetypeType.Stats s = read("""
                {"min_speed": 8.64, "max_speed": 17.28, "min_health": 10, "max_health": 60,
                 "min_jump": 2, "max_jump": 6, "bash_damage": 3, "bash_knockback": 0.5,
                 "spook_chance": [0.3, 0.2, 0.1], "chest_rows": 5, "chest_and_riders": true,
                 "large_cart": true, "steady": true, "medkit_multiplier": 3,
                 "knockback_resistance": 0.4, "heal_interval": 200, "powder_snow": true,
                 "road_bonus": [0.05, 0.1, 0.2], "fall_damage_waiver": 6, "step_height": 1.6,
                 "armor_multiplier": -0.5}
                """);
        assertTrue(warns.isEmpty(), warns.toString());
        assertEquals(0.2D, s.lowSpeed(), 1e-9);
        assertEquals(0.4D, s.highSpeed(), 1e-9);
        assertEquals(10.0D, s.lowHealth(), 1e-9);
        assertEquals(60.0D, s.highHealth(), 1e-9);
        assertEquals(2.0D, BhJumpHeight.blocks(s.lowJump()), 1e-6);
        assertEquals(6.0D, BhJumpHeight.blocks(s.highJump()), 1e-6);
        assertEquals(3.0D, s.bashDamage(), 1e-9);
        assertEquals(0.5D, s.bashKnockback(), 1e-9);
        assertEquals(0.3D, s.spook0(), 1e-9);
        assertEquals(0.2D, s.spook1(), 1e-9);
        assertEquals(0.1D, s.spook2(), 1e-9);
        assertEquals(5, s.chestRows());
        assertTrue(s.chestAndRiders());
        assertTrue(s.largeCart());
        assertTrue(s.steady());
        assertEquals(3, s.medkitMultiplier());
        assertEquals(0.4D, s.knockbackResistance(), 1e-9);
        assertEquals(200, s.healInterval());
        assertTrue(s.powderSnow());
        assertEquals(0.05D, s.road0(), 1e-9);
        assertEquals(0.1D, s.road1(), 1e-9);
        assertEquals(0.2D, s.road2(), 1e-9);
        assertEquals(6.0D, s.fallWaiver(), 1e-9);
        assertEquals(1.6D, s.stepHeight(), 1e-9);
        assertEquals(-0.5D, s.armor(), 1e-9);
    }

    @Test
    void partialFileOnlyTouchesItsKeys() {
        ArchetypeType.Stats s = read("{\"max_health\": 22}");
        assertEquals(22.0D, s.highHealth(), 1e-9);
        assertEquals(BASE.lowHealth(), s.lowHealth(), 1e-9);
        assertEquals(BASE.lowSpeed(), s.lowSpeed(), 1e-9);
        assertEquals(BASE.spook1(), s.spook1(), 1e-9);
    }

    @Test
    void singleSpookNumberScalesLikeBuiltIn() {
        ArchetypeType.Stats s = read("{\"spook_chance\": 0.2}");
        assertEquals(0.2D, s.spook0(), 1e-9);
        assertEquals(0.14D, s.spook1(), 1e-9);
        assertEquals(0.1D, s.spook2(), 1e-9);
    }

    @Test
    void shortArrayRepeatsItsLastValue() {
        ArchetypeType.Stats s = read("{\"road_bonus\": [0.1, 0.3]}");
        assertEquals(0.1D, s.road0(), 1e-9);
        assertEquals(0.3D, s.road1(), 1e-9);
        assertEquals(0.3D, s.road2(), 1e-9);
        assertTrue(warns.isEmpty());
    }

    @Test
    void longArrayIsCutWithAWarning() {
        ArchetypeType.Stats s = read("{\"road_bonus\": [0.1, 0.2, 0.3, 0.4]}");
        assertEquals(0.3D, s.road2(), 1e-9);
        assertTrue(warned("road_bonus"));
    }

    @Test
    void emptyArrayKeepsFallback() {
        ArchetypeType.Stats s = read("{\"road_bonus\": []}");
        assertEquals(BASE.road0(), s.road0(), 1e-9);
        assertTrue(warned("road_bonus"));
    }

    @Test
    void minAboveMaxKeepsBothOldValues() {
        ArchetypeType.Stats s = read("{\"min_speed\": 15, \"max_speed\": 10, \"min_health\": 5}");
        assertEquals(BASE.lowSpeed(), s.lowSpeed(), 1e-9);
        assertEquals(BASE.highSpeed(), s.highSpeed(), 1e-9);
        assertEquals(5.0D, s.lowHealth(), 1e-9);
        assertTrue(warned("speed"));
    }

    @Test
    void minAboveFallbackMaxIsCaughtToo() {
        ArchetypeType.Stats s = read("{\"min_health\": 25}");
        assertEquals(BASE.lowHealth(), s.lowHealth(), 1e-9);
        assertEquals(BASE.highHealth(), s.highHealth(), 1e-9);
        assertTrue(warned("health"));
    }

    @Test
    void equalMinAndMaxIsFine() {
        ArchetypeType.Stats s = read("{\"min_health\": 20, \"max_health\": 20}");
        assertEquals(20.0D, s.lowHealth(), 1e-9);
        assertEquals(20.0D, s.highHealth(), 1e-9);
        assertTrue(warns.isEmpty());
    }

    @Test
    void outOfRangeValuesAreClamped() {
        ArchetypeType.Stats s = read("""
                {"min_health": -4, "max_health": 99999, "max_speed": 500, "spook_chance": 3,
                 "chest_rows": 40, "knockback_resistance": 2, "step_height": -1, "medkit_multiplier": 0}
                """);
        assertEquals(1.0D, s.lowHealth(), 1e-9);
        assertEquals(1024.0D, s.highHealth(), 1e-9);
        assertEquals(100.0D / 43.2D, s.highSpeed(), 1e-9);
        assertEquals(1.0D, s.spook0(), 1e-9);
        assertEquals(6, s.chestRows());
        assertEquals(1.0D, s.knockbackResistance(), 1e-9);
        assertEquals(0.0D, s.stepHeight(), 1e-9);
        assertEquals(1, s.medkitMultiplier());
        assertTrue(warned("min_health"));
        assertTrue(warned("chest_rows"));
    }

    @Test
    void wrongTypesFallBackPerKey() {
        ArchetypeType.Stats s = read("""
                {"max_health": "lots", "steady": "yes", "chest_rows": {"a": 1}, "bash_damage": 2}
                """);
        assertEquals(BASE.highHealth(), s.highHealth(), 1e-9);
        assertFalse(s.steady());
        assertEquals(BASE.chestRows(), s.chestRows());
        assertEquals(2.0D, s.bashDamage(), 1e-9);
        assertTrue(warned("max_health"));
        assertTrue(warned("steady"));
        assertTrue(warned("chest_rows"));
    }

    @Test
    void nanAndInfinityAreRejected() {
        ArchetypeType.Stats s = read("{\"bash_damage\": NaN, \"max_health\": Infinity, \"min_health\": -Infinity}");
        assertEquals(BASE.bashDamage(), s.bashDamage(), 1e-9);
        assertEquals(BASE.highHealth(), s.highHealth(), 1e-9);
        assertEquals(BASE.lowHealth(), s.lowHealth(), 1e-9);
        assertTrue(warned("bash_damage"));
    }

    @Test
    void fractionalWholeNumbersRoundWithAWarning() {
        ArchetypeType.Stats s = read("{\"chest_rows\": 4.6, \"heal_interval\": 99.4}");
        assertEquals(5, s.chestRows());
        assertEquals(99, s.healInterval());
        assertTrue(warned("chest_rows"));
    }

    @Test
    void unknownKeysAreReported() {
        read("{\"max_sped\": 20}");
        assertTrue(warned("max_sped"));
    }

    @Test
    void nullValueKeepsFallback() {
        ArchetypeType.Stats s = read("{\"max_health\": null}");
        assertEquals(BASE.highHealth(), s.highHealth(), 1e-9);
        assertTrue(warned("max_health"));
    }

    @Test
    void jumpBeyondTheSolverCeilingIsClamped() {
        ArchetypeType.Stats s = read("{\"max_jump\": 500}");
        assertTrue(s.highJump() <= 3.0D);
        assertTrue(warned("max_jump"));
    }
}
