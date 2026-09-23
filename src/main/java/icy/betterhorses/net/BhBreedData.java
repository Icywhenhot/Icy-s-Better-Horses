package icy.betterhorses.net;

import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record BhBreedData(ArchetypeType archetype, int chestRows, int bondedChestRows, int spawnWeight) {

    private static final int DEFAULT_SPAWN_WEIGHT = 5;
    private static final BhBreedData SPECIES_DEFAULT = new BhBreedData(
            BhContent.NONE.get(), BhContent.NONE.get().defaultChestRows(),
            BhContent.NONE.get().defaultChestRows(), DEFAULT_SPAWN_WEIGHT);

    private static Map<ResourceLocation, BhBreedData> BUILT_IN = Map.of();
    private static final Map<ResourceLocation, BhBreedData> live = new HashMap<>();

    public static void initializeBuiltIns() {
        BUILT_IN = builtIn();
        live.clear();
        live.putAll(BUILT_IN);
    }

    public static BhBreedData of(HorseBreed breed) {
        if (!breed.isRealBreed()) {
            return SPECIES_DEFAULT;
        }
        return of(idOf(breed));
    }

    public static BhBreedData of(ResourceLocation id) {
        BhBreedData data = live.get(id);
        return data != null ? data : BUILT_IN.get(id);
    }

    public static BhBreedData of(@Nullable ResourceKey<BreedType> id) {
        return id == null ? SPECIES_DEFAULT : of(id.location());
    }

    public static BhBreedData speciesDefault() {
        return SPECIES_DEFAULT;
    }

    public static BhBreedData builtIn(HorseBreed breed) {
        if (!breed.isRealBreed()) {
            return SPECIES_DEFAULT;
        }
        return builtIn(idOf(breed));
    }

    public static BhBreedData builtIn(ResourceLocation id) {
        return BUILT_IN.get(id);
    }

    public static Map<ResourceLocation, BhBreedData> all() {
        return Map.copyOf(live);
    }

    public static void replaceAll(Map<ResourceLocation, BhBreedData> loaded) {
        live.clear();
        live.putAll(BUILT_IN);
        live.putAll(loaded);
    }

    public static void resetToBuiltIn() {
        live.clear();
        live.putAll(BUILT_IN);
    }

    public int rowsAt(int bondTier) {
        return bondTier >= 2 ? bondedChestRows : chestRows;
    }

    private static ResourceLocation idOf(HorseBreed breed) {
        return ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, breed.id());
    }

    private static Map<ResourceLocation, BhBreedData> builtIn() {
        Map<ResourceLocation, BhBreedData> map = new HashMap<>();
        Registry<BreedType> registry = BhRegistries.breedTypeRegistry();
        for (BreedType type : registry) {
            ResourceLocation id = registry.getKey(type);
            if (id == null) {
                continue;
            }
            ArchetypeType archetype = resolveArchetype(type.archetype());
            int rows = type.chestRowsOverride() != null ? type.chestRowsOverride() : archetype.defaultChestRows();
            int bonded = type.bondedChestRowsOverride() != null ? type.bondedChestRowsOverride() : rows;
            map.put(id, new BhBreedData(archetype, rows, bonded, DEFAULT_SPAWN_WEIGHT));
        }
        return Map.copyOf(map);
    }

    private static ArchetypeType resolveArchetype(ResourceKey<ArchetypeType> key) {
        ArchetypeType type = BhRegistries.archetypeTypeRegistry().get(key.location());
        return type != null ? type : BhContent.NONE.get();
    }
}
