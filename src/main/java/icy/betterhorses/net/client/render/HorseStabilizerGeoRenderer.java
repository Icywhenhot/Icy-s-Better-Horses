package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.util.Color;

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
        render(poseStack, animatable, bufferSource, renderType, buffer, packedLight, partialTick);
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
        int argb = BhMountedHorseVisibility.applyOpacity(
                super.getRenderColor(animatable, partialTick, packedLight).argbInt(),
                BhMountedHorseVisibility.currentOpacity());
        return Color.ofARGB((argb >>> 24) & 0xFF, (argb >>> 16) & 0xFF, (argb >>> 8) & 0xFF, argb & 0xFF);
    }

    @Override
    public void preRender(PoseStack poseStack, HorseStabilizerAnimatable animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int color) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, color);
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
