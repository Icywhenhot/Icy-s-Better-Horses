package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.client.ChargeShakeController;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow public abstract float getXRot();

    @Shadow public abstract float getYRot();

    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Shadow public abstract Entity getEntity();

    @ModifyArg(method = "setup", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"), index = 1)
    private double bh_raiseSmallCartView(double y) {
        Entity entity = getEntity();
        if (entity == null || entity != Minecraft.getInstance().player) return y;

        Entity vehicle = entity.getVehicle();
        AbstractHorse horse;
        if (vehicle instanceof AbstractHorse mount) {
            horse = mount;
        } else if (vehicle instanceof HorseCartEntity cart) {
            if (cart.size().isLarge()) return y;
            horse = cart.boundHorse();
        } else {
            return y;
        }
        if (!BhHorseKind.managed(horse)) return y;

        IHorseData data = IHorseData.of(horse);
        return data.bh_hasCartGear() && !data.bh_hasLargeCart()
                && BhBreedData.of(data.bh_getBreedKey()).archetype() == BhContent.DRAFT.get()
                ? y + 0.5D : y;
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void bh_applyChargeShake(BlockGetter level, Entity entity, boolean detached,
                                     boolean mirrored, float partialTick, CallbackInfo ci) {
        if (entity == null || entity != Minecraft.getInstance().player
                || !BhHorseKind.managed(entity.getVehicle())) return;

        float yaw = ChargeShakeController.yawOffset();
        float pitch = ChargeShakeController.pitchOffset();
        if (yaw == 0.0F && pitch == 0.0F) return;

        setRotation(getYRot() + yaw, getXRot() + pitch);
    }
}
