package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.mixin.HorseModelAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

// stabilizer wing layer for 1.21.11 + GeckoLib 5
public final class HorseStabilizerLayer<S extends EquineRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {

    private static final HorseStabilizerGeoRenderer GEO_RENDERER = new HorseStabilizerGeoRenderer();

    private static final double TORSO_X_OFFSET = 0.1D / 16.0D;
    private static final double FEET_Y_IN_FLIPPED_FRAME = 1.35D;
    private static final double TORSO_Z_OFFSET = -1.0D / 16.0D;
    private static final float MODEL_ROLL_DEGREES = 180.0F;

    public HorseStabilizerLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            S state,
            float yRot,
            float xRot) {
        IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
        if (!bhState.bh_hasStabilizer() || BhRenderContext.currentOpacity() < 0.999F) {
            return;
        }

        CameraRenderState camera = BhRenderContext.currentCamera();
        if (camera == null) {
            // outside a LivingEntityRenderer.submit scope (shouldn't happen for a horse layer
            return;
        }

        HorseStabilizerAnimatable animatable = HorseStabilizerAnimatable.getById(bhState.bh_getHorseId());
        if (animatable == null) {
            return;
        }

        ModelPart body = ((HorseModelAccessor) this.getParentModel()).bh_getBody();

        poseStack.pushPose();
        body.translateAndRotate(poseStack);
        poseStack.translate(
                -body.x / 16.0F,
                FEET_Y_IN_FLIPPED_FRAME - body.y / 16.0F,
                -body.z / 16.0F + TORSO_Z_OFFSET);
        poseStack.mulPose(Axis.ZP.rotationDegrees(MODEL_ROLL_DEGREES));

        // GeckoLib 5 entry point. the renderer creates and fills its own GeoRenderState internally
        GEO_RENDERER.performRenderPass(
                animatable,
                null,
                poseStack,
                collector,
                camera,
                packedLight,
                0);

        poseStack.popPose();
    }
}
