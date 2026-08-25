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
        extends AbstractHorseRenderer<PercheronHorse, PercheronHorseRenderState, PercheronHorseModel> {

    public PercheronHorseRenderer(EntityRendererProvider.Context context,
                                  ModelLayerLocation adultLayer,
                                  ModelLayerLocation babyLayer) {
        super(context,
                new PercheronHorseModel(context.bakeLayer(adultLayer)),
                new PercheronFoalModel(context.bakeLayer(babyLayer)));

        this.addLayer(BhTackLayer.<PercheronHorseRenderState, PercheronHorseModel>forItem(this,
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_SADDLE)),
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_SADDLE_BABY)),
                state -> state.saddle,
                PercheronTackTextures::saddle));

        this.addLayer(BhTackLayer.<PercheronHorseRenderState, PercheronHorseModel>forItem(this,
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_ARMOR)),
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_ARMOR_BABY)),
                state -> state.bodyArmorItem,
                PercheronTackTextures::armor));

        this.addLayer(new BhTackLayer<>(this,
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_CHEST)),
                new PercheronHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? PercheronTackTextures.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public PercheronHorseRenderState createRenderState() {
        return new PercheronHorseRenderState();
    }

    @Override
    public void extractRenderState(PercheronHorse entity, PercheronHorseRenderState state, float partialTick) {
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
                ((IHorseData) entity).bh_getCommand() == HorseCommand.STAY;

        state.entityId = entity.getId();

        BhEquineGait.fillJumpInputs(entity, state);
        BhEquineGait gait = BhEquineGait.get(entity.getId());
        gait.advance(state, state.ageInTicks);
    }

    @Override
    public Identifier getTextureLocation(PercheronHorseRenderState state) {
        return state.coatTexture;
    }
}
