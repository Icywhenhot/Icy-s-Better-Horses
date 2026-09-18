package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.PercheronHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PercheronHorseRenderer extends BhHorseRenderer<PercheronHorse> {

    public PercheronHorseRenderer(EntityRendererProvider.Context context,
                               ModelLayerLocation adultLayer,
                               ModelLayerLocation babyLayer) {
        super(context,
                new PercheronHorseModel(context.bakeLayer(adultLayer)),
                new PercheronFoalModel(context.bakeLayer(babyLayer)),
                layer -> new PercheronHorseModel(context.bakeLayer(layer)),
                BhModelLayers.PERCHERON_SADDLE,
                BhModelLayers.PERCHERON_SADDLE_BABY,
                BhModelLayers.PERCHERON_ARMOR,
                BhModelLayers.PERCHERON_ARMOR_BABY,
                BhModelLayers.PERCHERON_CHEST,
                BhModelLayers.PERCHERON_CHEST_BABY,
                BhTackTextures.PERCHERON);
    }
}
