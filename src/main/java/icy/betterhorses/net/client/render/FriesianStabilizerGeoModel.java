package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

/**
 * Stabilizer for the Friesian horse: its own geometry and texture, but the <em>same</em>
 * animation file as the generic one.
 *
 * <p>That reuse only works because the bones the animation drives - {@code wingsL},
 * {@code wingsL2}, {@code canister left}, {@code canister right} - are named identically in
 * every model. The Friesian source calls its sacks {@code left_sacks}/{@code right_sacks},
 * so the exporter renames them on the way out. If the two ever drift apart the wings will
 * still deploy and the sacks will sit frozen, which is the quiet failure to watch for.
 */
public final class FriesianStabilizerGeoModel extends GeoModel<HorseStabilizerAnimatable> {

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st_friesian");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            IcysBetterHorses.MOD_ID, "textures/entity/horse/friesian/stabilizer.png");
    // deliberately the shared animation, not a copy
    private static final Identifier ANIMATION =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "st");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(HorseStabilizerAnimatable animatable) {
        return ANIMATION;
    }
}
