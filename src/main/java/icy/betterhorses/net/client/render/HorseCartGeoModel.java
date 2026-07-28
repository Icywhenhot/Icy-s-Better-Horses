package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

/**
 * GeoModel for the pulled cart entity. Resolves to:
 * <ul>
 *   <li>{@code assets/icys-better-horses/geckolib/models/horse_cart.geo.json}</li>
 *   <li>{@code assets/icys-better-horses/geckolib/animations/horse_cart.animation.json}</li>
 *   <li>{@code assets/icys-better-horses/textures/entity/horse_cart.png}</li>
 * </ul>
 * The first two are exported from Blockbench; the texture is extracted from the bbmodel.
 */
public final class HorseCartGeoModel extends GeoModel<HorseCartEntity> {
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
    public Identifier getAnimationResource(HorseCartEntity animatable) {
        return ANIMATION;
    }
}
