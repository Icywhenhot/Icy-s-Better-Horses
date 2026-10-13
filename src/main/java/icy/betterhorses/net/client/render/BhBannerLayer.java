package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class BhBannerLayer<S extends EquineRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

    public BhBannerLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       S state, float yRot, float xRot) {
        if (state.isInvisible || BhRenderContext.currentOpacity() < 1.0F
                || !(getParentModel() instanceof BhHorseModel model)) return;
        var banner = ((IBhEquineStabilizerState) state).bh_getBanner();
        if (banner.isEmpty()) return;

        float[] box = {Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE};
        model.bh_getBody().visit(new PoseStack(), (pose, path, index, cube) -> {
            if (!path.isEmpty()) return;
            box[0] = Math.min(box[0], cube.minY);
            box[1] = Math.min(box[1], cube.minZ);
            box[2] = Math.max(box[2], cube.maxZ);
        });
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        model.bh_getBody().translateAndRotate(poseStack);
        poseStack.translate(0.0F, (box[0] + 2.6F) / 16.0F,
                (box[1] + (box[2] - box[1]) * 0.76F) / 16.0F);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        banner.submit(poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
