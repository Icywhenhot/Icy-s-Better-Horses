package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BhBreedMule;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class MuleRenderer extends BhHorseRenderer<BhBreedMule> {

    public MuleRenderer(EntityRendererProvider.Context context) {
        super(context,
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_HORSE)),
                new DonkeyFoalModel(context.bakeLayer(BhModelLayers.MULE_BABY)),
                layer -> new MediumHorseModel(context.bakeLayer(layer)),
                BhModelLayers.MEDIUM_SADDLE, BhModelLayers.MEDIUM_SADDLE_BABY,
                BhModelLayers.MEDIUM_CARPET, BhModelLayers.MEDIUM_CARPET_BABY,
                BhModelLayers.MEDIUM_ARMOR, BhModelLayers.MEDIUM_ARMOR_BABY,
                BhModelLayers.MEDIUM_CHEST, BhModelLayers.MEDIUM_CHEST_BABY,
                BhTackTextures.MEDIUM);
    }

    @Override
    public void extractRenderState(BhBreedMule entity, BhHorseRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.riddenHeadDrop = 25.0F * Mth.DEG_TO_RAD;
    }
}
