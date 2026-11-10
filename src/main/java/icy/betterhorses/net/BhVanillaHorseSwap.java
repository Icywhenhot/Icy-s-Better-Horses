package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.entity.BhUndeadHorse;
import icy.betterhorses.net.registry.BhBreeds;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BhVanillaHorseSwap {

    private BhVanillaHorseSwap() {}

    public static boolean trySwap(Entity entity) {
        if (!(entity instanceof AbstractHorse horse)
                || (horse.getClass() != Horse.class && horse.getClass() != Donkey.class && horse.getClass() != Mule.class
                    && horse.getClass() != SkeletonHorse.class && horse.getClass() != ZombieHorse.class)
                || !(horse.level() instanceof ServerLevel level)
                || !horse.isAlive() || horse.isPassenger()
                || horse instanceof SkeletonHorse skeleton && skeleton.isTrap()) {
            return false;
        }
        List<Entity> riders = List.copyOf(horse.getPassengers());
        for (Entity rider : riders) {
            if (rider instanceof Player) {
                return false;
            }
        }

        CompoundTag backup = BhHorseBackup.find(horse);
        EntityType<?> restored = backup == null ? null : BhHorseBackup.typeOf(backup);
        ResourceKey<BreedType> breedKey = null;
        EntityType<?> entityType = restored;
        if (entityType == null && (horse instanceof SkeletonHorse || horse instanceof ZombieHorse)) {
            List<EntityType<BhUndeadHorse>> pool = horse instanceof SkeletonHorse ? ModEntities.SKELETONS : ModEntities.ZOMBIES;
            entityType = pool.get(horse.getRandom().nextInt(pool.size()));
            breedKey = BhBreeds.keyOf(HorseBreed.byId(EntityType.getKey(entityType).getPath()));
        }
        if (entityType == null && horse instanceof Mule) {
            entityType = ModEntities.MEDIUM_MULE;
            breedKey = BhContent.MEDIUM_MULE.getKey();
        }
        if (entityType == null) {
            breedKey = horse instanceof Donkey ? null : IHorseData.of(horse).bh_getBreedKey();
            if (breedKey == null && horse instanceof Donkey) {
                breedKey = BhBreeds.keyOf(HorseBreed.pickDonkey(level.getBiome(horse.blockPosition()), horse.getRandom()));
            }
            if (breedKey == null) {
                breedKey = pickForBiome(level, horse);
                if (breedKey == null) {
                    return false;
                }
            }
            entityType = ModEntities.forBreed(breedKey);
            if (entityType == null) {
                return false;
            }
        }
        if (!(entityType.create(level) instanceof BhBreedHorse swap)) {
            return false;
        }

        CompoundTag tag = horse.saveWithoutId(new CompoundTag());
        if (restored != null) {
            BhHorseBackup.restoreInto(tag, backup);
        } else {
            tag.putString("BH_BreedId", breedKey.location().toString());
        }
        swap.load(tag);
        if (restored == null) {
            swap.bhConvertFrom(horse);
            if (!(swap instanceof BhUndeadHorse)) swap.setHealth(Math.min(horse.getHealth(), swap.getMaxHealth()));
        }
        horse.ejectPassengers();
        horse.remove(Entity.RemovalReason.CHANGED_DIMENSION);
        if (!level.addFreshEntity(swap)) {
            ((icy.betterhorses.net.mixin.EntityAccessor) horse).bh_unsetRemoved();
            if (!level.addFreshEntity(horse)) {
                throw new IllegalStateException("horse conversion rollback failed " + horse.getUUID());
            }
            reseat(riders, horse);
            return false;
        }
        reseat(riders, swap);
        if (horse instanceof AbstractChestedHorse chested && chested.hasChest()) {
            ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                ItemStack stack = ItemStack.parseOptional(level.registryAccess(), items.getCompound(i));
                if (!stack.isEmpty()) {
                    swap.spawnAtLocation(stack);
                }
            }
            swap.spawnAtLocation(Items.CHEST);
        }
        if (IHorseData.of(swap).bh_isOwned()) HorseTracker.register(swap);
        return true;
    }

    private static void reseat(List<Entity> riders, Entity mount) {
        for (Entity rider : riders) {
            if (rider.isAlive()) rider.startRiding(mount, true);
        }
    }

    private static @Nullable ResourceKey<BreedType> pickForBiome(ServerLevel level, AbstractHorse horse) {
        HorseBreed picked = HorseBreed.pickForBiome(level.getBiome(horse.blockPosition()), horse.getRandom());
        HorseBreed fallback = picked != null
                ? picked
                : HorseBreed.fromId(horse.getRandom().nextInt(HorseBreed.HORSE_BREED_COUNT));
        return BhBreeds.keyOf(fallback);
    }
}
