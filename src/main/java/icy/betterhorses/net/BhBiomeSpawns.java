package icy.betterhorses.net;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.LinkedHashSet;
import java.util.Set;

public final class BhBiomeSpawns {

    private static final Set<ResourceKey<Biome>> HORSE_BIOMES = HorseBreed.allBreedBiomes();
    private static final Set<ResourceKey<Biome>> VANILLA_HORSE_BIOMES = Set.of(
            Biomes.PLAINS,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.SAVANNA,
            Biomes.SAVANNA_PLATEAU,
            Biomes.WINDSWEPT_SAVANNA
    );
    private static final Set<ResourceKey<Biome>> EXTRA_HORSE_BIOMES = buildExtraHorseBiomes();

    private BhBiomeSpawns() {}

    public static boolean isHorseBiome(ResourceKey<Biome> biomeKey) {
        return HORSE_BIOMES.contains(biomeKey);
    }

    public static boolean isExtraHorseBiome(ResourceKey<Biome> biomeKey) {
        return EXTRA_HORSE_BIOMES.contains(biomeKey);
    }

    private static Set<ResourceKey<Biome>> buildExtraHorseBiomes() {
        LinkedHashSet<ResourceKey<Biome>> extra = new LinkedHashSet<>(HORSE_BIOMES);
        extra.removeAll(VANILLA_HORSE_BIOMES);
        return Set.copyOf(extra);
    }
}
