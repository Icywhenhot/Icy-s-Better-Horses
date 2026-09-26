package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySwimSpeedMixin {

    @ModifyArg(
            method = "travel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V",
                    ordinal = 0),
            index = 0)
    private float bh_scaleSwimAccel(float amount) {
        LivingEntity self = (LivingEntity) (Object) this;
        AttributeInstance instance = self.getAttribute(BhAttributes.SWIM_SPEED);
        if (instance == null) {
            return amount;
        }
        return (float) (amount * instance.getValue());
    }
}
