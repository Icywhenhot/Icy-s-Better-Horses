package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.client.render.IBhRiderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererRiderMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("RETURN"))
    private void bh_captureRiddenHorse(LivingEntity entity, LivingEntityRenderState state,
                                       float partialTick, CallbackInfo ci) {
        if (entity.getVehicle() instanceof AbstractHorse horse) {
            ((IBhRiderState) state).bh_setRiddenHorse(horse.getId(),
                    Mth.rotLerp(partialTick, horse.yBodyRotO, horse.yBodyRot),
                    IHorseData.of(horse).bh_hasCartGear());
        } else {
            ((IBhRiderState) state).bh_setRiddenHorse(-1, 0.0F, false);
        }
        HorseCartEntity cart = entity.getVehicle() instanceof HorseCartEntity carrying ? carrying
                : entity.getVehicle() instanceof AbstractHorse puller && IHorseData.of(puller).bh_hasCartGear()
                ? IHorseData.of(puller).bh_getCartEntity() : null;
        if (cart != null && !cart.isRemoved()) {
            Vec3 shift = cart.renderSeat(entity, partialTick).subtract(entity.getPosition(partialTick))
                    .add(0.0D, cart.bedBounce(), 0.0D);
            ((IBhRiderState) state).bh_setCartSeat(shift, cart.gluedRenderYaw(partialTick),
                    cart.renderTilt(partialTick) + cart.bedRock(),
                    (float) entity.getVehicleAttachmentPoint(entity.getVehicle()).y);
        } else {
            ((IBhRiderState) state).bh_setCartSeat(null, 0.0F, 0.0F, 0.0F);
        }
    }

}
