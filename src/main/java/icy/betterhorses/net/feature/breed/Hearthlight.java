package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import icy.betterhorses.net.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class Hearthlight implements BreedAbility {


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
            clear(level, horse);
            glow(data, false);
            return;
        }
        follow(level, horse);
        glow(data, lit != null && horse.getControllingPassenger() instanceof Player);
    }

    private void follow(ServerLevel level, AbstractHorse horse) {
        BlockPos want = spot(level, horse);
        if (want == null) {
            clear(level, horse);
            return;
        }
        if (!want.equals(lit)) clear(level, horse);
        ModBlocks.HEARTHLIGHT.hold(level, want, horse.getUUID());
        lit = want;
    }

    private static @Nullable BlockPos spot(ServerLevel level, AbstractHorse horse) {
        BlockPos head = BlockPos.containing(horse.getEyePosition());
        for (BlockPos pos : new BlockPos[]{head, head.above(), head.below(), horse.blockPosition()}) {
            BlockState at = level.getBlockState(pos);
            if (at.isAir() || at.is(ModBlocks.HEARTHLIGHT)) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        if (horse.level() instanceof ServerLevel level) {
            clear(level, horse);
        }
    }

    private static void glow(IHorseData data, boolean on) {
        int packed = data.bh_getSurge();
        int want = on ? BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, 0) : 0;
        if (packed != want) {
            data.bh_setSurge(want);
        }
    }

    private void clear(ServerLevel level, AbstractHorse horse) {
        if (lit == null) return;
        ModBlocks.HEARTHLIGHT.release(level, lit, horse.getUUID());
        lit = null;
    }
}
