package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.item.HorseCartItem;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

/** Same cart geo/texture as the entity, typed for the {@link HorseCartItem} render path. */
public final class HorseCartItemGeoModel extends GeoModel<HorseCartItem> {
    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_cart");
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "textures/entity/horse_cart.png");
    private static final Identifier ANIMATION =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_cart");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(HorseCartItem animatable) {
        return ANIMATION;
    }
}
