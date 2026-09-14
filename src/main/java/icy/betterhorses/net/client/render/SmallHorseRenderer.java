package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.SmallHorse;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SmallHorseRenderer<T extends SmallHorse> extends BhHorseRenderer<T> {

    public SmallHorseRenderer(EntityRendererProvider.Context context) {
        super(context,
                new SmallHorseModel<>(context.bakeLayer(BhModelLayers.SMALL_HORSE)),
                new SmallFoalModel<>(context.bakeLayer(BhModelLayers.SMALL_HORSE_BABY)),
                layer -> new SmallHorseModel<>(context.bakeLayer(layer)),
                BhModelLayers.SMALL_SADDLE,
                BhModelLayers.SMALL_SADDLE_BABY,
                BhModelLayers.SMALL_ARMOR,
                BhModelLayers.SMALL_ARMOR_BABY,
                BhModelLayers.SMALL_CHEST,
                BhModelLayers.SMALL_CHEST_BABY,
                BhTackTextures.SMALL);
    }
}
