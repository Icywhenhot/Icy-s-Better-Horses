package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class PercheronStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "geckolib/models/st_percheron.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IcysBetterHorses.RESOURCE_NAMESPACE, "textures/entity/horse/percheron/stabilizer.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "geckolib/animations/st.animation.json");

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
