package icy.betterhorses.net;

import net.minecraft.world.entity.player.Player;

public interface HorseInventoryLayoutAccess {
    void bh_refreshLayout();

    boolean bh_hasUpgradedSaddleLayout();

    boolean bh_hasChestStorageLayout();

    int bh_getGearStartIndex();

    int bh_getChestStartIndex();

    // true while the cart in the shared stabilizer slot is held there by a chest fitted
    default boolean bh_isCartSlotLocked() {
        return false;
    }

    // true while the saddle is held on by a hitched cart. the cart hangs off the saddle's gear slots,
    // so pulling the saddle out from under it would strand the cart
    default boolean bh_isSaddleSlotLocked() {
        return false;
    }

    default void bh_onMenuRemoved(Player player) {
    }
}
