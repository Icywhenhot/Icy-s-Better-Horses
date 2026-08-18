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

/**
 * Glues a rider's <em>model</em> to the horse's animated saddle, without moving the rider.
 *
 * <p>A passenger's entity position drives both its model and its camera, so actually moving the
 * rider to keep it attached would drag the player's point of view through every bob, bank and
 * jump arc. So the attachment point - and therefore the POV - is left exactly as it was, and the
 * offset is applied to the pose stack at draw time only.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererRiderMixin {

    /** 1.0 = fully glued. Pull down if the rider reads as welded rather than seated. */
    @org.spongepowered.asm.mixin.Unique
    private static final float BH_RIDER_FOLLOW = 1.0F;

    /**
     * Whether each in-flight {@code submit} pushed a pose, so the matching return pops exactly
     * once. A stack rather than a boolean because nothing promises these calls never nest.
     */
    @org.spongepowered.asm.mixin.Unique
    private static final ArrayDeque<Boolean> BH_PUSHED = new ArrayDeque<>();

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("RETURN"))
    private void bh_captureRiddenHorse(LivingEntity entity, LivingEntityRenderState state,
                                       float partialTick, CallbackInfo ci) {
        if (entity.getVehicle() instanceof AbstractHorse horse) {
            // Same interpolation the horse's own renderer uses, so the rider turns with the body
            // rather than with the raw entity yaw.
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
        // LivingEntityRendererMixin cancels submit outright for a fully faded horse. A cancel
        // returns without ever reaching a RETURN opcode, so a pose pushed here would never be
        // popped and the whole frame's matrix stack would drift. Mirror its condition and stay
        // out. Only reachable at all when one horse is riding another, which the cart allows.
        if (state instanceof icy.betterhorses.net.client.render.IBhEquineStabilizerState equine
                && equine.bh_getOpacity() <= 0.01F) {
            BH_PUSHED.push(Boolean.FALSE);
            return;
        }

        int horseId = state instanceof IBhRiderState rider ? rider.bh_getRiddenHorseId() : -1;
        BhRiderMotion motion = horseId < 0 ? BhRiderMotion.NONE : BhRiderMotion.get(horseId);
        // Vanilla horses never publish, so they fall through here untouched.
        if (motion.isRest()) {
            BH_PUSHED.push(Boolean.FALSE);
            return;
        }

        float yaw = ((IBhRiderState) state).bh_getRiddenHorseYaw();
        float rad = yaw * Mth.DEG_TO_RAD;
        float cos = Mth.cos(rad);
        float sin = Mth.sin(rad);

        // At yaw 0 a horse faces +z, its right is -x. So right = (-cos, 0, -sin) and
        // forward = (-sin, 0, cos).
        float right = motion.right() * BH_RIDER_FOLLOW;
        float forward = motion.forward() * BH_RIDER_FOLLOW;

        poseStack.pushPose();
        BH_PUSHED.push(Boolean.TRUE);

        poseStack.translate(
                right * -cos + forward * -sin,
                motion.up() * BH_RIDER_FOLLOW,
                right * -sin + forward * cos);

        // Rotate about the saddle, which is where the rider's own origin already sits. The
        // (180 - yaw) basis is the one the horse's model is drawn in; the pitch sign is flipped
        // because that basis also carries the model renderer's scale(-1, -1, 1), and conjugating
        // a rotation about X by that flip negates the angle. Roll survives it unchanged, because
        // the flip negates both of the axes a Z rotation acts on.
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
