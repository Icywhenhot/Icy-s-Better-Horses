package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.ChargeShakeController;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow public abstract float xRot();

    @Shadow public abstract float yRot();

    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "update", at = @At("TAIL"))
    private void bh_applyChargeShake(DeltaTracker deltaTracker, CallbackInfo ci) {
        float yaw = yRot() + ChargeShakeController.yawOffset();
        float pitch = xRot() + ChargeShakeController.pitchOffset();
        setRotation(yaw, pitch);
    }
}
