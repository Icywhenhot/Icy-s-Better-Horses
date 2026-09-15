package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class HorseChestLayer<T extends AbstractHorse, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation CHEST_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/horse/donkey.png");

    private final ModelPart chest;

    public HorseChestLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.chest = bakeChest();
    }

    private static ModelPart bakeChest() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder pouch = CubeListBuilder.create().texOffs(26, 21).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
        root.addOrReplaceChild(
                "left_chest", pouch, PartPose.offsetAndRotation(6.0F, -8.0F, 0.0F, 0.0F, (float) (-Math.PI / 2), 0.0F));
        root.addOrReplaceChild(
                "right_chest", pouch, PartPose.offsetAndRotation(-6.0F, -8.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F));
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || entity.isBaby()
                || !(entity instanceof IHorseData data) || !data.bh_hasGear(GearSlot.CHEST)) {
            return;
        }
        if (entity instanceof AbstractChestedHorse chested && chested.hasChest()) {
            return;
        }
        if (!(this.getParentModel() instanceof BhHorseModelAccess access)) {
            return;
        }

        float opacity = BhMountedHorseVisibility.currentOpacity();
        if (opacity <= 0.01F) {
            return;
        }
        RenderType renderType = opacity < 1.0F
                ? RenderType.entityTranslucent(CHEST_TEXTURE)
                : RenderType.entityCutout(CHEST_TEXTURE);

        poseStack.pushPose();
        access.bh_getBody().translateAndRotate(poseStack);
        this.chest.render(poseStack, bufferSource.getBuffer(renderType), packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                BhMountedHorseVisibility.applyOpacity(-1, opacity));
        poseStack.popPose();
    }
}
