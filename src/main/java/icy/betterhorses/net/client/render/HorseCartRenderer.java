package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class HorseCartRenderer extends GeoEntityRenderer<HorseCartEntity> {

    public HorseCartRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseCartGeoModel());
    }

    @Override
    public void preRender(PoseStack poseStack, HorseCartEntity cart, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        super.preRender(poseStack, cart, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
        showBone(model, cart.size().chestBone(), cart.hasChest());
        showBone(model, "plow", cart.hasPlough());
        showBone(model, "bone3", cart.isPlaced());
    }

    private static void showBone(BakedGeoModel model, String name, boolean show) {
        model.getBone(name).ifPresent(bone -> {
            bone.setHidden(!show);
            bone.setChildrenHidden(!show);
        });
    }

    @Override
    protected void applyRotations(HorseCartEntity cart, PoseStack pose, float ageInTicks, float rotationYaw,
                                  float partialTick) {
        super.applyRotations(cart, pose, ageInTicks, cart.gluedRenderYaw(partialTick), partialTick);
    }

    @Override
    public void render(HorseCartEntity cart, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        Vec3 glued = cart.gluedRenderPosition(partialTick);
        if (glued != null) {
            Vec3 normal = cart.getPosition(partialTick);
            pose.pushPose();
            pose.translate(glued.x - normal.x, glued.y - normal.y, glued.z - normal.z);
            super.render(cart, yaw, partialTick, pose, buffers, light);
            pose.popPose();
            return;
        }
        super.render(cart, yaw, partialTick, pose, buffers, light);
    }
}
