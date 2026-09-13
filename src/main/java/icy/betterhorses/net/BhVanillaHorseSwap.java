package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.biome.Biome;

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

        HorseBreed breed = IHorseData.of(horse).bh_getBreed();
        if (!breed.isRealBreed()) {
            breed = pickForBiome(level, horse);
        }

        BhBreedHorse swap = ModEntities.forBreed(breed).create(level, EntitySpawnReason.CONVERSION);
        if (swap == null) {
            return false;
        }

        var out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, horse.registryAccess());
        horse.saveWithoutId(out);
        var tag = out.buildResult();
        tag.putString("BH_BreedId", breed.id());
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

    private static HorseBreed pickForBiome(ServerLevel level, Horse horse) {
        HorseBreed picked = HorseBreed.pickForBiome(level.getBiome(horse.blockPosition()), horse.getRandom());
        return picked != null
                ? picked
                : HorseBreed.fromId(horse.getRandom().nextInt(HorseBreed.HORSE_BREED_COUNT));
    }

}
