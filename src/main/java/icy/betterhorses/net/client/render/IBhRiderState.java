package icy.betterhorses.net.client.render;

public interface IBhRiderState {

    void bh_setRiddenHorse(int horseId, float bodyYaw, boolean onCart);

    int bh_getRiddenHorseId();

    float bh_getRiddenHorseYaw();

    boolean bh_isRidingCart();
}
