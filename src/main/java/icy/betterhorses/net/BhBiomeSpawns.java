package icy.betterhorses.net;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.LinkedHashSet;
import java.util.Set;

// Adds EntityType.HORSE natural-spawn entries to every biome any breed lists, so biomes like deserts and snowy plains can host horses. Weight/group size match vanilla plains horses to keep overall density reasonable.
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
                            .anyMatch(weighted -> weighted.value().type() == EntityType.HORSE);
                    float originalProbability = mobSettings.getCreatureProbability();
                    boolean boostedProbability = !alreadyHasHorse
                            && originalProbability < HORSE_CREATURE_PROBABILITY_FLOOR;

                    if (!alreadyHasHorse) {
                        context.getSpawnSettings().addSpawn(
                                MobCategory.CREATURE,
                                new MobSpawnSettings.SpawnerData(EntityType.HORSE, HORSE_MIN_GROUP, HORSE_MAX_GROUP),
                                HORSE_SPAWN_WEIGHT);
                    }

                    if (boostedProbability) {
                        context.getSpawnSettings().setCreatureSpawnProbability(HORSE_CREATURE_PROBABILITY_FLOOR);
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
