package icy.betterhorses.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.io.Reader;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class BhBreedLoader {

    private static final String DIR = "better_horses/breed";

    private BhBreedLoader() {}

    public static void register(AddServerReloadListenersEvent event) {
        event.addListener(
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "breeds"),
                (ResourceManagerReloadListener) BhBreedLoader::load);
    }

    private static void load(ResourceManager manager) {
        EnumMap<HorseBreed, BhBreedData> loaded = new EnumMap<>(HorseBreed.class);
        Map<Identifier, Resource> found = manager.listResources(DIR, id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : found.entrySet()) {
            Identifier file = entry.getKey();
            String name = file.getPath();
            name = name.substring(name.lastIndexOf('/') + 1, name.length() - ".json".length());
            HorseBreed breed = HorseBreed.byId(name);
            if (breed == HorseBreed.UNKNOWN_SPECIES && !"unknown_species".equals(name)) {
                IcysBetterHorses.LOGGER.warn("[breeds] {} does not name a breed, skipping", file);
                continue;
            }

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (!(parsed instanceof JsonObject root)) {
                    IcysBetterHorses.LOGGER.warn("[breeds] {} is not a json object, skipping", file);
                    continue;
                }
                loaded.put(breed, read(breed, root));
            } catch (Exception exception) {
                IcysBetterHorses.LOGGER.warn("[breeds] could not read {}", file, exception);
            }
        }

        BhBreedData.replaceAll(loaded);
        IcysBetterHorses.LOGGER.info("[breeds] loaded {} breed definitions", loaded.size());
    }

    private static BhBreedData read(HorseBreed breed, JsonObject root) {
        BhBreedData fallback = BhBreedData.builtIn(breed);
        BreedArchetype arch = fallback.archetype();
        if (root.has("class")) {
            String wanted = root.get("class").getAsString().toUpperCase(Locale.ROOT);
            try {
                arch = BreedArchetype.valueOf(wanted);
            } catch (IllegalArgumentException ignored) {
                IcysBetterHorses.LOGGER.warn("[breeds] {} has unknown class '{}', keeping {}",
                        breed.id(), root.get("class").getAsString(), arch);
            }
        }
        int rows = clampRows(root, "chest_rows", fallback.chestRows());
        int bonded = clampRows(root, "bonded_chest_rows", Math.max(fallback.bondedChestRows(), rows));
        int weight = root.has("spawn_weight")
                ? Math.max(0, root.get("spawn_weight").getAsInt())
                : fallback.spawnWeight();
        return new BhBreedData(arch, rows, Math.max(bonded, rows), weight);
    }

    private static int clampRows(JsonObject root, String key, int fallback) {
        if (!root.has(key)) {
            return fallback;
        }
        return Math.clamp(root.get(key).getAsInt(), 0, 6);
    }
}
