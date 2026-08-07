package icy.betterhorses.net;

import net.minecraft.world.entity.player.Player;

public interface HorseInventoryLayoutAccess {
    void bh_refreshLayout();

    boolean bh_hasUpgradedSaddleLayout();

    boolean bh_hasChestStorageLayout();

    int bh_getGearStartIndex();

    int bh_getChestStartIndex();

    /**
     * True while the cart in the shared stabilizer slot is held there by a chest fitted to it.
     * Unhitching the cart would strand the chest and its contents, so the slot refuses to give the
     * cart back until the chest has been sheared off. The screen reads this to flash the slot red.
     */
    default boolean bh_isCartSlotLocked() {
        return false;
    }

    default void bh_onMenuRemoved(Player player) {
    }
}
