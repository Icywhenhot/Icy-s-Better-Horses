package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.FriesianHorse;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Renderer for the Friesian horse.
 *
 * <p>Extends {@link AbstractHorseRenderer} rather than a bespoke renderer so the mod's
 * existing {@code AbstractHorseRendererMixin} still applies - that is what attaches the
 * stabilizer wings, the chest layer and the cart reins. {@link FriesianHorseModel}
 * inherits {@code HorseModelAccessor} from {@link BhHorseModel} for the same reason: the
 * stabilizer layer asks the parent model for its body bone, and would otherwise fail the cast.
 */
public class FriesianHorseRenderer
        extends AbstractHorseRenderer<FriesianHorse, FriesianHorseRenderState, FriesianHorseModel> {

    public FriesianHorseRenderer(EntityRendererProvider.Context context,
                                 ModelLayerLocation adultLayer,
                                 ModelLayerLocation babyLayer) {
        super(context,
                new FriesianHorseModel(context.bakeLayer(adultLayer)),
                new FriesianHorseModel(context.bakeLayer(babyLayer)));

        // AbstractHorseRenderer adds no equipment layers - vanilla's HorseRenderer does that,
        // and it is final so it cannot be extended. These use BhTackLayer rather than
        // SimpleEquipmentLayer because the latter cannot be told which texture to use and
        // resolves a vanilla one through the equipment-asset system instead.
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

        // The chest is the mod's own gear rather than an EquipmentSlot item, so it is driven by
        // the flag the renderer mixin already puts on the render state.
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
        state.coatTexture = entity.bhCoats().texture(entity.bhCoat());
        state.hurt = entity.hurtTime > 0 ? entity.hurtTime / 10.0F : 0.0F;

        // a stable per-entity offset: two horses side by side must not breathe in sync.
        // derived from the id rather than random() so it survives across frames.
        state.phaseOffset = (entity.getId() * 0.6180339887F % 1.0F) * Mth.TWO_PI;

        // gait weights have to be integrated over time, so they live outside the state
        BhEquineGait.get(entity.getId()).advance(state, state.ageInTicks);
    }

    @Override
    public Identifier getTextureLocation(FriesianHorseRenderState state) {
        return state.coatTexture;
    }
}
