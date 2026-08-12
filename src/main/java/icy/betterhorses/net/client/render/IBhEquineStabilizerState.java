package icy.betterhorses.net.client.render;

import icy.betterhorses.net.HorseStabilizerState;

// duck-typed extension carried by net.minecraft.client.renderer.entity.state.EquineRenderState via
public interface IBhEquineStabilizerState {

    void bh_setStabilizerData(boolean hasStabilizer, HorseStabilizerState state, int horseId, float partialTick);

    boolean bh_hasStabilizer();

    HorseStabilizerState bh_getStabilizerState();

    // entity id of the source horse, used as the stable key into HorseStabilizerAnimatable
    int bh_getHorseId();

    // sub-tick interpolation captured during extractRenderState
    float bh_getPartialTick();

    void bh_setMountedViewData(boolean riddenByPlayerInFirstPerson, float opacity);

    boolean bh_isRiddenByPlayerInFirstPerson();

    float bh_getOpacity();

    void bh_setChestGear(boolean hasChestGear, boolean enderChest);

    // true when a chest / ender chest sits in the horse's gear slot, so HorseChestLayer draws the pouch
    boolean bh_hasChestGear();

    // true when that chest is an ender chest, which gets its own pannier texture
    boolean bh_hasEnderChestGear();
}
