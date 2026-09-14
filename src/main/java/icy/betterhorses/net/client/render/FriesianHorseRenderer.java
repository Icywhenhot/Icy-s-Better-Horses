package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.FriesianHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class FriesianHorseRenderer extends BhHorseRenderer<FriesianHorse> {

    public FriesianHorseRenderer(EntityRendererProvider.Context context,
                               ModelLayerLocation adultLayer,
                               ModelLayerLocation babyLayer) {
        super(context,
                new FriesianHorseModel(context.bakeLayer(adultLayer)),
                new FriesianFoalModel(context.bakeLayer(babyLayer)),
                layer -> new FriesianHorseModel(context.bakeLayer(layer)),
                BhModelLayers.FRIESIAN_SADDLE,
                BhModelLayers.FRIESIAN_SADDLE_BABY,
                BhModelLayers.FRIESIAN_ARMOR,
                BhModelLayers.FRIESIAN_ARMOR_BABY,
                BhModelLayers.FRIESIAN_CHEST,
                BhModelLayers.FRIESIAN_CHEST_BABY,
                BhTackTextures.FRIESIAN);
    }
}
