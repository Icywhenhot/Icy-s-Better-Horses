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

    /**
     * One stabilizer item, but its geometry, texture and anchor depend on which horse wears it.
     *
     * @param feetY   height of the horse model's origin above its feet, in blocks. The brace is
     *                modelled in the horse's own coordinate space measuring up from the feet,
     *                while Minecraft entity models put their origin 24px above the feet - so for
     *                a model authored that way this is exactly 24/16, not a number to tune.
     * @param zOffset fore/aft nudge, needed only where the brace's z does not already match the
     *                body's.
     */
    private record Variant(HorseStabilizerGeoRenderer renderer, double feetY, double zOffset) {}

    // The generic brace keeps its hand-tuned anchor: it was authored at 256x256 in a different
    // frame, so the derived 24/16 does not apply to it. Do not "correct" this to 1.5.
    private static final Variant GENERIC = new Variant(
            new HorseStabilizerGeoRenderer(), 1.35D, -1.0D / 16.0D);

    // st_icelandic's brace cube is byte-identical to the Icelandic horse's body cube
    // (-4,11,-11)->(4,20,7) plus 0.1 inflate, so the anchor is exact and needs no tuning.
    private static final Variant ICELANDIC = new Variant(
            new HorseStabilizerGeoRenderer(new IcelandicStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    // st_freisian's brace is authored in the same absolute Blockbench frame as the Friesian
    // body - same half-width (x +/-5) and same floor (y 15); only its z span is shorter,
    // which is a modelling choice. So it takes the same derived anchor: the 24/16 is the
    // model origin's height above the feet, not a tuned number, and it holds for any brace
    // modelled in the horse's own space regardless of how long the brace itself is.
    private static final Variant FRIESIAN = new Variant(
            new HorseStabilizerGeoRenderer(new FriesianStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    // One brace for the whole medium size class, because those breeds are one mesh.
    private static final Variant MEDIUM = new Variant(
            new HorseStabilizerGeoRenderer(new MediumStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    // A map rather than a chain of ifs: this is keyed per entity type but a size class maps
    // several types onto one brace, so the list grows faster than the number of models.
    // Map.ofEntries rather than Map.of because Map.of tops out at ten pairs, and the medium
    // class alone is already six.
    private static final java.util.Map<net.minecraft.world.entity.EntityType<?>, Variant> BY_TYPE =
            java.util.Map.ofEntries(
                    java.util.Map.entry(icy.betterhorses.net.ModEntities.ICELANDIC_HORSE, ICELANDIC),
                    java.util.Map.entry(icy.betterhorses.net.ModEntities.FRIESIAN_HORSE, FRIESIAN),
                    java.util.Map.entry(icy.betterhorses.net.ModEntities.APPALOOSA_HORSE, MEDIUM),
                    java.util.Map.entry(icy.betterhorses.net.ModEntities.THOROUGHBRED_HORSE, MEDIUM),
                    java.util.Map.entry(icy.betterhorses.net.ModEntities.AMERICAN_PAINT_HORSE, MEDIUM),
                    java.util.Map.entry(icy.betterhorses.net.ModEntities.ANDALUSIAN_HORSE, MEDIUM),
                    java.util.Map.entry(icy.betterhorses.net.ModEntities.MUSTANG_HORSE, MEDIUM),
                    java.util.Map.entry(icy.betterhorses.net.ModEntities.QUARTER_HORSE, MEDIUM));

    private static Variant variantFor(EquineRenderState state) {
        return BY_TYPE.getOrDefault(state.entityType, GENERIC);
    }

    // The old FEET_Y_IN_FLIPPED_FRAME / TORSO_Z_OFFSET / TORSO_X_OFFSET constants moved into
    // Variant below, so each stabilizer model can carry the anchor its geometry was authored for.
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
        Variant variant = variantFor(state);

        // Anchor off the body's REST y where the model can tell us, so the brace follows the
        // barrel as it dips and pitches. Subtracting the live body.y instead cancels the
        // animation out - identical at rest, frozen in motion, which is the bug this fixes.
        // Models that cannot report a rest pose keep the old pinned behaviour unchanged.
        double anchorY = (this.getParentModel() instanceof BhHorseModel model)
                ? variant.feetY() - model.bhBodyRestY() / 16.0D
                : variant.feetY() - body.y / 16.0D;

        poseStack.pushPose();
        body.translateAndRotate(poseStack);
        poseStack.translate(
                -body.x / 16.0F,
                anchorY,
                -body.z / 16.0F + variant.zOffset());
        poseStack.mulPose(Axis.ZP.rotationDegrees(MODEL_ROLL_DEGREES));

        // GeckoLib 5 entry point. the renderer creates and fills its own GeoRenderState internally
        variant.renderer().performRenderPass(
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
