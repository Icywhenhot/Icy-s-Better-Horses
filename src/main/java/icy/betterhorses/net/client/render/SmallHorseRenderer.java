package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.SmallHorse;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class SmallHorseRenderer<T extends SmallHorse>
        extends BhHorseRenderer<T> {

    public SmallHorseRenderer(EntityRendererProvider.Context context) {
        super(context,
                new SmallHorseModel(context.bakeLayer(BhModelLayers.SMALL_HORSE)),
                new SmallFoalModel(context.bakeLayer(BhModelLayers.SMALL_HORSE_BABY)),
                layer -> new SmallHorseModel(context.bakeLayer(layer)),
                BhModelLayers.SMALL_SADDLE, BhModelLayers.SMALL_SADDLE_BABY,
                BhModelLayers.SMALL_CARPET, BhModelLayers.SMALL_CARPET_BABY,
                BhModelLayers.SMALL_ARMOR, BhModelLayers.SMALL_ARMOR_BABY,
                BhModelLayers.SMALL_CHEST, BhModelLayers.SMALL_CHEST_BABY,
                BhTackTextures.SMALL);
    }

    @Override
    protected void gaitPose(T entity, BhHorseRenderState state) {
        state.riddenHeadDrop = 25.0F * Mth.DEG_TO_RAD;
    }
}
