package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.BhMountedHorseVisibility;
import icy.betterhorses.net.client.render.BhRenderContext;
import icy.betterhorses.net.client.render.IBhEquineStabilizerState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Redirect(
            method = "getRenderType",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderType(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType bh_useTranslucentHorseRenderType(
            EntityModel<?> model,
            Identifier texture,
            LivingEntityRenderState renderState,
            boolean visible,
            boolean translucent,
            boolean glowing) {
        if (renderState instanceof IBhEquineStabilizerState bhState) {
            float opacity = bhState.bh_getOpacity();
            if (opacity > 0.0F && opacity < 1.0F) {
                return RenderTypes.entityTranslucent(texture);
            }
        }

        return model.renderType(texture);
    }

    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"),
            index = 6)
    private int bh_applyHorseOpacityToMainModel(int colorArgb) {
        return BhMountedHorseVisibility.applyOpacity(colorArgb, BhRenderContext.currentOpacity());
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void bh_skipFullyTransparentHorse(
            LivingEntityRenderState renderState,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera,
            CallbackInfo ci) {
        if (renderState instanceof IBhEquineStabilizerState bhState && bhState.bh_getOpacity() <= 0.01F) {
            ci.cancel();
        }
    }
}
