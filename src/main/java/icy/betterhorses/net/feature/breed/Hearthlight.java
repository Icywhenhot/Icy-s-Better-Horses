package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class Hearthlight implements BreedAbility {

    private static final int LEVEL = 10;

    private @Nullable BlockPos lit;
    private boolean hauling;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.level() instanceof ServerLevel level)) {
            return;
        }
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        boolean cart = tier >= 1 && BhAbility.HAFLINGER_HAUL.on() && data.bh_hasCartGear()
                && horse.getControllingPassenger() instanceof Player;
        if (cart && !hauling) {
            BhSurge.pulse(data, 0, 1);
        }
        hauling = cart;

        if (tier < 2 || !BhAbility.HAFLINGER_LIGHT.on() || level.isBrightOutside()) {
            clear(level);
            glow(data, false);
            return;
        }
        follow(level, horse);
        glow(data, lit != null && horse.getControllingPassenger() instanceof Player);
    }

    private void follow(ServerLevel level, AbstractHorse horse) {
        BlockPos want = spot(level, horse);
        if (want == null || want.equals(lit)) {
            return;
        }
        BlockPos was = lit;
        level.setBlockAndUpdate(want, Blocks.LIGHT.defaultBlockState()
                .setValue(LightBlock.LEVEL, LEVEL));
        lit = want;
        if (was != null && level.getBlockState(was).is(Blocks.LIGHT)) {
            level.setBlockAndUpdate(was, Blocks.AIR.defaultBlockState());
        }
    }

    private static @Nullable BlockPos spot(ServerLevel level, AbstractHorse horse) {
        BlockPos head = BlockPos.containing(horse.getEyePosition());
        for (BlockPos pos : new BlockPos[]{head, head.above(), head.below(), horse.blockPosition()}) {
            BlockState at = level.getBlockState(pos);
            if (at.isAir() || at.is(Blocks.LIGHT)) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        if (horse.level() instanceof ServerLevel level) {
            clear(level);
        }
    }

    private static void glow(IHorseData data, boolean on) {
        int packed = data.bh_getSurge();
        int want = on ? BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, 0) : 0;
        if (packed != want) {
            data.bh_setSurge(want);
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
