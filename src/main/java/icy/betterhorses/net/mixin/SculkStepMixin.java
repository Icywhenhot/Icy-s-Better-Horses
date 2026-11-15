package icy.betterhorses.net.mixin;

import icy.betterhorses.net.feature.breed.SculkHush;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SculkSensorBlock.class, SculkShriekerBlock.class})
public abstract class SculkStepMixin {

    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void bh_hushedHooves(Level level, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (SculkHush.hushed(entity)) {
            ci.cancel();
        }
    }
}
