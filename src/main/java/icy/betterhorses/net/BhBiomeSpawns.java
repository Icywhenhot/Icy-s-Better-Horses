package icy.betterhorses.net;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.LinkedHashSet;
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

    private static final int HORSE_SPAWN_WEIGHT = 5;
    private static final int HORSE_MIN_GROUP = 2;
    private static final int HORSE_MAX_GROUP = 6;
    private static final float HORSE_CREATURE_PROBABILITY_FLOOR = 0.10F;
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

    public static void register() {
        if (HORSE_BIOMES.isEmpty()) return;

        BiomeModifications.create(Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_biome_spawns"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(HORSE_BIOMES), (selectionContext, context) -> {
                    MobSpawnSettings mobSettings = selectionContext.getBiome().getMobSettings();
                    boolean alreadyHasHorse = mobSettings.getMobs(MobCategory.CREATURE).unwrap().stream()
                            .anyMatch(weighted -> weighted.value().type() == EntityTypes.HORSE);
                    float originalProbability = mobSettings.getCreatureProbability();
                    boolean boostedProbability = !alreadyHasHorse
                            && originalProbability < HORSE_CREATURE_PROBABILITY_FLOOR;

                    if (!alreadyHasHorse) {
                        context.getMobSpawnSettings().addSpawn(
                                MobCategory.CREATURE,
                                new MobSpawnSettings.SpawnerData(EntityTypes.HORSE, HORSE_MIN_GROUP, HORSE_MAX_GROUP),
                                HORSE_SPAWN_WEIGHT);
                    }

                    if (boostedProbability) {
                        context.getMobSpawnSettings().setCreatureGenerationProbability(HORSE_CREATURE_PROBABILITY_FLOOR);
                    }
                });
    }

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
