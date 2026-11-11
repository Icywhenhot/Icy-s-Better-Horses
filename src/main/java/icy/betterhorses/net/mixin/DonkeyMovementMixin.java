package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import icy.betterhorses.net.feature.breed.WinterFeet;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class DonkeyMovementMixin {

    @ModifyExpressionValue(method = "travelInAir", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    private float bh_iceTraction(float friction) {
        return WinterFeet.friction((LivingEntity) (Object) this, friction);
    }
}
