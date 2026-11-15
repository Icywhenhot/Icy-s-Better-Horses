package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BhBreedMule;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class MuleRenderer extends BhHorseRenderer<BhBreedMule> {

    public MuleRenderer(EntityRendererProvider.Context context, int size) {
        super(context,
                new MuleModel(context.bakeLayer(pick(size, BhModelLayers.SMALL_MULE, BhModelLayers.MULE, BhModelLayers.LARGE_MULE))),
                new DonkeyFoalModel(context.bakeLayer(BhModelLayers.MULE_BABY)),
                layer -> new MuleModel(context.bakeLayer(layer)),
                pick(size, BhModelLayers.SMALL_MULE_SADDLE, BhModelLayers.MULE_SADDLE, BhModelLayers.LARGE_MULE_SADDLE),
                pick(size, BhModelLayers.SMALL_MULE_SADDLE_BABY, BhModelLayers.MULE_SADDLE_BABY, BhModelLayers.LARGE_MULE_SADDLE_BABY),
                pick(size, BhModelLayers.SMALL_MULE_CARPET, BhModelLayers.MULE_CARPET, BhModelLayers.LARGE_MULE_CARPET),
                pick(size, BhModelLayers.SMALL_MULE_CARPET_BABY, BhModelLayers.MULE_CARPET_BABY, BhModelLayers.LARGE_MULE_CARPET_BABY),
                pick(size, BhModelLayers.SMALL_MULE_ARMOR, BhModelLayers.MULE_ARMOR, BhModelLayers.LARGE_MULE_ARMOR),
                pick(size, BhModelLayers.SMALL_MULE_ARMOR_BABY, BhModelLayers.MULE_ARMOR_BABY, BhModelLayers.LARGE_MULE_ARMOR_BABY),
                pick(size, BhModelLayers.SMALL_MULE_CHEST, BhModelLayers.MULE_CHEST, BhModelLayers.LARGE_MULE_CHEST),
                pick(size, BhModelLayers.SMALL_MULE_CHEST_BABY, BhModelLayers.MULE_CHEST_BABY, BhModelLayers.LARGE_MULE_CHEST_BABY),
                pick(size, BhTackTextures.SMALL_MULE, BhTackTextures.MULE, BhTackTextures.LARGE_MULE));
    }

    private static <T> T pick(int size, T small, T medium, T large) {
        return size == 0 ? small : size == 2 ? large : medium;
    }
}
