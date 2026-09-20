package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies swim speed by scaling horizontal movement after {@code travel()} runs.
 * Simpler than hooking mid-method like Forge, and the same for steady swimming.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySwimSpeedMixin {

    @Inject(method = "travel", at = @At("RETURN"))
    private void bh_applySwimSpeed(Vec3 input, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isInWater() || self.isFallFlying()) {
            return;
        }
        AttributeInstance instance = self.getAttribute(BhAttributes.SWIM_SPEED);
        if (instance == null) {
            return;
        }
        double factor = instance.getValue();
        if (factor == 1.0D) {
            return;
        }
        Vec3 motion = self.getDeltaMovement();
        self.setDeltaMovement(motion.x * factor, motion.y, motion.z * factor);
    }
}
