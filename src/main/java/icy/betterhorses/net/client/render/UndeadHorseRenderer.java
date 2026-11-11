package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BhUndeadHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class UndeadHorseRenderer extends BhHorseRenderer<BhUndeadHorse> {

    public UndeadHorseRenderer(EntityRendererProvider.Context context, ModelLayerLocation body,
                               ModelLayerLocation saddle, ModelLayerLocation carpet,
                               ModelLayerLocation armor, ModelLayerLocation chest, BhTackTextures textures) {
        this(context, body, saddle, carpet, armor, chest, textures, body == BhModelLayers.LARGE_UNDEAD_HORSE);
    }

    private UndeadHorseRenderer(EntityRendererProvider.Context context, ModelLayerLocation body,
                                ModelLayerLocation saddle, ModelLayerLocation carpet,
                                ModelLayerLocation armor, ModelLayerLocation chest, BhTackTextures textures,
                                boolean draft) {
        super(context,
                new UndeadHorseModel(context.bakeLayer(body), draft),
                new UndeadHorseModel(context.bakeLayer(body), draft),
                layer -> new UndeadHorseModel(context.bakeLayer(layer), draft),
                saddle, saddle, carpet, carpet, armor, armor, chest, chest, textures);
    }
}
