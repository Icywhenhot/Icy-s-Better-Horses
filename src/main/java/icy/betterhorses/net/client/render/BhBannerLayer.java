package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icy.betterhorses.net.BhBanners;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BhBannerLayer<T extends BhBreedHorse> extends RenderLayer<T, BhHorseModel<T>> {

    private static final Direction[] SIDES = {null, Direction.DOWN, Direction.UP, Direction.NORTH,
            Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final float HERALDS_STAND = 2.01F / 16.0F;
    private static final float HERALDS_DROP = -0.04F;
    private static final float HERALDS_PUSH = 0.143F;

    private final float y;
    private final float z;

    public BhBannerLayer(RenderLayerParent<T, BhHorseModel<T>> parent) {
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

    protected ItemStack banner(T entity, ItemStack armor) {
        return IHorseData.of(entity).bh_getBanner();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible() || BhMountedHorseVisibility.currentOpacity() < 1.0F) return;
        ItemStack armor = IHorseData.of(entity).bh_getBarding();
        if (armor.isEmpty()) return;
        ItemStack banner = banner(entity, armor);
        if (banner.isEmpty()) return;

        poseStack.pushPose();
        getParentModel().root().translateAndRotate(poseStack);
        getParentModel().bh_getBody().translateAndRotate(poseStack);
        poseStack.translate(0.0F, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
        if (BhBanners.heralds(banner)) {
            heralds(poseStack, buffer, packedLight, banner, entity);
        } else {
            Minecraft.getInstance().getItemRenderer().renderStatic(banner, ItemDisplayContext.HEAD,
                    packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), 0);
        }
        poseStack.popPose();
    }

    private void heralds(PoseStack poseStack, MultiBufferSource buffer, int light, ItemStack banner, T entity) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(banner, entity.level(), entity, 0);
        poseStack.translate(0.0F, 1.0F, 7.0F / 16.0F);
        poseStack.scale(1.5F, 1.5F, 1.5F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        poseStack.translate(0.0F, HERALDS_DROP, HERALDS_PUSH);
        VertexConsumer out = buffer.getBuffer(Sheets.translucentItemSheet());
        RandomSource random = RandomSource.create(42L);
        for (Direction side : SIDES) {
            for (BakedQuad quad : model.getQuads(null, side, random)) {
                if (top(quad) > HERALDS_STAND) {
                    out.putBulkData(poseStack.last(), quad, 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
                }
            }
        }
    }

    private static float top(BakedQuad quad) {
        int[] v = quad.getVertices();
        int stride = v.length / 4;
        float y = -Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            y = Math.max(y, Float.intBitsToFloat(v[i * stride + 1]));
        }
        return y;
    }
}
