package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class ProjectileImpactMixin {

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void bh_deflectOffHorse(EntityHitResult hit, CallbackInfo ci) {
        if (IcysBetterHorses.bh_deflectProjectile((Projectile) (Object) this, hit)) {
            ci.cancel();
        }
    }
}
