package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.TarpanHorse;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class TarpanHorseRenderer extends BhHorseRenderer<TarpanHorse> {

    public TarpanHorseRenderer(EntityRendererProvider.Context context) {
        super(context,
                new TarpanHorseModel(context.bakeLayer(BhModelLayers.TARPAN_HORSE)),
                new TarpanFoalModel(context.bakeLayer(BhModelLayers.TARPAN_HORSE_BABY)),
                layer -> new TarpanHorseModel(context.bakeLayer(layer)),
                BhModelLayers.TARPAN_SADDLE,
                BhModelLayers.TARPAN_SADDLE_BABY,
                BhModelLayers.TARPAN_CARPET,
                BhModelLayers.TARPAN_CARPET_BABY,
                BhModelLayers.TARPAN_ARMOR,
                BhModelLayers.TARPAN_ARMOR_BABY,
                BhModelLayers.TARPAN_CHEST,
                BhModelLayers.TARPAN_CHEST_BABY,
                BhTackTextures.TARPAN);
        addHorseshoes(BhModelLayers.TARPAN_HORSE);
    }
}
