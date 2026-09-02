package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BrickBreak implements BreedAbility {

    private static final TagKey<Block> BREAKABLE = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_breakable"));

    private static final double MIN_SPEED = 0.30D;
    private static final int COOLDOWN = 600;
    private static final float SELF_DAMAGE = 4.0F;

    private int cooldown;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (!BhConfig.brickBreakEnabled()
                || BhHorseTraits.bondTier(data.bh_getBond()) < 2
                || !data.bh_isAbilityToggled()
                || !(horse.level() instanceof ServerLevel level)
                || !(horse.getControllingPassenger() instanceof Player rider)) {
            return;
        }

        Vec3 motion = horse.getDeltaMovement();
        Vec3 flat = new Vec3(motion.x, 0.0D, motion.z);
        if (flat.length() < MIN_SPEED) {
            return;
        }

        BlockPos ahead = BlockPos.containing(horse.position().add(flat.normalize().scale(1.2D)));
        boolean broke = false;
        for (BlockPos pos : new BlockPos[]{ahead, ahead.above()}) {
            BlockState st = level.getBlockState(pos);
            if (st.isAir() || !st.is(BREAKABLE)) {
                continue;
            }
            if (rider instanceof ServerPlayer sp
                    && !level.mayInteract(sp, pos)) {
                continue;
            }
            level.destroyBlock(pos, true, horse);
            broke = true;
        }
        if (broke) {
            cooldown = COOLDOWN;
            DamageSource src = level.damageSources().generic();
            horse.hurtServer(level, src, SELF_DAMAGE);
        }
    }
}
