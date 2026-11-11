package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public final class DonkeyStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {

    private static final Identifier ANIMATION =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st");

    private final Identifier model;
    private final Identifier texture;

    public DonkeyStabilizerGeoModel(String breed) {
        this.model = Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st_" + breed);
        this.texture = Identifier.fromNamespaceAndPath(
                IcysBetterHorses.MOD_ID, "textures/entity/horse/" + breed + "/stabilizer.png");
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return model;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return texture;
    }

    @Override
    public Identifier getAnimationResource(HorseStabilizerAnimatable animatable) {
        return ANIMATION;
    }
}
