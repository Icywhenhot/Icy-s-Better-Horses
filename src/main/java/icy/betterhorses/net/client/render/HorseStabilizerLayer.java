package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.inventory.GearSlot;
import icy.betterhorses.net.mixin.HorseModelAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/**
 * Stabilizer wing render layer for 1.21.1.
 * 1.21.1 has no separate render-state phase, so we read the live entity directly inside render(),
 * sync the GeckoLib animatable, and delegate to the GeoObjectRenderer anchored to the horse body bone.
 */
public final class HorseStabilizerLayer<T extends AbstractHorse, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final HorseStabilizerGeoRenderer GEO_RENDERER = new HorseStabilizerGeoRenderer();

    private static final double TORSO_X_OFFSET = 8.0D / 16.0D;
    private static final double FEET_Y_IN_FLIPPED_FRAME = 2D;
    private static final double TORSO_Z_OFFSET = -8.3D / 16.0D;
    private static final float MODEL_ROLL_DEGREES = 180.0F;

    public HorseStabilizerLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!(entity instanceof IHorseData data) || !data.bh_hasGear(GearSlot.STABILIZER)) {
            return;
        }

        HorseStabilizerState state = data.bh_getStabilizerState();
        HorseStabilizerAnimatable animatable = HorseStabilizerAnimatable.get(entity);
        animatable.syncFromHorse(entity, state, ageInTicks);

        M model = this.getParentModel();
        if (!(model instanceof HorseModelAccessor accessor)) {
            return;
        }
        ModelPart body = accessor.bh_getBody();

        poseStack.pushPose();
        body.translateAndRotate(poseStack);
        poseStack.translate(
                -body.x / 16.0F + TORSO_X_OFFSET,
                FEET_Y_IN_FLIPPED_FRAME - body.y / 16.0F,
                -body.z / 16.0F + TORSO_Z_OFFSET);
        poseStack.mulPose(Axis.ZP.rotationDegrees(MODEL_ROLL_DEGREES));

        GEO_RENDERER.renderAt(poseStack, animatable, bufferSource, partialTicks, packedLight);

        poseStack.popPose();
    }
}
