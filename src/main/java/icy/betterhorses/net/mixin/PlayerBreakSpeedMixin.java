package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerBreakSpeedMixin {

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void bh_mountedBreakSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        float adjusted = IcysBetterHorses.bh_mountedBreakSpeed(
                (Player) (Object) this, cir.getReturnValueF());
        if (adjusted != cir.getReturnValueF()) {
            cir.setReturnValue(adjusted);
        }
    }
}
