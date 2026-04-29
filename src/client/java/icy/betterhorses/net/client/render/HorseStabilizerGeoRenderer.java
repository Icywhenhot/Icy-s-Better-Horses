package icy.betterhorses.net.client.render;

import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class HorseStabilizerGeoRenderer
        extends GeoObjectRenderer<HorseStabilizerAnimatable, Void, GeoRenderState.Impl> {

    public HorseStabilizerGeoRenderer() {
        super(new HorseStabilizerGeoModel());
    }

    @Override
    public GeoRenderState.Impl createRenderState(HorseStabilizerAnimatable animatable, Void owner) {
        return new GeoRenderState.Impl();
    }

    @Override
    public void addRenderData(HorseStabilizerAnimatable animatable, Void owner,
                              GeoRenderState.Impl renderState, float partialTick) {
        renderState.addGeckolibData(HorseStabilizerGeoModel.WINGS_VISIBLE, animatable.isActive());
    }
}
