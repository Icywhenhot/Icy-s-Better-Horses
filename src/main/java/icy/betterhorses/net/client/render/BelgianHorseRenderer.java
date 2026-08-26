package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.entity.BelgianHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * The Belgian Draft, the third breed on the large rig.
 *
 * <p>The foal is the shared large-breed foal, and here that is not a judgement
 * call: {@code belgian baby.bbmodel} is byte-for-byte the Percheron's file. So
 * the baby layer bakes {@code PercheronFoalGeometry} and the coats come from the
 * Belgian's own foal atlases, transferred onto exactly that mesh.
 *
 * <p>The tack baby layers still bake adult geometry, for a different reason:
 * foals wear no tack, so those layers never draw at all.
 */
public class BelgianHorseRenderer
        extends AbstractHorseRenderer<BelgianHorse, BelgianHorseRenderState, BelgianHorseModel> {

    public BelgianHorseRenderer(EntityRendererProvider.Context context,
                                  ModelLayerLocation adultLayer,
                                  ModelLayerLocation babyLayer) {
        super(context,
                new BelgianHorseModel(context.bakeLayer(adultLayer)),
                new BelgianFoalModel(context.bakeLayer(babyLayer)));

        this.addLayer(BhTackLayer.<BelgianHorseRenderState, BelgianHorseModel>forItem(this,
                new BelgianHorseModel(context.bakeLayer(BhModelLayers.BELGIAN_SADDLE)),
                new BelgianHorseModel(context.bakeLayer(BhModelLayers.BELGIAN_SADDLE_BABY)),
                state -> state.saddle,
                BelgianTackTextures::saddle));

        this.addLayer(BhTackLayer.<BelgianHorseRenderState, BelgianHorseModel>forItem(this,
                new BelgianHorseModel(context.bakeLayer(BhModelLayers.BELGIAN_ARMOR)),
                new BelgianHorseModel(context.bakeLayer(BhModelLayers.BELGIAN_ARMOR_BABY)),
                state -> state.bodyArmorItem,
                BelgianTackTextures::armor));

        this.addLayer(new BhTackLayer<>(this,
                new BelgianHorseModel(context.bakeLayer(BhModelLayers.BELGIAN_CHEST)),
                new BelgianHorseModel(context.bakeLayer(BhModelLayers.BELGIAN_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? BelgianTackTextures.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public BelgianHorseRenderState createRenderState() {
        return new BelgianHorseRenderState();
    }

    @Override
    public void extractRenderState(BelgianHorse entity, BelgianHorseRenderState state, float partialTick) {
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
    public Identifier getTextureLocation(BelgianHorseRenderState state) {
        return state.coatTexture;
    }
}
