package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class HorseCartRenderer extends GeoEntityRenderer<HorseCartEntity> {

    public HorseCartRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseCartGeoModel());
    }

    @Override
    public void render(HorseCartEntity cart, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        getGeoModel().getBone(cart.size().chestBone()).ifPresent(bone -> bone.setHidden(!cart.hasChest()));
        getGeoModel().getBone("plow").ifPresent(bone -> bone.setHidden(!cart.hasPlough()));
        getGeoModel().getBone("bone3").ifPresent(bone -> bone.setHidden(!cart.isPlaced()));

        Vec3 glued = cart.gluedRenderPosition(partialTick);
        if (glued != null) {
            Vec3 normal = cart.getPosition(partialTick);
            pose.pushPose();
            pose.translate(glued.x - normal.x, glued.y - normal.y, glued.z - normal.z);
            super.render(cart, cart.gluedRenderYaw(partialTick), partialTick, pose, buffers, light);
            pose.popPose();
            return;
        }
        super.render(cart, yaw, partialTick, pose, buffers, light);
    }
}
