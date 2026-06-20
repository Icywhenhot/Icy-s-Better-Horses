package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

// GeoModel for the stabilizer wings.
public final class HorseStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "geo/st.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "textures/entity/horse_stabilizer.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "animations/st.animation.json");

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

    @Override
    public void setCustomAnimations(HorseStabilizerAnimatable animatable, long instanceId,
                                    AnimationState<HorseStabilizerAnimatable> state) {
        super.setCustomAnimations(animatable, instanceId, state);
        boolean showWings = animatable.isActive();
        getBone("wingsL").ifPresent(bone -> bone.setHidden(!showWings));
        getBone("wingsL2").ifPresent(bone -> bone.setHidden(!showWings));
    }
}
