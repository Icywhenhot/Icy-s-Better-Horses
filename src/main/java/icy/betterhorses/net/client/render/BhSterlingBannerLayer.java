package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.orcinus.galosphere.init.GDataComponents;

public class BhSterlingBannerLayer<T extends BhBreedHorse> extends RenderLayer<T, BhHorseModel<T>> {

    private final float y;
    private final float z;

    public BhSterlingBannerLayer(RenderLayerParent<T, BhHorseModel<T>> parent) {
        super(parent);
        float[] box = {Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE};
        parent.getModel().bh_getBody().visit(new PoseStack(), (pose, path, index, cube) -> {
            if (!path.isEmpty()) return;
            box[0] = Math.min(box[0], cube.minY);
            box[1] = Math.min(box[1], cube.minZ);
            box[2] = Math.max(box[2], cube.maxZ);
        });
        y = (box[0] + 2.6F) / 16.0F;
        z = (box[1] + (box[2] - box[1]) * 0.76F) / 16.0F;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || BhMountedHorseVisibility.currentOpacity() < 1.0F) return;
        ItemStack banner = entity.getBodyArmorItem().get(GDataComponents.STERLING_ATTACHED);
        if (banner == null || banner.isEmpty()) return;

        poseStack.pushPose();
        getParentModel().root().translateAndRotate(poseStack);
        getParentModel().bh_getBody().translateAndRotate(poseStack);
        poseStack.translate(0.0F, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        Minecraft.getInstance().getItemRenderer().renderStatic(banner, ItemDisplayContext.HEAD,
                packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), 0);
        poseStack.popPose();
    }
}
