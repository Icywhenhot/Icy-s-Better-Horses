package icy.betterhorses.net.client.render;

import icy.betterhorses.net.HorseStabilizerState;

public interface IBhEquineStabilizerState {

    void bh_setStabilizerData(boolean hasStabilizer, HorseStabilizerState state, int horseId, float partialTick);

    boolean bh_hasStabilizer();

    HorseStabilizerState bh_getStabilizerState();

    int bh_getHorseId();

    float bh_getPartialTick();

    void bh_setMountedViewData(boolean riddenByPlayerInFirstPerson, float opacity);

    boolean bh_isRiddenByPlayerInFirstPerson();

    float bh_getOpacity();

    void bh_setChestGear(boolean hasChestGear, boolean enderChest);

    boolean bh_hasChestGear();

    boolean bh_hasEnderChestGear();
}
