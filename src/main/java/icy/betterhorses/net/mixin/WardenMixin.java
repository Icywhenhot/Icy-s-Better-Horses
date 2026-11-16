package icy.betterhorses.net.mixin;

import icy.betterhorses.net.feature.breed.SculkHush;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Warden.class)
public abstract class WardenMixin {

    @Inject(method = "canTargetEntity", at = @At("HEAD"), cancellable = true)
    private void bh_ignoreSculkHorses(@Nullable Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity != null && SculkHush.hiddenFrom(entity)) {
            cir.setReturnValue(false);
        }
    }
}
