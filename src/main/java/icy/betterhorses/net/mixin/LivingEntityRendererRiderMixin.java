package icy.betterhorses.net.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.BhRiderSeat;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.client.render.BhMountedHorseVisibility;
import icy.betterhorses.net.client.render.BhRiderMotion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererRiderMixin {

    @Unique private static final float BH_RIDER_FOLLOW = 1.0F;

    @Unique private static final ArrayDeque<Boolean> BH_PUSHED = new ArrayDeque<>();

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void bh_offsetRiderToSaddle(LivingEntity entity, float yaw, float partialTicks,
                                        PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                        CallbackInfo ci) {
        if (!(entity.getVehicle() instanceof AbstractHorse horse)
                || BhMountedHorseVisibility.getOpacity(horse) <= 0.01F) {
            BH_PUSHED.push(Boolean.FALSE);
            return;
        }

        int horseId = horse.getId();
        BhRiderMotion motion = IHorseData.of(horse).bh_hasCartGear()
                ? BhRiderMotion.NONE
                : BhRiderMotion.get(horseId);
        Vec3 seat = BhRiderSeat.applied(horseId);
        if (motion.isRest() && seat.lengthSqr() == 0.0D) {
            BH_PUSHED.push(Boolean.FALSE);
            return;
        }

        float horseYaw = Mth.rotLerp(partialTicks, horse.yBodyRotO, horse.yBodyRot);
        float rad = horseYaw * Mth.DEG_TO_RAD;
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

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - horseYaw));
        poseStack.mulPose(Axis.XP.rotation(-motion.pitch() * BH_RIDER_FOLLOW));
        poseStack.mulPose(Axis.ZP.rotation(motion.roll() * BH_RIDER_FOLLOW));
        poseStack.mulPose(Axis.YP.rotationDegrees(horseYaw - 180.0F));
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void bh_restoreRiderPose(LivingEntity entity, float yaw, float partialTicks,
                                     PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                     CallbackInfo ci) {
        if (!BH_PUSHED.isEmpty() && BH_PUSHED.pop()) {
            poseStack.popPose();
        }
    }
}
