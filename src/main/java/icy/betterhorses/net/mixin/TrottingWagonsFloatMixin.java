package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhWaterline;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon", remap = false)
public abstract class TrottingWagonsFloatMixin {

    @Unique private static final double BH_DRAFT = 0.6D;
    @Unique private static final double BH_EASE = 0.25D;
    @Unique private static final double BH_MAX_RISE = 0.15D;
    @Unique private static final double BH_GRAVITY = 0.04D;

    @Inject(method = "moveTick", at = @At("HEAD"), require = 0)
    private void bh_floatOnWater(CallbackInfo ci) {
        Entity wagon = (Entity) (Object) this;
        double surface = BhWaterline.surface(wagon.level(), wagon.getX(), wagon.getY(), wagon.getZ());
        if (Double.isNaN(surface)) {
            return;
        }
        double rise = Mth.clamp((surface - BH_DRAFT - wagon.getY()) * BH_EASE, -BH_MAX_RISE, BH_MAX_RISE);
        Vec3 motion = wagon.getDeltaMovement();
        wagon.setDeltaMovement(motion.x, rise + (wagon.isNoGravity() ? 0.0D : BH_GRAVITY), motion.z);
        wagon.setOnGround(true);
    }
}
