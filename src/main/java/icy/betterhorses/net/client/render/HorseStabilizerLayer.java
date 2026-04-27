package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.inventory.GearSlot;
import icy.betterhorses.net.mixin.HorseModelAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class HorseStabilizerLayer<T extends AbstractHorse, M extends HorseModel<T>> extends RenderLayer<T, M> {
    private static final HorseStabilizerGeoRenderer GEO_RENDERER = new HorseStabilizerGeoRenderer();
    private static final double TORSO_X_OFFSET = -1.0D / 16.0D;
    private static final double TORSO_Y_OFFSET = -37.0D / 16.0D;
    private static final double TORSO_Z_OFFSET = -2.0D / 16.0D;
    private static final float MODEL_ROLL_DEGREES = 180.0F;

    public HorseStabilizerLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T horse,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (horse.isInvisible() || horse.isBaby() || !(horse instanceof IHorseData data)) {
            return;
        }

        if (!data.bh_hasGear(GearSlot.STABILIZER)) {
            return;
        }

        HorseStabilizerAnimatable animatable = HorseStabilizerAnimatable.get(horse);
        animatable.syncFromHorse(horse, data.bh_getStabilizerState());

        ModelPart body = ((HorseModelAccessor) this.getParentModel()).bh_getBody();

        poseStack.pushPose();
        body.translateAndRotate(poseStack);
        poseStack.translate(
                -body.x / 16.0F + TORSO_X_OFFSET,
                -(body.y + 6.0F) / 16.0F + TORSO_Y_OFFSET,
                -body.z / 16.0F + TORSO_Z_OFFSET);
        poseStack.mulPose(Axis.ZP.rotationDegrees(MODEL_ROLL_DEGREES));

        RenderType renderType = GEO_RENDERER.getRenderType(
                animatable,
                GEO_RENDERER.getTextureLocation(animatable),
                bufferSource,
                partialTick);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        GEO_RENDERER.render(poseStack, animatable, bufferSource, renderType, vertexConsumer, packedLight, partialTick);
        poseStack.popPose();
    }
}
