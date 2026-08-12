package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.MediumHorse;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Renderer for every horse in the medium size class.
 *
 * <p>One renderer, generic over the entity type, rather than one per breed. The three
 * medium breeds share a mesh, a saddle, an armour, a chest and a stabilizer; the only thing
 * that differs is which coat texture comes off the entity, and that is read through
 * {@link MediumHorse#bhCoats()} without the renderer needing to know the breed. Registering
 * it three times gives three independent instances.
 *
 * <p>Extends {@link AbstractHorseRenderer} rather than a bespoke renderer so the mod's
 * existing {@code AbstractHorseRendererMixin} still applies - that is what attaches the
 * stabilizer wings, the chest layer and the cart reins. {@link MediumHorseModel} inherits
 * {@code HorseModelAccessor} from {@link BhHorseModel} for the same reason: the stabilizer
 * layer asks the parent model for its body bone, and would otherwise fail the cast.
 */
public class MediumHorseRenderer<T extends MediumHorse>
        extends AbstractHorseRenderer<T, MediumHorseRenderState, MediumHorseModel> {

    public MediumHorseRenderer(EntityRendererProvider.Context context) {
        super(context,
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_HORSE)),
                new MediumHorseModel(context.bakeLayer(BhModelLayers.MEDIUM_HORSE_BABY)));

        // AbstractHorseRenderer adds no equipment layers - vanilla's HorseRenderer does that,
        // and it is final so it cannot be extended. These use BhTackLayer rather than
        // SimpleEquipmentLayer because the latter cannot be told which texture to use and
        // resolves a vanilla one through the equipment-asset system instead.
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

        // The chest is the mod's own gear rather than an EquipmentSlot item, so it is driven by
        // the flag the renderer mixin already puts on the render state.
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
        state.coatTexture = entity.bhCoats().texture(entity.bhCoat());
        state.hurt = entity.hurtTime > 0 ? entity.hurtTime / 10.0F : 0.0F;

        // a stable per-entity offset: two horses side by side must not breathe in sync.
        // derived from the id rather than random() so it survives across frames.
        state.phaseOffset = (entity.getId() * 0.6180339887F % 1.0F) * Mth.TWO_PI;

        // gait weights have to be integrated over time, so they live outside the state
        BhEquineGait.get(entity.getId()).advance(state, state.ageInTicks);
    }

    @Override
    public Identifier getTextureLocation(MediumHorseRenderState state) {
        return state.coatTexture;
    }
}
