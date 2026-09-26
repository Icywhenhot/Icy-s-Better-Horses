package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.registry.StabilizerBody;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.Map;

public final class HorseStabilizerLayer<T extends AbstractHorse, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private record Variant(HorseStabilizerGeoRenderer renderer, double feetY, double zOffset) {}

    private static final double BREED_FEET_Y = 24.0D / 16.0D;

    private static final Variant GENERIC = new Variant(
            new HorseStabilizerGeoRenderer(), 1.35D, -1.0D / 16.0D);
    private static final Variant ICELANDIC = new Variant(
            new HorseStabilizerGeoRenderer(new IcelandicStabilizerGeoModel()), BREED_FEET_Y, 0.0D);
    private static final Variant FRIESIAN = new Variant(
            new HorseStabilizerGeoRenderer(new FriesianStabilizerGeoModel()), BREED_FEET_Y, 0.0D);
    private static final Variant MEDIUM = new Variant(
            new HorseStabilizerGeoRenderer(new MediumStabilizerGeoModel()), BREED_FEET_Y, 0.0D);
    private static final Variant SMALL = new Variant(
            new HorseStabilizerGeoRenderer(new SmallStabilizerGeoModel()), BREED_FEET_Y, 0.0D);
    private static final Variant HAFLINGER = new Variant(
            new HorseStabilizerGeoRenderer(new HaflingerStabilizerGeoModel()), BREED_FEET_Y, 0.0D);
    private static final Variant PERCHERON = new Variant(
            new HorseStabilizerGeoRenderer(new PercheronStabilizerGeoModel()), BREED_FEET_Y, 0.0D);
    private static final Variant SHIRE = new Variant(
            new HorseStabilizerGeoRenderer(new ShireStabilizerGeoModel()), BREED_FEET_Y, 0.0D);
    private static final Variant BELGIAN = new Variant(
            new HorseStabilizerGeoRenderer(new BelgianStabilizerGeoModel()), BREED_FEET_Y, 0.0D);


    private static final Map<StabilizerBody, Variant> BY_BODY = Map.of(
            StabilizerBody.ICELANDIC, ICELANDIC,
            StabilizerBody.FRIESIAN, FRIESIAN,
            StabilizerBody.MEDIUM, MEDIUM,
            StabilizerBody.SMALL, SMALL,
            StabilizerBody.HAFLINGER, HAFLINGER,
            StabilizerBody.PERCHERON, PERCHERON,
            StabilizerBody.SHIRE, SHIRE,
            StabilizerBody.BELGIAN, BELGIAN);

    private static final float MODEL_ROLL_DEGREES = 180.0F;

    private static final double GEO_BLOCK_ANCHOR = 0.5D;
    private static final double GEO_BLOCK_ANCHOR_Y = 0.51D;

    public HorseStabilizerLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || !(entity instanceof IHorseData data) || !data.bh_hasStabilizerItem()) {
            return;
        }
        if (BhMountedHorseVisibility.currentOpacity() <= 0.01F) {
            return;
        }

        M model = this.getParentModel();
        if (!(model instanceof BhHorseModelAccess access)) {
            return;
        }
        ModelPart body = access.bh_getBody();

        HorseStabilizerState state = data.bh_getStabilizerState();
        HorseStabilizerAnimatable animatable = HorseStabilizerAnimatable.get(entity);
        animatable.syncFromHorse(entity, state, ageInTicks);

        Variant variant = BY_BODY.getOrDefault(stabilizerBodyOf(data), GENERIC);
        float restX = body.x;
        float restY = body.y;
        float restZ = body.z;
        if (model instanceof BhHorseModel<?> breed) {
            BhHorseModel.Rest rest = breed.bhBodyRest();
            restX = rest.x();
            restY = rest.y();
            restZ = rest.z();
        }

        poseStack.pushPose();
        if (model instanceof BhHorseModel<?> breed) {
            breed.root().translateAndRotate(poseStack);
        }
        body.translateAndRotate(poseStack);
        poseStack.translate(
                -restX / 16.0F + GEO_BLOCK_ANCHOR,
                variant.feetY() - restY / 16.0D + GEO_BLOCK_ANCHOR_Y,
                -restZ / 16.0F + variant.zOffset() - GEO_BLOCK_ANCHOR);
        poseStack.mulPose(Axis.ZP.rotationDegrees(MODEL_ROLL_DEGREES));

        variant.renderer().renderAt(poseStack, animatable, bufferSource, partialTicks, packedLight);

        poseStack.popPose();
    }

    private static StabilizerBody stabilizerBodyOf(IHorseData data) {
        ResourceKey<BreedType> breedKey = data.bh_getBreedKey();
        if (breedKey == null) {
            return StabilizerBody.GENERIC;
        }
        BreedType breed = BhRegistries.breedTypeRegistry().get(breedKey.location());
        return breed != null ? breed.stabilizerBody() : StabilizerBody.GENERIC;
    }
}
