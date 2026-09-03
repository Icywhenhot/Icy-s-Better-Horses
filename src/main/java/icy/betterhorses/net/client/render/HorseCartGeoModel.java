package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public final class HorseCartGeoModel extends GeoModel<HorseCartEntity> {

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return HorseCartRenderer.sizeOf(renderState).model();
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return HorseCartRenderer.sizeOf(renderState).texture();
    }

    @Override
    public Identifier getAnimationResource(HorseCartEntity animatable) {
        return animatable.size().animation();
    }
}
