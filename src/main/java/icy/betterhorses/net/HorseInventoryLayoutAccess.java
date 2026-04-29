package icy.betterhorses.net;

public interface HorseInventoryLayoutAccess {
    default void bh_refreshLayout() {
    }

    boolean bh_hasUpgradedSaddleLayout();

    boolean bh_hasChestStorageLayout();

    int bh_getGearStartIndex();

    int bh_getChestStartIndex();
}
