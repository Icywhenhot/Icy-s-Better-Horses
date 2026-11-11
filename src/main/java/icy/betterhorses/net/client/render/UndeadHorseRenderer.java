package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BhUndeadHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class UndeadHorseRenderer extends BhHorseRenderer<BhUndeadHorse> {

    public UndeadHorseRenderer(EntityRendererProvider.Context context, ModelLayerLocation body,
                               ModelLayerLocation saddle, ModelLayerLocation carpet,
                               ModelLayerLocation armor, ModelLayerLocation chest, BhTackTextures textures) {
        super(context,
                new UndeadHorseModel(context.bakeLayer(body)),
                new UndeadHorseModel(context.bakeLayer(body)),
                layer -> new UndeadHorseModel(context.bakeLayer(layer)),
                saddle, saddle, carpet, carpet, armor, armor, chest, chest, textures);
    }
}
