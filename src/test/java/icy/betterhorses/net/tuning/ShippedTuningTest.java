package icy.betterhorses.net.tuning;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import icy.betterhorses.net.BhJumpHeight;
import icy.betterhorses.net.registry.ArchetypeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShippedTuningTest {

    private static final Path DIR = Path.of("src/main/resources/data/icys-better-horses/better_horses");

    private static final ArchetypeType.Stats BLANK = new ArchetypeType.Stats(
            0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, false, false, false, 1, 0, 0, false, 0, 0, 0, 0, 0, 0);

    private static final Map<String, ArchetypeType.Stats> EXPECTED = new TreeMap<>();

    @BeforeAll
    static void setup() {
        EXPECTED.put("race", ArchetypeType.builder()
                .speed(11 / 43.2D, 15 / 43.2D).health(15.0D, 20.0D).jump(0.72D, 1.02D)
                .bashDamage(0.6D).bashKnockback(0.8D).defaultChestRows(3).stats());
        EXPECTED.put("war", ArchetypeType.builder()
                .speed(9 / 43.2D, 15 / 43.2D).health(25.0D, 40.0D).jump(0.57D, 0.97D)
                .bashDamage(1.5D).bashKnockback(1.2D).defaultChestRows(3)
                .suppressesRear(true).medkitMultiplier(2).stats());
        EXPECTED.put("western", ArchetypeType.builder()
                .speed(8 / 43.2D, 15 / 43.2D).health(15.0D, 30.0D).jump(0.57D, 0.84D)
                .bashDamage(1.0D).bashKnockback(1.0D).defaultChestRows(3)
                .pathSpeedBonus(0.10D, 0.20D, 0.50D).stats());
        EXPECTED.put("draft", ArchetypeType.builder()
                .speed(7 / 43.2D, 10 / 43.2D).health(35.0D, 50.0D).jump(0.38D, 0.72D)
                .bashDamage(1.25D).bashKnockback(2.0D).defaultChestRows(4).allowsChestAndRiders(true)
                .baseSpookChance(0.05D).knockbackResistance(0.6D).allowsLargeCart(true).stats());
        EXPECTED.put("pony", ArchetypeType.builder()
                .speed(8 / 43.2D, 13 / 43.2D).health(25.0D, 40.0D).jump(0.57D, 0.84D)
                .bashDamage(0.75D).bashKnockback(0.8D).defaultChestRows(3)
                .passiveHealInterval(400).walksOnPowderSnow(true).fallDamageWaiver(15.0D).stepHeight(2.0D).stats());
        EXPECTED.put("donkey", ArchetypeType.builder()
                .speed(0.1852D, 0.2778D).health(35.0D, 50.0D).jump(0.545D, 0.803D)
                .bashDamage(1.5D).bashKnockback(1.0D).defaultChestRows(4)
                .baseSpookChance(0.0D).stepHeight(2.0D).allowsLargeCart(true).armor(-0.25D).stats());
        EXPECTED.put("mule", ArchetypeType.builder()
                .speed(0.1620D, 0.3472D).health(15.0D, 50.0D).jump(0.38D, 1.02D)
                .bashDamage(1.25D).bashKnockback(1.0D).defaultChestRows(4).fallDamageWaiver(8.0D).stats());
        EXPECTED.put("none", ArchetypeType.builder()
                .speed(0.1125D, 0.3375D).health(15.0D, 30.0D).jump(0.40D, 1.00D)
                .bashDamage(1.0D).bashKnockback(1.0D).defaultChestRows(3).baseSpookChance(0.0D).stats());
        EXPECTED.put("skeleton", ArchetypeType.builder()
                .speed(10.0D / 43.2D, 17.0D / 43.2D).health(15.0D, 20.0D)
                .jump(BhJumpHeight.strength(3.0D), BhJumpHeight.strength(5.0D))
                .bashDamage(0.75D).bashKnockback(1.0D).defaultChestRows(3).baseSpookChance(0.0D).stats());
        EXPECTED.put("zombie", ArchetypeType.builder()
                .speed(8.0D / 43.2D, 11.0D / 43.2D).health(39.0D, 54.0D)
                .jump(BhJumpHeight.strength(2.0D), BhJumpHeight.strength(4.0D))
                .bashDamage(1.5D).bashKnockback(1.0D).defaultChestRows(3).baseSpookChance(0.0D).stats());
        EXPECTED.put("wild", ArchetypeType.builder()
                .speed(7.0D / 43.2D, 14.0D / 43.2D).health(25.0D, 50.0D).jump(0.45D, 0.90D)
                .bashDamage(1.5D).bashKnockback(1.2D).defaultChestRows(3).baseSpookChance(0.20D).stats());
    }

    private static JsonObject json(Path p) throws IOException {
        return JsonParser.parseString(Files.readString(p)).getAsJsonObject();
    }

    private static List<String> names(String sub) throws IOException {
        try (Stream<Path> s = Files.list(DIR.resolve(sub))) {
            return s.map(p -> p.getFileName().toString().replace(".json", "")).sorted().toList();
        }
    }

    @Test
    void everyClassHasAFile() throws IOException {
        assertEquals(List.copyOf(EXPECTED.keySet()), names("class"));
    }

    @Test
    void classFilesMatchTheBuiltInTable() throws IOException {
        for (Map.Entry<String, ArchetypeType.Stats> e : EXPECTED.entrySet()) {
            List<String> warns = new ArrayList<>();
            ArchetypeType.Stats got = ClassFile.read(json(DIR.resolve("class/" + e.getKey() + ".json")), BLANK, warns::add);
            ArchetypeType.Stats want = e.getValue();
            String n = e.getKey();
            assertTrue(warns.isEmpty(), n + " " + warns);
            assertEquals(want.lowSpeed(), got.lowSpeed(), 1e-6, n);
            assertEquals(want.highSpeed(), got.highSpeed(), 1e-6, n);
            assertEquals(want.lowHealth(), got.lowHealth(), 1e-9, n);
            assertEquals(want.highHealth(), got.highHealth(), 1e-9, n);
            assertEquals(want.lowJump(), got.lowJump(), 1e-5, n);
            assertEquals(want.highJump(), got.highJump(), 1e-5, n);
            assertEquals(want.bashDamage(), got.bashDamage(), 1e-9, n);
            assertEquals(want.bashKnockback(), got.bashKnockback(), 1e-9, n);
            assertEquals(want.spook0(), got.spook0(), 1e-9, n);
            assertEquals(want.spook1(), got.spook1(), 1e-9, n);
            assertEquals(want.spook2(), got.spook2(), 1e-9, n);
            assertEquals(want.chestRows(), got.chestRows(), n);
            assertEquals(want.chestAndRiders(), got.chestAndRiders(), n);
            assertEquals(want.largeCart(), got.largeCart(), n);
            assertEquals(want.steady(), got.steady(), n);
            assertEquals(want.medkitMultiplier(), got.medkitMultiplier(), n);
            assertEquals(want.knockbackResistance(), got.knockbackResistance(), 1e-9, n);
            assertEquals(want.healInterval(), got.healInterval(), n);
            assertEquals(want.powderSnow(), got.powderSnow(), n);
            assertEquals(want.road0(), got.road0(), 1e-9, n);
            assertEquals(want.road1(), got.road1(), 1e-9, n);
            assertEquals(want.road2(), got.road2(), 1e-9, n);
            assertEquals(want.fallWaiver(), got.fallWaiver(), 1e-9, n);
            assertEquals(want.stepHeight(), got.stepHeight(), 1e-9, n);
            assertEquals(want.armor(), got.armor(), 1e-9, n);
        }
    }

    @Test
    void bondFileIsTheDefaults() throws IOException {
        List<String> warns = new ArrayList<>();
        JsonObject obj = json(DIR.resolve("bond.json"));
        assertEquals(BondRules.DEFAULTS, BondRules.read(obj, warns::add));
        assertTrue(warns.isEmpty(), warns.toString());
        assertEquals(BondRules.class.getRecordComponents().length, obj.size());
    }

    @Test
    void breedingFileIsTheDefaults() throws IOException {
        List<String> warns = new ArrayList<>();
        JsonObject obj = json(DIR.resolve("breeding.json"));
        assertEquals(FoalRules.DEFAULTS, FoalRules.read(obj, warns::add));
        assertTrue(warns.isEmpty(), warns.toString());
        assertEquals(4, obj.size());
    }

    private static final Pattern TUNE = Pattern.compile("Tune\\.of\\(\"([a-z_]+)\",\\s*\"([a-z_]+)\",\\s*([^)]*)\\)");

    private static Map<String, Map<String, double[]>> declared() throws IOException {
        Map<String, Map<String, double[]>> out = new TreeMap<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                if (p.getFileName().toString().equals("Tune.java")) {
                    continue;
                }
                Matcher m = TUNE.matcher(Files.readString(p));
                while (m.find()) {
                    String[] raw = m.group(3).split(",");
                    double[] full = new double[3];
                    for (int i = 0; i < 3; i++) {
                        full[i] = Double.parseDouble(raw[Math.min(i, raw.length - 1)].trim().replaceAll("[DdFf]$", ""));
                    }
                    double[] prev = out.computeIfAbsent(m.group(1), g -> new TreeMap<>()).put(m.group(2), full);
                    assertTrue(prev == null, "declared twice " + m.group(1) + "/" + m.group(2));
                }
            }
        }
        return out;
    }

    @Test
    void sourceDeclaresTunes() throws IOException {
        assertTrue(declared().size() >= 20);
    }

    @Test
    void abilityFilesMatchEveryTune() throws IOException {
        Map<String, Map<String, double[]>> all = declared();
        assertEquals(List.copyOf(all.keySet()), names("ability"));
        for (Map.Entry<String, Map<String, double[]>> g : all.entrySet()) {
            JsonObject obj = json(DIR.resolve("ability/" + g.getKey() + ".json"));
            assertEquals(g.getValue().keySet(), new HashSet<>(obj.keySet()), g.getKey());
            for (Map.Entry<String, double[]> t : g.getValue().entrySet()) {
                List<String> warns = new ArrayList<>();
                double[] got = Fields.tiers(t.getKey(), obj.get(t.getKey()), new double[]{-1, -1, -1},
                        -Double.MAX_VALUE, Double.MAX_VALUE, warns::add);
                assertTrue(warns.isEmpty(), g.getKey() + "/" + t.getKey() + " " + warns);
                for (int i = 0; i < 3; i++) {
                    assertEquals(t.getValue()[i], got[i], 1e-9, g.getKey() + "/" + t.getKey() + " tier " + i);
                }
            }
        }
    }

    @Test
    void everyTuneHolderIsListed() throws IOException {
        Set<String> listed = new HashSet<>();
        for (Class<?> c : TuneHolders.ALL) {
            listed.add(c.getSimpleName());
        }
        Set<String> holders = new HashSet<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                String name = p.getFileName().toString().replace(".java", "");
                if (!name.equals("Tune") && TUNE.matcher(Files.readString(p)).find()) {
                    holders.add(name);
                }
            }
        }
        assertEquals(holders, listed);
    }
}
