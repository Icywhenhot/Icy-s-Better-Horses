package icy.betterhorses.net.client.render;

import icy.betterhorses.net.HorseStabilizerState;

public interface BhStabilizerStateAccess {
    boolean bh_hasStabilizer();

    void bh_setHasStabilizer(boolean hasStabilizer);

    HorseStabilizerState bh_getStabilizerState();

    void bh_setStabilizerState(HorseStabilizerState state);

    int bh_getHorseId();

    void bh_setHorseId(int horseId);
}
