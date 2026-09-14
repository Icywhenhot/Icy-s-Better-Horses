package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.ChargeShakeController;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow public abstract float getXRot();

    @Shadow public abstract float getYRot();

    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "setup", at = @At("TAIL"))
    private void bh_applyChargeShake(BlockGetter level, Entity entity, boolean detached,
                                     boolean mirrored, float partialTick, CallbackInfo ci) {
        float yaw = getYRot() + ChargeShakeController.yawOffset();
        float pitch = getXRot() + ChargeShakeController.pitchOffset();
        setRotation(yaw, pitch);
    }
}
