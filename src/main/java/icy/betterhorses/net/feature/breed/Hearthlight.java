package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class Hearthlight implements BreedAbility {

    private static final int LEVEL = 10;
    private static final int INTERVAL = 5;

    private @Nullable BlockPos lit;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.level() instanceof ServerLevel level)) {
            return;
        }
        if (BhHorseTraits.bondTier(data.bh_getBond()) < 2 || level.isBrightOutside()) {
            clear(level);
            return;
        }
        if (horse.tickCount % INTERVAL != 0) {
            return;
        }

        BlockPos want = horse.blockPosition().above();
        if (want.equals(lit)) {
            return;
        }
        clear(level);
        if (level.getBlockState(want).isAir()) {
            level.setBlockAndUpdate(want, Blocks.LIGHT.defaultBlockState()
                    .setValue(LightBlock.LEVEL, LEVEL));
            lit = want;
        }
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        if (horse.level() instanceof ServerLevel level) {
            clear(level);
        }
    }

    private void clear(ServerLevel level) {
        if (lit == null) {
            return;
        }
        BlockState state = level.getBlockState(lit);
        if (state.is(Blocks.LIGHT)) {
            level.setBlockAndUpdate(lit, Blocks.AIR.defaultBlockState());
        }
        lit = null;
    }
}
