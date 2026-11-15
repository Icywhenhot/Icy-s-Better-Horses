package icy.betterhorses.net.client.render;

import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.IcelandicHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class IcelandicHorseRenderer
        extends BhHorseRenderer<IcelandicHorse> {

    public IcelandicHorseRenderer(EntityRendererProvider.Context context,
                                  ModelLayerLocation adultLayer,
                                  ModelLayerLocation babyLayer) {
        super(context,
                new IcelandicHorseModel(context.bakeLayer(adultLayer)),
                new IcelandicFoalModel(context.bakeLayer(babyLayer)),
                layer -> new IcelandicHorseModel(context.bakeLayer(layer)),
                BhModelLayers.ICELANDIC_SADDLE, BhModelLayers.ICELANDIC_SADDLE_BABY,
                BhModelLayers.ICELANDIC_CARPET, BhModelLayers.ICELANDIC_CARPET_BABY,
                BhModelLayers.ICELANDIC_ARMOR, BhModelLayers.ICELANDIC_ARMOR_BABY,
                BhModelLayers.ICELANDIC_CHEST, BhModelLayers.ICELANDIC_CHEST_BABY,
                BhTackTextures.ICELANDIC);
    }

    @Override
    protected void gaitPose(IcelandicHorse entity, BhHorseRenderState state) {
        state.gaitedBlend = 1.0F;

        IHorseData data = IHorseData.of(entity);
        int gear = data.bh_getGaitGear();
        boolean following = data.bh_isOwned() && data.bh_getCommand().equals(BhContent.COMMAND_FOLLOW.key());
        state.toltRequest = state.isRidden
                ? (gear == BhGears.TOLT_GEAR ? 1.0F : 0.0F)
                : (following ? 1.0F : 0.0F);
    }
}
