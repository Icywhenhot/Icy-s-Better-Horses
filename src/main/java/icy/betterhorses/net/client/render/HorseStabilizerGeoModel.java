package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

// GeoModel for the stabilizer wings
public final class HorseStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {
    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st");
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "textures/entity/horse_stabilizer.png");
    private static final Identifier ANIMATION =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st");

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
