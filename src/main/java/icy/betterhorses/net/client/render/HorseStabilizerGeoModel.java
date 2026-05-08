package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * GeoModel for the stabilizer wings.
 *
 * GeckoLib 5: {@code getModelResource}/{@code getTextureResource} now take a {@link GeoRenderState}
 * (not the animatable directly), and {@code setCustomAnimations} was removed in favour of
 * {@code addAdditionalStateData}. The wing-visibility logic that used to live here will need to be
 * reimplemented through the new render-state pipeline once the stabilizer layer is ported.
 */
public final class HorseStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {
    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "geo/st.geo.json");
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "textures/entity/horse_stabilizer.png");
    private static final Identifier ANIMATION =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "animations/st.animation.json");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(HorseStabilizerAnimatable animatable) {
        return ANIMATION;
    }
}
