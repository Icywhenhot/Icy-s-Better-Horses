package icy.betterhorses.net;

import net.minecraft.world.Container;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

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

    default boolean bh_isCartSlotLocked() {
        return false;
    }

    default boolean bh_isSaddleSlotLocked() {
        return false;
    }

    default void bh_onMenuRemoved(Player player) {
    }

    default @Nullable AbstractHorse bh_mount() {
        return null;
    }
}
