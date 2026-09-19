package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

public final class HorseStabilizerGeoRenderer extends GeoObjectRenderer<HorseStabilizerAnimatable> {

    public HorseStabilizerGeoRenderer() {
        this(new HorseStabilizerGeoModel());
    }

    public HorseStabilizerGeoRenderer(GeoModel<HorseStabilizerAnimatable> model) {
        super(model);
    }

    public void renderAt(PoseStack poseStack, HorseStabilizerAnimatable animatable,
                         MultiBufferSource bufferSource, float partialTick, int packedLight) {
        RenderType renderType = getRenderType(animatable, getTextureLocation(animatable), bufferSource, partialTick);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        render(poseStack, animatable, bufferSource, renderType, buffer, packedLight);
    }

    @Override
    public RenderType getRenderType(HorseStabilizerAnimatable animatable, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return BhMountedHorseVisibility.currentOpacity() < 1.0F
                ? RenderType.entityTranslucent(texture)
                : super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public Color getRenderColor(HorseStabilizerAnimatable animatable, float partialTick, int packedLight) {
        Color base = super.getRenderColor(animatable, partialTick, packedLight);
        float opacity = BhMountedHorseVisibility.currentOpacity();
        if (opacity >= 1.0F) {
            return base;
        }
        return Color.ofRGBA(base.getRed(), base.getGreen(), base.getBlue(),
                (int) (base.getAlpha() * opacity));
    }

    @Override
    public void preRender(PoseStack poseStack, HorseStabilizerAnimatable animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
        boolean wings = animatable.isActive();
        model.getBone("wingsL").ifPresent(bone -> {
            bone.setHidden(!wings);
            bone.setChildrenHidden(!wings);
        });
        model.getBone("wingsL2").ifPresent(bone -> {
            bone.setHidden(!wings);
            bone.setChildrenHidden(!wings);
        });
    }
}
