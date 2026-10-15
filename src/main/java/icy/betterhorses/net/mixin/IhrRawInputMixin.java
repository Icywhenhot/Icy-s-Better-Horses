package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.HorseGearController;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.leclowndu93150.horseimprovements.compat.RawMovementInput", remap = false)
public abstract class IhrRawInputMixin {

    @Inject(method = "get", at = @At("RETURN"), cancellable = true, require = 0)
    private static void bh_keepGaitForward(CallbackInfoReturnable<Vec2> cir) {
        float forward = HorseGearController.INSTANCE.driven();
        if (!Float.isNaN(forward)) {
            cir.setReturnValue(new Vec2(cir.getReturnValue().x, forward));
        }
    }
}
