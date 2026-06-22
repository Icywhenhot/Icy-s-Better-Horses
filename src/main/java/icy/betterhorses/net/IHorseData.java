package icy.betterhorses.net;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import icy.betterhorses.net.inventory.GearSlot;

import java.util.UUID;

public interface IHorseData {
    @Nullable UUID bh_getOwner();
    void bh_setOwner(@Nullable UUID owner);

    HorseCommand bh_getCommand();
    void bh_setCommand(HorseCommand command);

    @Nullable BlockPos bh_getHome();
    void bh_setHome(@Nullable BlockPos pos);

    @Nullable BlockPos bh_getWanderCenter();
    void bh_setWanderCenter(@Nullable BlockPos pos);

    default void bh_setWanderCommand(BlockPos pos) {
        bh_setWanderCenter(pos);
        bh_setCommand(HorseCommand.WANDER);
    }

    @Nullable BlockPos bh_getHitchpostPos();
    void bh_setHitchpostPos(@Nullable BlockPos pos);

    int bh_getBond();
    void bh_setBond(int level);

    /** True once this horse has already been awarded the one-time name-tag bond. */
    boolean bh_hasReceivedNameTagBond();
    void bh_setReceivedNameTagBond(boolean received);

    HorseGender bh_getGender();
    void bh_setGender(HorseGender gender);

    HorseBreed bh_getBreed();
    void bh_setBreed(HorseBreed breed);

    boolean bh_isMixedBreed();
    void bh_setMixedBreed(boolean mixed);

    HorseStabilizerState bh_getStabilizerState();
    void bh_setStabilizerState(HorseStabilizerState state);
    int bh_getGearFlags();

    default boolean bh_isOwned() {
        return bh_getOwner() != null;
    }

    default boolean bh_isHitched() {
        return bh_getHitchpostPos() != null;
    }

    default boolean bh_hasGear(GearSlot slot) {
        return (bh_getGearFlags() & (1 << slot.ordinal())) != 0;
    }

    // --- Upgraded saddle gear + chest ---

    /** True when the upgraded saddle item occupies the horse saddle slot. */
    boolean bh_hasUpgradedSaddle();

    /** Place the given upgraded saddle stack into the horse saddle slot (1.20.1 has no equipSaddle(stack)). */
    void bh_equipUpgradedSaddle(ItemStack saddle);

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
