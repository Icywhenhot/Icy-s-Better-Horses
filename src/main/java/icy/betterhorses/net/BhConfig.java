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

    private static final String KEY_ABILITIES = "abilities";
    private static final String KEY_CLASS_MASTER = "class_abilities";
    private static final String KEY_BREED_MASTER = "breed_abilities";
    private static final String KEY_CLASS_LIST = "class";
    private static final String KEY_BREED_LIST = "breed";
    private static final String KEY_TUNING = "tuning";
    private static final String KEY_BOND_AMOUNT = "bond_per_interval";
    private static final String KEY_BOND_MINUTES = "bond_interval_minutes";
    private static final String KEY_SPAWN_WEIGHT = "spawn_weight";
    private static final String KEY_GROUP_MIN = "spawn_group_min";
    private static final String KEY_GROUP_MAX = "spawn_group_max";
    private static final String KEY_SPAWN_FLOOR = "spawn_probability_floor";

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(IcysBetterHorses.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static final EnumMap<BhFeature, Boolean> features = new EnumMap<>(BhFeature.class);
    private static final EnumMap<BhAbility, Boolean> abilities = new EnumMap<>(BhAbility.class);
    private static BhTuning tuning = BhTuning.defaults();
    private static boolean classMaster = true;
    private static boolean breedMaster = true;

    private static @Nullable EnumMap<BhFeature, Boolean> ownFeatures;
    private static @Nullable EnumMap<BhAbility, Boolean> ownAbilities;
    private static @Nullable BhTuning ownTuning;
    private static boolean ownClassMaster;
    private static boolean ownBreedMaster;

    static {
        reset();
    }

    private static void reset() {
        features.clear();
        for (BhFeature feature : BhFeature.values()) {
            features.put(feature, true);
        }
        abilities.clear();
        for (BhAbility ability : BhAbility.values()) {
            abilities.put(ability, ability.fresh());
        }
        tuning = BhTuning.defaults();
        classMaster = true;
        breedMaster = true;
    }

    public static boolean featureEnabled(BhFeature feature) {
        return features.getOrDefault(feature, true);
    }

    public static BhTuning tuning() {
        return tuning;
    }

    public static List<String> disabledFeatures() {
        List<String> off = new ArrayList<>();
        for (BhFeature feature : BhFeature.values()) {
            if (!featureEnabled(feature)) off.add(feature.key());
        }
        return off;
    }

    public static List<String> disabledAbilities() {
        List<String> off = new ArrayList<>();
        for (BhAbility ability : BhAbility.values()) {
            if (!abilities.getOrDefault(ability, true)) off.add(ability.key());
        }
        return off;
    }

    public static synchronized void adoptServer(List<String> offFeatures, boolean classOn, boolean breedOn,
                                                List<String> offAbilities, BhTuning serverTuning) {
        if (ownFeatures == null) {
            ownFeatures = new EnumMap<>(features);
            ownAbilities = new EnumMap<>(abilities);
            ownTuning = tuning;
            ownClassMaster = classMaster;
            ownBreedMaster = breedMaster;
        }
        for (BhFeature feature : BhFeature.values()) {
            features.put(feature, !offFeatures.contains(feature.key()));
        }
        for (BhAbility ability : BhAbility.values()) {
            abilities.put(ability, !offAbilities.contains(ability.key()));
        }
        tuning = serverTuning.clamped();
        classMaster = classOn;
        breedMaster = breedOn;
    }

    public static synchronized void dropServer() {
        if (ownFeatures == null || ownAbilities == null || ownTuning == null) {
            return;
        }
        features.clear();
        features.putAll(ownFeatures);
        abilities.clear();
        abilities.putAll(ownAbilities);
        tuning = ownTuning;
        classMaster = ownClassMaster;
        breedMaster = ownBreedMaster;
        ownFeatures = null;
        ownAbilities = null;
        ownTuning = null;
    }

    public static boolean serverManaged() {
        return ownFeatures != null;
    }

    private static void reportAbilities() {
        List<String> off = disabledAbilities();
        IcysBetterHorses.LOGGER.info("Abilities: class master {}, breed master {}, disabled {}",
                yesNo(classMaster), yesNo(breedMaster), off.isEmpty() ? "none" : off);
    }

    public static boolean abilityEnabled(BhAbility ability) {
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

    public static Map<BhFeature, Boolean> featureView() {
        return Map.copyOf(ownFeatures != null ? ownFeatures : features);
    }

    public static BhTuning tuningView() {
        return ownTuning != null ? ownTuning : tuning;
    }

    public static synchronized void apply(Map<BhFeature, Boolean> wanted, BhTuning edited) {
        BhTuning safe = edited.clamped();
        if (ownFeatures != null) {
            ownFeatures.putAll(wanted);
            ownTuning = safe;
        } else {
            features.putAll(wanted);
            tuning = safe;
        }
        save();
    }

    public static synchronized void load() {
        if (!Files.exists(CONFIG_PATH)) {
            reset();
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

            reset();
            for (BhFeature feature : BhFeature.values()) {
                needsRewrite |= !root.has(feature.key());
                features.put(feature, readToggle(root, feature.key(), true));
            }

            needsRewrite |= !root.has(KEY_ABILITIES);
            JsonObject section = root.getAsJsonObject(KEY_ABILITIES);
            if (section != null) {
                classMaster = readToggle(section, KEY_CLASS_MASTER, true);
                breedMaster = readToggle(section, KEY_BREED_MASTER, true);
                JsonObject perClass = section.getAsJsonObject(KEY_CLASS_LIST);
                JsonObject perBreed = section.getAsJsonObject(KEY_BREED_LIST);
                for (BhAbility ability : abilities.keySet()) {
                    JsonObject from = ability.classPerk() ? perClass : perBreed;
                    if (from != null) {
                        abilities.put(ability, readToggle(from, ability.key(), ability.fresh()));
                    }
                }
            }

            needsRewrite |= !root.has(KEY_TUNING);
            JsonObject numbers = root.getAsJsonObject(KEY_TUNING);
            BhTuning fallback = BhTuning.defaults();
            if (numbers != null) {
                tuning = new BhTuning(
                        readInt(numbers, KEY_BOND_AMOUNT, fallback.bondAmount()),
                        readInt(numbers, KEY_BOND_MINUTES, fallback.bondMinutes()),
                        readInt(numbers, KEY_SPAWN_WEIGHT, fallback.spawnWeight()),
                        readInt(numbers, KEY_GROUP_MIN, fallback.groupMin()),
                        readInt(numbers, KEY_GROUP_MAX, fallback.groupMax()),
                        readDouble(numbers, KEY_SPAWN_FLOOR, fallback.spawnFloor())).clamped();
            }
        } catch (Exception exception) {
            reset();
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
        return featureEnabled(BhFeature.STABILIZER);
    }

    public static boolean medkitEnabled() {
        return featureEnabled(BhFeature.MEDKIT);
    }

    public static boolean hitchpostEnabled() {
        return featureEnabled(BhFeature.HITCHPOST);
    }

    public static boolean hoovesEnabled() {
        return featureEnabled(BhFeature.HOOVES);
    }

    public static boolean horseExclusivityEnabled() {
        return featureEnabled(BhFeature.HORSE_EXCLUSIVITY);
    }

    public static boolean multiRidingEnabled() {
        return featureEnabled(BhFeature.MULTI_RIDING);
    }

    public static boolean horseCombatEnabled() {
        return featureEnabled(BhFeature.HORSE_COMBAT);
    }

    public static boolean transparentHorsesEnabled() {
        return featureEnabled(BhFeature.TRANSPARENT_HORSES);
    }

    public static boolean genderBreedingEnabled() {
        return featureEnabled(BhFeature.GENDER_BREEDING);
    }

    public static boolean horsePvpEnabled() {
        return featureEnabled(BhFeature.HORSE_PVP);
    }

    public static boolean cartPickupEnabled() {
        return featureEnabled(BhFeature.CART_PICKUP);
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

    private static int readInt(JsonObject root, String key, int defaultValue) {
        JsonElement element = root.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            if (element != null) {
                IcysBetterHorses.LOGGER.warn("Config key '{}' must be a number. Using default {}.", key, defaultValue);
            }
            return defaultValue;
        }
        return element.getAsInt();
    }

    private static double readDouble(JsonObject root, String key, double defaultValue) {
        JsonElement element = root.get(key);
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            if (element != null) {
                IcysBetterHorses.LOGGER.warn("Config key '{}' must be a number. Using default {}.", key, defaultValue);
            }
            return defaultValue;
        }
        return element.getAsDouble();
    }

    private static synchronized void save() {
        Map<BhFeature, Boolean> mineFeatures = ownFeatures != null ? ownFeatures : features;
        Map<BhAbility, Boolean> mineAbilities = ownAbilities != null ? ownAbilities : abilities;
        BhTuning mineTuning = ownTuning != null ? ownTuning : tuning;
        boolean mineClass = ownFeatures != null ? ownClassMaster : classMaster;
        boolean mineBreed = ownFeatures != null ? ownBreedMaster : breedMaster;

        JsonObject root = new JsonObject();
        for (BhFeature feature : BhFeature.values()) {
            root.addProperty(feature.key(), yesNo(mineFeatures.getOrDefault(feature, true)));
        }

        JsonObject perClass = new JsonObject();
        JsonObject perBreed = new JsonObject();
        for (BhAbility ability : BhAbility.values()) {
            JsonObject into = ability.classPerk() ? perClass : perBreed;
            into.addProperty(ability.key(), yesNo(mineAbilities.getOrDefault(ability, true)));
        }
        JsonObject section = new JsonObject();
        section.addProperty(KEY_CLASS_MASTER, yesNo(mineClass));
        section.addProperty(KEY_BREED_MASTER, yesNo(mineBreed));
        section.add(KEY_CLASS_LIST, perClass);
        section.add(KEY_BREED_LIST, perBreed);
        root.add(KEY_ABILITIES, section);

        JsonObject numbers = new JsonObject();
        numbers.addProperty(KEY_BOND_AMOUNT, mineTuning.bondAmount());
        numbers.addProperty(KEY_BOND_MINUTES, mineTuning.bondMinutes());
        numbers.addProperty(KEY_SPAWN_WEIGHT, mineTuning.spawnWeight());
        numbers.addProperty(KEY_GROUP_MIN, mineTuning.groupMin());
        numbers.addProperty(KEY_GROUP_MAX, mineTuning.groupMax());
        numbers.addProperty(KEY_SPAWN_FLOOR, mineTuning.spawnFloor());
        root.add(KEY_TUNING, numbers);

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

    private BhConfig() {}
}
