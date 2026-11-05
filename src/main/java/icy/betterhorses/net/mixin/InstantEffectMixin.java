package icy.betterhorses.net.mixin;

import icy.betterhorses.net.feature.breed.ThickHide;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.effect.HealOrHarmMobEffect")
public abstract class InstantEffectMixin {

    @Inject(method = "applyInstantenousEffect", at = @At("HEAD"), cancellable = true)
    private void bh_refuseInstantHarm(Entity source, Entity indirect, LivingEntity target,
                                     int amplifier, double scale, CallbackInfo ci) {
        if (((MobEffect) (Object) this).getCategory() == MobEffectCategory.HARMFUL && ThickHide.immune(target)) {
            ci.cancel();
        }
    }
}
