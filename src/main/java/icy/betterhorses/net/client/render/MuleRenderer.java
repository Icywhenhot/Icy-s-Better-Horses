package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BhBreedMule;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class MuleRenderer extends BhHorseRenderer<BhBreedMule> {

    public MuleRenderer(EntityRendererProvider.Context context) {
        super(context,
                new MediumHorseModel<>(context.bakeLayer(BhModelLayers.MULE)),
                new MediumFoalModel<>(context.bakeLayer(BhModelLayers.MULE_BABY)),
                layer -> new MediumHorseModel<>(context.bakeLayer(layer)),
                BhModelLayers.MULE_SADDLE, BhModelLayers.MULE_SADDLE_BABY,
                BhModelLayers.MULE_CARPET, BhModelLayers.MULE_CARPET_BABY,
                BhModelLayers.MULE_ARMOR, BhModelLayers.MULE_ARMOR_BABY,
                BhModelLayers.MULE_CHEST, BhModelLayers.MULE_CHEST_BABY,
                BhTackTextures.MULE);
    }
}
