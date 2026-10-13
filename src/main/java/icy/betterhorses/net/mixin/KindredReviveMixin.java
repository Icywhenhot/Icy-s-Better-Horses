package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.HorseTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.silvertide.kindred.attachment.KindredData", remap = false)
public abstract class KindredReviveMixin {

    @Inject(method = "setBonded", at = @At("HEAD"), require = 0)
    private static void bh_adoptKindredHorse(Entity entity, @Coerce Object bonded, CallbackInfo ci) {
        if (entity instanceof AbstractHorse horse && !horse.level().isClientSide()
                && !horse.isAddedToLevel() && BhHorseKind.managed(horse)) {
            HorseTracker.adoptRevived(horse);
        }
    }
}
