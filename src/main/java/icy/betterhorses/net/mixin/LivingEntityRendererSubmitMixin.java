package icy.betterhorses.net.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.client.render.BhRenderContext;
import icy.betterhorses.net.client.render.IBhEquineStabilizerState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// the vanilla RenderLayer.submit(PoseStack, SubmitNodeCollector, int, s, float
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererSubmitMixin {

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"))
    private void bh_pushCamera(LivingEntityRenderState state, PoseStack poseStack,
                               SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        BhRenderContext.pushCamera(camera);
        float opacity = state instanceof IBhEquineStabilizerState bhState ? bhState.bh_getOpacity() : 1.0F;
        BhRenderContext.pushOpacity(opacity);
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("RETURN"))
    private void bh_clearCameraOnReturn(LivingEntityRenderState state, PoseStack poseStack,
                                        SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        BhRenderContext.clearCamera();
        BhRenderContext.clearOpacity();
    }
}
