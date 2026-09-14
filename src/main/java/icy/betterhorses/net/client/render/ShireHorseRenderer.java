package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.ShireHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ShireHorseRenderer extends BhHorseRenderer<ShireHorse> {

    public ShireHorseRenderer(EntityRendererProvider.Context context,
                               ModelLayerLocation adultLayer,
                               ModelLayerLocation babyLayer) {
        super(context,
                new ShireHorseModel(context.bakeLayer(adultLayer)),
                new ShireFoalModel(context.bakeLayer(babyLayer)),
                layer -> new ShireHorseModel(context.bakeLayer(layer)),
                BhModelLayers.SHIRE_SADDLE,
                BhModelLayers.SHIRE_SADDLE_BABY,
                BhModelLayers.SHIRE_ARMOR,
                BhModelLayers.SHIRE_ARMOR_BABY,
                BhModelLayers.SHIRE_CHEST,
                BhModelLayers.SHIRE_CHEST_BABY,
                BhTackTextures.SHIRE);
    }
}
