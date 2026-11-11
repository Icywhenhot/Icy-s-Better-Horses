package icy.betterhorses.net.mixin;

import icy.betterhorses.net.feature.breed.FieldHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @Inject(method = "turnToDirt", at = @At("HEAD"), cancellable = true)
    private static void bh_noTrample(Entity entity, BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (FieldHand.blocksTrample(entity)) {
            ci.cancel();
        }
    }
}
