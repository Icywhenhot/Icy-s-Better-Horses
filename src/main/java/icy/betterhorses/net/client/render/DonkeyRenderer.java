package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BhBreedDonkey;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class DonkeyRenderer extends BhHorseRenderer<BhBreedDonkey> {

    public DonkeyRenderer(EntityRendererProvider.Context context, ModelLayerLocation adult, ModelLayerLocation baby,
                          ModelLayerLocation saddle, ModelLayerLocation saddleBaby,
                          ModelLayerLocation carpet, ModelLayerLocation carpetBaby,
                          ModelLayerLocation armor, ModelLayerLocation armorBaby,
                          ModelLayerLocation chest, ModelLayerLocation chestBaby,
                          BhTackTextures textures) {
        super(context,
                new DonkeyModel(context.bakeLayer(adult)),
                new DonkeyFoalModel(context.bakeLayer(baby)),
                layer -> new DonkeyModel(context.bakeLayer(layer)),
                saddle, saddleBaby, carpet, carpetBaby, armor, armorBaby, chest, chestBaby, textures);
    }
}
