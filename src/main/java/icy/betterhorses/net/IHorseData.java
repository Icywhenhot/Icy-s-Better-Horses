package icy.betterhorses.net;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IHorseData {
    @Nullable UUID bh_getOwner();
    void bh_setOwner(@Nullable UUID owner);

    HorseCommand bh_getCommand();
    void bh_setCommand(HorseCommand command);

    @Nullable BlockPos bh_getHome();
    void bh_setHome(@Nullable BlockPos pos);

    int bh_getBond();
    void bh_setBond(int level);

    HorseStabilizerState bh_getStabilizerState();
    void bh_setStabilizerState(HorseStabilizerState state);

    default boolean bh_isOwned() {
        return bh_getOwner() != null;
    }

    // --- Upgraded saddle gear + chest ---

    /** True when the upgraded saddle item occupies the horse saddle slot. */
    boolean bh_hasUpgradedSaddle();

    /** 5-slot container for the CHEST, HOOVES, MEDKIT, STABILIZER, HITCHPOST gear items. */
    SimpleContainer bh_getGearContainer();

    /** 27-slot sub-inventory, only usable when chest gear is equipped. */
    SimpleContainer bh_getChestContainer();

    /** True when the chest gear item is in its gear slot. */
    boolean bh_hasChestGear();

    /** Called by the menu when the chest gear slot is cleared, so contents can be dropped. */
    void bh_onChestGearRemoved(ItemStack previousChestGear);

    /** Called when the upgraded saddle is removed, so dependent gear and storage can be dropped. */
    void bh_onUpgradedSaddleRemoved(ItemStack previousSaddle);
}
