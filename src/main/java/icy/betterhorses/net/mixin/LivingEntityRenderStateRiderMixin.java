package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.IBhRiderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateRiderMixin implements IBhRiderState {

    @Unique private int bh_riddenHorseId = -1;
    @Unique private float bh_riddenHorseYaw;
    @Unique private boolean bh_ridingCart;
    @Unique private @Nullable Vec3 bh_cartShift;
    @Unique private float bh_cartYaw;
    @Unique private float bh_cartPitch;
    @Unique private float bh_cartPivot;

    @Override
    public void bh_setCartSeat(@Nullable Vec3 shift, float yaw, float pitch, float pivot) {
        this.bh_cartShift = shift;
        this.bh_cartYaw = yaw;
        this.bh_cartPitch = pitch;
        this.bh_cartPivot = pivot;
    }

    @Override
    public @Nullable Vec3 bh_getCartShift() {
        return this.bh_cartShift;
    }

    @Override
    public float bh_getCartYaw() {
        return this.bh_cartYaw;
    }

    @Override
    public float bh_getCartPitch() {
        return this.bh_cartPitch;
    }

    @Override
    public float bh_getCartPivot() {
        return this.bh_cartPivot;
    }

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
