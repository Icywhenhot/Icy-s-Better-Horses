package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class HorseStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {
    public static final DataTicket<Boolean> WINGS_VISIBLE =
            DataTicket.create("bh_stabilizer_wings_visible", Boolean.class);

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "textures/entity/horse_stabilizer.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st");

    @Override
    public ResourceLocation getModelResource(GeoRenderState state) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HorseStabilizerAnimatable animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(AnimationState<HorseStabilizerAnimatable> state) {
        super.setCustomAnimations(state);
        boolean showWings = Boolean.TRUE.equals(
                state.renderState().getOrDefaultGeckolibData(WINGS_VISIBLE, Boolean.FALSE));
        getBone("wingsL").ifPresent(bone -> bone.setHidden(!showWings));
        getBone("wingsL2").ifPresent(bone -> bone.setHidden(!showWings));
    }
}
