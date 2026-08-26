package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.entity.ClydesdaleHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * The Clydesdale, the fourth breed on the large rig, and the first that brings
 * no tack of its own.
 *
 * <p><b>The saddle, the armour and the chest are the PERCHERON'S</b> -- its
 * model layers and its textures, referenced rather than copied. That is sound
 * because the Clydesdale mesh is the Percheron's twenty-three cubes byte for
 * byte plus twelve feathering cubes on the legs, and every cube the tack sits
 * on is in the first set: the saddle and chest ride the barrel, the bridle the
 * head and muzzle, the armour cuff the foreleg column. Not one of them moved.
 *
 * <p>Referencing rather than duplicating is deliberate. Copies of a shell that
 * is genuinely the same shell drift -- the pipeline guide records exactly that
 * happening when a saddle arrived as a byte-identical copy sitting 2px off its
 * horse -- and there is nothing here to keep two copies honest. If the
 * Clydesdale ever wants its own armour (the cuff art was drawn for a bare leg
 * and now sits over the top of the feather), that is the moment to split it,
 * and it is a change to this file and BhModelLayers alone.
 *
 * <p>The stabilizer shares the same way, in {@link HorseStabilizerLayer}: the
 * brace is modelled against a body cube this breed also has unchanged.
 *
 * <p>The foal is the shared large-breed foal, so the baby layer bakes
 * {@code PercheronFoalGeometry} and the coats come from the Clydesdale's own
 * foal atlases, transferred onto exactly that mesh. The tack baby layers still
 * bake adult geometry, for a different reason: foals wear no tack, so those
 * layers never draw.
 */
public class ClydesdaleHorseRenderer
        extends AbstractHorseRenderer<ClydesdaleHorse, ClydesdaleHorseRenderState, ClydesdaleHorseModel> {

    public ClydesdaleHorseRenderer(EntityRendererProvider.Context context,
                                   ModelLayerLocation adultLayer,
                                   ModelLayerLocation babyLayer) {
        super(context,
                new ClydesdaleHorseModel(context.bakeLayer(adultLayer)),
                new ClydesdaleFoalModel(context.bakeLayer(babyLayer)));

        this.addLayer(BhTackLayer.<ClydesdaleHorseRenderState, ClydesdaleHorseModel>forItem(this,
                new ClydesdaleHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_SADDLE)),
                new ClydesdaleHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_SADDLE_BABY)),
                state -> state.saddle,
                PercheronTackTextures::saddle));

        this.addLayer(BhTackLayer.<ClydesdaleHorseRenderState, ClydesdaleHorseModel>forItem(this,
                new ClydesdaleHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_ARMOR)),
                new ClydesdaleHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_ARMOR_BABY)),
                state -> state.bodyArmorItem,
                PercheronTackTextures::armor));

        this.addLayer(new BhTackLayer<>(this,
                new ClydesdaleHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_CHEST)),
                new ClydesdaleHorseModel(context.bakeLayer(BhModelLayers.PERCHERON_CHEST_BABY)),
                state -> {
                    IBhEquineStabilizerState bhState = (IBhEquineStabilizerState) (Object) state;
                    return bhState.bh_hasChestGear()
                            ? PercheronTackTextures.chest(bhState.bh_hasEnderChestGear())
                            : null;
                }));
    }

    @Override
    public ClydesdaleHorseRenderState createRenderState() {
        return new ClydesdaleHorseRenderState();
    }

    @Override
    public void extractRenderState(ClydesdaleHorse entity, ClydesdaleHorseRenderState state, float partialTick) {
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
    public Identifier getTextureLocation(ClydesdaleHorseRenderState state) {
        return state.coatTexture;
    }
}
