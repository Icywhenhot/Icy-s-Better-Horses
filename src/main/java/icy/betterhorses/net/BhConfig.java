package icy.betterhorses.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class BhConfig {

    private static final String KEY_STABILIZER = "stabilizer";
    private static final String KEY_MEDKIT = "medkit";
    private static final String KEY_HITCHPOST = "hitchpost";
    private static final String KEY_HOOVES = "hooves";
    private static final String KEY_HORSE_EXCLUSIVITY = "horse_exclusivity";
    private static final String KEY_MULTI_RIDING = "multiriding";

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(IcysBetterHorses.MOD_ID + ".json");
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static State state = State.defaults();

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

            state = new State(
                    readToggle(root, KEY_STABILIZER, defaults.stabilizer()),
                    readToggle(root, KEY_MEDKIT, defaults.medkit()),
                    readToggle(root, KEY_HITCHPOST, defaults.hitchpost()),
                    readToggle(root, KEY_HOOVES, defaults.hooves()),
                    readToggle(root, KEY_HORSE_EXCLUSIVITY, defaults.horseExclusivity()),
                    readToggle(root, KEY_MULTI_RIDING, defaults.multiRiding()));
        } catch (Exception exception) {
            state = defaults;
            IcysBetterHorses.LOGGER.warn("Failed to load config from {}. Reverting to defaults.",
                    CONFIG_PATH, exception);
            save();
            return;
        }

        if (needsRewrite) {
            save();
        }

        IcysBetterHorses.LOGGER.info("Loaded config from {}", CONFIG_PATH);
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
        JsonObject root = new JsonObject();
        root.addProperty(KEY_STABILIZER, yesNo(state.stabilizer()));
        root.addProperty(KEY_MEDKIT, yesNo(state.medkit()));
        root.addProperty(KEY_HITCHPOST, yesNo(state.hitchpost()));
        root.addProperty(KEY_HOOVES, yesNo(state.hooves()));
        root.addProperty(KEY_HORSE_EXCLUSIVITY, yesNo(state.horseExclusivity()));
        root.addProperty(KEY_MULTI_RIDING, yesNo(state.multiRiding()));

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
            boolean multiRiding) {

        private static State defaults() {
            return new State(true, true, true, true, true, true);
        }
    }

    private BhConfig() {}
}
