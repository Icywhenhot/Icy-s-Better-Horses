package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.entity.ShireHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * The Shire, the second breed on the large rig.
 *
 * <p>The foal is the shared large-breed foal -- the Percheron's mesh, the same
 * file rather than a copy -- so the baby layer bakes {@code PercheronFoalGeometry}
 * and the coats come from the Shire's own foal atlases, which were transferred
 * onto exactly that mesh.
 *
 * <p>The tack baby layers still bake adult geometry, for a different reason:
 * foals wear no tack, so those layers never draw at all.
 */
public class ShireHorseRenderer
        extends AbstractHorseRenderer<ShireHorse, ShireHorseRenderState, ShireHorseModel> {

    public ShireHorseRenderer(EntityRendererProvider.Context context,
                                  ModelLayerLocation adultLayer,
                                  ModelLayerLocation babyLayer) {
        super(context,
                new ShireHorseModel(context.bakeLayer(adultLayer)),
                new ShireFoalModel(context.bakeLayer(babyLayer)));

        this.addLayer(BhTackLayer.<ShireHorseRenderState, ShireHorseModel>forItem(this,
                new ShireHorseModel(context.bakeLayer(BhModelLayers.SHIRE_SADDLE)),
                new ShireHorseModel(context.bakeLayer(BhModelLayers.SHIRE_SADDLE_BABY)),
                state -> state.saddle,
                ShireTackTextures::saddle));

        this.addLayer(BhTackLayer.<ShireHorseRenderState, ShireHorseModel>forItem(this,
                new ShireHorseModel(context.bakeLayer(BhModelLayers.SHIRE_ARMOR)),
                new ShireHorseModel(context.bakeLayer(BhModelLayers.SHIRE_ARMOR_BABY)),
                state -> state.bodyArmorItem,
                ShireTackTextures::armor));

        this.addLayer(new BhTackLayer<>(this,
                new ShireHorseModel(context.bakeLayer(BhModelLayers.SHIRE_CHEST)),
                new ShireHorseModel(context.bakeLayer(BhModelLayers.SHIRE_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? ShireTackTextures.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public ShireHorseRenderState createRenderState() {
        return new ShireHorseRenderState();
    }

    @Override
    public void extractRenderState(ShireHorse entity, ShireHorseRenderState state, float partialTick) {
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
    public Identifier getTextureLocation(ShireHorseRenderState state) {
        return state.coatTexture;
    }
}
