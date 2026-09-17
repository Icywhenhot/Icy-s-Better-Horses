package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseGroupData;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(Horse.class)
public abstract class HorseFinalizeSpawnMixin {

    @Unique
    private static final Logger BH_LOGGER = LoggerFactory.getLogger("icys-better-horses/spawn");

    @Unique
    @Nullable
    private HorseBreed bh_pendingGroupBreed;

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void bh_captureGroupBreed(ServerLevelAccessor level,
                                      DifficultyInstance difficulty,
                                      MobSpawnType reason,
                                      @Nullable SpawnGroupData groupData,
                                      @Nullable CompoundTag dataTag,
                                      CallbackInfoReturnable<SpawnGroupData> cir) {
        if (groupData instanceof BhHorseGroupData existing) {
            this.bh_pendingGroupBreed = existing.breed();
        } else {
            this.bh_pendingGroupBreed = null;
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"), cancellable = true)
    private void bh_applyBreedAndCoat(ServerLevelAccessor level,
                                      DifficultyInstance difficulty,
                                      MobSpawnType reason,
                                      @Nullable SpawnGroupData groupData,
                                      @Nullable CompoundTag dataTag,
                                      CallbackInfoReturnable<SpawnGroupData> cir) {
        Horse self = (Horse) (Object) this;
        IHorseData data = (IHorseData) self;

        if (data.bh_getBreed() != HorseBreed.UNKNOWN_SPECIES) {
            this.bh_pendingGroupBreed = null;
            return;
        }

        HorseBreed breed = this.bh_pendingGroupBreed != null
                ? this.bh_pendingGroupBreed
                : bh_pickBreedForBiome(level, self);
        this.bh_pendingGroupBreed = null;

        data.bh_setBreed(breed);
        data.bh_setMixedBreed(false);

        HorseBreed.Coat coat = breed.rollCoat(self.getRandom());
        if (coat != null) {
            ((HorseAccessor) self).bh_setVariantAndMarkings(coat.color(), coat.markings());
        }

        String biomeId = level.getBiome(self.blockPosition())
                .unwrapKey()
                .map(key -> key.location().toString())
                .orElse("<unregistered>");
        BH_LOGGER.info("[HORSE_SPAWN] reason={} pos={} biome={} breed={} coat={}",
                reason, self.blockPosition(), biomeId, breed, coat);

        cir.setReturnValue(new BhHorseGroupData(breed, cir.getReturnValue()));
    }

    @Unique
    private HorseBreed bh_pickBreedForBiome(ServerLevelAccessor level, Horse self) {
        Holder<Biome> biome = level.getBiome(self.blockPosition());
        HorseBreed picked = HorseBreed.pickForBiome(biome, self.getRandom());
        return picked != null ? picked : HorseBreed.fromId(self.getRandom().nextInt(HorseBreed.HORSE_BREED_COUNT));
    }
}
