package icy.betterhorses.net.client.render;

public interface IBhRiderState {

    void bh_setRiddenHorse(int horseId, float bodyYaw);

    int bh_getRiddenHorseId();

    float bh_getRiddenHorseYaw();
}
