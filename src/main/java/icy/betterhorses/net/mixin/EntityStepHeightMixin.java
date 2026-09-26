package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityStepHeightMixin {

    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void bh_addStepHeightAttribute(CallbackInfoReturnable<Float> cir) {
        if (!((Entity) (Object) this instanceof LivingEntity living)) {
            return;
        }
        AttributeInstance instance = living.getAttribute(BhAttributes.STEP_HEIGHT_ADDITION);
        if (instance == null) {
            return;
        }
        cir.setReturnValue(Math.max(0.0F, cir.getReturnValueF() + (float) instance.getValue()));
    }
}
