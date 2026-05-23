package icy.betterhorses.net.client.render;

import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * GeckoLib 5.3 (1.21.10) object renderer for the stabilizer wings.
 *
 * {@link GeoObjectRenderer} takes three type parameters {@code <T animatable, O relatedObject, R
 * renderState>}; we have no related object so we use {@link Void} and the default
 * {@link GeoRenderState.Impl}. Wing visibility is pushed onto the render state here and consumed by
 * {@link HorseStabilizerGeoModel#setCustomAnimations} (the 5.3 bone-hiding hook).
 */
public final class HorseStabilizerGeoRenderer
        extends GeoObjectRenderer<HorseStabilizerAnimatable, Void, GeoRenderState.Impl> {

    public HorseStabilizerGeoRenderer() {
        super(new HorseStabilizerGeoModel());
    }

    @Override
    public GeoRenderState.Impl createRenderState(HorseStabilizerAnimatable animatable, Void relatedObject) {
        return new GeoRenderState.Impl();
    }

    @Override
    public void addRenderData(
            HorseStabilizerAnimatable animatable,
            Void relatedObject,
            GeoRenderState.Impl renderState,
            float partialTick) {
        renderState.addGeckolibData(HorseStabilizerGeoModel.WINGS_VISIBLE, animatable.isActive());
    }
}
