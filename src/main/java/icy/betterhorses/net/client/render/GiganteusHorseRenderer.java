package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.GiganteusHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class GiganteusHorseRenderer extends BhHorseRenderer<GiganteusHorse> {

    public GiganteusHorseRenderer(EntityRendererProvider.Context context,
                               ModelLayerLocation adultLayer,
                               ModelLayerLocation babyLayer) {
        super(context,
                new GiganteusHorseModel(context.bakeLayer(adultLayer)),
                new GiganteusFoalModel(context.bakeLayer(babyLayer)),
                layer -> new GiganteusHorseModel(context.bakeLayer(layer)),
                BhModelLayers.GIGANTEUS_SADDLE,
                BhModelLayers.GIGANTEUS_SADDLE_BABY,
                BhModelLayers.GIGANTEUS_CARPET,
                BhModelLayers.GIGANTEUS_CARPET_BABY,
                BhModelLayers.GIGANTEUS_ARMOR,
                BhModelLayers.GIGANTEUS_ARMOR_BABY,
                BhModelLayers.GIGANTEUS_CHEST,
                BhModelLayers.GIGANTEUS_CHEST_BABY,
                BhTackTextures.GIGANTEUS);
        addHorseshoes(adultLayer);
    }
}
