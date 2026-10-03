package icy.betterhorses.net;

import icy.betterhorses.net.entity.CartType;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.inventory.GearSlot;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.registry.CommandType;
import icy.betterhorses.net.registry.GenderType;
import icy.betterhorses.net.registry.SpeciesType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IHorseData {
    @Nullable UUID bh_getOwner();
    void bh_setOwner(@Nullable UUID owner);

    // Legacy 1.21.1 command storage bridge; registry-facing callers use the key helpers below.
    HorseCommand bh_getCommand();
    void bh_setCommand(HorseCommand command);

    default ResourceKey<CommandType> bh_getCommandKey() {
        return switch (bh_getCommand()) {
            case STAY -> BhContent.COMMAND_STAY.key();
            case RETURN_HOME -> BhContent.COMMAND_RETURN_HOME.key();
            case SET_HOME -> BhContent.COMMAND_SET_HOME.key();
            case WANDER -> BhContent.COMMAND_WANDER.key();
            case ABILITY -> BhContent.COMMAND_ABILITY.key();
            default -> BhContent.COMMAND_FOLLOW.key();
        };
    }

    default void bh_setCommandKey(ResourceKey<CommandType> command) {
        ResourceLocation id = command.location();
        if (id.equals(BhContent.COMMAND_STAY.key().location())) bh_setCommand(HorseCommand.STAY);
        else if (id.equals(BhContent.COMMAND_RETURN_HOME.key().location())) bh_setCommand(HorseCommand.RETURN_HOME);
        else if (id.equals(BhContent.COMMAND_SET_HOME.key().location())) bh_setCommand(HorseCommand.SET_HOME);
        else if (id.equals(BhContent.COMMAND_WANDER.key().location())) bh_setCommand(HorseCommand.WANDER);
        else if (id.equals(BhContent.COMMAND_ABILITY.key().location())) bh_setCommand(HorseCommand.ABILITY);
        else bh_setCommand(HorseCommand.FOLLOW);
    }

    @Nullable BlockPos bh_getHome();
    void bh_setHome(@Nullable BlockPos pos);
    @Nullable ResourceKey<Level> bh_getHomeDimension();
    @Nullable BlockPos bh_getWanderCenter();
    void bh_setWanderCenter(@Nullable BlockPos pos);

    int bh_getBond();
    void bh_setBond(int level);
    int bh_getGeneration();
    void bh_setGeneration(int generation);
    boolean bh_hasReceivedNameTagBond();
    void bh_setReceivedNameTagBond(boolean received);

    HorseGender bh_getGender();
    void bh_setGender(HorseGender gender);

    default ResourceKey<GenderType> bh_getGenderKey() {
        return bh_getGender() == HorseGender.FEMALE ? BhContent.FEMALE.key() : BhContent.MALE.key();
    }

    default void bh_setGenderKey(ResourceKey<GenderType> gender) {
        bh_setGender(gender.equals(BhContent.FEMALE.key()) ? HorseGender.FEMALE : HorseGender.MALE);
    }

    HorseBreed bh_getBreed();
    void bh_setBreed(HorseBreed breed);

    default @Nullable ResourceKey<BreedType> bh_getBreedKey() {
        HorseBreed breed = bh_getBreed();
        if (!breed.isRealBreed()) return null;
        return ResourceKey.create(BhRegistries.BREED_TYPES,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, breed.id()));
    }

    default void bh_setBreedKey(@Nullable ResourceKey<BreedType> breed) {
        if (breed == null) {
            bh_setBreed(HorseBreed.UNKNOWN_SPECIES);
            return;
        }
        HorseBreed legacy = HorseBreed.byId(breed.location().getPath());
        bh_setBreed(legacy.isRealBreed() ? legacy : HorseBreed.UNKNOWN_SPECIES);
    }

    default ResourceKey<SpeciesType> bh_getSpecies() {
        return switch (bh_getBreed()) {
            case DONKEY_SPECIES -> BhContent.SPECIES_DONKEY.key();
            case MULE_SPECIES -> BhContent.SPECIES_MULE.key();
            case SKELETON_SPECIES -> BhContent.SPECIES_SKELETON.key();
            case ZOMBIE_SPECIES -> BhContent.SPECIES_ZOMBIE.key();
            default -> BhContent.SPECIES_NONE.key();
        };
    }

    default void bh_setSpecies(ResourceKey<SpeciesType> species) {
        if (species.equals(BhContent.SPECIES_DONKEY.key())) bh_setBreed(HorseBreed.DONKEY_SPECIES);
        else if (species.equals(BhContent.SPECIES_MULE.key())) bh_setBreed(HorseBreed.MULE_SPECIES);
        else if (species.equals(BhContent.SPECIES_SKELETON.key())) bh_setBreed(HorseBreed.SKELETON_SPECIES);
        else if (species.equals(BhContent.SPECIES_ZOMBIE.key())) bh_setBreed(HorseBreed.ZOMBIE_SPECIES);
        else if (!bh_getBreed().isRealBreed()) bh_setBreed(HorseBreed.UNKNOWN_SPECIES);
    }

    boolean bh_isMixedBreed();
    void bh_setMixedBreed(boolean mixed);

    HorseStabilizerState bh_getStabilizerState();
    void bh_setStabilizerState(HorseStabilizerState state);
    int bh_getGearFlags();

    default boolean bh_isOwned() { return bh_getOwner() != null; }
    default boolean bh_maySaddleUp(@Nullable UUID playerId) {
        UUID owner = bh_getOwner();
        if (owner == null) return true;
        return owner.equals(playerId) || (playerId != null && HorseTracker.isTrusted(owner, playerId));
    }
    default boolean bh_mayHandle(@Nullable UUID playerId) { return bh_maySaddleUp(playerId); }

    int bh_getGear();
    void bh_setGear(int gear);
    @Nullable UUID bh_getCombatTarget();
    void bh_setCombatTarget(@Nullable UUID target);
    int bh_getSpookTicks();
    void bh_setSpookTicks(int ticks);
    int bh_getCombatState();
    int bh_getKickTicks();
    int bh_getJumpCue();
    void bh_cueJump();
    void bh_setKickTicks(int ticks);
    boolean bh_isAbilityPaused();
    void bh_setAbilityPaused(boolean paused);

    default int bh_getChestRows() {
        if (bh_hasEnderChestGear()) return 3;
        return BhBreedData.of(bh_getBreedKey()).rowsAt(BhHorseTraits.bondTier(bh_getBond()));
    }

    int bh_getGaitGear();
    void bh_setGaitGear(int gear);
    boolean bh_isFreeLook();
    void bh_setFreeLook(boolean freeLook);
    int bh_getStompTicks();
    void bh_setStompTicks(int ticks);
    int bh_getSurge();
    void bh_setSurge(int packed);

    default int bh_getAbilitySurge(int slot) {
        return slot == 0 ? bh_getSurge() : 0;
    }
    default void bh_setAbilitySurge(int slot, int packed) {
        if (slot == 0) bh_setSurge(packed);
    }

    int bh_getPerkSurge();
    void bh_setPerkSurge(int packed);
    int bh_getPulse();
    void bh_setPulse(int packed);
    int bh_getCharge();
    void bh_setCharge(int fill);

    default boolean bh_hasGear(GearSlot slot) { return (bh_getGearFlags() & (1 << slot.ordinal())) != 0; }
    boolean bh_hasCartGear();
    int bh_getBondRemainder();
    void bh_setBondRemainder(int value);
    long bh_getRescueReadyAt();
    void bh_setRescueReadyAt(long value);
    void bh_onRemoved();
    @Nullable HorseCartEntity bh_getCartEntity();
    default void bh_bindCartEntity(HorseCartEntity cart) {}
    @Nullable UUID bh_getCartId();
    void bh_setCartId(@Nullable UUID id);

    default boolean bh_hasStabilizerItem() {
        return this instanceof Horse
                && bh_hasGear(GearSlot.STABILIZER) && !bh_hasCartGear()
                && bh_getStabilizerCharge() > 0.0F;
    }
    float bh_getStabilizerCharge();
    void bh_syncStabilizerCharge();

    default boolean bh_mayUseLargeCart() {
        return BhBreedData.of(bh_getBreedKey()).archetype().allowsLargeCart();
    }

    CartType bh_getCartType();
    void bh_syncCartType();

    boolean bh_hasCartChest();
    ItemStack bh_getCartChestItem();
    void bh_setCartChest(ItemStack chest);
    SimpleContainer bh_getCartChestContainer();
    void bh_dropCartChest();
    boolean bh_hasCartPlough();
    ItemStack bh_getCartPlough();
    void bh_setCartPlough(ItemStack hoe);
    void bh_dropCartPlough();
    void bh_ridePlayer(Player player);
    void bh_clearStanding();
    boolean bh_hasUpgradedSaddle();
    SimpleContainer bh_getGearContainer();
    SimpleContainer bh_getChestContainer();
    boolean bh_hasChestGear();
    boolean bh_hasEnderChestGear();
    void bh_onChestGearRemoved(ItemStack previousChestGear);
    void bh_onUpgradedSaddleRemoved(ItemStack previousSaddle);
    boolean bh_hasAnyEquipment();
    void bh_disown();

    static IHorseData of(AbstractHorse horse) { return (IHorseData) horse; }
}
