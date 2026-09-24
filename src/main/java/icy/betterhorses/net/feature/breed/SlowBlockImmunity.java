package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public final class SlowBlockImmunity implements BreedAbility {

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        show(data, wading(horse), 0);
    }

    public static void show(IHorseData data, boolean stuck, int variant) {
        int want = stuck ? BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, variant) : 0;
        data.bh_setSurge(want);
    }

    public static boolean wading(AbstractHorse horse) {
        return snags(horse.level().getBlockState(horse.blockPosition()))
                || snags(horse.getBlockStateOn());
    }

    private static boolean snags(BlockState state) {
        return state.is(Blocks.COBWEB)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.SOUL_SAND)
                || state.is(Blocks.HONEY_BLOCK);
    }

    public static boolean ignoresSlowBlocks(Entity entity) {
        if (entity instanceof AbstractHorse horse) {
            return ignoresSlowBlocks(IHorseData.of(horse));
        }
        return entity.getVehicle() instanceof AbstractHorse mount
                && ignoresSlowBlocks(IHorseData.of(mount));
    }

    public static boolean shrugsOffSlowBlockDamage(Entity entity, DamageSource source) {
        return source.is(DamageTypes.SWEET_BERRY_BUSH) && ignoresSlowBlocks(entity);
    }

    public static boolean ignoresSlowBlocks(IHorseData data) {
        ResourceKey<BreedType> breedKey = data.bh_getBreedKey();
        if (Objects.equals(breedKey, BhContent.PERCHERON.key())) {
            return BhAbility.PERCHERON_MOMENTUM.on();
        }
        if (Objects.equals(breedKey, BhContent.ICELANDIC.key())) {
            return BhAbility.ICELANDIC_MOMENTUM.on();
        }
        return false;
    }
}
