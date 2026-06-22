package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

// GeoModel for the stabilizer wings. GeckoLib 5.3 (1.21.10): getModelResource/getTextureResource take a GeoRenderState, and per-frame bone visibility is driven through setCustomAnimations(AnimationState) reading a DataTicket populated by HorseStabilizerGeoRenderer#addRenderData.
public final class HorseStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {
    public static final DataTicket<Boolean> WINGS_VISIBLE =
            DataTicket.create("icys_better_horses_stabilizer_wings_active", Boolean.class);

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "textures/entity/horse_stabilizer.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st");

    @Override
    public ResourceLocation getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
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
