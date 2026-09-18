package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class HorseCartGeoModel extends GeoModel<HorseCartEntity> {

    @Override
    public ResourceLocation getModelResource(HorseCartEntity cart) {
        return cart.size().model();
    }

    @Override
    public ResourceLocation getTextureResource(HorseCartEntity cart) {
        return cart.size().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(HorseCartEntity cart) {
        return cart.size().animation();
    }
}
