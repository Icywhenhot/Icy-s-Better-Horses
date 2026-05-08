package icy.betterhorses.net.client.render;

import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/**
 * GeckoLib 5 widened {@link GeoObjectRenderer} from one type parameter to three:
 * {@code <T animatable, O relatedObject, R renderState>}. We don't need a related object,
 * so we use {@link Void}, and the default {@link GeoRenderState.Impl} suffices for the state.
 */
public final class HorseStabilizerGeoRenderer
        extends GeoObjectRenderer<HorseStabilizerAnimatable, Void, GeoRenderState.Impl> {
    public HorseStabilizerGeoRenderer() {
        super(new HorseStabilizerGeoModel());
    }
}
