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

    int bh_getGeneration();
    void bh_setGeneration(int generation);

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

    default boolean bh_maySaddleUp(@Nullable UUID playerId) {
        UUID owner = bh_getOwner();
        if (owner == null) return true;
        return owner.equals(playerId) || (playerId != null && HorseTracker.isTrusted(owner, playerId));
    }

    default boolean bh_mayHandle(@Nullable UUID playerId) {
        return bh_maySaddleUp(playerId);
    }

    int bh_getGear();
    void bh_setGear(int gear);

    int bh_getGaitGear();
    void bh_setGaitGear(int gear);

    /**
     * True while the rider is in third person, where the camera is free and the horse is steered
     * with A/D instead of by pointing the view. Synced from the riding client - see
     * {@code BhSteerModePayload}.
     */
    boolean bh_isFreeSteer();
    void bh_setFreeSteer(boolean freeSteer);

    default boolean bh_isHitched() {
        return bh_getHitchpostPos() != null;
    }

    default boolean bh_hasGear(GearSlot slot) {
        return (bh_getGearFlags() & (1 << slot.ordinal())) != 0;
    }

    boolean bh_hasCartGear();

    @Nullable icy.betterhorses.net.entity.HorseCartEntity bh_getCartEntity();

    default boolean bh_hasStabilizerItem() {
        return this instanceof net.minecraft.world.entity.animal.equine.Horse
                && bh_hasGear(GearSlot.STABILIZER) && !bh_hasCartGear();
    }

    boolean bh_hasCartChest();

    void bh_setCartChest(boolean attached);

    SimpleContainer bh_getCartChestContainer();

    void bh_dropCartChest();

    void bh_ridePlayer(net.minecraft.world.entity.player.Player player);

    boolean bh_hasUpgradedSaddle();

    SimpleContainer bh_getGearContainer();

    SimpleContainer bh_getChestContainer();

    boolean bh_hasChestGear();

    boolean bh_hasEnderChestGear();

    void bh_onChestGearRemoved(ItemStack previousChestGear);

    void bh_onUpgradedSaddleRemoved(ItemStack previousSaddle);

    boolean bh_hasAnyEquipment();

    void bh_disown();
}
