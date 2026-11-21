package icy.betterhorses.net;

import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.tuning.Ranges;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record BhBreedData(ArchetypeType archetype, int chestRows, int midChestRows, int bondedChestRows, int spawnWeight, Ranges ranges) {

    private static final int DEFAULT_SPAWN_WEIGHT = 5;
    public static final int CLASS_ROWS = -1;
    private static final BhBreedData SPECIES_DEFAULT = new BhBreedData(
            BhContent.NONE.get(), CLASS_ROWS, CLASS_ROWS, CLASS_ROWS, DEFAULT_SPAWN_WEIGHT, Ranges.NONE);

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

    @Override
    public int chestRows() {
        return chestRows < 0 ? archetype.defaultChestRows() : chestRows;
    }

    @Override
    public int midChestRows() {
        return midChestRows < 0 ? chestRows() : Math.min(Math.max(midChestRows, chestRows()), bondedChestRows());
    }

    @Override
    public int bondedChestRows() {
        return bondedChestRows < 0 ? chestRows() : Math.max(bondedChestRows, chestRows());
    }

    public boolean classRows() {
        return chestRows < 0;
    }

    public boolean classMidRows() {
        return midChestRows < 0;
    }

    public boolean classBondedRows() {
        return bondedChestRows < 0;
    }

    public int rowsAt(int bondTier) {
        return bondTier >= 2 ? bondedChestRows() : bondTier == 1 ? midChestRows() : chestRows();
    }

    public double rollSpeed(RandomSource random) {
        return ranges.rollSpeed(archetype.stats(), random);
    }

    public double rollHealth(RandomSource random) {
        return ranges.rollHealth(archetype.stats(), random);
    }

    public double rollJump(RandomSource random) {
        return ranges.rollJump(archetype.stats(), random);
    }

    public double clampSpeed(double v) {
        return ranges.clampSpeed(archetype.stats(), v);
    }

    public double clampHealth(double v) {
        return ranges.clampHealth(archetype.stats(), v);
    }

    public double clampJump(double v) {
        return ranges.clampJump(archetype.stats(), v);
    }

    public double[] speedRange() {
        return ranges.speed(archetype.stats());
    }

    public double[] healthRange() {
        return ranges.health(archetype.stats());
    }

    public double[] jumpRange() {
        return ranges.jump(archetype.stats());
    }

    public static double topSpeed() {
        double best = ArchetypeType.topSpeed();
        for (BhBreedData d : live.values()) {
            best = Math.max(best, d.speedRange()[1]);
        }
        return best;
    }

    public static double topJump() {
        double best = ArchetypeType.topJump();
        for (BhBreedData d : live.values()) {
            best = Math.max(best, d.jumpRange()[1]);
        }
        return best;
    }

    public static double topHealth() {
        double best = ArchetypeType.topHealth();
        for (BhBreedData d : live.values()) {
            best = Math.max(best, d.healthRange()[1]);
        }
        return best;
    }

    private static ResourceLocation idOf(HorseBreed breed) {
        return new ResourceLocation(IcysBetterHorses.MOD_ID, breed.id());
    }

    private static Map<ResourceLocation, BhBreedData> builtIn() {
        Map<ResourceLocation, BhBreedData> map = new HashMap<>();
        IForgeRegistry<BreedType> registry = BhRegistries.breedTypeRegistry();
        for (BreedType type : registry) {
            ResourceLocation id = registry.getKey(type);
            if (id == null) {
                continue;
            }
            ArchetypeType archetype = resolveArchetype(type.archetype());
            int rows = type.chestRowsOverride() != null ? type.chestRowsOverride() : CLASS_ROWS;
            int mid = type.midChestRowsOverride() != null ? type.midChestRowsOverride() : CLASS_ROWS;
            int bonded = type.bondedChestRowsOverride() != null ? type.bondedChestRowsOverride() : CLASS_ROWS;
            map.put(id, new BhBreedData(archetype, rows, mid, bonded, DEFAULT_SPAWN_WEIGHT, Ranges.NONE));
        }
        return Map.copyOf(map);
    }

    private static ArchetypeType resolveArchetype(ResourceKey<ArchetypeType> key) {
        ArchetypeType type = BhRegistries.archetypeTypeRegistry().getValue(key.location());
        return type != null ? type : BhContent.NONE.get();
    }
}
