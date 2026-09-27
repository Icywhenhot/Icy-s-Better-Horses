package icy.betterhorses.net;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface HorseInventoryLayoutAccess {
    record Storage(Container container, int from, int to) {}

    void bh_refreshLayout();

    boolean bh_hasUpgradedSaddleLayout();

    boolean bh_hasChestStorageLayout();

    int bh_getGearStartIndex();

    int bh_getChestStartIndex();

    int bh_getChestRows();

    List<Storage> bh_storage();

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
