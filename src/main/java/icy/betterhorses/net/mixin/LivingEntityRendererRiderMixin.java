package icy.betterhorses.net.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.client.render.BhRiderMotion;
import icy.betterhorses.net.client.render.IBhRiderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererRiderMixin {

    @org.spongepowered.asm.mixin.Unique
    private static final float BH_RIDER_FOLLOW = 1.0F;

    @org.spongepowered.asm.mixin.Unique
    private static final ArrayDeque<Boolean> BH_PUSHED = new ArrayDeque<>();

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("RETURN"))
    private void bh_captureRiddenHorse(LivingEntity entity, LivingEntityRenderState state,
                                       float partialTick, CallbackInfo ci) {
        if (entity.getVehicle() instanceof AbstractHorse horse) {
            ((IBhRiderState) state).bh_setRiddenHorse(horse.getId(),
                    Mth.rotLerp(partialTick, horse.yBodyRotO, horse.yBodyRot));
        } else {
            ((IBhRiderState) state).bh_setRiddenHorse(-1, 0.0F);
        }
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"))
    private void bh_offsetRiderToSaddle(LivingEntityRenderState state, PoseStack poseStack,
                                        SubmitNodeCollector collector, CameraRenderState camera,
                                        CallbackInfo ci) {
        if (state instanceof icy.betterhorses.net.client.render.IBhEquineStabilizerState equine
                && equine.bh_getOpacity() <= 0.01F) {
            BH_PUSHED.push(Boolean.FALSE);
            return;
        }

        int horseId = state instanceof IBhRiderState rider ? rider.bh_getRiddenHorseId() : -1;
        BhRiderMotion motion = horseId < 0 ? BhRiderMotion.NONE : BhRiderMotion.get(horseId);
        net.minecraft.world.phys.Vec3 seat =
                horseId < 0 ? net.minecraft.world.phys.Vec3.ZERO
                            : icy.betterhorses.net.BhRiderSeat.applied(horseId);
        if (motion.isRest() && seat.lengthSqr() == 0.0D) {
            BH_PUSHED.push(Boolean.FALSE);
            return;
        }

        float yaw = ((IBhRiderState) state).bh_getRiddenHorseYaw();
        float rad = yaw * Mth.DEG_TO_RAD;
        float cos = Mth.cos(rad);
        float sin = Mth.sin(rad);

        float right = motion.right() * BH_RIDER_FOLLOW;
        float forward = motion.forward() * BH_RIDER_FOLLOW;

        poseStack.pushPose();
        BH_PUSHED.push(Boolean.TRUE);

        poseStack.translate(
                right * -cos + forward * -sin - seat.x,
                motion.up() * BH_RIDER_FOLLOW - seat.y,
                right * -sin + forward * cos - seat.z);

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotation(-motion.pitch() * BH_RIDER_FOLLOW));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(motion.roll() * BH_RIDER_FOLLOW));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw - 180.0F));
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("RETURN"))
    private void bh_restoreRiderPose(LivingEntityRenderState state, PoseStack poseStack,
                                     SubmitNodeCollector collector, CameraRenderState camera,
                                     CallbackInfo ci) {
        if (!BH_PUSHED.isEmpty() && BH_PUSHED.pop()) {
            poseStack.popPose();
        }
    }
}
