package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.breed.SlowBlockImmunity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntitySlowBlockMixin {

    @Inject(method = "getBlockSpeedFactor", at = @At("HEAD"), cancellable = true)
    private void bh_ignoreSlowBlocks(CallbackInfoReturnable<Float> cir) {
        Object self = this;
        if (self instanceof AbstractHorse horse
                && SlowBlockImmunity.ignoresSlowBlocks(IHorseData.of(horse))) {
            cir.setReturnValue(1.0F);
        }
    }
}
