package icy.betterhorses.net.tuning;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TuneTest {

    private static final Tune FLAT = Tune.of("tune_test", "radius", 12.0D).within(0.0D, 64.0D);
    private static final Tune TIERED = Tune.of("tune_test", "bonus", 0.35D, 0.50D, 0.50D).within(0.0D, 10.0D);
    private static final Tune TICKS = Tune.of("tune_test", "cooldown", 100.0D, 100.0D, 60.0D).within(0.0D, 72000.0D);

    private final List<String> warns = new ArrayList<>();

    private void load(String json) {
        Tune.load("tune_test", JsonParser.parseString(json).getAsJsonObject(), warns::add);
    }

    @AfterEach
    void reset() {
        Tune.resetAll();
    }

    @Test
    void defaultsBeforeAnyLoad() {
        assertEquals(12.0D, FLAT.get(), 1e-12);
        assertEquals(12.0D, FLAT.at(2), 1e-12);
        assertEquals(0.35D, TIERED.at(0), 1e-12);
        assertEquals(0.50D, TIERED.at(1), 1e-12);
        assertEquals(60, TICKS.ticks(2));
    }

    @Test
    void tierIndexIsClamped() {
        assertEquals(0.35D, TIERED.at(-1), 1e-12);
        assertEquals(0.50D, TIERED.at(9), 1e-12);
    }

    @Test
    void numberSetsEveryTier() {
        load("{\"bonus\": 0.8}");
        assertEquals(0.8D, TIERED.at(0), 1e-12);
        assertEquals(0.8D, TIERED.at(2), 1e-12);
    }

    @Test
    void arraySetsPerTierAndRepeatsTheLast() {
        load("{\"bonus\": [0.1, 0.4]}");
        assertEquals(0.1D, TIERED.at(0), 1e-12);
        assertEquals(0.4D, TIERED.at(1), 1e-12);
        assertEquals(0.4D, TIERED.at(2), 1e-12);
    }

    @Test
    void clampedToItsRange() {
        load("{\"radius\": 9000, \"bonus\": -3}");
        assertEquals(64.0D, FLAT.get(), 1e-12);
        assertEquals(0.0D, TIERED.at(1), 1e-12);
        assertEquals(2, warns.size());
    }

    @Test
    void ticksRound() {
        load("{\"cooldown\": [10.6, 20.2, 30.5]}");
        assertEquals(11, TICKS.ticks(0));
        assertEquals(20, TICKS.ticks(1));
        assertEquals(31, TICKS.ticks(2));
    }

    @Test
    void unknownKeyWarns() {
        load("{\"raduis\": 3}");
        assertTrue(warns.stream().anyMatch(w -> w.contains("raduis")));
        assertEquals(12.0D, FLAT.get(), 1e-12);
    }

    @Test
    void badValueKeepsDefault() {
        load("{\"radius\": \"far\", \"bonus\": [1, \"x\"]}");
        assertEquals(12.0D, FLAT.get(), 1e-12);
        assertEquals(0.35D, TIERED.at(0), 1e-12);
        assertEquals(2, warns.size());
    }

    @Test
    void resetRestoresDefaults() {
        load("{\"radius\": 3}");
        assertEquals(3.0D, FLAT.get(), 1e-12);
        Tune.resetAll();
        assertEquals(12.0D, FLAT.get(), 1e-12);
    }

    @Test
    void loadingAFileResetsItsOtherKeys() {
        load("{\"radius\": 3}");
        load("{\"bonus\": 1}");
        assertEquals(12.0D, FLAT.get(), 1e-12);
    }

    @Test
    void duplicateDeclarationIsRejected() {
        try {
            Tune.of("tune_test", "radius", 1.0D);
        } catch (IllegalStateException expected) {
            return;
        }
        throw new AssertionError("duplicate tune allowed");
    }

    @Test
    void unknownGroupIsReported() {
        assertTrue(Tune.knows("tune_test"));
        assertTrue(!Tune.knows("no_such_ability"));
    }
}
