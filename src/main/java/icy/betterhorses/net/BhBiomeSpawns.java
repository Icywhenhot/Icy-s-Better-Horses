package icy.betterhorses.net;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;

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

    private BhBiomeSpawns() {}

    public static void register() {
        if (HORSE_BIOMES.isEmpty()) return;

        BiomeModifications.create(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_biome_spawns"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.includeByKey(HORSE_BIOMES), (selectionContext, context) -> {
                    MobSpawnSettings mobSettings = selectionContext.getBiome().getMobSettings();
                    boolean alreadyHasHorse = mobSettings.getMobs(MobCategory.CREATURE).unwrap().stream()
                            .anyMatch(data -> data.type == EntityType.HORSE);
                    float originalProbability = mobSettings.getCreatureProbability();
                    boolean boostedProbability = !alreadyHasHorse
                            && originalProbability < HORSE_CREATURE_PROBABILITY_FLOOR;

                    if (!alreadyHasHorse) {
                        context.getSpawnSettings().addSpawn(
                                MobCategory.CREATURE,
                                new MobSpawnSettings.SpawnerData(
                                        EntityType.HORSE, HORSE_SPAWN_WEIGHT, HORSE_MIN_GROUP, HORSE_MAX_GROUP));
                    }

                    if (boostedProbability) {
                        context.getSpawnSettings().setCreatureSpawnProbability(HORSE_CREATURE_PROBABILITY_FLOOR);
                    }

                    if (!alreadyHasHorse || boostedProbability) {
                        IcysBetterHorses.LOGGER.info(
                                "[SPAWN_REG] biome={} addHorse={} creatureProb={} -> {}",
                                selectionContext.getBiomeKey().location(),
                                !alreadyHasHorse,
                                originalProbability,
                                boostedProbability ? HORSE_CREATURE_PROBABILITY_FLOOR : originalProbability);
                    }
                });
    }

    public static boolean isHorseBiome(ResourceKey<Biome> biomeKey) {
        return HORSE_BIOMES.contains(biomeKey);
    }
}
