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
        extends AbstractHorseRenderer<FriesianHorse, BhHorseRenderState, FriesianHorseModel> {

    public FriesianHorseRenderer(EntityRendererProvider.Context context,
                                 ModelLayerLocation adultLayer,
                                 ModelLayerLocation babyLayer) {
        super(context,
                new FriesianHorseModel(context.bakeLayer(adultLayer)),
                new FriesianFoalModel(context.bakeLayer(babyLayer)));

        this.addLayer(BhTackLayer.<BhHorseRenderState, FriesianHorseModel>forItem(this,
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_SADDLE)),
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_SADDLE_BABY)),
                state -> state.saddle,
                BhTackTextures.FRIESIAN::saddle));

        this.addLayer(BhTackLayer.<BhHorseRenderState, FriesianHorseModel>forArmor(this,
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_ARMOR)),
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_ARMOR_BABY)),
                BhTackTextures.FRIESIAN));

        this.addLayer(new BhTackLayer<>(this,
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_CHEST)),
                new FriesianHorseModel(context.bakeLayer(BhModelLayers.FRIESIAN_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? BhTackTextures.FRIESIAN.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public BhHorseRenderState createRenderState() {
        return new BhHorseRenderState();
    }

    @Override
    public void extractRenderState(FriesianHorse entity, BhHorseRenderState state, float partialTick) {
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
                IHorseData.of(entity).bh_getCommand() == HorseCommand.STAY;

        state.entityId = entity.getId();

        BhEquineGait.advanceFor(entity, state);
    }

    @Override
    public Identifier getTextureLocation(BhHorseRenderState state) {
        return state.coatTexture;
    }
}
