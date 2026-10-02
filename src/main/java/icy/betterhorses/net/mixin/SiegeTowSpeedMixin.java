package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhSiegeTow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "banduty.stoneycore.entity.custom.AbstractSiegeEntity", remap = false)
public abstract class SiegeTowSpeedMixin {

    @Shadow
    public abstract double getVelocity(Entity entity);

    @Inject(method = "getVelocity", at = @At("HEAD"), cancellable = true, require = 0)
    private void bh_pullByClass(Entity rider, CallbackInfoReturnable<Double> cir) {
        if (!(rider instanceof Player)) return;
        if (((Entity) (Object) this).getFirstPassenger() instanceof AbstractHorse horse && BhHorseKind.managed(horse)) {
            cir.setReturnValue(BhSiegeTow.pull(horse, getVelocity(horse)));
        }
    }
}
