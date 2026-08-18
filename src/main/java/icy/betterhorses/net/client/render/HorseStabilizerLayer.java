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

public final class HorseStabilizerLayer<S extends EquineRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {

    private record Variant(HorseStabilizerGeoRenderer renderer, double feetY, double zOffset) {}

    private static final Variant GENERIC = new Variant(
            new HorseStabilizerGeoRenderer(), 1.35D, -1.0D / 16.0D);

    private static final Variant ICELANDIC = new Variant(
            new HorseStabilizerGeoRenderer(new IcelandicStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    private static final Variant FRIESIAN = new Variant(
            new HorseStabilizerGeoRenderer(new FriesianStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    private static final Variant MEDIUM = new Variant(
            new HorseStabilizerGeoRenderer(new MediumStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

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
        if (!bhState.bh_hasStabilizer()) {
            return;
        }

        // Only bail once the horse itself is gone. Bailing on any fade at all - which is what
        // "< 0.999" did - deleted the brace the moment the rider tilted their view past 30
        // degrees, i.e. exactly when they looked down to check their own gear. The horse went
        // translucent, the brace went missing. It fades with the horse instead now; see
        // HorseStabilizerGeoRenderer.
        if (BhRenderContext.currentOpacity() <= 0.01F) {
            return;
        }

        CameraRenderState camera = BhRenderContext.currentCamera();
        if (camera == null) {
            return;
        }

        HorseStabilizerAnimatable animatable = HorseStabilizerAnimatable.getById(bhState.bh_getHorseId());
        if (animatable == null) {
            return;
        }

        ModelPart body = ((HorseModelAccessor) this.getParentModel()).bh_getBody();
        Variant variant = variantFor(state);

        double anchorY = (this.getParentModel() instanceof BhHorseModel model)
                ? variant.feetY() - model.bhBodyRestY() / 16.0D
                : variant.feetY() - body.y / 16.0D;

        poseStack.pushPose();
        // Root first, then body. The turning lean and the jump arc are authored on the root
        // (see BhHorseModel: rootPart.zRot is the bank, rootPart.xRot the arc), because the root
        // parents the barrel *and* the legs so the whole animal tips together. A layer's pose
        // starts in model space with none of that applied, so starting from the body alone hangs
        // the stabilizer off a bone whose parent transform it never saw - it stayed upright while
        // the horse banked out from under it. The root's own translation is the counter-offset
        // that keeps the ground pivot still, and the anchor below is a ground-level point, so it
        // rides the rotation without drifting.
        this.getParentModel().root().translateAndRotate(poseStack);
        body.translateAndRotate(poseStack);
        poseStack.translate(
                -body.x / 16.0F,
                anchorY,
                -body.z / 16.0F + variant.zOffset());
        poseStack.mulPose(Axis.ZP.rotationDegrees(MODEL_ROLL_DEGREES));

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
