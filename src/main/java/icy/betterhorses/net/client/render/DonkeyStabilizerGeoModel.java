package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class DonkeyStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {

    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "animations/st.animation.json");

    private final ResourceLocation model;
    private final ResourceLocation texture;

    public DonkeyStabilizerGeoModel(String breed) {
        this.model = ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "geo/st_" + breed + ".geo.json");
        this.texture = ResourceLocation.fromNamespaceAndPath(
                IcysBetterHorses.RESOURCE_NAMESPACE, "textures/entity/horse/" + breed + "/stabilizer.png");
    }

    @Override
    public ResourceLocation getModelResource(HorseStabilizerAnimatable animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(HorseStabilizerAnimatable animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(HorseStabilizerAnimatable animatable) {
        return ANIMATION;
    }
}
