package icy.betterhorses.net.client.render;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface IBhRiderState {

    void bh_setCartSeat(@Nullable Vec3 shift, float yaw, float pitch, float pivot);

    @Nullable Vec3 bh_getCartShift();

    float bh_getCartYaw();

    float bh_getCartPitch();

    float bh_getCartPivot();

    void bh_setRiddenHorse(int horseId, float bodyYaw, boolean onCart);

    int bh_getRiddenHorseId();

    float bh_getRiddenHorseYaw();

    boolean bh_isRidingCart();
}
