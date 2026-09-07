package icy.betterhorses.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BhConfig {

    private static final String KEY_STABILIZER = "stabilizer";
    private static final String KEY_MEDKIT = "medkit";
    private static final String KEY_HITCHPOST = "hitchpost";
    private static final String KEY_HOOVES = "hooves";
    private static final String KEY_HORSE_EXCLUSIVITY = "horse_exclusivity";
    private static final String KEY_MULTI_RIDING = "multiriding";
    private static final String KEY_HORSE_COMBAT = "horse_combat";
    private static final String KEY_TRANSPARENT_HORSES = "transparent_horses";
    private static final String KEY_GENDER_BREEDING = "gender_breeding";
    private static final String KEY_ABILITIES = "abilities";
    private static final String KEY_CLASS_MASTER = "class_abilities";
    private static final String KEY_BREED_MASTER = "breed_abilities";
    private static final String KEY_CLASS_LIST = "class";
    private static final String KEY_BREED_LIST = "breed";

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(IcysBetterHorses.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static State state = State.defaults();
    private static @Nullable State ownState;
    private static @Nullable EnumMap<BhAbility, Boolean> ownAbilities;
    private static boolean ownClassMaster;
    private static boolean ownBreedMaster;
    private static final EnumMap<BhAbility, Boolean> abilities = new EnumMap<>(BhAbility.class);
    private static boolean classMaster = true;
    private static boolean breedMaster = true;

    static {
        resetAbilities();
    }

    private static void resetAbilities() {
        abilities.clear();
        for (BhAbility ability : BhAbility.values()) {
            abilities.put(ability, ability.fresh());
        }
        classMaster = true;
        breedMaster = true;
    }

    public static int packToggles() {
        int bits = 0;
        boolean[] flags = {state.stabilizer(), state.medkit(), state.hitchpost(), state.hooves(),
                state.horseExclusivity(), state.multiRiding(), state.horseCombat(),
                state.transparentHorses(), state.genderBreeding()};
        for (int i = 0; i < flags.length; i++) {
            if (flags[i]) {
                bits |= 1 << i;
            }
        }
        return bits;
    }

    public static int packMasters() {
        return (classMaster ? 1 : 0) | (breedMaster ? 2 : 0);
    }

    public static long packAbilities() {
        long bits = 0L;
        for (BhAbility ability : BhAbility.values()) {
            if (abilities.getOrDefault(ability, true)) {
                bits |= 1L << ability.ordinal();
            }
        }
        return bits;
    }

    public static synchronized void adoptServer(int toggles, int masters, long packed) {
        if (ownState == null) {
            ownState = state;
            ownAbilities = new EnumMap<>(abilities);
            ownClassMaster = classMaster;
            ownBreedMaster = breedMaster;
        }
        state = new State(
                (toggles & 1) != 0, (toggles & 2) != 0, (toggles & 4) != 0, (toggles & 8) != 0,
                (toggles & 16) != 0, (toggles & 32) != 0, (toggles & 64) != 0,
                (toggles & 128) != 0, (toggles & 256) != 0);
        classMaster = (masters & 1) != 0;
        breedMaster = (masters & 2) != 0;
        for (BhAbility ability : BhAbility.values()) {
            abilities.put(ability, (packed & (1L << ability.ordinal())) != 0L);
        }
    }

    public static synchronized void dropServer() {
        if (ownState == null || ownAbilities == null) {
            return;
        }
        state = ownState;
        abilities.clear();
        abilities.putAll(ownAbilities);
        classMaster = ownClassMaster;
        breedMaster = ownBreedMaster;
        ownState = null;
        ownAbilities = null;
    }

    public static boolean serverManaged() {
        return ownState != null;
    }

    private static void reportAbilities() {
        List<String> off = new ArrayList<>();
        for (BhAbility ability : BhAbility.values()) {
            if (!abilities.getOrDefault(ability, true)) {
                off.add(ability.key());
            }
        }
        IcysBetterHorses.LOGGER.info("Abilities: class master {}, breed master {}, disabled {}",
                yesNo(classMaster), yesNo(breedMaster), off.isEmpty() ? "none" : off);
    }

    public static boolean abilityEnabled(BhAbility ability) {
        if (abilities.isEmpty()) {
            return true;
        }
        boolean master = ability.classPerk() ? classMaster : breedMaster;
        return master && abilities.getOrDefault(ability, true);
    }

    public static boolean anyAbilityEnabled(HorseBreed breed) {
        if (!breedMaster) {
            return false;
        }
        for (BhAbility ability : BhAbility.values()) {
            if (ability.breed() == breed && abilities.getOrDefault(ability, true)) {
                return true;
            }
        }
        return false;
    }

    public static boolean classAbilitiesEnabled() {
        return classMaster;
    }

    public static boolean breedAbilitiesEnabled() {
        return breedMaster;
    }

    public static synchronized void applyAbilities(boolean classOn, boolean breedOn,
                                                   Map<BhAbility, Boolean> wanted) {
        if (ownAbilities != null) {
            ownClassMaster = classOn;
            ownBreedMaster = breedOn;
            ownAbilities.putAll(wanted);
        } else {
            classMaster = classOn;
            breedMaster = breedOn;
            abilities.putAll(wanted);
        }
        save();
    }

    public static Map<BhAbility, Boolean> abilities() {
        return Map.copyOf(abilities);
    }

    public static synchronized void load() {
        State defaults = State.defaults();
        if (!Files.exists(CONFIG_PATH)) {
            state = defaults;
            save();
            IcysBetterHorses.LOGGER.info("Created default config at {}", CONFIG_PATH);
            return;
        }

        boolean needsRewrite = false;
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!(parsed instanceof JsonObject root)) {
                throw new JsonParseException("Expected a top-level JSON object");
            }

            needsRewrite |= !root.has(KEY_STABILIZER);
            needsRewrite |= !root.has(KEY_MEDKIT);
            needsRewrite |= !root.has(KEY_HITCHPOST);
            needsRewrite |= !root.has(KEY_HOOVES);
            needsRewrite |= !root.has(KEY_HORSE_EXCLUSIVITY);
            needsRewrite |= !root.has(KEY_MULTI_RIDING);
            needsRewrite |= !root.has(KEY_HORSE_COMBAT);
            needsRewrite |= !root.has(KEY_TRANSPARENT_HORSES);
            needsRewrite |= !root.has(KEY_GENDER_BREEDING);
            needsRewrite |= !root.has(KEY_ABILITIES);

            state = new State(
                    readToggle(root, KEY_STABILIZER, defaults.stabilizer()),
                    readToggle(root, KEY_MEDKIT, defaults.medkit()),
                    readToggle(root, KEY_HITCHPOST, defaults.hitchpost()),
                    readToggle(root, KEY_HOOVES, defaults.hooves()),
                    readToggle(root, KEY_HORSE_EXCLUSIVITY, defaults.horseExclusivity()),
                    readToggle(root, KEY_MULTI_RIDING, defaults.multiRiding()),
                    readToggle(root, KEY_HORSE_COMBAT, defaults.horseCombat()),
                    readToggle(root, KEY_TRANSPARENT_HORSES, defaults.transparentHorses()),
                    readToggle(root, KEY_GENDER_BREEDING, defaults.genderBreeding()));

            resetAbilities();
            JsonObject section = root.getAsJsonObject(KEY_ABILITIES);
            if (section != null) {
                classMaster = readToggle(section, KEY_CLASS_MASTER, true);
                breedMaster = readToggle(section, KEY_BREED_MASTER, true);
                JsonObject perClass = section.getAsJsonObject(KEY_CLASS_LIST);
                JsonObject perBreed = section.getAsJsonObject(KEY_BREED_LIST);
                for (BhAbility ability : abilities.keySet()) {
                    JsonObject from = ability.classPerk() ? perClass : perBreed;
                    if (from != null) {
                        abilities.put(ability, readToggle(from, ability.key(), true));
                    }
                }
            }
        } catch (Exception exception) {
            state = defaults;
            resetAbilities();
            IcysBetterHorses.LOGGER.warn("Failed to load config from {}. Using defaults for this run; "
                    + "the file is left as it is so nothing you set is lost.", CONFIG_PATH, exception);
            return;
        }

        if (needsRewrite) {
            save();
        }

        IcysBetterHorses.LOGGER.info("Loaded config from {}", CONFIG_PATH);
        reportAbilities();
    }

    public static boolean stabilizerEnabled() {
        return state.stabilizer();
    }

    public static boolean medkitEnabled() {
        return state.medkit();
    }

    public static boolean hitchpostEnabled() {
        return state.hitchpost();
    }

    public static boolean hoovesEnabled() {
        return state.hooves();
    }

    public static boolean horseExclusivityEnabled() {
        return state.horseExclusivity();
    }

    public static boolean multiRidingEnabled() {
        return state.multiRiding();
    }

    public static boolean horseCombatEnabled() {
        return state.horseCombat();
    }

    public static boolean transparentHorsesEnabled() {
        return state.transparentHorses();
    }

    public static boolean genderBreedingEnabled() {
        return state.genderBreeding();
    }

    public static synchronized void apply(boolean stabilizer, boolean medkit, boolean hitchpost,
                                          boolean hooves, boolean horseExclusivity, boolean multiRiding,
                                          boolean horseCombat,
                                          boolean transparentHorses, boolean genderBreeding) {
        State edited = new State(stabilizer, medkit, hitchpost, hooves, horseExclusivity, multiRiding,
                horseCombat, transparentHorses, genderBreeding);
        if (ownState != null) {
            ownState = edited;
        } else {
            state = edited;
        }
        save();
    }

    private static boolean readToggle(JsonObject root, String key, boolean defaultValue) {
        if (!root.has(key)) {
            return defaultValue;
        }

        JsonElement element = root.get(key);
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }

        if (!element.isJsonPrimitive()) {
            IcysBetterHorses.LOGGER.warn("Config key '{}' must be yes/no or true/false. Using default {}.",
                    key, yesNo(defaultValue));
            return defaultValue;
        }

        if (element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }

        if (element.getAsJsonPrimitive().isNumber()) {
            return element.getAsInt() != 0;
        }

        String normalized = element.getAsString().trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "yes", "true", "on", "1", "enabled" -> true;
            case "no", "false", "off", "0", "disabled" -> false;
            default -> {
                IcysBetterHorses.LOGGER.warn("Config key '{}' had unknown value '{}'. Using default {}.",
                        key, element.getAsString(), yesNo(defaultValue));
                yield defaultValue;
            }
        };
    }

    private static synchronized void save() {
        State mine = ownState != null ? ownState : state;
        Map<BhAbility, Boolean> mineAbilities = ownAbilities != null ? ownAbilities : abilities;
        boolean mineClass = ownState != null ? ownClassMaster : classMaster;
        boolean mineBreed = ownState != null ? ownBreedMaster : breedMaster;

        JsonObject root = new JsonObject();
        root.addProperty(KEY_STABILIZER, yesNo(mine.stabilizer()));
        root.addProperty(KEY_MEDKIT, yesNo(mine.medkit()));
        root.addProperty(KEY_HITCHPOST, yesNo(mine.hitchpost()));
        root.addProperty(KEY_HOOVES, yesNo(mine.hooves()));
        root.addProperty(KEY_HORSE_EXCLUSIVITY, yesNo(mine.horseExclusivity()));
        root.addProperty(KEY_MULTI_RIDING, yesNo(mine.multiRiding()));
        root.addProperty(KEY_HORSE_COMBAT, yesNo(mine.horseCombat()));
        root.addProperty(KEY_TRANSPARENT_HORSES, yesNo(mine.transparentHorses()));
        root.addProperty(KEY_GENDER_BREEDING, yesNo(mine.genderBreeding()));

        JsonObject perClass = new JsonObject();
        JsonObject perBreed = new JsonObject();
        for (Map.Entry<BhAbility, Boolean> entry : mineAbilities.entrySet()) {
            JsonObject into = entry.getKey().classPerk() ? perClass : perBreed;
            into.addProperty(entry.getKey().key(), yesNo(entry.getValue()));
        }
        JsonObject section = new JsonObject();
        section.addProperty(KEY_CLASS_MASTER, yesNo(mineClass));
        section.addProperty(KEY_BREED_MASTER, yesNo(mineBreed));
        section.add(KEY_CLASS_LIST, perClass);
        section.add(KEY_BREED_LIST, perBreed);
        root.add(KEY_ABILITIES, section);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException exception) {
            IcysBetterHorses.LOGGER.warn("Failed to save config to {}", CONFIG_PATH, exception);
        }
    }

    private static String yesNo(boolean enabled) {
        return enabled ? "yes" : "no";
    }

    private record State(
            boolean stabilizer,
            boolean medkit,
            boolean hitchpost,
            boolean hooves,
            boolean horseExclusivity,
            boolean multiRiding,
            boolean horseCombat,
            boolean transparentHorses,
            boolean genderBreeding) {

        private static State defaults() {
            return new State(true, true, true, true, true, true, true, true, true);
        }
    }

    private BhConfig() {}
}
