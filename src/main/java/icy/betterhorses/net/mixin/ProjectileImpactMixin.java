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

    // Every projectile's onHit calls super first, so one hook here catches them all.
    // Cancelling at HEAD skips the hit, same as cancelling Forge's ProjectileImpactEvent.
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void bh_deflectOffHorse(HitResult hitResult, CallbackInfo ci) {
        if (hitResult instanceof EntityHitResult hit
                && IcysBetterHorses.bh_deflectProjectile((Projectile) (Object) this, hit)) {
            ci.cancel();
        }
    }
}
