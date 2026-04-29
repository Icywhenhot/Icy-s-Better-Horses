package icy.betterhorses.net.client.mixin;

import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.client.render.BhStabilizerStateAccess;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EquineRenderState.class)
public abstract class EquineRenderStateMixin implements BhStabilizerStateAccess {

    @Unique private boolean bh_hasStabilizer;
    @Unique private HorseStabilizerState bh_stabilizerState = HorseStabilizerState.CLOSED;
    @Unique private int bh_horseId;

    @Override
    public boolean bh_hasStabilizer() {
        return this.bh_hasStabilizer;
    }

    @Override
    public void bh_setHasStabilizer(boolean hasStabilizer) {
        this.bh_hasStabilizer = hasStabilizer;
    }

    @Override
    public HorseStabilizerState bh_getStabilizerState() {
        return this.bh_stabilizerState;
    }

    @Override
    public void bh_setStabilizerState(HorseStabilizerState state) {
        this.bh_stabilizerState = state;
    }

    @Override
    public int bh_getHorseId() {
        return this.bh_horseId;
    }

    @Override
    public void bh_setHorseId(int horseId) {
        this.bh_horseId = horseId;
    }
}
