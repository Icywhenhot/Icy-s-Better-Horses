package icy.betterhorses.net.client.render;

import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.IcelandicHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class IcelandicHorseRenderer
        extends AbstractHorseRenderer<IcelandicHorse, IcelandicHorseRenderState, IcelandicHorseModel> {

    public IcelandicHorseRenderer(EntityRendererProvider.Context context,
                                  ModelLayerLocation adultLayer,
                                  ModelLayerLocation babyLayer) {
        super(context,
                new IcelandicHorseModel(context.bakeLayer(adultLayer)),
                new IcelandicFoalModel(context.bakeLayer(babyLayer)));

        this.addLayer(BhTackLayer.<IcelandicHorseRenderState, IcelandicHorseModel>forItem(this,
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_SADDLE)),
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_SADDLE_BABY)),
                state -> state.saddle,
                IcelandicTackTextures::saddle));

        this.addLayer(BhTackLayer.<IcelandicHorseRenderState, IcelandicHorseModel>forItem(this,
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_ARMOR)),
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_ARMOR_BABY)),
                state -> state.bodyArmorItem,
                IcelandicTackTextures::armor));


        this.addLayer(new BhTackLayer<>(this,
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_CHEST)),
                new IcelandicHorseModel(context.bakeLayer(BhModelLayers.ICELANDIC_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? IcelandicTackTextures.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public IcelandicHorseRenderState createRenderState() {
        return new IcelandicHorseRenderState();
    }

    @Override
    public void extractRenderState(IcelandicHorse entity, IcelandicHorseRenderState state, float partialTick) {
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

        IHorseData data = (IHorseData) entity;
        int gear = data.bh_getGaitGear();
        boolean following = data.bh_isOwned() && data.bh_getCommand() == HorseCommand.FOLLOW;
        state.toltRequest = state.isRidden
                ? (gear == BhGears.TOLT_LOW_GEAR || gear == BhGears.TOLT_HIGH_GEAR ? 1.0F : 0.0F)
                : (following ? 1.0F : 0.0F);

        state.commandedToStay =
                ((IHorseData) entity).bh_getCommand() == HorseCommand.STAY;

        state.entityId = entity.getId();

        BhEquineGait.fillJumpInputs(entity, state);
        BhEquineGait gait = BhEquineGait.get(entity.getId());
        gait.advance(state, state.ageInTicks);
    }

    @Override
    public Identifier getTextureLocation(IcelandicHorseRenderState state) {
        return state.coatTexture;
    }
}
