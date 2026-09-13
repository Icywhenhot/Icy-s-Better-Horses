package icy.betterhorses.net;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class BhBiomeSpawns {

    private static final TagKey<Biome> SPAWNS = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "spawns_horses"));

    private BhBiomeSpawns() {}

    public static void register(RegisterEvent event) {
        event.register(
                NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "horse_biome_spawns"),
                () -> SpawnModifier.CODEC);
    }

    private enum SpawnModifier implements BiomeModifier {
        INSTANCE;

        private static final MapCodec<SpawnModifier> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.ADD || !biome.is(SPAWNS)) return;

            BhTuning tuning = BhConfig.tuning();
            if (tuning.spawnWeight() <= 0) return;

            MobSpawnSettings mobSettings = biome.value().getMobSettings();
            boolean alreadyHasHorse = mobSettings.getMobs(MobCategory.CREATURE).unwrap().stream()
                    .anyMatch(weighted -> weighted.value().type() == EntityType.HORSE);
            float floor = (float) tuning.spawnFloor();

            if (!alreadyHasHorse) {
                builder.getMobSpawnSettings().addSpawn(
                        MobCategory.CREATURE,
                        tuning.spawnWeight(),
                        new MobSpawnSettings.SpawnerData(EntityType.HORSE, tuning.groupMin(), tuning.groupMax()));
            }
            if (!alreadyHasHorse && mobSettings.getCreatureProbability() < floor) {
                builder.getMobSpawnSettings().creatureGenerationProbability(floor);
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }
}
