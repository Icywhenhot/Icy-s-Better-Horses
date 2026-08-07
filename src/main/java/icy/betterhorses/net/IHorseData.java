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

    /**
     * Copy counter for the whistle's respawn system. When the whistle can't reach the real entity
     * (unloaded chunk), a fresh copy is spawned from the stored snapshot with a higher generation;
     * any copy with a lower generation than the world's current one is stale and gets discarded on load.
     */
    int bh_getGeneration();
    void bh_setGeneration(int generation);

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

    /**
     * True when the horse-cart item occupies the (shared) stabilizer gear slot. Backed by a synced
     * flag so it is correct on both sides (the gear container itself is not synced to clients).
     * Drives cart spawn/despawn in {@code AbstractHorseMixin.bh_tickCart}.
     */
    boolean bh_hasCartGear();

    /**
     * True when the stabilizer item (not the cart) occupies the shared slot. Uses the synced gear
     * flag plus {@link #bh_hasCartGear()}, so it is safe on the client for the wing render layer.
     * Only true {@link net.minecraft.world.entity.animal.equine.Horse Horse}s benefit from the
     * stabilizer — mules, donkeys, skeleton/zombie horses can't wear it (they can still pull a
     * cart, which shares this slot), so the effect and wing render are gated to real horses here.
     */
    default boolean bh_hasStabilizerItem() {
        return this instanceof net.minecraft.world.entity.animal.equine.Horse
                && bh_hasGear(GearSlot.STABILIZER) && !bh_hasCartGear();
    }

    /**
     * True when a chest has been mounted on the cart this horse pulls. Backed by a synced flag: the
     * renderer needs it to decide whether to draw the cart's chest bone, and the horse GUI needs it
     * to know the cart is locked in its gear slot.
     *
     * <p>Chest state lives on the <i>horse</i> rather than the cart because the cart entity is
     * derived state that is discarded and respawned on every chunk reload
     * (see {@code HorseCartEntity#shouldBeSaved}); anything stored on it would not survive.</p>
     */
    boolean bh_hasCartChest();

    void bh_setCartChest(boolean attached);

    /**
     * Storage behind the cart's chest — 54 slots, the size of a double chest. Only meaningful while
     * {@link #bh_hasCartChest()}.
     */
    SimpleContainer bh_getCartChestContainer();

    /**
     * Drops the cart's chest item along with everything inside it, and clears the attached flag.
     * Called for every route that separates a horse from its cart other than the player shearing it
     * off by hand: death, upgraded-saddle removal, and the cart item leaving the gear slot.
     */
    void bh_dropCartChest();

    /**
     * Seats a player on this horse through its normal ride path (so ownership gating still applies).
     * Used when boarding the cart: cart riders are passengers of the horse — merely attached at the
     * bench — so driving stays vanilla horse control.
     */
    void bh_ridePlayer(net.minecraft.world.entity.player.Player player);

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

    // --- Management screen ---

    /**
     * True while the horse still carries anything at all: saddle, body armor, its vanilla inventory,
     * the mod's gear slots or its chest storage. Disowning is refused until this is false, so nothing
     * the player owns walks off with a horse they just let go of.
     */
    boolean bh_hasAnyEquipment();

    /**
     * Releases the horse back into the wild: ejects riders, untames it, clears the owner (which also
     * drops its whistle snapshot), and forgets home, hitchpost and bond. Server side only.
     */
    void bh_disown();
}
