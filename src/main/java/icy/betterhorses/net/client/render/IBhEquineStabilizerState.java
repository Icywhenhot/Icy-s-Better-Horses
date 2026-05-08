package icy.betterhorses.net.client.render;

import icy.betterhorses.net.HorseStabilizerState;

/**
 * Duck-typed extension carried by {@code net.minecraft.client.renderer.entity.state.EquineRenderState}
 * via mixin. 1.21.11's render pipeline forbids touching the live {@code AbstractHorse} from inside
 * {@code RenderLayer.submit} — only the {@code RenderState} is visible there — so we capture the
 * data we need at {@code extractRenderState} time and read it back during submit.
 */
public interface IBhEquineStabilizerState {

    void bh_setStabilizerData(boolean hasStabilizer, HorseStabilizerState state, int horseId, float partialTick);

    boolean bh_hasStabilizer();

    HorseStabilizerState bh_getStabilizerState();

    /** Entity id of the source horse — used as the stable key into {@code HorseStabilizerAnimatable}. */
    int bh_getHorseId();

    /** Sub-tick interpolation captured during {@code extractRenderState}. */
    float bh_getPartialTick();

    void bh_setMountedViewData(boolean riddenByPlayerInFirstPerson, float opacity);

    boolean bh_isRiddenByPlayerInFirstPerson();

    float bh_getOpacity();
}
