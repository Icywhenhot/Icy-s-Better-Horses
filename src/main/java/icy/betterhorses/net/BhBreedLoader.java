package icy.betterhorses.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class BhBreedLoader {

    private static final String DIR = "better_horses/breed";

    private BhBreedLoader() {}

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return new ResourceLocation(
                                IcysBetterHorses.RESOURCE_NAMESPACE, "breed_data");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        load(manager);
                    }
                });
    }

    private static void load(ResourceManager manager) {
        Map<ResourceLocation, BhBreedData> loaded = new HashMap<>();
        Map<ResourceLocation, Resource> found = manager.listResources(DIR, id -> id.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : found.entrySet()) {
            ResourceLocation file = entry.getKey();
            String name = file.getPath();
            name = name.substring(name.lastIndexOf('/') + 1, name.length() - ".json".length());
            ResourceLocation breedId = new ResourceLocation(IcysBetterHorses.MOD_ID, name);
            if (!BhRegistries.breedTypeRegistry().containsKey(breedId)) {
                IcysBetterHorses.LOGGER.warn("[breeds] {} does not name a registered breed, skipping", file);
                continue;
            }

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (!(parsed instanceof JsonObject root)) {
                    IcysBetterHorses.LOGGER.warn("[breeds] {} is not a json object, skipping", file);
                    continue;
                }
                loaded.put(breedId, read(breedId, root));
            } catch (Exception exception) {
                IcysBetterHorses.LOGGER.warn("[breeds] could not read {}", file, exception);
            }
        }

        BhBreedData.replaceAll(loaded);
        IcysBetterHorses.LOGGER.info("[breeds] loaded {} breed definitions", loaded.size());
    }

    private static BhBreedData read(ResourceLocation breedId, JsonObject root) {
        BhBreedData fallback = BhBreedData.builtIn(breedId);
        ArchetypeType arch = fallback.archetype();
        if (root.has("class")) {
            String wanted = root.get("class").getAsString();
            ArchetypeType resolved = resolveArchetype(wanted);
            if (resolved != null) {
                arch = resolved;
            } else {
                IcysBetterHorses.LOGGER.warn("[breeds] {} has unknown class '{}', keeping {}",
                        breedId, wanted, arch);
            }
        }
        int rows = clampRows(root, "chest_rows", fallback.chestRows());
        int bonded = clampRows(root, "bonded_chest_rows", Math.max(fallback.bondedChestRows(), rows));
        int weight = root.has("spawn_weight")
                ? Math.max(0, root.get("spawn_weight").getAsInt())
                : fallback.spawnWeight();
        return new BhBreedData(arch, rows, Math.max(bonded, rows), weight);
    }

    private static @Nullable ArchetypeType resolveArchetype(String wanted) {
        ResourceLocation location = wanted.indexOf(':') >= 0
                ? ResourceLocation.tryParse(wanted)
                : new ResourceLocation(IcysBetterHorses.MOD_ID, wanted.toLowerCase(Locale.ROOT));
        return location == null ? null : BhRegistries.archetypeTypeRegistry().get(location);
    }

    private static int clampRows(JsonObject root, String key, int fallback) {
        if (!root.has(key)) {
            return fallback;
        }
        return Mth.clamp(root.get(key).getAsInt(), 0, 6);
    }
}
