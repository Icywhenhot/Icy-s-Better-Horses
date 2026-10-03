package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.IcelandicHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class IcelandicHorseRenderer extends BhHorseRenderer<IcelandicHorse> {

    public IcelandicHorseRenderer(EntityRendererProvider.Context context,
                               ModelLayerLocation adultLayer,
                               ModelLayerLocation babyLayer) {
        super(context,
                new IcelandicHorseModel(context.bakeLayer(adultLayer)),
                new IcelandicFoalModel(context.bakeLayer(babyLayer)),
                layer -> new IcelandicHorseModel(context.bakeLayer(layer)),
                BhModelLayers.ICELANDIC_SADDLE,
                BhModelLayers.ICELANDIC_SADDLE_BABY,
                BhModelLayers.ICELANDIC_ARMOR,
                BhModelLayers.ICELANDIC_ARMOR_BABY,
                BhModelLayers.ICELANDIC_CHEST,
                BhModelLayers.ICELANDIC_CHEST_BABY,
                BhTackTextures.ICELANDIC);
    }
}
