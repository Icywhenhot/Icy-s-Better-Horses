package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.entity.PercheronHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class PercheronHorseRenderer
        extends AbstractHorseRenderer<PercheronHorse, BhHorseRenderState, PercheronHorseModel> {

    public PercheronHorseRenderer(EntityRendererProvider.Context context,
                                  ModelLayerLocation adultLayer,
                                  ModelLayerLocation babyLayer) {
        super(context,
                new PercheronHorseModel(context.bakeLayer(adultLayer)),
                new PercheronFoalModel(context.bakeLayer(babyLayer)));

        this.addLayer(BhTackLayer.<BhHorseRenderState, PercheronHorseModel>forItem(this,
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_SADDLE)),
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_SADDLE_BABY)),
                state -> state.saddle,
                BhTackTextures.PERCHERON::saddle));

        this.addLayer(BhTackLayer.<BhHorseRenderState, PercheronHorseModel>forItem(this,
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_ARMOR)),
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_ARMOR_BABY)),
                state -> state.bodyArmorItem,
                BhTackTextures.PERCHERON::armor));

        this.addLayer(new BhTackLayer<>(this,
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_CHEST)),
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? BhTackTextures.PERCHERON.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public BhHorseRenderState createRenderState() {
        return new BhHorseRenderState();
    }

    @Override
    public void extractRenderState(PercheronHorse entity, BhHorseRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.onGround = entity.onGround();
        state.isPassenger = entity.isPassenger();
        state.coatTexture = entity.bhCoats().texture(entity.bhCoat(), entity.isBaby());
        state.hurt = entity.hurtTime > 0 ? entity.hurtTime / 10.0F : 0.0F;

        state.bodyYaw = entity.getYRot();
        state.healthFraction = entity.getMaxHealth() > 0.0F
                ? Mth.clamp(entity.getHealth() / entity.getMaxHealth(), 0.0F, 1.0F)
                : 1.0F;

        state.phaseOffset = (entity.getId() * 0.6180339887F % 1.0F) * Mth.TWO_PI;

        state.riddenHeadDrop = 20.0F * Mth.DEG_TO_RAD;

        state.commandedToStay =
                IHorseData.of(entity).bh_getCommand() == HorseCommand.STAY;

        state.entityId = entity.getId();

        BhEquineGait.advanceFor(entity, state);
    }

    @Override
    public Identifier getTextureLocation(BhHorseRenderState state) {
        return state.coatTexture;
    }
}
