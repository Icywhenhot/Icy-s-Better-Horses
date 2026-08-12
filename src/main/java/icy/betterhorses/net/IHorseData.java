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

    @Nullable BlockPos bh_getHitchpostPos();
    void bh_setHitchpostPos(@Nullable BlockPos pos);

    int bh_getBond();
    void bh_setBond(int level);

    // copy counter for the whistle's respawn system
    int bh_getGeneration();
    void bh_setGeneration(int generation);

    // true once this horse has already been awarded the one-time name-tag bond
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

    // true when this player may take the reins: the owner, or someone the owner trusted with
    // /horse trust. an unowned horse has nobody to gate it, so everyone passes
    default boolean bh_maySaddleUp(@Nullable UUID playerId) {
        UUID owner = bh_getOwner();
        if (owner == null) return true;
        return owner.equals(playerId) || (playerId != null && HorseTracker.isTrusted(owner, playerId));
    }

    default boolean bh_isHitched() {
        return bh_getHitchpostPos() != null;
    }

    default boolean bh_hasGear(GearSlot slot) {
        return (bh_getGearFlags() & (1 << slot.ordinal())) != 0;
    }

    // true when the horse-cart item occupies the (shared) stabilizer gear slot
    boolean bh_hasCartGear();

    // the cart this horse is pulling, if one is live right now. server side only
    @Nullable icy.betterhorses.net.entity.HorseCartEntity bh_getCartEntity();

    // true when the stabilizer item (not the cart) occupies the shared slot
    default boolean bh_hasStabilizerItem() {
        return this instanceof net.minecraft.world.entity.animal.equine.Horse
                && bh_hasGear(GearSlot.STABILIZER) && !bh_hasCartGear();
    }

    // true when a chest has been mounted on the cart this horse pulls
    boolean bh_hasCartChest();

    void bh_setCartChest(boolean attached);

    // storage behind the cart's chest, 54 slots, the size of a double chest
    SimpleContainer bh_getCartChestContainer();

    // drops the cart's chest item along with everything inside it, and clears the attached flag
    void bh_dropCartChest();

    // seats a player on this horse through its normal ride path (so ownership gating still applies)
    void bh_ridePlayer(net.minecraft.world.entity.player.Player player);

    // upgraded saddle gear + chest

    // true when the upgraded saddle item occupies the horse saddle slot
    boolean bh_hasUpgradedSaddle();

    // 5-slot container for the CHEST, HOOVES, MEDKIT, STABILIZER, HITCHPOST gear items
    SimpleContainer bh_getGearContainer();

    // 27-slot sub-inventory, only usable when chest gear is equipped
    SimpleContainer bh_getChestContainer();

    // true when the chest gear item is in its gear slot. server side only: it reads the gear
    // container, which is never synced. On the client use bh_hasGear(GearSlot.CHEST) instead
    boolean bh_hasChestGear();

    // true when that chest gear item is specifically an ender chest rather than a plain one.
    // synced, so the renderer can pick between the two panniers on either side
    boolean bh_hasEnderChestGear();

    // called by the menu when the chest gear slot is cleared, so contents can be dropped
    void bh_onChestGearRemoved(ItemStack previousChestGear);

    // called when the upgraded saddle is removed, so dependent gear and storage can be dropped
    void bh_onUpgradedSaddleRemoved(ItemStack previousSaddle);

    // management screen

    // true while the horse still carries anything at all: saddle, body armor, its vanilla inventory
    boolean bh_hasAnyEquipment();

    // releases the horse back into the wild: ejects riders, untames
    void bh_disown();
}
