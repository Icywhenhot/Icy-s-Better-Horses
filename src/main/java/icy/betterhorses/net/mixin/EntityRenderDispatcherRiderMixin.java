package icy.betterhorses.net.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.BhRiderSeat;
import icy.betterhorses.net.client.render.BhRiderMotion;
import icy.betterhorses.net.client.render.IBhRiderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import com.mojang.math.Axis;
import icy.betterhorses.net.client.render.IBhEquineStabilizerState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherRiderMixin {

    @Unique
    private static final float BH_RIDER_FOLLOW = 1.0F;

    @Unique
    private static final ArrayDeque<Boolean> BH_PUSHED = new ArrayDeque<>();

    @Inject(
            method = "submit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", shift = At.Shift.BEFORE))
    private void bh_offsetRiderToSaddle(EntityRenderState state, CameraRenderState camera, double x, double y, double z,
                                        PoseStack poseStack, SubmitNodeCollector collector,
                                        CallbackInfo ci) {
        if (state instanceof IBhEquineStabilizerState equine
                && equine.bh_getOpacity() <= 0.01F) {
            BH_PUSHED.push(Boolean.FALSE);
            return;
        }

        Vec3 cartShift = state instanceof IBhRiderState seated ? seated.bh_getCartShift() : null;
        if (cartShift != null) {
            IBhRiderState seated = (IBhRiderState) state;
            float pivot = seated.bh_getCartPivot();
            poseStack.pushPose();
            BH_PUSHED.push(Boolean.TRUE);
            poseStack.translate(cartShift.x, cartShift.y + pivot, cartShift.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - seated.bh_getCartYaw()));
            poseStack.mulPose(Axis.XP.rotationDegrees(seated.bh_getCartPitch()));
            poseStack.mulPose(Axis.YP.rotationDegrees(seated.bh_getCartYaw() - 180.0F));
            poseStack.translate(0.0F, -pivot, 0.0F);
            return;
        }

        int horseId = state instanceof IBhRiderState rider ? rider.bh_getRiddenHorseId() : -1;
        boolean onCart = state instanceof IBhRiderState rider && rider.bh_isRidingCart();
        BhRiderMotion motion = horseId < 0 || onCart ? BhRiderMotion.NONE : BhRiderMotion.get(horseId);
        Vec3 seat =
                horseId < 0 ? Vec3.ZERO
                            : BhRiderSeat.applied(horseId);
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

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotation(-motion.pitch() * BH_RIDER_FOLLOW));
        poseStack.mulPose(Axis.ZP.rotation(motion.roll() * BH_RIDER_FOLLOW));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 180.0F));
    }

    @Inject(
            method = "submit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", shift = At.Shift.AFTER))
    private void bh_restoreRiderPose(EntityRenderState state, CameraRenderState camera, double x, double y, double z,
                                     PoseStack poseStack, SubmitNodeCollector collector,
                                     CallbackInfo ci) {
        if (!BH_PUSHED.isEmpty() && BH_PUSHED.pop()) {
            poseStack.popPose();
        }
    }
}
