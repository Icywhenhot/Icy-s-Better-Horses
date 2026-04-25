package icy.betterhorses.net.item;

import icy.betterhorses.net.HitchpostPlacementTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;

/**
 * A fence-backed hitchpost that can exist in a temporary pending state until it is confirmed by
 * tethering a horse.
 */
public class HitchpostBlock extends FenceBlock {

    public static final BooleanProperty CONFIRMED = BooleanProperty.create("confirmed");

    public HitchpostBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CONFIRMED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CONFIRMED);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            releaseLeashedMobs(level, pos);
            HitchpostPlacementTracker.clearPendingPlacementAt(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    public static void releaseLeashedMobs(Level level, BlockPos pos) {
        AABB bounds = new AABB(pos).inflate(1.5D);

        for (Mob mob : level.getEntitiesOfClass(Mob.class, bounds)) {
            Entity leashHolder = mob.getLeashHolder();
            if (leashHolder instanceof LeashFenceKnotEntity knot && knot.blockPosition().equals(pos)) {
                mob.dropLeash(true, false);
            }
        }

        for (LeashFenceKnotEntity knot : level.getEntitiesOfClass(LeashFenceKnotEntity.class, bounds)) {
            if (knot.blockPosition().equals(pos)) {
                knot.discard();
            }
        }
    }
}
