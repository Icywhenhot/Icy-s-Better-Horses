package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.ClydesdaleHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ClydesdaleHorseRenderer extends BhHorseRenderer<ClydesdaleHorse> {

    public ClydesdaleHorseRenderer(EntityRendererProvider.Context context,
                               ModelLayerLocation adultLayer,
                               ModelLayerLocation babyLayer) {
        super(context,
                new ClydesdaleHorseModel(context.bakeLayer(adultLayer)),
                new ClydesdaleFoalModel(context.bakeLayer(babyLayer)),
                layer -> new ClydesdaleHorseModel(context.bakeLayer(layer)),
                BhModelLayers.PERCHERON_SADDLE,
                BhModelLayers.PERCHERON_SADDLE_BABY,
                BhModelLayers.PERCHERON_ARMOR,
                BhModelLayers.PERCHERON_ARMOR_BABY,
                BhModelLayers.PERCHERON_CHEST,
                BhModelLayers.PERCHERON_CHEST_BABY,
                BhTackTextures.PERCHERON);
    }
}
