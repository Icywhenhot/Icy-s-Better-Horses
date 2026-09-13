package icy.betterhorses.net;

import java.util.EnumMap;
import java.util.Map;

public record BhBreedData(BreedArchetype archetype, int chestRows, int bondedChestRows, int spawnWeight) {

    private static final EnumMap<HorseBreed, BhBreedData> BUILT_IN = builtIn();
    private static final EnumMap<HorseBreed, BhBreedData> live = new EnumMap<>(BUILT_IN);

    public static BhBreedData of(HorseBreed breed) {
        BhBreedData data = live.get(breed);
        return data != null ? data : BUILT_IN.get(breed);
    }

    public static BhBreedData builtIn(HorseBreed breed) {
        return BUILT_IN.get(breed);
    }

    public static Map<HorseBreed, BhBreedData> all() {
        return Map.copyOf(live);
    }

    public static void replaceAll(Map<HorseBreed, BhBreedData> loaded) {
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

    private static EnumMap<HorseBreed, BhBreedData> builtIn() {
        EnumMap<HorseBreed, BhBreedData> map = new EnumMap<>(HorseBreed.class);
        for (HorseBreed breed : HorseBreed.values()) {
            BreedArchetype arch = breed.builtInArchetype();
            int rows = arch.chestRows();
            int bonded = rows;
            switch (breed) {
                case BELGIAN -> {
                    rows = 6;
                    bonded = 6;
                }
                case HAFLINGER -> {
                    rows = 4;
                    bonded = 6;
                }
                case MORGAN -> bonded = 4;
                default -> { }
            }
            map.put(breed, new BhBreedData(arch, rows, bonded, 5));
        }
        return map;
    }
}
