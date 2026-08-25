package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.entity.MediumHorse;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class MediumHorseRenderer<T extends MediumHorse>
        extends AbstractHorseRenderer<T, MediumHorseRenderState, MediumHorseModel> {

    public MediumHorseRenderer(EntityRendererProvider.Context context) {
        super(context,
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_HORSE)),
                new MediumFoalModel(context.bakeLayer(BhModelLayers.MEDIUM_HORSE_BABY)));

        this.addLayer(BhTackLayer.<MediumHorseRenderState, MediumHorseModel>forItem(this,
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_SADDLE)),
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_SADDLE_BABY)),
                state -> state.saddle,
                MediumTackTextures::saddle));

        this.addLayer(BhTackLayer.<MediumHorseRenderState, MediumHorseModel>forItem(this,
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_ARMOR)),
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_ARMOR_BABY)),
                state -> state.bodyArmorItem,
                MediumTackTextures::armor));


        this.addLayer(new BhTackLayer<>(this,
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_CHEST)),
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? MediumTackTextures.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public MediumHorseRenderState createRenderState() {
        return new MediumHorseRenderState();
    }

    @Override
    public void extractRenderState(T entity, MediumHorseRenderState state, float partialTick) {
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

        state.riddenHeadDrop = 25.0F * Mth.DEG_TO_RAD;

        state.commandedToStay =
                ((IHorseData) entity).bh_getCommand() == HorseCommand.STAY;

        state.entityId = entity.getId();

        BhEquineGait.fillJumpInputs(entity, state);
        BhEquineGait gait = BhEquineGait.get(entity.getId());
        gait.advance(state, state.ageInTicks);
    }

    @Override
    public Identifier getTextureLocation(MediumHorseRenderState state) {
        return state.coatTexture;
    }
}
