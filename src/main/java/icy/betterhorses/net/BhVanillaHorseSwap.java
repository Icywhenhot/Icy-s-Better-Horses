package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhBreedEntity;
import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Optional;

public final class BhVanillaHorseSwap {

    private BhVanillaHorseSwap() {}

    public static boolean trySwap(Entity entity) {
        if (!(entity instanceof Horse horse) || horse instanceof BhBreedEntity
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

        swap.bhConvertFrom(horse);
        transfer(horse, swap);
        IcysBetterHorses.LOGGER.info("[HORSE_SWAP] replaced vanilla horse at {} with {}",
                horse.blockPosition(), breed);
        horse.discard();
        level.addFreshEntity(swap);
        return true;
    }

    private static HorseBreed pickForBiome(ServerLevel level, Horse horse) {
        Holder<Biome> biome = level.getBiome(horse.blockPosition());
        Optional<ResourceKey<Biome>> key = biome.unwrapKey();
        if (key.isPresent()) {
            List<HorseBreed> matches = HorseBreed.breedsForBiome(key.get());
            if (!matches.isEmpty()) {
                return matches.get(horse.getRandom().nextInt(matches.size()));
            }
        }
        return HorseBreed.fromId(horse.getRandom().nextInt(HorseBreed.HORSE_BREED_COUNT));
    }

    private static void transfer(Horse from, BhBreedHorse to) {
        to.snapTo(from.getX(), from.getY(), from.getZ(), from.getYRot(), from.getXRot());
        to.yHeadRot = from.yHeadRot;
        to.yBodyRot = from.yBodyRot;
        to.setAge(from.getAge());
        to.setTamed(from.isTamed());
        to.setCustomName(from.getCustomName());
        to.setCustomNameVisible(from.isCustomNameVisible());
        to.setInvulnerable(from.isInvulnerable());
        to.setPersistenceRequired();
        to.setHealth(Math.min(from.getHealth(), to.getMaxHealth()));

        IHorseData src = IHorseData.of(from);
        IHorseData dst = IHorseData.of(to);
        dst.bh_setOwner(src.bh_getOwner());
        dst.bh_setBond(src.bh_getBond());
        dst.bh_setGender(src.bh_getGender());
        dst.bh_setGeneration(src.bh_getGeneration());
        dst.bh_setCommand(src.bh_getCommand());
        dst.bh_setHome(src.bh_getHome());
        dst.bh_setReceivedNameTagBond(src.bh_hasReceivedNameTagBond());
        dst.bh_setMixedBreed(src.bh_isMixedBreed());

        moveAll(src.bh_getGearContainer(), dst.bh_getGearContainer());
        moveAll(src.bh_getChestContainer(), dst.bh_getChestContainer());
    }

    private static void moveAll(SimpleContainer from, SimpleContainer to) {
        if (from == null || to == null) {
            return;
        }
        int n = Math.min(from.getContainerSize(), to.getContainerSize());
        for (int i = 0; i < n; i++) {
            to.setItem(i, from.removeItemNoUpdate(i));
        }
        to.setChanged();
    }
}
