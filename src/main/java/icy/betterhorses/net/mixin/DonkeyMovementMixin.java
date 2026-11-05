package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import icy.betterhorses.net.feature.breed.WinterFeet;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class DonkeyMovementMixin {

    @ModifyExpressionValue(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F"))
    private float bh_iceTraction(float friction) {
        return WinterFeet.friction((LivingEntity) (Object) this, friction);
    }
}
