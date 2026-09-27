package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhWagonHitch;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon", remap = false)
public abstract class TrottingWagonsHitchMixin {

    @Inject(method = "lambda$tryHitchHorse$0", at = @At("HEAD"), cancellable = true, require = 0)
    private static void bh_allowBreeds(Player player, Mob mob, CallbackInfoReturnable<Boolean> cir) {
        if (mob.getLeashHolder() == player && BhHorseKind.managed(mob)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setHorse", at = @At("HEAD"), require = 0)
    private void bh_markHitched(Mob horse, @Coerce Object side, CallbackInfo ci) {
        if (horse != null && BhHorseKind.managed(horse)) {
            BhWagonHitch.mark(horse, (Entity) (Object) this);
        }
    }
}
