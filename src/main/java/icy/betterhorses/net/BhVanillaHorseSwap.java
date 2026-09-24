package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.registry.BhBreeds;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class BhVanillaHorseSwap {

    private BhVanillaHorseSwap() {}

    public static boolean trySwap(Entity entity) {
        if (!(entity instanceof Horse horse) || horse.getClass() != Horse.class
                || !(horse.level() instanceof ServerLevel level)
                || !horse.isAlive() || horse.isVehicle() || horse.isPassenger()) {
            return false;
        }

        ResourceKey<BreedType> breedKey = IHorseData.of(horse).bh_getBreedKey();
        if (breedKey == null) {
            breedKey = pickForBiome(level, horse);
            if (breedKey == null) {
                return false;
            }
        }

        EntityType<? extends BhBreedHorse> entityType = ModEntities.forBreed(breedKey);
        if (entityType == null) {
            return false;
        }
        BhBreedHorse swap = entityType.create(level, EntitySpawnReason.CONVERSION);
        if (swap == null) {
            return false;
        }

        var out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, horse.registryAccess());
        horse.saveWithoutId(out);
        var tag = out.buildResult();
        tag.putString("BH_BreedId", breedKey.identifier().toString());
        swap.load(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, horse.registryAccess(), tag));
        swap.bhConvertFrom(horse);
        swap.setHealth(Math.min(horse.getHealth(), swap.getMaxHealth()));
        horse.remove(Entity.RemovalReason.CHANGED_DIMENSION);
        if (!level.addFreshEntity(swap)) {
            ((icy.betterhorses.net.mixin.EntityAccessor) horse).bh_unsetRemoved();
            if (!level.addFreshEntity(horse)) {
                throw new IllegalStateException("horse conversion rollback failed " + horse.getUUID());
            }
            return false;
        }
        if (IHorseData.of(swap).bh_isOwned()) HorseTracker.register(swap);
        return true;
    }

    private static @Nullable ResourceKey<BreedType> pickForBiome(ServerLevel level, Horse horse) {
        HorseBreed picked = HorseBreed.pickForBiome(level.getBiome(horse.blockPosition()), horse.getRandom());
        HorseBreed fallback = picked != null
                ? picked
                : HorseBreed.fromId(horse.getRandom().nextInt(HorseBreed.HORSE_BREED_COUNT));
        return BhBreeds.keyOf(fallback);
    }
}
