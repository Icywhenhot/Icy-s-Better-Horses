package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.client.render.IBhEquineStabilizerState;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds stabilizer-related fields onto vanilla {@link EquineRenderState} so the data the live
 * entity carries (via {@code IHorseData}) is available at submit-time, when only the render
 * state is reachable in 1.21.11's split-extract/submit pipeline.
 *
 * Populated each frame by {@code AbstractHorseRendererMixin.bh_captureStabilizerState}; consumed
 * by {@code HorseStabilizerLayer.submit}.
 */
@Mixin(EquineRenderState.class)
public abstract class EquineRenderStateMixin implements IBhEquineStabilizerState {

    @Unique private boolean bh_hasStabilizer;
    @Unique private HorseStabilizerState bh_stabilizerState = HorseStabilizerState.CLOSED;
    @Unique private int bh_horseId = -1;
    @Unique private float bh_partialTick;

    @Override
    public void bh_setStabilizerData(boolean hasStabilizer, HorseStabilizerState state, int horseId, float partialTick) {
        this.bh_hasStabilizer = hasStabilizer;
        this.bh_stabilizerState = state == null ? HorseStabilizerState.CLOSED : state;
        this.bh_horseId = horseId;
        this.bh_partialTick = partialTick;
    }

    @Override
    public boolean bh_hasStabilizer() {
        return this.bh_hasStabilizer;
    }

    @Override
    public HorseStabilizerState bh_getStabilizerState() {
        return this.bh_stabilizerState;
    }

    @Override
    public int bh_getHorseId() {
        return this.bh_horseId;
    }

    @Override
    public float bh_getPartialTick() {
        return this.bh_partialTick;
    }
}
