package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.BhMountedHorseVisibility;
import icy.betterhorses.net.client.render.BhRenderContext;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseMarkingLayer.class)
public abstract class HorseMarkingLayerMixin extends RenderLayer<HorseRenderState, HorseModel> {

    protected HorseMarkingLayerMixin(RenderLayerParent<HorseRenderState, HorseModel> renderer) {
        super(renderer);
    }

    @Inject(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HorseRenderState;FF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void bh_skipFullyTransparentMarkings(
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.SubmitNodeCollector collector,
            int light,
            HorseRenderState state,
            float yRot,
            float xRot,
            CallbackInfo ci) {
        if (BhRenderContext.currentOpacity() <= 0.01F) {
            ci.cancel();
        }
    }

    // fade the coat pattern along with the rest of the horse when the rider looks down
    @Redirect(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HorseRenderState;FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private void bh_applyHorseOpacityToMarkings(
            OrderedSubmitNodeCollector collector,
            Model<Object> model,
            Object state,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        collector.submitModel(
                model,
                state,
                poseStack,
                renderType,
                lightCoords,
                overlayCoords,
                BhMountedHorseVisibility.applyOpacity(-1, BhRenderContext.currentOpacity()),
                null,
                outlineColor,
                crumblingOverlay);
    }
}
