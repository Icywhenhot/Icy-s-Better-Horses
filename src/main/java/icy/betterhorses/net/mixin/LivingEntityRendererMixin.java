package icy.betterhorses.net.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.client.render.BhMountedHorseVisibility;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fades the horse out when the rider looks down. 1.21.1 implementation:
 *   - HEAD of render(): push opacity onto BhMountedHorseVisibility's thread-local.
 *   - Swap render type to entityTranslucent when faded so alpha blending is enabled.
 *   - ModifyArg on EntityModel.renderToBuffer() index=4 (the int packed color/alpha): scale
 *     the alpha component by the pushed opacity.
 *   - RETURN of render(): pop the thread-local back to 1.0.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"))
    private void bh_pushHorseOpacity(LivingEntity entity, float yaw, float partialTicks,
                                     PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                     CallbackInfo ci) {
        if (entity instanceof AbstractHorse horse) {
            BhMountedHorseVisibility.pushOpacity(BhMountedHorseVisibility.getOpacity(horse));
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"))
    private void bh_popHorseOpacity(LivingEntity entity, float yaw, float partialTicks,
                                    PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                    CallbackInfo ci) {
        if (entity instanceof AbstractHorse) {
            BhMountedHorseVisibility.popOpacity();
        }
    }

    @ModifyArg(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"),
               index = 4)
    private int bh_applyOpacityToColor(int color) {
        float opacity = BhMountedHorseVisibility.currentOpacity();
        if (opacity >= 1.0F || opacity <= 0.0F) {
            return color;
        }
        int origAlpha = (color >>> 24) & 0xFF;
        int newAlpha = Math.round(origAlpha * opacity) & 0xFF;
        return (color & 0x00FFFFFF) | (newAlpha << 24);
    }

    @Inject(method = "getRenderType(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;",
            at = @At("RETURN"),
            cancellable = true)
    private void bh_translucentForFadedHorse(LivingEntity entity, boolean bodyVisible, boolean translucent, boolean glowing,
                                             CallbackInfoReturnable<RenderType> cir) {
        if (!(entity instanceof AbstractHorse horse)) {
            return;
        }
        float opacity = BhMountedHorseVisibility.getOpacity(horse);
        if (opacity >= 1.0F || opacity <= 0.0F) {
            return;
        }
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, ?> self = (LivingEntityRenderer<LivingEntity, ?>) (Object) this;
        cir.setReturnValue(RenderType.entityTranslucent(self.getTextureLocation(entity)));
    }
}
