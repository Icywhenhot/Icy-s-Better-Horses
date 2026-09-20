package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.ModSounds;
import icy.betterhorses.net.BhCriteria;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.HorseGender;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.BhHorseInteraction;
import icy.betterhorses.net.BhHorseStorage;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhVanillaHorseSwap;
import icy.betterhorses.net.BhHorseSteering;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.feature.BreedAbilities;
import icy.betterhorses.net.feature.breed.BreedAbility;
import icy.betterhorses.net.feature.CartRig;
import icy.betterhorses.net.feature.breed.ArchetypePerks;
import icy.betterhorses.net.feature.breed.Ironclad;
import icy.betterhorses.net.feature.HorseCombat;
import icy.betterhorses.net.feature.FrostHooves;
import icy.betterhorses.net.feature.HorseFeature;
import icy.betterhorses.net.feature.RiderGate;
import icy.betterhorses.net.feature.Stabilizer;
import icy.betterhorses.net.feature.SpeedRecord;
import icy.betterhorses.net.feature.SaddleWatch;
import icy.betterhorses.net.feature.SwimBoost;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.inventory.CartChestMenu;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.goal.HorseFollowOwnerGoal;
import icy.betterhorses.net.goal.HorseReturnHomeGoal;
import icy.betterhorses.net.goal.DefendOwnerGoal;
import icy.betterhorses.net.goal.HorseStayGoal;
import icy.betterhorses.net.goal.SpookGoal;
import icy.betterhorses.net.goal.HorseWanderBoundsGoal;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;
import icy.betterhorses.net.BhRiderSeat;
import icy.betterhorses.net.entity.BhBreedEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements IHorseData, IHorseAbilityHost {

    @Shadow
    protected SimpleContainer inventory;

    @Shadow
    private int eatingCounter;

    @Shadow
    private int standCounter;

    @Shadow
    protected abstract void doPlayerRide(Player player);


    @Unique private volatile @Nullable UUID bh_owner = null;
    @Unique private HorseCommand bh_command = HorseCommand.FOLLOW;
    @Unique private @Nullable BlockPos bh_home = null;
    @Unique private @Nullable ResourceKey<Level> bh_homeDim = null;
    @Unique private @Nullable BlockPos bh_wanderCenter = null;
    @Unique private int bh_bond = 0;
    @Unique private boolean bh_nameTagBondReceived = false;
    @Unique private int bh_generation = 0;
    @Unique
    private final SimpleContainer bh_gearContainer = new SimpleContainer(GearSlot.COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            AbstractHorseMixin.this.bh_syncGearFlags();
        }
    };
    @Unique private static final int BH_CHEST_MAX_SLOTS = 54;
    @Unique private final SimpleContainer bh_chestContainer = new SimpleContainer(BH_CHEST_MAX_SLOTS);
    @Unique private static final int BH_CART_CHEST_SIZE = CartChestMenu.SLOTS;
    @Unique private @Nullable SimpleContainer bh_cartChestContainer;
    @Unique private ItemStack bh_cartPlow = ItemStack.EMPTY;
    @Unique private boolean bh_fedGoldenAppleThisTick = false;
    @Unique private static final float BH_HURT_NEIGH_CHANCE = 0.3F;
    @Unique private static final int BH_GRAZE_ROLL_INTERVAL = 1200;
    @Unique private static final int BH_GRAZE_HURT_COOLDOWN_TICKS = 200;
    @Unique private int bh_grazeBlockedUntilTick = 0;
    @Unique private int bh_gear = 0;
    @Unique private @Nullable UUID bh_combatTarget = null;
    @Unique private boolean bh_abilityPaused = false;
    @Unique private int bh_spookTicks = 0;

    @Unique private final SaddleWatch bh_saddle = new SaddleWatch();
    @Unique private final CartRig bh_cartRig = new CartRig();
    @Unique private final HorseCombat bh_combat = new HorseCombat();
    @Unique private final BreedAbilities bh_abilities = new BreedAbilities();

    @Unique private HorseFeature[] bh_features;

    @Unique
    private HorseFeature[] bh_features() {
        if (bh_features == null) {
            bh_features = new HorseFeature[]{
            bh_saddle,
            (horse, data) -> bh_clearGearWhenUnridden(horse),
            new SpeedRecord(),
            new RiderGate(),
            new Stabilizer(),
            bh_cartRig,
            new SwimBoost(),
            new FrostHooves(),
            bh_combat,
            bh_abilities,
            };
        }
        return bh_features;
    }

    @Unique private static final float BH_HOOVES_FALL_DAMAGE_MULTIPLIER = 0.5F;

    @Unique private static final EntityDataAccessor<Integer> BH_BOND =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique private static final EntityDataAccessor<Integer> BH_STABILIZER_STATE =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique private static final EntityDataAccessor<Integer> BH_GEAR_FLAGS =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique private static final EntityDataAccessor<Integer> BH_GENDER_ID =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique private static final EntityDataAccessor<Integer> BH_BREED_ID =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique private static final EntityDataAccessor<Boolean> BH_BREED_MIXED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BOOLEAN);
    @Unique private static final EntityDataAccessor<Integer> BH_GEAR = bh_intKey();
    @Unique private static final EntityDataAccessor<Integer> BH_GAIT_GEAR = bh_intKey();
    @Unique private static final EntityDataAccessor<Integer> BH_COMBAT = bh_intKey();
    @Unique private static final EntityDataAccessor<Integer> BH_KICK = bh_intKey();
    @Unique private static final EntityDataAccessor<Integer> BH_STOMP = bh_intKey();
    @Unique private static final EntityDataAccessor<Integer> BH_SURGE = bh_intKey();
    @Unique private static final EntityDataAccessor<Integer> BH_PULSE = bh_intKey();
    @Unique private static final EntityDataAccessor<Integer> BH_PERK = bh_intKey();
    @Unique private static final EntityDataAccessor<Integer> BH_CHARGE = bh_intKey();
    @Unique private static final EntityDataAccessor<Integer> BH_COMMAND_ID = bh_intKey();
    @Unique private static final EntityDataAccessor<Boolean> BH_FREE_LOOK = bh_boolKey();
    @Unique private static final EntityDataAccessor<Boolean> BH_CART = bh_boolKey();
    @Unique private static final EntityDataAccessor<Boolean> BH_CART_CHEST = bh_boolKey();
    @Unique private static final EntityDataAccessor<Boolean> BH_CART_PLOW = bh_boolKey();
    @Unique private static final EntityDataAccessor<Boolean> BH_CART_LARGE = bh_boolKey();
    @Unique private static final EntityDataAccessor<Boolean> BH_ENDER_CHEST = bh_boolKey();
    @Unique private static final EntityDataAccessor<Boolean> BH_UPGRADED_SADDLE = bh_boolKey();
    @Unique private static final EntityDataAccessor<String> BH_OWNER_ID =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.STRING);

    @Unique
    private static EntityDataAccessor<Integer> bh_intKey() {
        return SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    }

    @Unique
    private static EntityDataAccessor<Boolean> bh_boolKey() {
        return SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BOOLEAN);
    }

    protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void bh_defineSynchedData(CallbackInfo ci) {
        this.entityData.define(BH_BOND, 0);
        this.entityData.define(BH_STABILIZER_STATE, 0);
        this.entityData.define(BH_GEAR_FLAGS, 0);
        this.entityData.define(BH_GENDER_ID, 0);
        this.entityData.define(BH_BREED_ID, HorseBreed.UNKNOWN_SPECIES.ordinal());
        this.entityData.define(BH_BREED_MIXED, false);
        this.entityData.define(BH_GEAR, 0);
        this.entityData.define(BH_GAIT_GEAR, 0);
        this.entityData.define(BH_COMBAT, 0);
        this.entityData.define(BH_KICK, 0);
        this.entityData.define(BH_STOMP, 0);
        this.entityData.define(BH_SURGE, 0);
        this.entityData.define(BH_PULSE, 0);
        this.entityData.define(BH_PERK, 0);
        this.entityData.define(BH_CHARGE, BhSurge.HIDDEN);
        this.entityData.define(BH_COMMAND_ID, HorseCommand.FOLLOW.ordinal());
        this.entityData.define(BH_FREE_LOOK, false);
        this.entityData.define(BH_CART, false);
        this.entityData.define(BH_CART_CHEST, false);
        this.entityData.define(BH_CART_PLOW, false);
        this.entityData.define(BH_CART_LARGE, false);
        this.entityData.define(BH_ENDER_CHEST, false);
        this.entityData.define(BH_UPGRADED_SADDLE, false);
        this.entityData.define(BH_OWNER_ID, "");
    }

    @Unique
    private <T> void bh_push(EntityDataAccessor<T> key, T value) {
        if (((AbstractHorse) (Object) this).level().isClientSide()) {
            return;
        }
        if (!this.entityData.get(key).equals(value)) {
            this.entityData.set(key, value);
        }
    }

    @Override
    public @Nullable UUID bh_getOwner() {
        if (level().isClientSide()) {
            return bh_parseOwner(this.entityData.get(BH_OWNER_ID));
        }
        return bh_owner;
    }

    @Override
    public void bh_setOwner(@Nullable UUID owner) {
        this.bh_owner = owner;
        bh_push(BH_OWNER_ID, owner == null ? "" : owner.toString());
        if (!level().isClientSide()) {
            AbstractHorse self = (AbstractHorse) (Object) this;
            if (owner != null) {
                HorseTracker.register(self);
            } else {
                HorseTracker.disown(self);
            }
        }
    }

    @Unique
    private static @Nullable UUID bh_parseOwner(String synced) {
        if (synced == null || synced.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(synced);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public HorseCommand bh_getCommand() {
        return ((AbstractHorse) (Object) this).level().isClientSide()
                ? HorseCommand.fromId(this.entityData.get(BH_COMMAND_ID))
                : bh_command;
    }

    @Override
    public void bh_setCommand(HorseCommand command) {
        this.bh_command = command;
        bh_push(BH_COMMAND_ID, command.ordinal());
    }

    @Override
    public @Nullable BlockPos bh_getHome() {
        return bh_home;
    }

    @Override
    public void bh_setHome(@Nullable BlockPos pos) {
        this.bh_home = pos;
        this.bh_homeDim = pos == null ? null : ((AbstractHorse) (Object) this).level().dimension();
    }

    @Override
    public @Nullable ResourceKey<Level> bh_getHomeDimension() {
        return this.bh_homeDim;
    }

    @Override
    public @Nullable BlockPos bh_getWanderCenter() {
        return bh_wanderCenter;
    }

    @Override
    public void bh_setWanderCenter(@Nullable BlockPos pos) {
        this.bh_wanderCenter = pos == null ? null : pos.immutable();
    }

    @Override
    public int bh_getBond() {
        return this.entityData.get(BH_BOND);
    }

    @Override
    public void bh_setBond(int level) {
        int previous = this.bh_bond;
        this.bh_bond = Math.max(0, Math.min(100, level));
        bh_push(BH_BOND, this.bh_bond);
        bh_applyBondAttributes();
        if (previous < 100 && this.bh_bond >= 100) {
            bh_awardOwner(BhCriteria.BOND_MAX);
        }
    }

    @Unique
    private void bh_awardOwner(String key) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide()) return;
        UUID owner = this.bh_getOwner();
        MinecraftServer server = self.level().getServer();
        if (owner == null || server == null) return;
        BhCriteria.fire(server.getPlayerList().getPlayer(owner), key);
    }

    @Override
    public int bh_getGeneration() {
        return this.bh_generation;
    }

    @Override
    public void bh_setGeneration(int generation) {
        this.bh_generation = generation;
    }

    @Override
    public boolean bh_hasReceivedNameTagBond() {
        return this.bh_nameTagBondReceived;
    }

    @Override
    public void bh_setReceivedNameTagBond(boolean received) {
        this.bh_nameTagBondReceived = received;
    }

    @Override
    public HorseGender bh_getGender() {
        return HorseGender.fromId(this.entityData.get(BH_GENDER_ID));
    }

    @Override
    public void bh_setGender(HorseGender gender) {
        bh_push(BH_GENDER_ID, gender.ordinal());
    }

    @Override
    public HorseBreed bh_getBreed() {
        if ((Object) this instanceof BhBreedEntity breedEntity) {
            return breedEntity.bhFixedBreed();
        }
        return HorseBreed.fromId(this.entityData.get(BH_BREED_ID));
    }

    @Override
    public void bh_setBreed(HorseBreed breed) {
        bh_push(BH_BREED_ID, breed.ordinal());
    }

    @Override
    public boolean bh_isMixedBreed() {
        return this.entityData.get(BH_BREED_MIXED);
    }

    @Override
    public void bh_setMixedBreed(boolean mixed) {
        bh_push(BH_BREED_MIXED, mixed);
    }

    @Override
    public HorseStabilizerState bh_getStabilizerState() {
        return HorseStabilizerState.fromId(this.entityData.get(BH_STABILIZER_STATE));
    }

    @Override
    public void bh_setStabilizerState(HorseStabilizerState state) {
        bh_push(BH_STABILIZER_STATE, state.ordinal());
    }

    @Override
    public int bh_getGearFlags() {
        return this.entityData.get(BH_GEAR_FLAGS);
    }

    @Override
    public boolean bh_hasUpgradedSaddle() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        return self.level().isClientSide()
                ? this.entityData.get(BH_UPGRADED_SADDLE)
                : inventory != null && inventory.getItem(0).is(ModItems.UPGRADED_SADDLE.get());
    }

    @Override
    public void bh_equipUpgradedSaddle(ItemStack saddle) {
        if (inventory != null) inventory.setItem(0, saddle);
    }

    @Override
    public SimpleContainer bh_getGearContainer() {
        return bh_gearContainer;
    }

    @Override
    public SimpleContainer bh_getChestContainer() {
        return bh_chestContainer;
    }

    @Override
    public boolean bh_hasChestGear() {
        ItemStack chestGear = bh_gearContainer.getItem(GearSlot.CHEST.ordinal());
        return chestGear.is(Items.CHEST) || chestGear.is(Items.ENDER_CHEST);
    }

    @Override
    public void bh_onChestGearRemoved(ItemStack previousChestGear) {
        if (previousChestGear.is(Items.CHEST)) {
            bh_dropChestContents();
        }
    }

    @Override
    public void bh_onUpgradedSaddleRemoved(ItemStack previousSaddle) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        bh_dropCartChest();
        bh_dropCartPlough();
        BhHorseStorage.dropContainerContents(self, serverLevel, bh_gearContainer);
        bh_dropChestContents();
        bh_syncGearFlags();
    }

    @Override
    public @Nullable HorseCartEntity bh_getCartEntity() {
        return this.bh_cartRig.cart();
    }

    @Unique private @Nullable UUID bh_cartId;
    @Unique private int bh_bondRemainder;
    @Unique private long bh_rescueReadyAt;

    @Override
    public int bh_getBondRemainder() { return bh_bondRemainder; }

    @Override
    public void bh_setBondRemainder(int value) { bh_bondRemainder = value; }

    @Override
    public long bh_getRescueReadyAt() { return bh_rescueReadyAt; }

    @Override
    public void bh_setRescueReadyAt(long value) { bh_rescueReadyAt = value; }

    @Override
    public void bh_onRemoved() {
        for (HorseFeature feature : bh_features()) feature.onRemoved((AbstractHorse) (Object) this, this);
    }

    @Override
    public @Nullable UUID bh_getCartId() {
        return bh_cartId;
    }

    @Override
    public void bh_setCartId(@Nullable UUID id) {
        bh_cartId = id;
    }

    @Override
    public boolean bh_hasLargeCart() {
        return this.entityData.get(BH_CART_LARGE);
    }

    @Override
    public void bh_setLargeCart(boolean large) {
        bh_push(BH_CART_LARGE, large && this.bh_mayUseLargeCart());
    }

    @Override
    public boolean bh_hasCartChest() {
        return this.entityData.get(BH_CART_CHEST);
    }

    @Override
    public void bh_setCartChest(boolean attached) {
        bh_push(BH_CART_CHEST, attached);
    }

    @Override
    public SimpleContainer bh_getCartChestContainer() {
        if (bh_cartChestContainer == null) {
            bh_cartChestContainer = new SimpleContainer(BH_CART_CHEST_SIZE);
        }
        return bh_cartChestContainer;
    }

    @Override
    public void bh_dropCartChest() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel) || !bh_hasCartChest()) {
            return;
        }
        bh_setCartChest(false);
        if (bh_cartChestContainer != null) {
            BhHorseStorage.dropContainerContents(self, serverLevel, bh_cartChestContainer);
        }
        self.spawnAtLocation(new ItemStack(Items.CHEST));
    }

    @Override
    public boolean bh_hasCartPlough() {
        return this.entityData.get(BH_CART_PLOW);
    }

    @Override
    public ItemStack bh_getCartPlough() {
        return bh_cartPlow;
    }

    @Override
    public void bh_setCartPlough(ItemStack hoe) {
        bh_cartPlow = hoe;
        bh_push(BH_CART_PLOW, !hoe.isEmpty());
    }

    @Override
    public void bh_dropCartPlough() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel) || bh_cartPlow.isEmpty()) {
            return;
        }
        ItemStack hoe = bh_cartPlow;
        bh_setCartPlough(ItemStack.EMPTY);
        self.spawnAtLocation(hoe);
    }

    @Override
    public boolean bh_hasAnyEquipment() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.isSaddled()
                || !inventory.getItem(AbstractHorse.INV_SLOT_ARMOR).isEmpty()) {
            return true;
        }
        return !this.inventory.isEmpty() || !bh_gearContainer.isEmpty() || !bh_chestContainer.isEmpty()
                || bh_hasCartChest() || bh_hasCartPlough();
    }

    @Override
    public void bh_disown() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        self.ejectPassengers();
        self.setOwnerUUID(null);
        self.setTamed(false);
        bh_setBond(0);
        bh_setHome(null);
        bh_setWanderCenter(self.blockPosition());
        bh_setCommand(HorseCommand.WANDER);
        bh_setOwner(null);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void bh_onWrite(CompoundTag output, CallbackInfo ci) {
        if (bh_owner != null) {
            output.putUUID("BH_Owner", bh_owner);
        }
        output.putInt("BH_AbilityPaused", bh_abilityPaused ? 1 : 0);
        output.putInt("BH_Command", bh_command.ordinal());
        output.putInt("BH_Bond", bh_bond);
        output.putInt("BH_BondRemainder", bh_bondRemainder);
        output.putLong("BH_RescueReadyAt", bh_rescueReadyAt);
        output.putInt("BH_Generation", bh_generation);
        output.putInt("BH_NameTagBondGiven", bh_nameTagBondReceived ? 1 : 0);
        if (bh_home != null) {
            bh_writeBlockPos(output, "BH_Home", bh_home);
        }
        if (bh_homeDim != null) {
            output.putString("BH_HomeDim", bh_homeDim.location().toString());
        }
        if (bh_wanderCenter != null) {
            bh_writeBlockPos(output, "BH_WanderCenter", bh_wanderCenter);
        }
        AbstractHorse self = (AbstractHorse) (Object) this;
        BhHorseStorage.writeContainer(output, "BH_Gear", bh_gearContainer);
        BhHorseStorage.writeContainer(output, "BH_Chest", bh_chestContainer);
        output.putBoolean("BH_CartChestOn", this.entityData.get(BH_CART_CHEST));
        output.putBoolean("BH_CartLarge", this.entityData.get(BH_CART_LARGE));
        if (bh_cartId != null) output.putUUID("BH_CartId", bh_cartId);
        if (bh_cartChestContainer != null) {
            BhHorseStorage.writeContainer(output, "BH_CartChest", bh_cartChestContainer);
        }
        if (!bh_cartPlow.isEmpty()) {
            output.put("BH_CartPlow", bh_cartPlow.save(new CompoundTag()));
        }
        output.putInt("BH_Gender", this.entityData.get(BH_GENDER_ID));
        output.putString("BH_BreedId", this.bh_getBreed().id());
        output.putBoolean("BH_BreedMixed", this.entityData.get(BH_BREED_MIXED));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void bh_onRead(CompoundTag input, CallbackInfo ci) {
        bh_owner = input.hasUUID("BH_Owner") ? input.getUUID("BH_Owner") : null;
        bh_cartId = input.hasUUID("BH_CartId") ? input.getUUID("BH_CartId") : null;
        bh_abilityPaused = input.getInt("BH_AbilityPaused") != 0;
        if (bh_owner == null) {
            bh_owner = ((AbstractHorse) (Object) this).getOwnerUUID();
        }
        bh_push(BH_OWNER_ID, bh_owner == null ? "" : bh_owner.toString());
        bh_command = HorseCommand.fromId(input.contains("BH_Command") ? input.getInt("BH_Command") : HorseCommand.FOLLOW.ordinal());
        bh_push(BH_COMMAND_ID, bh_command.ordinal());
        bh_bond = input.getInt("BH_Bond");
        bh_bondRemainder = Math.floorMod(input.getInt("BH_BondRemainder"), 2);
        bh_rescueReadyAt = input.getLong("BH_RescueReadyAt");
        bh_generation = input.getInt("BH_Generation");
        bh_push(BH_BOND, bh_bond);
        bh_nameTagBondReceived = input.contains("BH_NameTagBondGiven")
                ? input.getInt("BH_NameTagBondGiven") != 0
                : bh_bond > 0;
        bh_home = BhHorseStorage.readLegacyBlockPos(input, "BH_Home");
        bh_homeDim = input.contains("BH_HomeDim", Tag.TAG_STRING)
                ? ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(input.getString("BH_HomeDim")))
                : null;
        bh_wanderCenter = BhHorseStorage.readLegacyBlockPos(input, "BH_WanderCenter");
        if (bh_home != null && bh_homeDim == null) {
            bh_homeDim = ((AbstractHorse) (Object) this).level().dimension();
        }
        bh_applyBondAttributes();
        AbstractHorse self = (AbstractHorse) (Object) this;
        BhHorseStorage.readContainer(input, "BH_Gear", bh_gearContainer);
        BhHorseStorage.readContainer(input, "BH_Chest", bh_chestContainer);
        bh_push(BH_CART_CHEST, input.getBoolean("BH_CartChestOn"));
        if (bh_hasCartChest()) {
            BhHorseStorage.readContainer(input, "BH_CartChest", bh_getCartChestContainer());
        }
        bh_setCartPlough(input.contains("BH_CartPlow", Tag.TAG_COMPOUND)
                ? ItemStack.of(input.getCompound("BH_CartPlow"))
                : ItemStack.EMPTY);
        BhHorseStorage.restoreUpgradedSaddle(inventory, input);
        bh_syncGearFlags();
        bh_afterLoad();

        if (input.contains("BH_Gender", Tag.TAG_INT)) {
            bh_push(BH_GENDER_ID, input.getInt("BH_Gender"));
        } else {
            bh_push(BH_GENDER_ID, this.random.nextBoolean() ? 0 : 1);
        }
        HorseBreed savedBreed = bh_readSavedBreed(input);
        if (savedBreed != null) {
            bh_push(BH_BREED_ID, savedBreed.ordinal());
            bh_push(BH_BREED_MIXED, input.getBoolean("BH_BreedMixed"));
        } else {
            bh_assignBreedPreservingCoat();
        }

        bh_setLargeCart(input.contains("BH_CartLarge") ? input.getBoolean("BH_CartLarge") : this.bh_mayUseLargeCart());
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void bh_assignTraitsOnSpawn(ServerLevelAccessor level,
                                        DifficultyInstance difficulty,
                                        MobSpawnType reason,
                                        @Nullable SpawnGroupData groupData,
                                        @Nullable CompoundTag dataTag,
                                        CallbackInfoReturnable<SpawnGroupData> cir) {
        bh_push(BH_GENDER_ID, this.random.nextBoolean() ? 0 : 1);

        if ((Object) this instanceof BhBreedEntity breedEntity) {
            bh_push(BH_BREED_ID, breedEntity.bhFixedBreed().ordinal());
            bh_push(BH_BREED_MIXED, false);
            return;
        }

        if (this.bh_getBreed() != HorseBreed.UNKNOWN_SPECIES) {
            return;
        }

        AbstractHorse self = (AbstractHorse) (Object) this;
        HorseBreed species = HorseBreed.speciesFor(self);
        if (species != null) {
            bh_push(BH_BREED_ID, species.ordinal());
            bh_push(BH_BREED_MIXED, false);
        }
    }

    @Unique
    private static @Nullable HorseBreed bh_readSavedBreed(CompoundTag input) {
        if (input.contains("BH_BreedId", Tag.TAG_STRING)) {
            return HorseBreed.byId(input.getString("BH_BreedId"));
        }
        return input.contains("BH_Breed", Tag.TAG_INT) ? HorseBreed.fromId(input.getInt("BH_Breed")) : null;
    }

    @Unique
    private static void bh_writeBlockPos(CompoundTag tag, String key, BlockPos pos) {
        tag.putInt(key + "X", pos.getX());
        tag.putInt(key + "Y", pos.getY());
        tag.putInt(key + "Z", pos.getZ());
    }

    @Unique
    private void bh_assignBreedPreservingCoat() {
        HorseBreed picked = BhHorseTraits.pickBreed((AbstractHorse) (Object) this, this.random);
        bh_push(BH_BREED_ID, picked.ordinal());
        bh_push(BH_BREED_MIXED, false);
    }

    @Inject(method = "createInventory", at = @At("TAIL"))
    private void bh_onCreateInventory(CallbackInfo ci) {
        bh_afterInventoryChange();
        this.bh_syncGearFlags();
    }

    @Inject(method = "containerChanged", at = @At("TAIL"))
    private void bh_onContainerChanged(Container invBasic, CallbackInfo ci) {
        this.bh_syncGearFlags();
    }

    @Override
    public @Nullable UUID bh_getCombatTarget() {
        return this.bh_combatTarget;
    }

    @Override
    public void bh_setCombatTarget(@Nullable UUID target) {
        this.bh_combatTarget = target;
        this.bh_syncCombatState();
    }

    @Override
    public int bh_getSpookTicks() {
        return this.bh_spookTicks;
    }

    @Override
    public void bh_setSpookTicks(int ticks) {
        this.bh_spookTicks = Math.max(0, ticks);
        this.bh_syncCombatState();
    }

    @Override
    public @Nullable BreedAbility bh_currentAbility() {
        return this.bh_abilities.current();
    }

    @Override
    public boolean bh_isAbilityPaused() {
        return this.bh_abilityPaused;
    }

    @Override
    public void bh_setAbilityPaused(boolean paused) {
        this.bh_abilityPaused = paused;
    }

    @Override
    public int bh_getCombatState() {
        return this.entityData.get(BH_COMBAT);
    }

    @Override
    public int bh_getKickTicks() {
        return this.entityData.get(BH_KICK);
    }

    @Override
    public void bh_setKickTicks(int ticks) {
        bh_push(BH_KICK, Math.max(0, ticks));
    }

    @Unique
    private void bh_syncCombatState() {
        int next = this.bh_spookTicks > 0 ? 2 : this.bh_combatTarget != null ? 1 : 0;
        if (this.entityData.get(BH_COMBAT) != next) {
            bh_push(BH_COMBAT, next);
        }
    }

    @Override
    public int bh_getGear() {
        return ((AbstractHorse) (Object) this).level().isClientSide()
                ? this.entityData.get(BH_GEAR)
                : bh_gear;
    }

    @Override
    public void bh_setGear(int gear) {
        this.bh_gear = Mth.clamp(gear, 0, BhGears.TOP_GEAR);
        bh_push(BH_GEAR, this.bh_gear);
    }

    @Override
    public int bh_getStompTicks() {
        return this.entityData.get(BH_STOMP);
    }

    @Override
    public void bh_setStompTicks(int ticks) {
        bh_push(BH_STOMP, Math.max(0, ticks));
    }

    @Override
    public int bh_getSurge() {
        return this.entityData.get(BH_SURGE);
    }

    @Override
    public void bh_setSurge(int packed) {
        if (this.entityData.get(BH_SURGE) != packed) {
            bh_push(BH_SURGE, packed);
        }
    }

    @Override
    public int bh_getPerkSurge() {
        return this.entityData.get(BH_PERK);
    }

    @Override
    public void bh_setPerkSurge(int packed) {
        if (this.entityData.get(BH_PERK) != packed) {
            bh_push(BH_PERK, packed);
        }
    }

    @Override
    public int bh_getPulse() {
        return this.entityData.get(BH_PULSE);
    }

    @Override
    public void bh_setPulse(int packed) {
        if (this.entityData.get(BH_PULSE) != packed) {
            bh_push(BH_PULSE, packed);
        }
    }

    @Override
    public int bh_getCharge() {
        return this.entityData.get(BH_CHARGE);
    }

    @Override
    public void bh_setCharge(int fill) {
        if (this.entityData.get(BH_CHARGE) != fill) {
            bh_push(BH_CHARGE, fill);
        }
    }

    @Override
    public boolean bh_isFreeLook() {
        return this.entityData.get(BH_FREE_LOOK);
    }

    @Override
    public void bh_setFreeLook(boolean freeLook) {
        if (this.entityData.get(BH_FREE_LOOK) != freeLook) {
            bh_push(BH_FREE_LOOK, freeLook);
        }
    }

    @Override
    public int bh_getGaitGear() {
        return this.entityData.get(BH_GAIT_GEAR);
    }

    @Override
    public void bh_setGaitGear(int gear) {
        bh_push(BH_GAIT_GEAR, Mth.clamp(gear, 0, BhGears.TOP_GEAR));
    }

    @Inject(method = "getRiddenSpeed", at = @At("RETURN"), cancellable = true)
    private void bh_applyGearSpeed(
            Player rider,
            CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(BhGears.riddenSpeed(bh_gear, cir.getReturnValueF()));
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void bh_neighWhenHurt(
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!cir.getReturnValueZ()
                || !(self instanceof Horse)
                || self.getRandom().nextFloat() >= BH_HURT_NEIGH_CHANCE) {
            return;
        }
        self.level().playSound(null, self.getX(), self.getY(), self.getZ(),
                ModSounds.HORSE_NEIGH.get(), self.getSoundSource(), 1.0F, 1.0F);
    }


    @ModifyConstant(method = "aiStep", constant = @Constant(intValue = 300))
    private int bh_grazeLessOften(int vanillaInterval) {
        return BH_GRAZE_ROLL_INTERVAL;
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;"
                            + "canEatGrass()Z"))
    private boolean bh_gateGrazing(AbstractHorse horse) {
        if (!horse.canEatGrass()) {
            return false;
        }
        if (this.bh_mayGraze(horse)) {
            return true;
        }
        if (horse.isEating()) {
            horse.setEating(false);
            this.eatingCounter = 0;
        }
        return false;
    }

    @Unique
    private boolean bh_mayGraze(AbstractHorse horse) {
        if (horse.isVehicle() || horse.tickCount < this.bh_grazeBlockedUntilTick) {
            return false;
        }
        if (!this.bh_isOwned()) {
            return true;
        }
        HorseCommand command = this.bh_getCommand();
        return command == HorseCommand.WANDER || command == HorseCommand.STAY;
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void bh_stopGrazingWhenHurt(
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            AbstractHorse self = (AbstractHorse) (Object) this;
            this.bh_grazeBlockedUntilTick = self.tickCount + BH_GRAZE_HURT_COOLDOWN_TICKS;
            this.bh_combat.onHurt(self, this, source);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_tick(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (BhVanillaHorseSwap.trySwap(self)) {
            return;
        }
        for (HorseFeature feature : this.bh_features()) {
            feature.tick(self, this);
        }
    }

    @Unique
    private void bh_clearGearWhenUnridden(AbstractHorse self) {
        if (self.getControllingPassenger() == null) {
            if (bh_gear != 0) {
                bh_setGear(0);
                bh_setGaitGear(0);
            }
            bh_setFreeLook(false);
            bh_setSurge(0);
            bh_setPerkSurge(0);
            bh_setPulse(0);
            bh_setCharge(BhSurge.HIDDEN);
        }
    }

    @Inject(method = "fedFood", at = @At("HEAD"))
    private void bh_markGoldenAppleFeed(Player player, ItemStack stack, CallbackInfoReturnable<InteractionResult> cir) {
        this.bh_fedGoldenAppleThisTick = stack.is(Items.GOLDEN_APPLE);
    }

    @Inject(method = "fedFood", at = @At("RETURN"))
    private void bh_rewardGoldenAppleBond(Player player, ItemStack stack, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            AbstractHorse self = (AbstractHorse) (Object) this;
            if (!this.bh_fedGoldenAppleThisTick || self.level().isClientSide() || !cir.getReturnValue().consumesAction()) {
                return;
            }

            BhHorseTraits.grantBond(this, 2);
        } finally {
            this.bh_fedGoldenAppleThisTick = false;
        }
    }

    @Inject(method = "doPlayerRide", at = @At("HEAD"), cancellable = true)
    private void bh_gateOwnerOnlyMount(Player player, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide() || !BhConfig.horseExclusivityEnabled()) return;
        if (this.bh_maySaddleUp(player.getUUID())) return;
        if (BhHorseInteraction.riderMayLeadPillion(self, this)) return;
        self.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys-better-horses.not_owner"));
        }
        if (player.getVehicle() == self) {
            player.stopRiding();
        }
        ci.cancel();
    }

    @Inject(
            method = "doPlayerRide",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;setStanding(Z)V",
                    shift = At.Shift.AFTER),
            cancellable = true)
    private void bh_rotateHorseInsteadOfPlayer(Player player, CallbackInfo ci) {
        if (BhHorseInteraction.rotateHorseInsteadOfPlayer((AbstractHorse) (Object) this, this, player)) {
            ci.cancel();
        }
    }

    @Inject(method = "tameWithName", at = @At("RETURN"))
    private void bh_claimHorseOnTame(Player player, CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!cir.getReturnValueZ() || self.level().isClientSide() || player.getUUID().equals(this.bh_getOwner())) {
            return;
        }

        this.bh_setOwner(player.getUUID());
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys-better-horses.claimed"));
            BhCriteria.fire(serverPlayer, BhCriteria.OWN_HORSE);
            BhCriteria.fireBreed(serverPlayer, this.bh_getBreed());
            BhCriteria.fireOwnedHorseCount(serverPlayer);
        }
    }

    @Inject(method = "openCustomInventoryScreen", at = @At("HEAD"), cancellable = true)
    private void bh_blockNonOwnerInventoryAccess(Player player, CallbackInfo ci) {
        if (BhHorseInteraction.blockNonOwnerInventoryAccess((AbstractHorse) (Object) this, this, player)) {
            ci.cancel();
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void bh_equipGearFromHand(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = BhHorseInteraction.equipGearFromHand(
                (AbstractHorse) (Object) this, this, player, hand);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void bh_allowSecondPlayerRider(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!self.isVehicle()
                || self.isBaby()
                || self.hasPassenger(player)
                || self.getPassengers().size() >= BhHorseSteering.bh_seatCount(this)) {
            return;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (self.isTamed() && !heldItem.isEmpty() && self.isFood(heldItem)) {
            InteractionResult fed = self.fedFood(player, heldItem);
            if (fed.consumesAction()) {
                cir.setReturnValue(fed);
                return;
            }
        }

        InteractionResult animalResult = super.mobInteract(player, hand);
        if (animalResult.consumesAction()) {
            cir.setReturnValue(animalResult);
            return;
        }

        if (self.isTamed() && player.isSecondaryUseActive()) {
            self.openCustomInventoryScreen(player);
            cir.setReturnValue((self.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME));
            return;
        }

        if (!heldItem.isEmpty()) {
            InteractionResult heldItemResult = heldItem.interactLivingEntity(player, self, hand);
            if (heldItemResult.consumesAction()) {
                cir.setReturnValue(heldItemResult);
                return;
            }
        }

        if (!BhConfig.multiRidingEnabled()) {
            return;
        }

        this.doPlayerRide(player);
        cir.setReturnValue((self.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME));
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void bh_adjustFallDamage(float distance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        BhHorseInteraction.StabilizerLanding landing =
                BhHorseInteraction.stabilizerLanding(self, this, distance);
        if (landing == BhHorseInteraction.StabilizerLanding.ABSORBED) {
            cir.setReturnValue(false);
            return;
        }

        double waiver = this.bh_getBreed().archetype().fallDamageWaiver();
        if (waiver > 0.0D && distance < waiver) {
            if (distance > 1.0D) {
                self.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
            }
            if (distance > self.getMaxFallDistance()) {
                BhSurge.pulsePerk(this, ArchetypePerks.FALL_BADGE);
            }
            cir.setReturnValue(false);
            return;
        }

        if (landing == BhHorseInteraction.StabilizerLanding.PASS_THROUGH) {
            return;
        }

        if (!this.bh_hasHoovesGear()) {
            return;
        }

        if (distance > 1.0D) {
            self.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
        }

        int reducedDamage = this.calculateFallDamage(distance, damageMultiplier * BH_HOOVES_FALL_DAMAGE_MULTIPLIER);
        if (reducedDamage <= 0) {
            cir.setReturnValue(false);
            return;
        }

        self.hurt(source, reducedDamage);
        if (self.isVehicle()) {
            for (Entity passenger : self.getIndirectPassengers()) {
                passenger.hurt(source, reducedDamage);
            }
        }

        this.playBlockFallSound();
        cir.setReturnValue(true);
    }

    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void bh_dropGearAndChest(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) return;
        bh_dropCartChest();
        bh_dropCartPlough();
        BhHorseStorage.dropContainerContents(self, level, bh_gearContainer);
        BhHorseStorage.dropContainerContents(self, level, bh_chestContainer);
        bh_syncGearFlags();
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void bh_onRegisterGoals(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        goalSelector.addGoal(1, new SpookGoal(self));
        goalSelector.addGoal(2, new DefendOwnerGoal(self));
        goalSelector.addGoal(3, new HorseStayGoal(self));
        goalSelector.addGoal(3, new HorseFollowOwnerGoal(self));
        goalSelector.addGoal(3, new HorseReturnHomeGoal(self));
        goalSelector.addGoal(3, new HorseWanderBoundsGoal(self));
    }

    @Shadow private float standAnimO;

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At("TAIL"))
    private void bh_offsetSecondPassenger(Entity passenger, Entity.MoveFunction move, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!self.hasPassenger(passenger)) return;
        if (this.bh_hasCartGear()) {
            BhRiderSeat.publish(self.getId(), Vec3.ZERO);
            Vec3 seat = HorseCartEntity.benchSeatOffset(
                    self, BhHorseSteering.benchSeatIndex(self, passenger), self.yBodyRot);
            move.accept(passenger, self.getX() + seat.x, self.getY() + seat.y, self.getZ() + seat.z);
            return;
        }
        float yaw = self.yBodyRot * Mth.DEG_TO_RAD;
        double rear = 0.7D * this.standAnimO * BhRiderSeat.REAR_CAMERA_FOLLOW;
        Vec3 shift = new Vec3(rear * Mth.sin(yaw),
                0.15D * this.standAnimO * BhRiderSeat.REAR_CAMERA_FOLLOW + BhRiderSeat.seatLift(self),
                -rear * Mth.cos(yaw));
        BhRiderSeat.publish(self.getId(), shift);
        double height = self.getPassengersRidingOffset();
        if (!(self instanceof BhBreedHorse)) height += passenger.getMyRidingOffset();
        Vec3 seat = self.position().add(shift).add(0.0D, height, 0.0D);
        Vec3 offset = BhHorseSteering.multiRiderOffset(self, passenger);
        if (offset != null) seat = seat.add(offset);
        move.accept(passenger, seat.x, seat.y, seat.z);
    }

    @Inject(method = "getRiddenRotation", at = @At("HEAD"), cancellable = true)
    private void bh_allowMountedFreeCamera(LivingEntity rider, CallbackInfoReturnable<Vec2> cir) {
        if (!(rider instanceof Player player)) {
            return;
        }

        AbstractHorse self = (AbstractHorse) (Object) this;

        Vec2 rotation = BhHorseSteering.riddenRotation(self, this, player);
        if (rotation != null) {
            cir.setReturnValue(rotation);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return BhHorseSteering.canAddPassenger((AbstractHorse) (Object) this, this, passenger);
    }


    @Inject(method = "getControllingPassenger", at = @At("RETURN"), cancellable = true)
    private void bh_keepPlayerAtTheReins(CallbackInfoReturnable<LivingEntity> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (cir.getReturnValue() != null
                || !this.bh_hasCartGear()
                || !self.isSaddled()) {
            return;
        }

        for (Entity passenger : self.getPassengers()) {
            if (passenger instanceof Player player) {
                cir.setReturnValue(player);
                return;
            }
        }
    }

    @Unique
    private void bh_applyBondAttributes() {
        BhHorseTraits.applyBondAttributes((AbstractHorse) (Object) this, bh_bond);
    }

    @Unique
    private boolean bh_hasHoovesGear() {
        return BhConfig.hoovesEnabled() && this.bh_hasGear(GearSlot.HOOVES);
    }

    @Unique
    private void bh_dropChestContents() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        BhHorseStorage.dropContainerContents(self, serverLevel, bh_chestContainer);
    }

    @Unique
    private void bh_syncGearFlags() {
        if (((AbstractHorse) (Object) this).level().isClientSide()) {
            return;
        }
        int flags = 0;
        for (GearSlot slot : GearSlot.values()) {
            if (slot.accepts(this.bh_gearContainer.getItem(slot.ordinal()))
                    && !this.bh_gearContainer.getItem(slot.ordinal()).isEmpty()) {
                flags |= 1 << slot.ordinal();
            }
        }

        bh_push(BH_GEAR_FLAGS, flags);
        boolean hadCart = this.entityData.get(BH_CART);
        boolean hasCart = this.bh_gearContainer.getItem(GearSlot.STABILIZER.ordinal()).is(ModItems.HORSE_CART.get());
        bh_push(BH_CART, hasCart);
        if (hasCart && !hadCart) {
            bh_setLargeCart(this.bh_mayUseLargeCart());
        }
        bh_push(BH_ENDER_CHEST,
                this.bh_gearContainer.getItem(GearSlot.CHEST.ordinal()).is(Items.ENDER_CHEST));
        bh_push(BH_UPGRADED_SADDLE,
                this.inventory != null && this.inventory.getItem(0).is(ModItems.UPGRADED_SADDLE.get()));
    }

    @Override
    public boolean bh_hasEnderChestGear() {
        return this.entityData.get(BH_ENDER_CHEST);
    }

    @Override
    public boolean bh_hasCartGear() {
        return this.entityData.get(BH_CART);
    }

    @Override
    public void bh_ridePlayer(Player player) {
        this.doPlayerRide(player);
    }

    @Override
    public void bh_clearStanding() {
        ((AbstractHorse) (Object) this).setStanding(false);
        this.standCounter = 0;
    }

    @Redirect(
            method = "handleStartJump(I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;"
                            + "standIfPossible()V"))
    private void bh_noRearOnStartJump(AbstractHorse horse) {
    }

    @Inject(method = "standIfPossible", at = @At("HEAD"), cancellable = true)
    private void bh_noRearInMidair(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!self.onGround()
                || (self.hurtTime > 0 && this.bh_getBreed().archetype().suppressRear())) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "onPlayerJump(I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;"
                            + "standIfPossible()V"))
    private void bh_noRearOnPlayerJump(AbstractHorse horse) {
    }

    @Unique
    private void bh_afterLoad() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        for (HorseFeature feature : this.bh_features()) {
            feature.onLoad(self, this);
        }
    }

    @Unique
    private void bh_afterInventoryChange() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        for (HorseFeature feature : this.bh_features()) {
            feature.onInventoryChanged(self, this);
        }
    }
}
