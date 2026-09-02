package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.breed.Ironclad;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class HorseProtectionMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void bh_deflectProjectiles(
            net.minecraft.server.level.ServerLevel level,
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile && Ironclad.deflectsProjectiles(IHorseData.of(self))) {
            direct.discard();
            cir.setReturnValue(false);
        }
    }

}
