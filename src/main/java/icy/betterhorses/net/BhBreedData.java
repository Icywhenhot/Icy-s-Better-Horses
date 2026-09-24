package icy.betterhorses.net;

import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record BhBreedData(ArchetypeType archetype, int chestRows, int bondedChestRows, int spawnWeight) {

    private static final int DEFAULT_SPAWN_WEIGHT = 5;
    private static final BhBreedData SPECIES_DEFAULT = new BhBreedData(
            BhContent.NONE.value(), BhContent.NONE.value().defaultChestRows(),
            BhContent.NONE.value().defaultChestRows(), DEFAULT_SPAWN_WEIGHT);

    private static @Nullable Map<Identifier, BhBreedData> BUILT_IN;
    private static final Map<Identifier, BhBreedData> live = new HashMap<>();

    private static Map<Identifier, BhBreedData> builtIns() {
        if (BUILT_IN == null) {
            BUILT_IN = builtIn();
        }
        return BUILT_IN;
    }

    public static BhBreedData of(HorseBreed breed) {
        if (!breed.isRealBreed()) {
            return SPECIES_DEFAULT;
        }
        return of(idOf(breed));
    }

    public static BhBreedData of(Identifier id) {
        BhBreedData data = live.get(id);
        return data != null ? data : builtIns().get(id);
    }

    public static BhBreedData of(@Nullable ResourceKey<BreedType> id) {
        return id == null ? SPECIES_DEFAULT : of(id.identifier());
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

    public static BhBreedData builtIn(Identifier id) {
        return builtIns().get(id);
    }

    public static Map<Identifier, BhBreedData> all() {
        return live.isEmpty() ? builtIns() : Map.copyOf(live);
    }

    public static void replaceAll(Map<Identifier, BhBreedData> loaded) {
        live.clear();
        live.putAll(builtIns());
        live.putAll(loaded);
    }

    public static void resetToBuiltIn() {
        live.clear();
        live.putAll(builtIns());
    }

    public int rowsAt(int bondTier) {
        return bondTier >= 2 ? bondedChestRows : chestRows;
    }

    private static Identifier idOf(HorseBreed breed) {
        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, breed.id());
    }

    private static Map<Identifier, BhBreedData> builtIn() {
        Map<Identifier, BhBreedData> map = new HashMap<>();
        Registry<BreedType> registry = BhRegistries.breedTypeRegistry();
        for (BreedType type : registry) {
            Identifier id = registry.getKey(type);
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
        ArchetypeType type = BhRegistries.archetypeTypeRegistry().getValue(key.identifier());
        return type != null ? type : BhContent.NONE.value();
    }
}
