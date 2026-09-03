package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.IBhRiderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateRiderMixin implements IBhRiderState {

    @Unique private int bh_riddenHorseId = -1;
    @Unique private float bh_riddenHorseYaw;
    @Unique private boolean bh_ridingCart;

    @Override
    public void bh_setRiddenHorse(int horseId, float bodyYaw, boolean onCart) {
        this.bh_riddenHorseId = horseId;
        this.bh_riddenHorseYaw = bodyYaw;
        this.bh_ridingCart = onCart;
    }

    @Override
    public boolean bh_isRidingCart() {
        return this.bh_ridingCart;
    }

    @Override
    public int bh_getRiddenHorseId() {
        return this.bh_riddenHorseId;
    }

    @Override
    public float bh_getRiddenHorseYaw() {
        return this.bh_riddenHorseYaw;
    }
}
