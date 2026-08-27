package icy.betterhorses.net.client.render;

import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.IcelandicHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class IcelandicHorseRenderer
        extends AbstractHorseRenderer<IcelandicHorse, BhHorseRenderState, IcelandicHorseModel> {

    public IcelandicHorseRenderer(EntityRendererProvider.Context context,
                                  ModelLayerLocation adultLayer,
                                  ModelLayerLocation babyLayer) {
        super(context,
                new IcelandicHorseModel(context.bakeLayer(adultLayer)),
                new IcelandicFoalModel(context.bakeLayer(babyLayer)));

        this.addLayer(BhTackLayer.<BhHorseRenderState, IcelandicHorseModel>forItem(this,
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_SADDLE)),
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_SADDLE_BABY)),
                state -> state.saddle,
                BhTackTextures.ICELANDIC::saddle));

        this.addLayer(BhTackLayer.<BhHorseRenderState, IcelandicHorseModel>forItem(this,
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_ARMOR)),
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_ARMOR_BABY)),
                state -> state.bodyArmorItem,
                BhTackTextures.ICELANDIC::armor));

        this.addLayer(new BhTackLayer<>(this,
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_CHEST)),
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? BhTackTextures.ICELANDIC.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public BhHorseRenderState createRenderState() {
        return new BhHorseRenderState();
    }

    @Override
    public void extractRenderState(IcelandicHorse entity, BhHorseRenderState state, float partialTick) {
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

        state.gaitedBlend = 1.0F;

        IHorseData data = IHorseData.of(entity);
        int gear = data.bh_getGaitGear();
        boolean following = data.bh_isOwned() && data.bh_getCommand() == HorseCommand.FOLLOW;
        state.toltRequest = state.isRidden
                ? (gear == BhGears.TOLT_LOW_GEAR || gear == BhGears.TOLT_HIGH_GEAR ? 1.0F : 0.0F)
                : (following ? 1.0F : 0.0F);

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
