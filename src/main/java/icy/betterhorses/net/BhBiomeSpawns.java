package icy.betterhorses.net;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

import java.util.Set;

/**
 * Adds {@code EntityType.HORSE} natural-spawn entries to every biome listed in
 * any breed's spawn list. Without this, biomes like deserts and snowy plains
 * never host horses no matter what {@link HorseBreed} says, because the natural
 * spawner won't pick a position for an entity type that has no spawn entry in
 * that biome's {@code MobSpawnSettings}.
 *
 * <p>Spawn weight and group size are tuned to match vanilla plains horses so
 * the overall horse density across the world stays reasonable.
 */
public final class BhBiomeSpawns {

    private static final int HORSE_SPAWN_WEIGHT = 4;
    private static final int HORSE_MIN_GROUP = 2;
    private static final int HORSE_MAX_GROUP = 5;

    private BhBiomeSpawns() {}

    public static void register() {
        Set<ResourceKey<Biome>> biomes = HorseBreed.allBreedBiomes();
        if (biomes.isEmpty()) return;

        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(biomes),
                MobCategory.CREATURE,
                EntityType.HORSE,
                HORSE_SPAWN_WEIGHT,
                HORSE_MIN_GROUP,
                HORSE_MAX_GROUP);
    }
}
