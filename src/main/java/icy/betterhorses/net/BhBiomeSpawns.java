package icy.betterhorses.net;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.eventbus.api.IEventBus;

public final class BhBiomeSpawns {

    private static final TagKey<Biome> SPAWNS = TagKey.create(Registries.BIOME,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "spawns_horses"));

    private static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,
                    IcysBetterHorses.RESOURCE_NAMESPACE);

    static {
        SERIALIZERS.register("horse_biome_spawns", () -> SpawnModifier.CODEC);
    }

    private BhBiomeSpawns() {}

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }

    private enum SpawnModifier implements BiomeModifier {
        INSTANCE;

        private static final Codec<SpawnModifier> CODEC = Codec.unit(INSTANCE);

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.ADD || !biome.is(SPAWNS)) return;

            BhTuning tuning = BhConfig.tuning();
            if (tuning.spawnWeight() <= 0) return;

            MobSpawnSettings mobSettings = biome.value().getMobSettings();
            boolean alreadyHasHorse = mobSettings.getMobs(MobCategory.CREATURE).unwrap().stream()
                    .anyMatch(spawn -> spawn.type == EntityType.HORSE);
            float floor = (float) tuning.spawnFloor();

            if (!alreadyHasHorse) {
                builder.getMobSpawnSettings().addSpawn(
                        MobCategory.CREATURE,
                        new MobSpawnSettings.SpawnerData(
                                EntityType.HORSE, tuning.spawnWeight(), tuning.groupMin(), tuning.groupMax()));
            }
            if (!alreadyHasHorse && mobSettings.getCreatureProbability() < floor) {
                builder.getMobSpawnSettings().creatureGenerationProbability(floor);
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }
}
