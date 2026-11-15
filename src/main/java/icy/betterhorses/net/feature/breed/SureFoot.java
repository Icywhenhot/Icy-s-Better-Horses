package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.entity.BhBreedMule;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SureFoot {

    private static final double STEP = 0.05D;
    private static final double DROP = 4.0D;

    private SureFoot() {}

    public static Vec3 hold(Entity entity, MoverType type, Vec3 move) {
        if (type != MoverType.SELF || move.y > 0.0D || !(entity instanceof BhBreedMule mule)
                || !mule.onGround() || !(mule.getControllingPassenger() instanceof Player)
                || !BhAbility.MULE_SURE_FOOT.on()) {
            return move;
        }
        boolean scorched = hot(mule, 0.0D, 0.0D);
        double x = move.x;
        double z = move.z;
        while (x != 0.0D && unsafe(mule, x, 0.0D, scorched)) x = shrink(x);
        while (z != 0.0D && unsafe(mule, 0.0D, z, scorched)) z = shrink(z);
        while (x != 0.0D && z != 0.0D && unsafe(mule, x, z, scorched)) {
            x = shrink(x);
            z = shrink(z);
        }
        return x == move.x && z == move.z ? move : new Vec3(x, move.y, z);
    }

    private static double shrink(double v) {
        return Math.abs(v) <= STEP ? 0.0D : v - Math.copySign(STEP, v);
    }

    private static boolean unsafe(Entity e, double x, double z, boolean scorched) {
        AABB box = e.getBoundingBox();
        AABB below = new AABB(box.minX + 0.001D + x, box.minY - DROP - 0.001D, box.minZ + 0.001D + z,
                box.maxX - 0.001D + x, box.minY, box.maxZ - 0.001D + z);
        if (e.level().noCollision(e, below)
                && e.level().getBlockStatesIfLoaded(below).noneMatch(s -> s.getFluidState().is(FluidTags.WATER))) {
            return true;
        }
        return !scorched && hot(e, x, z);
    }

    private static boolean hot(Entity e, double x, double z) {
        AABB box = e.getBoundingBox();
        AABB feet = new AABB(box.minX + x, box.minY - 0.5D, box.minZ + z, box.maxX + x, box.minY + 0.5D, box.maxZ + z);
        return e.level().getBlockStatesIfLoaded(feet).anyMatch(SureFoot::burns);
    }

    private static boolean burns(BlockState state) {
        return state.getFluidState().is(FluidTags.LAVA) || state.is(BlockTags.FIRE) || state.is(Blocks.MAGMA_BLOCK)
                || state.is(BlockTags.CAMPFIRES) && state.getValue(CampfireBlock.LIT);
    }
}
