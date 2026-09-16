package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.HorseBreed;
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
            Identifier.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "horse_breakable"));

    public static final double MIN_SPEED = 0.30D;
    private static final double REACH = 1.2D;
    private static final int WALL_HEIGHT = 3;
    private static final int WINDUP = 8;
    private static final int COOLDOWN = 600;
    private static final float SELF_DAMAGE = 4.0F;

    private final int badge;
    private int cooldown;
    private int charged;
    private Vec3 heading = Vec3.ZERO;

    public static boolean breaks(BlockState state) {
        return !state.isAir() && state.is(BREAKABLE);
    }

    public static BhAbility gate(HorseBreed breed) {
        return breed == HorseBreed.SHIRE ? BhAbility.SHIRE_BRICK : BhAbility.BELGIAN_BRICK;
    }

    public BrickBreak() {
        this(0);
    }

    public BrickBreak(int badge) {
        this.badge = badge;
    }

    private static boolean wall(ServerLevel level, Vec3 nose, double footY) {
        BlockPos foot = BlockPos.containing(nose.x, footY, nose.z);
        for (int up = 0; up < WALL_HEIGHT; up++) {
            BlockPos pos = foot.above(up);
            if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (!(badge == 0 ? BhAbility.BELGIAN_BRICK : BhAbility.SHIRE_BRICK).on()
                || BhHorseTraits.bondTier(data.bh_getBond()) < 2
                || !(horse.level() instanceof ServerLevel level)
                || !(horse.getControllingPassenger() instanceof Player rider)) {
            return;
        }

        Vec3 motion = horse.getKnownMovement();
        Vec3 flat = new Vec3(motion.x, 0.0D, motion.z);
        if (flat.length() >= MIN_SPEED) {
            heading = flat.normalize();
            charged = WINDUP;
        } else if (charged > 0) {
            charged--;
        }
        if (charged <= 0 || heading.lengthSqr() < 1.0E-4D) {
            return;
        }

        Vec3 nose = horse.position().add(heading.scale(REACH));
        if (!wall(level, nose, horse.getY())) {
            return;
        }

        Vec3 side = new Vec3(-heading.z, 0.0D, heading.x);
        boolean broke = false;
        for (int across = -1; across <= 1; across++) {
            for (int up = 0; up < WALL_HEIGHT; up++) {
                BlockPos pos = BlockPos.containing(
                        nose.add(side.scale(across)).add(0.0D, up, 0.0D));
                BlockState st = level.getBlockState(pos);
                if (st.isAir() || !st.is(BREAKABLE)) {
                    continue;
                }
                if (rider instanceof ServerPlayer sp && !level.mayInteract(sp, pos)) {
                    continue;
                }
                level.destroyBlock(pos, true, horse);
                broke = true;
            }
        }
        if (broke) {
            cooldown = COOLDOWN;
            DamageSource src = level.damageSources().generic();
            horse.hurtServer(level, src, SELF_DAMAGE);
            BhSurge.pulse(data, 0, badge);
            charged = 0;
        }
    }
}
