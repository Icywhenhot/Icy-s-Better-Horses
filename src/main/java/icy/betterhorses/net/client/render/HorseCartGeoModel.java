package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.CartType;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class HorseCartGeoModel extends GeoModel<HorseCartEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return HorseCartRenderer.typeOf(renderState).model();
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return HorseCartRenderer.typeOf(renderState).texture();
    }

    @Override
    public Identifier getAnimationResource(HorseCartEntity animatable) {
        return animatable.type().animation();
    }
}
