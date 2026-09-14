package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.HaflingerHorse;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class HaflingerHorseRenderer extends BhHorseRenderer<HaflingerHorse> {

    public HaflingerHorseRenderer(EntityRendererProvider.Context context) {
        super(context,
                new HaflingerHorseModel(context.bakeLayer(BhModelLayers.HAFLINGER_HORSE)),
                new HaflingerFoalModel(context.bakeLayer(BhModelLayers.HAFLINGER_HORSE_BABY)),
                layer -> new HaflingerHorseModel(context.bakeLayer(layer)),
                BhModelLayers.HAFLINGER_SADDLE,
                BhModelLayers.HAFLINGER_SADDLE_BABY,
                BhModelLayers.HAFLINGER_ARMOR,
                BhModelLayers.HAFLINGER_ARMOR_BABY,
                BhModelLayers.HAFLINGER_CHEST,
                BhModelLayers.HAFLINGER_CHEST_BABY,
                BhTackTextures.HAFLINGER);
    }
}
