package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.IcelandicHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Renderer for the Icelandic horse.
 *
 * <p>Extends {@link AbstractHorseRenderer} rather than a bespoke renderer so the mod's
 * existing {@code AbstractHorseRendererMixin} still applies - that is what attaches the
 * stabilizer wings, the chest layer and the cart reins. {@link IcelandicHorseModel}
 * implements {@code HorseModelAccessor} for the same reason: the stabilizer layer asks the
 * parent model for its body bone, and would otherwise fail the cast.
 */
public class IcelandicHorseRenderer
        extends AbstractHorseRenderer<IcelandicHorse, IcelandicHorseRenderState, IcelandicHorseModel> {


    public IcelandicHorseRenderer(EntityRendererProvider.Context context,
                                  ModelLayerLocation adultLayer,
                                  ModelLayerLocation babyLayer) {
        super(context,
                new IcelandicHorseModel(context.bakeLayer(adultLayer)),
                new IcelandicHorseModel(context.bakeLayer(babyLayer)));

        // AbstractHorseRenderer adds no equipment layers - vanilla's HorseRenderer does that,
        // and it is final so it cannot be extended. These use BhTackLayer rather than
        // SimpleEquipmentLayer because the latter cannot be told which texture to use and
        // resolves a vanilla one through the equipment-asset system instead.
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

        // The chest is the mod's own gear rather than an EquipmentSlot item, so it is driven by
        // the flag the renderer mixin already puts on the render state.
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
        state.coatTexture = entity.bhCoats().texture(entity.bhCoat());
        state.hurt = entity.hurtTime > 0 ? entity.hurtTime / 10.0F : 0.0F;

        // a stable per-entity offset: two horses side by side must not breathe in sync.
        // derived from the id rather than random() so it survives across frames.
        state.phaseOffset = (entity.getId() * 0.6180339887F % 1.0F) * Mth.TWO_PI;

        // gait weights have to be integrated over time, so they live outside the state
        BhEquineGait.get(entity.getId()).advance(state, state.ageInTicks);
    }

    @Override
    public Identifier getTextureLocation(IcelandicHorseRenderState state) {
        return state.coatTexture;
    }
}
