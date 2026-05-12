package icy.betterhorses.net;

import net.minecraft.world.entity.player.Player;

public interface HorseInventoryLayoutAccess {
    void bh_refreshLayout();

    boolean bh_hasUpgradedSaddleLayout();

    boolean bh_hasChestStorageLayout();

    int bh_getGearStartIndex();

    int bh_getChestStartIndex();

    default void bh_onMenuRemoved(Player player) {
    }
}
