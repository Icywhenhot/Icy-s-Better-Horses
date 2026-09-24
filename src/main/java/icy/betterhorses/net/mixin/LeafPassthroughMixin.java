package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhFeature;
import icy.betterhorses.net.BhHorseKind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public abstract class LeafPassthroughMixin extends Block {

    private LeafPassthroughMixin() {
        super(null);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (BhFeature.LEAF_PASSTHROUGH.on() && context instanceof EntityCollisionContext ecc) {
            Entity entity = ecc.getEntity();
            if (entity != null && (BhHorseKind.managed(entity) || BhHorseKind.managed(entity.getVehicle()))) {
                return Shapes.empty();
            }
        }
        return super.getCollisionShape(state, level, pos, context);
    }
}
