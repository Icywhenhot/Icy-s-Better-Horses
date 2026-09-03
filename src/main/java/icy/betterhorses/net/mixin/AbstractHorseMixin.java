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
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.feature.BreedAbilities;
import icy.betterhorses.net.feature.breed.BreedAbility;
import icy.betterhorses.net.feature.CartRig;
import icy.betterhorses.net.feature.HorseCombat;
import icy.betterhorses.net.feature.FrostHooves;
import icy.betterhorses.net.feature.HitchTether;
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
import icy.betterhorses.net.item.HitchpostBlock;
import icy.betterhorses.net.goal.HorseFollowOwnerGoal;
import icy.betterhorses.net.goal.HorseReturnHomeGoal;
import icy.betterhorses.net.goal.DefendOwnerGoal;
import icy.betterhorses.net.goal.HorseStayGoal;
import icy.betterhorses.net.goal.PackmateFollowGoal;
import icy.betterhorses.net.goal.SpookGoal;
import icy.betterhorses.net.goal.HorseWanderBoundsGoal;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

import java.util.Optional;
import java.util.UUID;
import icy.betterhorses.net.BhRiderSeat;
import icy.betterhorses.net.entity.BhBreedEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements IHorseData, IHorseAbilityHost {

    @Shadow
    protected SimpleContainer inventory;

    @Shadow
    private int eatingCounter;

    @Shadow
    private EntityReference<LivingEntity> owner;

    @Shadow
    protected abstract void doPlayerRide(Player player);

    @Unique
    private static final EntityDataAccessor<Integer> BH_BOND_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> BH_STABILIZER_STATE_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> BH_GEAR_FLAGS_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Boolean> BH_CART_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Boolean> BH_CART_CHEST_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Boolean> BH_ENDER_CHEST_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Optional<BlockPos>> BH_HITCHPOST_POS_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    @Unique
    private static final EntityDataAccessor<Integer> BH_GENDER_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> BH_BREED_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Boolean> BH_BREED_MIXED_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<String> BH_OWNER_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Integer> BH_COMMAND_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> BH_GEAR_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> BH_GAIT_GEAR_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Integer> BH_COMBAT_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Integer> BH_KICK_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Boolean> BH_FREE_LOOK_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> BH_CART_LARGE_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Integer> BH_STOMP_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Integer> BH_SURGE_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Integer> BH_PERK_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);

    @Unique
    private static final EntityDataAccessor<Integer> BH_CHARGE_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);

    @Unique private volatile @Nullable UUID bh_owner = null;
    @Unique private HorseCommand bh_command = HorseCommand.FOLLOW;
    @Unique private @Nullable BlockPos bh_home = null;
    @Unique private @Nullable BlockPos bh_wanderCenter = null;
    @Unique private @Nullable BlockPos bh_hitchpostPos = null;
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
    @Unique private final SimpleContainer bh_cartChestContainer = new SimpleContainer(BH_CART_CHEST_SIZE);
    @Unique private boolean bh_fedGoldenAppleThisTick = false;
    @Unique private static final float BH_HURT_NEIGH_CHANCE = 0.3F;
    @Unique private static final int BH_GRAZE_ROLL_INTERVAL = 1200;
    @Unique private static final int BH_GRAZE_HURT_COOLDOWN_TICKS = 200;
    @Unique private int bh_grazeBlockedUntilTick = 0;
    @Unique private int bh_gear = 0;
    @Unique private @Nullable UUID bh_combatTarget = null;
    @Unique private @Nullable UUID bh_pairedTo = null;
    @Unique private boolean bh_abilityToggled = false;
    @Unique private int bh_spookTicks = 0;

    @Unique private final SaddleWatch bh_saddle = new SaddleWatch();
    @Unique private final CartRig bh_cartRig = new CartRig();
    @Unique private final HitchTether bh_hitch = new HitchTether();
    @Unique private final HorseCombat bh_combat = new HorseCombat();
    @Unique private final BreedAbilities bh_abilities = new BreedAbilities();

    @Unique
    private final HorseFeature[] bh_features = {
            bh_saddle,
            (horse, data) -> bh_clearGearWhenUnridden(horse),
            new SpeedRecord(),
            new RiderGate(),
            new Stabilizer(),
            bh_cartRig,
            new SwimBoost(),
            new FrostHooves(),
            bh_hitch,
            bh_combat,
            bh_abilities,
    };

    @Unique private static final float BH_HOOVES_FALL_DAMAGE_MULTIPLIER = 0.5F;

    protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    public @Nullable UUID bh_getOwner() {
        if (level().isClientSide()) {
            return bh_parseOwner(this.entityData.get(BH_OWNER_SYNCED));
        }
        return bh_owner;
    }

    @Override
    public void bh_setOwner(@Nullable UUID owner) {
        this.bh_owner = owner;
        this.entityData.set(BH_OWNER_SYNCED, owner == null ? "" : owner.toString());
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
                ? HorseCommand.fromId(this.entityData.get(BH_COMMAND_SYNCED))
                : bh_command;
    }

    @Override
    public void bh_setCommand(HorseCommand command) {
        this.bh_command = command;
        this.entityData.set(BH_COMMAND_SYNCED, command.ordinal());
    }

    @Override
    public @Nullable BlockPos bh_getHome() {
        return bh_home;
    }

    @Override
    public void bh_setHome(@Nullable BlockPos pos) {
        this.bh_home = pos;
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
    public @Nullable BlockPos bh_getHitchpostPos() {
        return bh_hitchpostPos;
    }

    @Override
    public void bh_setHitchpostPos(@Nullable BlockPos pos) {
        this.bh_hitchpostPos = pos == null ? null : pos.immutable();
        this.bh_hitch.anchorAt(this.bh_hitchpostPos == null
                ? null
                : ((AbstractHorse) (Object) this).position());
        this.entityData.set(BH_HITCHPOST_POS_SYNCED, Optional.ofNullable(this.bh_hitchpostPos));
    }

    @Override
    public int bh_getBond() {
        return this.entityData.get(BH_BOND_SYNCED);
    }

    @Override
    public void bh_setBond(int level) {
        int previous = this.bh_bond;
        this.bh_bond = Math.max(0, Math.min(100, level));
        this.entityData.set(BH_BOND_SYNCED, this.bh_bond);
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
        return HorseGender.fromId(this.entityData.get(BH_GENDER_SYNCED));
    }

    @Override
    public void bh_setGender(HorseGender gender) {
        this.entityData.set(BH_GENDER_SYNCED, gender.ordinal());
    }

    @Override
    public HorseBreed bh_getBreed() {
        if ((Object) this instanceof BhBreedEntity breedEntity) {
            return breedEntity.bhFixedBreed();
        }
        return HorseBreed.fromId(this.entityData.get(BH_BREED_SYNCED));
    }

    @Override
    public void bh_setBreed(HorseBreed breed) {
        this.entityData.set(BH_BREED_SYNCED, breed.ordinal());
    }

    @Override
    public boolean bh_isMixedBreed() {
        return this.entityData.get(BH_BREED_MIXED_SYNCED);
    }

    @Override
    public void bh_setMixedBreed(boolean mixed) {
        this.entityData.set(BH_BREED_MIXED_SYNCED, mixed);
    }

    @Override
    public HorseStabilizerState bh_getStabilizerState() {
        return HorseStabilizerState.fromId(this.entityData.get(BH_STABILIZER_STATE_SYNCED));
    }

    @Override
    public void bh_setStabilizerState(HorseStabilizerState state) {
        this.entityData.set(BH_STABILIZER_STATE_SYNCED, state.ordinal());
    }

    @Override
    public int bh_getGearFlags() {
        return this.entityData.get(BH_GEAR_FLAGS_SYNCED);
    }

    @Override
    public boolean bh_hasUpgradedSaddle() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        return self.getItemBySlot(EquipmentSlot.SADDLE).is(ModItems.UPGRADED_SADDLE);
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
        BhHorseStorage.dropContainerContents(self, serverLevel, bh_gearContainer);
        bh_dropChestContents();
        bh_syncGearFlags();
    }

    @Override
    public @Nullable HorseCartEntity bh_getCartEntity() {
        return this.bh_cartRig.cart();
    }

    @Override
    public boolean bh_hasLargeCart() {
        return this.entityData.get(BH_CART_LARGE_SYNCED);
    }

    @Override
    public void bh_setLargeCart(boolean large) {
        this.entityData.set(BH_CART_LARGE_SYNCED, large && this.bh_mayUseLargeCart());
    }

    @Override
    public boolean bh_hasCartChest() {
        return this.entityData.get(BH_CART_CHEST_SYNCED);
    }

    @Override
    public void bh_setCartChest(boolean attached) {
        this.entityData.set(BH_CART_CHEST_SYNCED, attached);
    }

    @Override
    public SimpleContainer bh_getCartChestContainer() {
        return bh_cartChestContainer;
    }

    @Override
    public void bh_dropCartChest() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel) || !bh_hasCartChest()) {
            return;
        }
        bh_setCartChest(false);
        BhHorseStorage.dropContainerContents(self, serverLevel, bh_cartChestContainer);
        self.spawnAtLocation(serverLevel, new ItemStack(Items.CHEST));
    }

    @Override
    public boolean bh_hasAnyEquipment() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!self.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()
                || !self.getItemBySlot(EquipmentSlot.BODY).isEmpty()) {
            return true;
        }
        return !this.inventory.isEmpty() || !bh_gearContainer.isEmpty() || !bh_chestContainer.isEmpty()
                || bh_hasCartChest();
    }

    @Override
    public void bh_disown() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        self.ejectPassengers();
        this.owner = null;
        self.setTamed(false);
        bh_setBond(0);
        bh_setHome(null);
        bh_setHitchpostPos(null);
        bh_setWanderCenter(self.blockPosition());
        bh_setCommand(HorseCommand.WANDER);
        bh_setOwner(null);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void bh_defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BH_BOND_SYNCED, 0);
        builder.define(BH_STABILIZER_STATE_SYNCED, HorseStabilizerState.CLOSED.ordinal());
        builder.define(BH_GEAR_FLAGS_SYNCED, 0);
        builder.define(BH_CART_SYNCED, false);
        builder.define(BH_CART_CHEST_SYNCED, false);
        builder.define(BH_CART_LARGE_SYNCED, false);
        builder.define(BH_ENDER_CHEST_SYNCED, false);
        builder.define(BH_HITCHPOST_POS_SYNCED, Optional.empty());
        builder.define(BH_GENDER_SYNCED, 0);
        builder.define(BH_BREED_SYNCED, HorseBreed.UNKNOWN_SPECIES.ordinal());
        builder.define(BH_BREED_MIXED_SYNCED, false);
        builder.define(BH_OWNER_SYNCED, "");
        builder.define(BH_COMMAND_SYNCED, HorseCommand.FOLLOW.ordinal());
        builder.define(BH_GEAR_SYNCED, 0);
        builder.define(BH_GAIT_GEAR_SYNCED, 0);
        builder.define(BH_FREE_LOOK_SYNCED, false);
        builder.define(BH_COMBAT_SYNCED, 0);
        builder.define(BH_KICK_SYNCED, 0);
        builder.define(BH_STOMP_SYNCED, 0);
        builder.define(BH_SURGE_SYNCED, 0);
        builder.define(BH_PERK_SYNCED, 0);
        builder.define(BH_CHARGE_SYNCED, BhSurge.HIDDEN);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void bh_onWrite(ValueOutput output, CallbackInfo ci) {
        if (bh_owner != null) {
            output.store("BH_Owner", UUIDUtil.CODEC, bh_owner);
        }
        if (bh_pairedTo != null) {
            output.store("BH_PairedTo", UUIDUtil.CODEC, bh_pairedTo);
        }
        output.putInt("BH_AbilityToggled", bh_abilityToggled ? 1 : 0);
        output.putInt("BH_Command", bh_command.ordinal());
        output.putInt("BH_Bond", bh_bond);
        output.putInt("BH_Generation", bh_generation);
        output.putInt("BH_NameTagBondGiven", bh_nameTagBondReceived ? 1 : 0);
        if (bh_home != null) {
            output.store("BH_Home", BlockPos.CODEC, bh_home);
        }
        if (bh_wanderCenter != null) {
            output.store("BH_WanderCenter", BlockPos.CODEC, bh_wanderCenter);
        }
        if (bh_hitchpostPos != null) {
            output.store("BH_Hitchpost", BlockPos.CODEC, bh_hitchpostPos);
        }
        BhHorseStorage.writeContainer(output.list("BH_Gear", BhHorseStorage.SlotEntry.CODEC), bh_gearContainer);
        BhHorseStorage.writeContainer(output.list("BH_Chest", BhHorseStorage.SlotEntry.CODEC), bh_chestContainer);
        output.putBoolean("BH_CartChestOn", this.entityData.get(BH_CART_CHEST_SYNCED));
        output.putBoolean("BH_CartLarge", this.entityData.get(BH_CART_LARGE_SYNCED));
        BhHorseStorage.writeContainer(output.list("BH_CartChest", BhHorseStorage.SlotEntry.CODEC), bh_cartChestContainer);
        output.putInt("BH_Gender", this.entityData.get(BH_GENDER_SYNCED));
        output.putInt("BH_Breed", this.entityData.get(BH_BREED_SYNCED));
        output.putBoolean("BH_BreedMixed", this.entityData.get(BH_BREED_MIXED_SYNCED));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void bh_onRead(ValueInput input, CallbackInfo ci) {
        bh_owner = input.read("BH_Owner", UUIDUtil.CODEC).orElse(null);
        bh_pairedTo = input.read("BH_PairedTo", UUIDUtil.CODEC).orElse(null);
        bh_abilityToggled = input.getIntOr("BH_AbilityToggled", 0) != 0;
        if (bh_owner == null) {
            EntityReference<LivingEntity> ownerRef = ((AbstractHorse) (Object) this).getOwnerReference();
            bh_owner = ownerRef == null ? null : ownerRef.getUUID();
        }
        this.entityData.set(BH_OWNER_SYNCED, bh_owner == null ? "" : bh_owner.toString());
        bh_command = HorseCommand.fromId(input.getIntOr("BH_Command", HorseCommand.FOLLOW.ordinal()));
        this.entityData.set(BH_COMMAND_SYNCED, bh_command.ordinal());
        bh_bond = input.getIntOr("BH_Bond", 0);
        bh_generation = input.getIntOr("BH_Generation", 0);
        this.entityData.set(BH_BOND_SYNCED, bh_bond);
        bh_nameTagBondReceived = input.getIntOr("BH_NameTagBondGiven", bh_bond > 0 ? 1 : 0) != 0;
        bh_home = input.read("BH_Home", BlockPos.CODEC).orElse(null);
        bh_wanderCenter = input.read("BH_WanderCenter", BlockPos.CODEC).orElse(null);
        bh_hitchpostPos = input.read("BH_Hitchpost", BlockPos.CODEC).orElse(null);
        if (bh_home == null) {
            bh_home = BhHorseStorage.readLegacyBlockPos(input, "BH_Home");
        }
        if (bh_wanderCenter == null) {
            bh_wanderCenter = BhHorseStorage.readLegacyBlockPos(input, "BH_WanderCenter");
        }
        if (bh_hitchpostPos == null) {
            bh_hitchpostPos = BhHorseStorage.readLegacyBlockPos(input, "BH_Hitchpost");
        }
        this.entityData.set(BH_HITCHPOST_POS_SYNCED, Optional.ofNullable(bh_hitchpostPos));
        bh_applyBondAttributes();
        BhHorseStorage.readContainer(input.listOrEmpty("BH_Gear", BhHorseStorage.SlotEntry.CODEC), bh_gearContainer);
        BhHorseStorage.readContainer(input.listOrEmpty("BH_Chest", BhHorseStorage.SlotEntry.CODEC), bh_chestContainer);
        this.entityData.set(BH_CART_CHEST_SYNCED, input.getBooleanOr("BH_CartChestOn", false));
        BhHorseStorage.readContainer(input.listOrEmpty("BH_CartChest", BhHorseStorage.SlotEntry.CODEC), bh_cartChestContainer);
        BhHorseStorage.restoreUpgradedSaddle(inventory, input);
        bh_syncGearFlags();
        bh_afterLoad();

        Optional<Integer> savedGender = input.getInt("BH_Gender");
        if (savedGender.isPresent()) {
            this.entityData.set(BH_GENDER_SYNCED, savedGender.get());
        } else {
            this.entityData.set(BH_GENDER_SYNCED, this.random.nextBoolean() ? 0 : 1);
        }
        Optional<Integer> savedBreed = input.getInt("BH_Breed");
        if (savedBreed.isPresent()) {
            this.entityData.set(BH_BREED_SYNCED, savedBreed.get());
            this.entityData.set(BH_BREED_MIXED_SYNCED, input.getBooleanOr("BH_BreedMixed", false));
        } else {
            bh_assignBreedPreservingCoat();
        }

        bh_setLargeCart(input.getBooleanOr("BH_CartLarge", this.bh_mayUseLargeCart()));
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void bh_assignTraitsOnSpawn(ServerLevelAccessor level,
                                        DifficultyInstance difficulty,
                                        EntitySpawnReason reason,
                                        @Nullable SpawnGroupData groupData,
                                        CallbackInfoReturnable<SpawnGroupData> cir) {
        this.entityData.set(BH_GENDER_SYNCED, this.random.nextBoolean() ? 0 : 1);

        if ((Object) this instanceof BhBreedEntity breedEntity) {
            this.entityData.set(BH_BREED_SYNCED, breedEntity.bhFixedBreed().ordinal());
            this.entityData.set(BH_BREED_MIXED_SYNCED, false);
            return;
        }

        if (this.bh_getBreed() != HorseBreed.UNKNOWN_SPECIES) {
            return;
        }

        AbstractHorse self = (AbstractHorse) (Object) this;
        HorseBreed species = HorseBreed.speciesFor(self);
        if (species != null) {
            this.entityData.set(BH_BREED_SYNCED, species.ordinal());
            this.entityData.set(BH_BREED_MIXED_SYNCED, false);
        }
    }

    @Unique
    private void bh_assignBreedPreservingCoat() {
        HorseBreed picked = BhHorseTraits.pickBreed((AbstractHorse) (Object) this, this.random);
        this.entityData.set(BH_BREED_SYNCED, picked.ordinal());
        this.entityData.set(BH_BREED_MIXED_SYNCED, false);
    }

    @Inject(method = "createInventory", at = @At("TAIL"))
    private void bh_onCreateInventory(CallbackInfo ci) {
        bh_afterInventoryChange();
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
    public boolean bh_isAbilityToggled() {
        return this.bh_abilityToggled;
    }

    @Override
    public void bh_setAbilityToggled(boolean on) {
        this.bh_abilityToggled = on;
    }

    @Override
    public @Nullable UUID bh_getPairedTo() {
        return this.bh_pairedTo;
    }

    @Override
    public void bh_setPairedTo(@Nullable UUID horseId) {
        this.bh_pairedTo = horseId;
    }

    @Override
    public int bh_getCombatState() {
        return this.entityData.get(BH_COMBAT_SYNCED);
    }

    @Override
    public int bh_getKickTicks() {
        return this.entityData.get(BH_KICK_SYNCED);
    }

    @Override
    public void bh_setKickTicks(int ticks) {
        this.entityData.set(BH_KICK_SYNCED, Math.max(0, ticks));
    }

    @Unique
    private void bh_syncCombatState() {
        int next = this.bh_spookTicks > 0 ? 2 : this.bh_combatTarget != null ? 1 : 0;
        if (this.entityData.get(BH_COMBAT_SYNCED) != next) {
            this.entityData.set(BH_COMBAT_SYNCED, next);
        }
    }

    @Override
    public int bh_getGear() {
        return ((AbstractHorse) (Object) this).level().isClientSide()
                ? this.entityData.get(BH_GEAR_SYNCED)
                : bh_gear;
    }

    @Override
    public void bh_setGear(int gear) {
        this.bh_gear = Mth.clamp(gear, 0, BhGears.TOP_GEAR);
        this.entityData.set(BH_GEAR_SYNCED, this.bh_gear);
    }

    @Override
    public int bh_getStompTicks() {
        return this.entityData.get(BH_STOMP_SYNCED);
    }

    @Override
    public void bh_setStompTicks(int ticks) {
        this.entityData.set(BH_STOMP_SYNCED, Math.max(0, ticks));
    }

    @Override
    public int bh_getSurge() {
        return this.entityData.get(BH_SURGE_SYNCED);
    }

    @Override
    public void bh_setSurge(int packed) {
        if (this.entityData.get(BH_SURGE_SYNCED) != packed) {
            this.entityData.set(BH_SURGE_SYNCED, packed);
        }
    }

    @Override
    public int bh_getPerkSurge() {
        return this.entityData.get(BH_PERK_SYNCED);
    }

    @Override
    public void bh_setPerkSurge(int packed) {
        if (this.entityData.get(BH_PERK_SYNCED) != packed) {
            this.entityData.set(BH_PERK_SYNCED, packed);
        }
    }

    @Override
    public int bh_getCharge() {
        return this.entityData.get(BH_CHARGE_SYNCED);
    }

    @Override
    public void bh_setCharge(int fill) {
        if (this.entityData.get(BH_CHARGE_SYNCED) != fill) {
            this.entityData.set(BH_CHARGE_SYNCED, fill);
        }
    }

    @Override
    public boolean bh_isFreeLook() {
        return this.entityData.get(BH_FREE_LOOK_SYNCED);
    }

    @Override
    public void bh_setFreeLook(boolean freeLook) {
        if (this.entityData.get(BH_FREE_LOOK_SYNCED) != freeLook) {
            this.entityData.set(BH_FREE_LOOK_SYNCED, freeLook);
        }
    }

    @Override
    public int bh_getGaitGear() {
        return this.entityData.get(BH_GAIT_GEAR_SYNCED);
    }

    @Override
    public void bh_setGaitGear(int gear) {
        this.entityData.set(BH_GAIT_GEAR_SYNCED, Mth.clamp(gear, 0, BhGears.TOP_GEAR));
    }

    @Inject(method = "getRiddenSpeed", at = @At("RETURN"), cancellable = true)
    private void bh_applyGearSpeed(
            Player rider,
            CallbackInfoReturnable<Float> cir) {
        if (bh_gear <= 0) {
            return;
        }
        float full = cir.getReturnValueF();
        float pace = BhGears.pace(bh_gear);
        cir.setReturnValue(pace > 0.0F
                ? Math.min(pace, full)
                : full * BhGears.speed(bh_gear));
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void bh_neighWhenHurt(
            ServerLevel level,
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!cir.getReturnValueZ()
                || !(self instanceof Horse)
                || self.getRandom().nextFloat() >= BH_HURT_NEIGH_CHANCE) {
            return;
        }
        level.playSound(null, self.getX(), self.getY(), self.getZ(),
                ModSounds.HORSE_NEIGH, self.getSoundSource(), 1.0F, 1.0F);
    }


    @ModifyConstant(method = "aiStep", constant = @Constant(intValue = 300))
    private int bh_grazeLessOften(int vanillaInterval) {
        return BH_GRAZE_ROLL_INTERVAL;
    }

    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;"
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

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void bh_stopGrazingWhenHurt(
            ServerLevel level,
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
        for (HorseFeature feature : this.bh_features) {
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
            BhHorseTraits.blockSameGenderBreeding(self, this, player);
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"),
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
                || self.getPassengers().size() >= 2) {
            return;
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

        ItemStack heldItem = player.getItemInHand(hand);
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
    private void bh_adjustFallDamage(double distance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
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
    private void bh_dropGearAndChest(ServerLevel level, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide()) return;
        if (this.bh_hitchpostPos != null) {
            HitchpostBlock.releaseHorse(level, self, false);
        }
        bh_dropCartChest();
        BhHorseStorage.dropContainerContents(self, level, bh_gearContainer);
        BhHorseStorage.dropContainerContents(self, level, bh_chestContainer);
        bh_syncGearFlags();
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void bh_onRegisterGoals(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        goalSelector.addGoal(1, new SpookGoal(self));
        goalSelector.addGoal(2, new DefendOwnerGoal(self));
        goalSelector.addGoal(3, new PackmateFollowGoal(self));
        goalSelector.addGoal(3, new HorseStayGoal(self));
        goalSelector.addGoal(3, new HorseFollowOwnerGoal(self));
        goalSelector.addGoal(3, new HorseReturnHomeGoal(self));
        goalSelector.addGoal(3, new HorseWanderBoundsGoal(self));
    }

    @Unique private Vec3 bh_rearSeatShift = Vec3.ZERO;

    @Redirect(
            method = "getPassengerAttachmentPoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)"
                            + "Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 bh_noRearRiderShift(Vec3 attachment, Vec3 rearOffset) {
        Vec3 applied = rearOffset.scale(BhRiderSeat.REAR_CAMERA_FOLLOW);
        this.bh_rearSeatShift = applied;
        return attachment.add(applied);
    }

    @Inject(method = "getPassengerAttachmentPoint", at = @At("RETURN"), cancellable = true)
    private void bh_offsetSecondPassenger(
            Entity passenger,
            EntityDimensions dimensions,
            float scaleFactor,
            CallbackInfoReturnable<Vec3> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;

        if (this.bh_hasCartGear()) {
            Vec3 camera = new Vec3(0.0D, BhRiderSeat.CART_CAMERA_LIFT, 0.0D);
            if (self.level().isClientSide()) {
                BhRiderSeat.publish(self.getId(), camera);
            }
            cir.setReturnValue(HorseCartEntity
                    .benchSeatOffset(self, BhHorseSteering.benchSeatIndex(self, passenger), self.yBodyRot)
                    .add(camera));
            return;
        }

        Vec3 lift = new Vec3(0.0D, BhRiderSeat.seatLift(self), 0.0D);
        BhRiderSeat.publish(self.getId(), this.bh_rearSeatShift.add(lift));
        if (lift.y != 0.0D) {
            cir.setReturnValue(cir.getReturnValue().add(lift));
        }

        Vec3 offset = BhHorseSteering.multiRiderOffset(self, passenger);
        if (offset != null) {
            cir.setReturnValue(cir.getReturnValue().add(offset));
        }
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

        this.entityData.set(BH_GEAR_FLAGS_SYNCED, flags);
        boolean hadCart = this.entityData.get(BH_CART_SYNCED);
        boolean hasCart = this.bh_gearContainer.getItem(GearSlot.STABILIZER.ordinal()).is(ModItems.HORSE_CART);
        this.entityData.set(BH_CART_SYNCED, hasCart);
        if (hasCart && !hadCart) {
            bh_setLargeCart(this.bh_mayUseLargeCart());
        }
        this.entityData.set(BH_ENDER_CHEST_SYNCED,
                this.bh_gearContainer.getItem(GearSlot.CHEST.ordinal()).is(Items.ENDER_CHEST));
    }

    @Override
    public boolean bh_hasEnderChestGear() {
        return this.entityData.get(BH_ENDER_CHEST_SYNCED);
    }

    @Override
    public boolean bh_hasCartGear() {
        return this.entityData.get(BH_CART_SYNCED);
    }

    @Override
    public void bh_ridePlayer(Player player) {
        this.doPlayerRide(player);
    }

    @Redirect(
            method = "handleStartJump(I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;"
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
                    target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;"
                            + "standIfPossible()V"))
    private void bh_noRearOnPlayerJump(AbstractHorse horse) {
    }

    @Unique
    private void bh_afterLoad() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        for (HorseFeature feature : this.bh_features) {
            feature.onLoad(self, this);
        }
    }

    @Unique
    private void bh_afterInventoryChange() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        for (HorseFeature feature : this.bh_features) {
            feature.onInventoryChanged(self, this);
        }
    }
}
