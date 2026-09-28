package icy.betterhorses.net;

import net.minecraft.world.entity.player.Player;

public interface HorseInventoryLayoutAccess {
    void bh_refreshLayout();

    boolean bh_hasUpgradedSaddleLayout();

    boolean bh_hasChestStorageLayout();

    int bh_getGearStartIndex();

    int bh_getChestStartIndex();

    int bh_getChestRows();

    boolean bh_isRiderPanel();

    void bh_setRiderPanel(boolean shown);

    int bh_getRiderGearStartIndex();

    default boolean bh_isCartSlotLocked() {
        return false;
    }

    default boolean bh_isSaddleSlotLocked() {
        return false;
    }

    default void bh_onMenuRemoved(Player player) {
    }
}
