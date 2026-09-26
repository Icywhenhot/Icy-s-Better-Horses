package icy.betterhorses.net;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;

public final class BhBiomeSpawns {

    private static final TagKey<Biome> SPAWNS = TagKey.create(Registries.BIOME,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "spawns_horses"));

    private BhBiomeSpawns() {}

    public static void register() {
        BiomeModifications
                .create(new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "horse_biome_spawns"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(SPAWNS), (selection, modification) -> {
                    BhTuning tuning = BhConfig.tuning();
                    if (tuning.spawnWeight() <= 0) {
                        return;
                    }

                    MobSpawnSettings mobSettings = selection.getBiome().getMobSettings();
                    boolean alreadyHasHorse = mobSettings.getMobs(MobCategory.CREATURE).unwrap().stream()
                            .anyMatch(spawn -> spawn.type == EntityType.HORSE);
                    float floor = (float) tuning.spawnFloor();

                    if (!alreadyHasHorse) {
                        modification.getSpawnSettings().addSpawn(MobCategory.CREATURE,
                                new MobSpawnSettings.SpawnerData(EntityType.HORSE,
                                        tuning.spawnWeight(), tuning.groupMin(), tuning.groupMax()));
                    }
                    if (!alreadyHasHorse && mobSettings.getCreatureProbability() < floor) {
                        modification.getSpawnSettings().setCreatureSpawnProbability(floor);
                    }
                });
    }
}
