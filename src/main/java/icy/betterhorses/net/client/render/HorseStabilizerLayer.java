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
import net.minecraft.client.renderer.state.CameraRenderState;

/**
 * Stabilizer wing layer for 1.21.11 + GeckoLib 5.
 *
 * The 1.21.5+ render pipeline split entity rendering into "extract → submit" phases:
 * by the time {@code RenderLayer.submit} runs, the live {@code AbstractHorse} is no longer
 * reachable, only the {@code RenderState}. We work around that with three pieces of plumbing:
 * <ol>
 *   <li>{@link IBhEquineStabilizerState} carries the gear flag, stabilizer state, entity id
 *       and partial tick on the render state — populated by
 *       {@code AbstractHorseRendererMixin.bh_captureStabilizerState}.</li>
 *   <li>{@link BhRenderContext} captures the {@code CameraRenderState} from the enclosing
 *       {@code LivingEntityRenderer.submit} call (via {@code LivingEntityRendererSubmitMixin}).
 *       GeckoLib 5 needs it for {@code submit}; the vanilla layer signature
 *       doesn't carry it.</li>
 *   <li>This class consumes both, positions the pose stack to anchor the wings to the horse's
 *       body bone (same offsets as the 1.21.0 implementation), then hands off to
 *       {@code GeoObjectRenderer.submit}.</li>
 * </ol>
 */
public final class HorseStabilizerLayer<S extends EquineRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {

    private static final HorseStabilizerGeoRenderer GEO_RENDERER = new HorseStabilizerGeoRenderer();

    private static final double TORSO_X_OFFSET = 8.0D / 16.0D;
    private static final double FEET_Y_IN_FLIPPED_FRAME = 1.85D;
    private static final double TORSO_Z_OFFSET = -8.3D / 16.0D;
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
        int horseId = bhState.bh_getHorseId();

        if (!bhState.bh_hasStabilizer()) {
            return;
        }
        // Previously: skipped whenever the horse was even slightly faded (e.g. mounted-view fade).
        // That made wings invisible the entire time the player was riding. Only skip when the horse
        // is essentially invisible (camera clipped through it).
        if (BhRenderContext.currentOpacity() < 0.05F) {
            return;
        }

        CameraRenderState camera = BhRenderContext.currentCamera();
        if (camera == null) {
            // Outside a LivingEntityRenderer.submit scope (shouldn't happen for a horse layer,
            // but defensively skip rather than NPE).
            return;
        }

        HorseStabilizerAnimatable animatable = HorseStabilizerAnimatable.getById(horseId);
        if (animatable == null) {
            return;
        }

        ModelPart body = ((HorseModelAccessor) this.getParentModel()).bh_getBody();

        poseStack.pushPose();
        body.translateAndRotate(poseStack);
        poseStack.translate(
                -body.x / 16.0F + TORSO_X_OFFSET,
                FEET_Y_IN_FLIPPED_FRAME - body.y / 16.0F,
                -body.z / 16.0F + TORSO_Z_OFFSET);
        poseStack.mulPose(Axis.ZP.rotationDegrees(MODEL_ROLL_DEGREES));

        // GeckoLib 5.3 entry point. The renderer creates and fills its own GeoRenderState
        // internally; we hand it the camera + collector + light. Unlike 5.4 (which introduced an
        // adjustRenderPose hook with a default +0.5/+0.51/+0.5 standalone-object centering that we
        // had to suppress), 5.3's submit applies no extra translation, so the body-anchored pose
        // above is used as-is. Animation timing is driven by the animatable's getTick clock; the
        // partialTick captured at extract time supplies sub-tick interpolation.
        GEO_RENDERER.submit(
                poseStack,
                animatable,
                null,
                collector,
                camera,
                packedLight,
                bhState.bh_getPartialTick(),
                null);

        poseStack.popPose();
    }
}
