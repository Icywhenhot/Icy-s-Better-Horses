package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BelgianHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BelgianHorseRenderer extends BhHorseRenderer<BelgianHorse> {

    public BelgianHorseRenderer(EntityRendererProvider.Context context,
                               ModelLayerLocation adultLayer,
                               ModelLayerLocation babyLayer) {
        super(context,
                new BelgianHorseModel(context.bakeLayer(adultLayer)),
                new BelgianFoalModel(context.bakeLayer(babyLayer)),
                layer -> new BelgianHorseModel(context.bakeLayer(layer)),
                BhModelLayers.BELGIAN_SADDLE,
                BhModelLayers.BELGIAN_SADDLE_BABY,
                BhModelLayers.BELGIAN_ARMOR,
                BhModelLayers.BELGIAN_ARMOR_BABY,
                BhModelLayers.BELGIAN_CHEST,
                BhModelLayers.BELGIAN_CHEST_BABY,
                BhTackTextures.BELGIAN);
    }
}
