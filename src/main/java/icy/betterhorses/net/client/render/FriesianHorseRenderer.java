package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.entity.FriesianHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class FriesianHorseRenderer
        extends AbstractHorseRenderer<FriesianHorse, FriesianHorseRenderState, FriesianHorseModel> {

    public FriesianHorseRenderer(EntityRendererProvider.Context context,
                                 ModelLayerLocation adultLayer,
                                 ModelLayerLocation babyLayer) {
        super(context,
                new FriesianHorseModel(context.bakeLayer(adultLayer)),
                new FriesianFoalModel(context.bakeLayer(babyLayer)));

        this.addLayer(BhTackLayer.<FriesianHorseRenderState, FriesianHorseModel>forItem(this,
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_SADDLE)),
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_SADDLE_BABY)),
                state -> state.saddle,
                FriesianTackTextures::saddle));

        this.addLayer(BhTackLayer.<FriesianHorseRenderState, FriesianHorseModel>forItem(this,
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_ARMOR)),
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_ARMOR_BABY)),
                state -> state.bodyArmorItem,
                FriesianTackTextures::armor));


        this.addLayer(new BhTackLayer<>(this,
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_CHEST)),
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? FriesianTackTextures.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public FriesianHorseRenderState createRenderState() {
        return new FriesianHorseRenderState();
    }

    @Override
    public void extractRenderState(FriesianHorse entity, FriesianHorseRenderState state, float partialTick) {
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

        state.commandedToStay =
                ((IHorseData) entity).bh_getCommand() == HorseCommand.STAY;

        state.entityId = entity.getId();

        BhEquineGait.fillJumpInputs(entity, state);
        BhEquineGait gait = BhEquineGait.get(entity.getId());
        gait.advance(state, state.ageInTicks);
    }

    @Override
    public Identifier getTextureLocation(FriesianHorseRenderState state) {
        return state.coatTexture;
    }
}
