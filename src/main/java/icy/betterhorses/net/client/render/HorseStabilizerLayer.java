package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EntityType;
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

    private static final Map<EntityType<?>, Variant> BY_TYPE = Map.ofEntries(
            Map.entry(ModEntities.ICELANDIC_HORSE.get(), ICELANDIC),
            Map.entry(ModEntities.FRIESIAN_HORSE.get(), FRIESIAN),
            Map.entry(ModEntities.APPALOOSA_HORSE.get(), MEDIUM),
            Map.entry(ModEntities.THOROUGHBRED_HORSE.get(), MEDIUM),
            Map.entry(ModEntities.AMERICAN_PAINT_HORSE.get(), MEDIUM),
            Map.entry(ModEntities.ANDALUSIAN_HORSE.get(), MEDIUM),
            Map.entry(ModEntities.MUSTANG_HORSE.get(), MEDIUM),
            Map.entry(ModEntities.QUARTER_HORSE.get(), MEDIUM),
            Map.entry(ModEntities.ARABIAN_HORSE.get(), SMALL),
            Map.entry(ModEntities.MORGAN_HORSE.get(), SMALL),
            Map.entry(ModEntities.HAFLINGER_HORSE.get(), HAFLINGER),
            Map.entry(ModEntities.PERCHERON_HORSE.get(), PERCHERON),
            Map.entry(ModEntities.SHIRE_HORSE.get(), SHIRE),
            Map.entry(ModEntities.BELGIAN_HORSE.get(), BELGIAN),
            Map.entry(ModEntities.CLYDESDALE_HORSE.get(), PERCHERON));

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

        Variant variant = BY_TYPE.getOrDefault(entity.getType(), GENERIC);
        double anchorY = model instanceof BhHorseModel<?> breed
                ? variant.feetY() - breed.bhBodyRestY() / 16.0D
                : variant.feetY() - body.y / 16.0D;

        poseStack.pushPose();
        if (model instanceof BhHorseModel<?> breed) {
            breed.root().translateAndRotate(poseStack);
        }
        body.translateAndRotate(poseStack);
        poseStack.translate(
                -body.x / 16.0F + GEO_BLOCK_ANCHOR,
                anchorY + GEO_BLOCK_ANCHOR_Y,
                -body.z / 16.0F + variant.zOffset() - GEO_BLOCK_ANCHOR);
        poseStack.mulPose(Axis.ZP.rotationDegrees(MODEL_ROLL_DEGREES));

        variant.renderer().renderAt(poseStack, animatable, bufferSource, partialTicks, packedLight);

        poseStack.popPose();
    }
}
