package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

public final class HorseStabilizerGeoRenderer extends GeoObjectRenderer<HorseStabilizerAnimatable> {

    public HorseStabilizerGeoRenderer() {
        super(new HorseStabilizerGeoModel());
    }

    public void renderAt(PoseStack poseStack, HorseStabilizerAnimatable animatable,
                         MultiBufferSource bufferSource, float partialTick, int packedLight) {
        RenderType renderType = getRenderType(animatable, getTextureLocation(animatable), bufferSource, partialTick);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        render(poseStack, animatable, bufferSource, renderType, buffer, packedLight);
    }
}
