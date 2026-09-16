package icy.betterhorses.net.mixin;

import icy.betterhorses.net.feature.breed.SlowBlockImmunity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntitySlowBlockMixin {

    @Inject(method = "getBlockSpeedFactor", at = @At("HEAD"), cancellable = true)
    private void bh_ignoreSlowBlocks(CallbackInfoReturnable<Float> cir) {
        if (bh_unbogged()) {
            cir.setReturnValue(1.0F);
        }
    }

    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    private void bh_ignoreSnaggingBlocks(BlockState state, Vec3 drag, CallbackInfo ci) {
        if (!state.is(Blocks.COBWEB) && !state.is(Blocks.SWEET_BERRY_BUSH)) {
            return;
        }
        if (bh_unbogged()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean bh_unbogged() {
        return SlowBlockImmunity.ignoresSlowBlocks((Entity) (Object) this);
    }
}
