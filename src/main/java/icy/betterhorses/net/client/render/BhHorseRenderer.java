package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.function.Function;

public class BhHorseRenderer<T extends BhBreedHorse> extends AbstractHorseRenderer<T, BhHorseRenderState, BhHorseModel> {

    protected BhHorseRenderer(EntityRendererProvider.Context context,
                              BhHorseModel adultModel,
                              BhHorseModel babyModel,
                              Function<ModelLayerLocation, BhHorseModel> models,
                              ModelLayerLocation saddle,
                              ModelLayerLocation saddleBaby,
                              ModelLayerLocation carpet,
                              ModelLayerLocation carpetBaby,
                              ModelLayerLocation armor,
                              ModelLayerLocation armorBaby,
                              ModelLayerLocation chest,
                              ModelLayerLocation chestBaby,
                              BhTackTextures textures) {
        super(context, adultModel, babyModel);

        this.addLayer(new HoofDustLayer<>(this));

        this.addLayer(BhTackLayer.<BhHorseRenderState, BhHorseModel>forItem(this,
                models.apply(saddle), models.apply(saddleBaby),
                state -> state.carpet == null ? state.saddle : null,
                textures::saddle));

        this.addLayer(new BhTackLayer<>(this, models.apply(carpet), models.apply(carpetBaby),
                state -> state.carpet == null ? null
                        : state.carpetTexture != null ? state.carpetTexture : textures.carpet(state.carpet)));

        this.addLayer(BhTackLayer.<BhHorseRenderState, BhHorseModel>forArmor(this,
                models.apply(armor), models.apply(armorBaby), textures));

        this.addLayer(new BhTackLayer<>(this, models.apply(chest), models.apply(chestBaby),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear() ? textures.chest(bhState.bh_hasEnderChestGear()) : null;
                }));
    }

    @Override
    public BhHorseRenderState createRenderState() {
        return new BhHorseRenderState();
    }

    @Override
    public void extractRenderState(T entity, BhHorseRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.onGround = entity.onGround();
        state.isPassenger = entity.isPassenger();
        state.coatTexture = BhNamedCoats.coat(entity);
        state.hurt = entity.hurtTime > 0 ? entity.hurtTime / 10.0F : 0.0F;

        state.bodyYaw = entity.getYRot();
        state.healthFraction = entity.getMaxHealth() > 0.0F
                ? Mth.clamp(entity.getHealth() / entity.getMaxHealth(), 0.0F, 1.0F)
                : 1.0F;

        state.phaseOffset = (entity.getId() * 0.6180339887F % 1.0F) * Mth.TWO_PI;

        state.commandedToStay =
                IHorseData.of(entity).bh_getCommand().equals(BhContent.COMMAND_STAY.key());

        state.entityId = BhEquineGait.renderId(entity.getId());

        BhEquineGait.advanceFor(entity, state);
    }

    @Override
    public Identifier getTextureLocation(BhHorseRenderState state) {
        return state.coatTexture;
    }
}
