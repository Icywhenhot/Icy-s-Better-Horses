package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileImpactMixin {

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void bh_deflectOffHorse(HitResult hitResult, CallbackInfo ci) {
        if (hitResult instanceof EntityHitResult hit
                && IcysBetterHorses.bh_deflectProjectile((Projectile) (Object) this, hit)) {
            ci.cancel();
        }
    }
}
