package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhFeature;
import icy.betterhorses.net.BhHorseKind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    @Shadow
    public abstract Block getBlock();

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void bh_leafPassthrough(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (!(getBlock() instanceof LeavesBlock)) return;
        if (BhFeature.LEAF_PASSTHROUGH.on() && context instanceof EntityCollisionContext ecc) {
            Entity entity = ecc.getEntity();
            if (entity != null && (BhHorseKind.managed(entity) || BhHorseKind.managed(entity.getVehicle()))) {
                cir.setReturnValue(Shapes.empty());
            }
        }
    }
}
