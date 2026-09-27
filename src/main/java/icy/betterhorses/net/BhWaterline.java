package icy.betterhorses.net;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

public final class BhWaterline {

    private static final int MAX_DEPTH = 32;

    private BhWaterline() {}

    public static double surface(Level level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(x, y, z).mutable();
        FluidState fluid = level.getFluidState(pos);
        if (!fluid.is(FluidTags.WATER)) {
            return Double.NaN;
        }
        for (int i = 0; i < MAX_DEPTH; i++) {
            FluidState above = level.getFluidState(pos.above());
            if (!above.is(FluidTags.WATER)) {
                return pos.getY() + fluid.getHeight(level, pos);
            }
            pos.move(0, 1, 0);
            fluid = above;
        }
        return pos.getY() + 1.0D;
    }
}
