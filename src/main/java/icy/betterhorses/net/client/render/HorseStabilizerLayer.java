package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.mixin.HorseModelAccessor;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.registry.StabilizerBody;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import java.util.Map;
import net.minecraft.world.entity.EntityType;

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

    private static final Variant SMALL = new Variant(
            new HorseStabilizerGeoRenderer(new SmallStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    private static final Variant HAFLINGER = new Variant(
            new HorseStabilizerGeoRenderer(new HaflingerStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    private static final Variant PERCHERON = new Variant(
            new HorseStabilizerGeoRenderer(new PercheronStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    private static final Variant SHIRE = new Variant(
            new HorseStabilizerGeoRenderer(new ShireStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    private static final Variant BELGIAN = new Variant(
            new HorseStabilizerGeoRenderer(new BelgianStabilizerGeoModel()), 24.0D / 16.0D, 0.0D);

    private static final Map<StabilizerBody, Variant> BY_BODY = Map.of(
            StabilizerBody.ICELANDIC, ICELANDIC,
            StabilizerBody.FRIESIAN, FRIESIAN,
            StabilizerBody.MEDIUM, MEDIUM,
            StabilizerBody.SMALL, SMALL,
            StabilizerBody.HAFLINGER, HAFLINGER,
            StabilizerBody.PERCHERON, PERCHERON,
            StabilizerBody.SHIRE, SHIRE,
            StabilizerBody.BELGIAN, BELGIAN);

    private static Variant variantFor(EquineRenderState state) {
        Identifier type = BuiltInRegistries.ENTITY_TYPE.getKey(state.entityType);
        for (BreedType breed : BhRegistries.breedTypeRegistry()) {
            if (breed.entityType().identifier().equals(type)) {
                return BY_BODY.getOrDefault(breed.stabilizerBody(), GENERIC);
            }
        }
        return GENERIC;
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
