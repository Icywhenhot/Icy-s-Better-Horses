package icy.betterhorses.net.client.render;

import software.bernie.geckolib.renderer.GeoObjectRenderer;

public final class HorseStabilizerGeoRenderer extends GeoObjectRenderer<HorseStabilizerAnimatable> {
    public HorseStabilizerGeoRenderer() {
        super(new HorseStabilizerGeoModel());
    }
}
