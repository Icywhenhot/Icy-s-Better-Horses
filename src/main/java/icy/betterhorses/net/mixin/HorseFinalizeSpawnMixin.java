package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseGroupData;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

// Captures the group breed at HEAD, then applies the coat and propagates it to siblings at TAIL.
@Mixin(Horse.class)
public abstract class HorseFinalizeSpawnMixin {

    @Unique
    @Nullable
    private HorseBreed bh_pendingGroupBreed;

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void bh_captureGroupBreed(ServerLevelAccessor level,
                                      DifficultyInstance difficulty,
                                      MobSpawnType reason,
                                      @Nullable SpawnGroupData groupData,
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
                                      CallbackInfoReturnable<SpawnGroupData> cir) {
        Horse self = (Horse) (Object) this;
        IHorseData data = (IHorseData) self;

        // Skip breed/coat application when an NBT-restored breed already exists (e.g. /summon with stored data).
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

        // Overwrite the random coat vanilla just set with one from the breed's allowed list.
        HorseBreed.Coat coat = breed.rollCoat(self.getRandom());
        if (coat != null) {
            ((HorseAccessor) self).bh_setVariantAndMarkings(coat.color(), coat.markings());
        }

        // Propagate breed to the next sibling; vanilla's return value is kept inside the wrapper.
        cir.setReturnValue(new BhHorseGroupData(breed, cir.getReturnValue()));
    }

    @Unique
    private HorseBreed bh_pickBreedForBiome(ServerLevelAccessor level, Horse self) {
        Holder<Biome> biome = level.getBiome(self.blockPosition());
        Optional<ResourceKey<Biome>> biomeKey = biome.unwrapKey();
        if (biomeKey.isPresent()) {
            List<HorseBreed> matches = HorseBreed.breedsForBiome(biomeKey.get());
            if (!matches.isEmpty()) {
                return matches.get(self.getRandom().nextInt(matches.size()));
            }
        }
        return HorseBreed.fromId(self.getRandom().nextInt(HorseBreed.HORSE_BREED_COUNT));
    }
}
