package icy.betterhorses.net.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LeafPassthroughMixin {

    // getBlock() is directly on BlockBehaviour.BlockStateBase — safe to @Shadow
    @Shadow public abstract net.minecraft.world.level.block.Block getBlock();

    /**
     * Makes leaf blocks have no collision when the entity interacting is a horse
     * or a player/entity riding a horse. Runs on both client and server so
     * client-side movement prediction also skips leaves.
     */
    @Inject(
        method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        at = @At("HEAD"), cancellable = true
    )
    private void bh_leafPassthrough(BlockGetter level, BlockPos pos, CollisionContext context,
                                     CallbackInfoReturnable<VoxelShape> cir) {
        if (!(getBlock() instanceof LeavesBlock)) return;

        if (!(context instanceof EntityCollisionContext ecc)) return;
        Entity entity = ecc.getEntity(); // returns Entity directly in 1.21
        if (entity == null) return;

        boolean horseMounted = entity instanceof AbstractHorse
                || entity.getVehicle() instanceof AbstractHorse;
        if (horseMounted) {
            cir.setReturnValue(Shapes.empty());
        }
    }
}
