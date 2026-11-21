package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.PrzewalskiHorse;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class PrzewalskiHorseRenderer extends BhHorseRenderer<PrzewalskiHorse> {

    public PrzewalskiHorseRenderer(EntityRendererProvider.Context context) {
        super(context,
                new PrzewalskiHorseModel(context.bakeLayer(BhModelLayers.PRZEWALSKI_HORSE)),
                new PrzewalskiFoalModel(context.bakeLayer(BhModelLayers.PRZEWALSKI_HORSE_BABY)),
                layer -> new PrzewalskiHorseModel(context.bakeLayer(layer)),
                BhModelLayers.PRZEWALSKI_SADDLE,
                BhModelLayers.PRZEWALSKI_SADDLE_BABY,
                BhModelLayers.PRZEWALSKI_CARPET,
                BhModelLayers.PRZEWALSKI_CARPET_BABY,
                BhModelLayers.PRZEWALSKI_ARMOR,
                BhModelLayers.PRZEWALSKI_ARMOR_BABY,
                BhModelLayers.PRZEWALSKI_CHEST,
                BhModelLayers.PRZEWALSKI_CHEST_BABY,
                BhTackTextures.PRZEWALSKI);
        addHorseshoes(BhModelLayers.PRZEWALSKI_HORSE);
    }
}
