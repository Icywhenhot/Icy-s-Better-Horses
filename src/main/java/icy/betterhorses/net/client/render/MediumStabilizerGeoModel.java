package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MediumStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "geo/st_medium.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            IcysBetterHorses.RESOURCE_NAMESPACE, "textures/entity/horse/medium/stabilizer.png");
    private static final ResourceLocation ANIMATION =
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "animations/st.animation.json");

    @Override
    public ResourceLocation getModelResource(HorseStabilizerAnimatable animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HorseStabilizerAnimatable animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(HorseStabilizerAnimatable animatable) {
        return ANIMATION;
    }
}
