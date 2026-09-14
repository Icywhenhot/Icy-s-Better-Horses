package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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

    public static boolean ignoresSlowBlocks(IHorseData data) {
        return switch (data.bh_getBreed()) {
            case PERCHERON -> BhAbility.PERCHERON_MOMENTUM.on();
            case ICELANDIC -> BhAbility.ICELANDIC_MOMENTUM.on();
            default -> false;
        };
    }
}
