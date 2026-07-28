package icy.betterhorses.net.client.render;

import icy.betterhorses.net.item.HorseCartItem;
import com.geckolib.renderer.GeoItemRenderer;

/** Renders the {@link HorseCartItem} as the 3D cart model in the GUI/hand. */
public final class HorseCartItemRenderer extends GeoItemRenderer<HorseCartItem> {
    public HorseCartItemRenderer() {
        super(new HorseCartItemGeoModel());
    }
}
