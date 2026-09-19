package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhHorseSteering;
import icy.betterhorses.net.BhRiderSeat;
import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.BhHorseStorage;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.HorseGender;
import icy.betterhorses.net.HorseStabilizerLogic;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.breed.BreedAbility;
import icy.betterhorses.net.ModAttachments;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.BhVanillaHorseSwap;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.feature.BreedAbilities;
import icy.betterhorses.net.feature.CartRig;
import icy.betterhorses.net.feature.FrostHooves;
import icy.betterhorses.net.feature.HorseCombat;
import icy.betterhorses.net.feature.HorseFeature;
import icy.betterhorses.net.feature.RiderGate;
import icy.betterhorses.net.feature.SaddleWatch;
import icy.betterhorses.net.feature.SpeedRecord;
import icy.betterhorses.net.feature.Stabilizer;
import icy.betterhorses.net.feature.SwimBoost;
import icy.betterhorses.net.goal.DefendOwnerGoal;
import icy.betterhorses.net.goal.HorseFollowOwnerGoal;
import icy.betterhorses.net.goal.HorseReturnHomeGoal;
import icy.betterhorses.net.goal.HorseStayGoal;
import icy.betterhorses.net.goal.HorseWanderBoundsGoal;
import icy.betterhorses.net.goal.SpookGoal;
import icy.betterhorses.net.inventory.BhSlotEntry;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import com.mojang.serialization.Codec;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements IHorseData, IHorseAbilityHost {

    @Shadow
    protected SimpleContainer inventory;

    @Shadow
    protected abstract void doPlayerRide(net.minecraft.world.entity.player.Player player);

    @Shadow
    protected abstract void setStanding(boolean standing);

    @Shadow
    protected int standCounter;

    @Unique private static final int BH_CART_CHEST_SIZE = 54;

    @Unique private @Nullable UUID bh_owner = null;
    @Unique private HorseCommand bh_command = HorseCommand.FOLLOW;
    @Unique private @Nullable BlockPos bh_home = null;
    @Unique private @Nullable BlockPos bh_wanderCenter = null;
    @Unique private int bh_bondRemainder = 0;
    @Unique private int bh_generation = 0;
    @Unique private boolean bh_abilityPaused = false;
    @Unique private long bh_rescueReadyAt = 0L;
    @Unique private int bh_gear = 0;
    @Unique private int bh_spookTicks = 0;
    @Unique private @Nullable UUID bh_combatTarget = null;
    @Unique private @Nullable UUID bh_cartId = null;
    @Unique private @Nullable ResourceKey<Level> bh_homeDim = null;
    @Unique private @Nullable SimpleContainer bh_cartChestContainer = null;
    @Unique private ItemStack bh_cartPlow = ItemStack.EMPTY;
    @Unique private boolean bh_nameTagBondReceived = false;
    @Unique private ModAttachments.BhHorseSyncState bh_syncState;
    @Unique
    private final SimpleContainer bh_gearContainer = new SimpleContainer(GearSlot.COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            AbstractHorseMixin.this.bh_syncGearFlags();
        }
    };
    @Unique private final SimpleContainer bh_chestContainer = new SimpleContainer(27);
    @Unique private CartRig bh_cartRig;
    @Unique private HorseCombat bh_combat;
    @Unique private BreedAbilities bh_abilities;
    @Unique private HorseFeature[] bh_features;

    @Unique
    private HorseCombat bh_combatFeature() {
        bh_features();
        return this.bh_combat;
    }

    @Unique
    private BreedAbilities bh_abilitiesFeature() {
        bh_features();
        return this.bh_abilities;
    }

    @Unique
    private HorseFeature[] bh_features() {
        if (this.bh_features == null) {
            this.bh_cartRig = new CartRig();
            this.bh_combat = new HorseCombat();
            this.bh_abilities = new BreedAbilities();
            this.bh_features = new HorseFeature[]{
                    new SaddleWatch(),
                    new SpeedRecord(),
                    new RiderGate(),
                    new Stabilizer(),
                    new SwimBoost(),
                    new FrostHooves(),
                    this.bh_cartRig,
                    this.bh_combat,
                    this.bh_abilities,
            };
        }
        return this.bh_features;
    }
    @Unique private boolean bh_fedGoldenAppleThisTick = false;

    @Unique
    private static final java.util.UUID BH_SPEED_ID =
            java.util.UUID.fromString("a1b2c3d4-0001-4a51-8b6f-1c2d3e4f5a6b");
    @Unique
    private static final java.util.UUID BH_JUMP_ID =
            java.util.UUID.fromString("a1b2c3d4-0002-4a51-8b6f-1c2d3e4f5a6b");
    @Unique private static final float BH_HOOVES_FALL_DAMAGE_MULTIPLIER = 0.5F;
    @Unique private static final double BH_STABILIZER_HALF_OPEN_DESCENT_SPEED = -0.35D;
    @Unique private static final double BH_STABILIZER_MAX_DESCENT_SPEED = -0.125D;
    @Unique private static final double BH_STABILIZER_SMOOTHING = 0.35D;
    @Unique private static final double BH_STABILIZER_HALF_OPEN_SMOOTHING = 0.2D;
    @Unique private static final float BH_WATER_SPEED_MULTIPLIER = 1.5F;
    @Unique private static final double BH_WATER_RISE_SPEED = 0.006D;
    @Unique private static final double BH_WATER_SURFACE_SPEED = 0.001D;
    @Unique private static final double BH_WATER_MAX_RISE_SPEED = 0.015D;
    @Unique private static final double BH_FROST_WALKER_SAMPLE_STEP = 0.75D;
    @Unique private static final double BH_FROST_WALKER_RESET_DISTANCE = 8.0D;
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
        this.entityData.define(BH_BOND, this.bh_syncState().bond);
        this.entityData.define(BH_STABILIZER_STATE, this.bh_syncState().stabilizerStateId);
        this.entityData.define(BH_GEAR_FLAGS, this.bh_syncState().gearFlags);
        this.entityData.define(BH_GENDER_ID, this.bh_syncState().genderId);
        this.entityData.define(BH_BREED_ID, this.bh_syncState().breedId);
        this.entityData.define(BH_BREED_MIXED, this.bh_syncState().breedMixed);
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
        if (!level().isClientSide()) {
            return bh_owner;
        }
        String id = this.entityData.get(BH_OWNER_ID);
        return id.isEmpty() ? null : UUID.fromString(id);
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
                HorseTracker.unregister(self);
            }
        }
    }

    @Override
    public HorseCommand bh_getCommand() {
        return level().isClientSide()
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
    public int bh_getBondRemainder() {
        return this.bh_bondRemainder;
    }

    @Override
    public void bh_setBondRemainder(int value) {
        this.bh_bondRemainder = value;
    }

    @Override
    public int bh_getBond() {
        return this.entityData.get(BH_BOND);
    }

    @Override
    public void bh_setBond(int level) {
        this.bh_syncState().bond = Math.max(0, Math.min(100, level));
        this.bh_syncHorseData();
        bh_applyBondAttributes();
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
        this.bh_syncState().genderId = gender.ordinal();
        this.bh_syncHorseData();
    }

    @Override
    public HorseBreed bh_getBreed() {
        return HorseBreed.fromId(this.entityData.get(BH_BREED_ID));
    }

    @Override
    public void bh_setBreed(HorseBreed breed) {
        this.bh_syncState().breedId = breed.ordinal();
        this.bh_syncHorseData();
    }

    @Override
    public boolean bh_isMixedBreed() {
        return this.entityData.get(BH_BREED_MIXED);
    }

    @Override
    public void bh_setMixedBreed(boolean mixed) {
        this.bh_syncState().breedMixed = mixed;
        this.bh_syncHorseData();
    }

    @Override
    public HorseStabilizerState bh_getStabilizerState() {
        return HorseStabilizerState.fromId(this.entityData.get(BH_STABILIZER_STATE));
    }

    @Override
    public void bh_setStabilizerState(HorseStabilizerState state) {
        this.bh_syncState().stabilizerStateId = state.ordinal();
        this.bh_syncHorseData();
    }

    @Override
    public int bh_getGearFlags() {
        return this.entityData.get(BH_GEAR_FLAGS);
    }

    @Override
    public boolean bh_hasUpgradedSaddle() {
        return inventory != null && inventory.getItem(0).is(ModItems.UPGRADED_SADDLE.get());
    }

    @Override
    public void bh_equipUpgradedSaddle(ItemStack saddle) {
        if (inventory != null) {
            inventory.setItem(0, saddle);
        }
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
        bh_dropContainerContents(self, serverLevel, bh_gearContainer);
        bh_dropChestContents();
        bh_syncGearFlags();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void bh_onWrite(CompoundTag output, CallbackInfo ci) {
        if (bh_owner != null) {
            output.putUUID("BH_Owner", bh_owner);
        }
        output.putInt("BH_Command", bh_command.ordinal());
        output.putInt("BH_Bond", this.bh_getBond());
        output.putInt("BH_NameTagBondGiven", bh_nameTagBondReceived ? 1 : 0);
        output.putInt("BH_BondRemainder", bh_bondRemainder);
        bh_writeBlockPos(output, "BH_Home", bh_home);
        bh_writeBlockPos(output, "BH_WanderCenter", bh_wanderCenter);
        output.put("BH_Gear", bh_writeContainer(bh_gearContainer));
        output.put("BH_Chest", bh_writeContainer(bh_chestContainer));
        output.putInt("BH_Gender", this.bh_getGender().ordinal());
        output.putInt("BH_Breed", this.bh_getBreed().ordinal());
        output.putBoolean("BH_BreedMixed", this.bh_isMixedBreed());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void bh_onRead(CompoundTag input, CallbackInfo ci) {
        if (input.hasUUID("BH_Owner")) {
            bh_owner = input.getUUID("BH_Owner");
        } else {
            AbstractHorse self = (AbstractHorse) (Object) this;
            bh_owner = self.getOwnerUUID();
        }
        bh_command = HorseCommand.fromId(input.contains("BH_Command") ? input.getInt("BH_Command") : HorseCommand.FOLLOW.ordinal());
        this.bh_syncState().bond = Math.max(0, Math.min(100, input.contains("BH_Bond") ? input.getInt("BH_Bond") : 0));
        bh_nameTagBondReceived = (input.contains("BH_NameTagBondGiven") ? input.getInt("BH_NameTagBondGiven") : (this.bh_syncState().bond > 0 ? 1 : 0)) != 0;
        bh_bondRemainder = input.getInt("BH_BondRemainder");
        bh_home = bh_readBlockPos(input, "BH_Home");
        bh_wanderCenter = bh_readBlockPos(input, "BH_WanderCenter");
        if (bh_home == null) bh_home = bh_readLegacyBlockPos(input, "BH_Home");
        if (bh_wanderCenter == null) bh_wanderCenter = bh_readLegacyBlockPos(input, "BH_WanderCenter");
        bh_applyBondAttributes();
        bh_readContainer(input, "BH_Gear", bh_gearContainer);
        bh_readContainer(input, "BH_Chest", bh_chestContainer);
        bh_restoreUpgradedSaddle(input);
        bh_syncGearFlags();

        if (input.contains("BH_Gender")) {
            this.bh_syncState().genderId = input.getInt("BH_Gender");
        } else {
            this.bh_syncState().genderId = this.random.nextBoolean() ? 0 : 1;
        }
        if (input.contains("BH_Breed")) {
            this.bh_syncState().breedId = input.getInt("BH_Breed");
            this.bh_syncState().breedMixed = input.contains("BH_BreedMixed") && input.getBoolean("BH_BreedMixed");
        } else {
            bh_assignBreedPreservingCoat();
        }
        this.bh_syncHorseData();
        AbstractHorse loaded = (AbstractHorse) (Object) this;
        for (HorseFeature feature : this.bh_features()) {
            feature.onLoad(loaded, this);
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void bh_assignTraitsOnSpawn(net.minecraft.world.level.ServerLevelAccessor level,
                                        net.minecraft.world.DifficultyInstance difficulty,
                                        net.minecraft.world.entity.MobSpawnType reason,
                                        @Nullable net.minecraft.world.entity.SpawnGroupData groupData,
                                        @Nullable net.minecraft.nbt.CompoundTag dataTag,
                                        CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> cir) {
        this.bh_syncState().genderId = this.random.nextBoolean() ? 0 : 1;

        if (this.bh_getBreed() != HorseBreed.UNKNOWN_SPECIES) {
            return;
        }

        AbstractHorse self = (AbstractHorse) (Object) this;
        HorseBreed species = HorseBreed.speciesFor(self);
        if (species != null) {
            this.bh_syncState().breedId = species.ordinal();
            this.bh_syncState().breedMixed = false;
        }
        this.bh_syncHorseData();
    }

    @Unique
    private void bh_assignBreedPreservingCoat() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        HorseBreed species = HorseBreed.speciesFor(self);
        if (species != null) {
            this.bh_syncState().breedId = species.ordinal();
            this.bh_syncState().breedMixed = false;
            return;
        }
        HorseBreed picked = HorseBreed.MUSTANG;
        if (self instanceof Horse horse) {
            java.util.List<HorseBreed> matches = HorseBreed.breedsMatchingCoat(horse.getVariant(), horse.getMarkings());
            if (!matches.isEmpty()) {
                picked = matches.get(this.random.nextInt(matches.size()));
            }
        }
        this.bh_syncState().breedId = picked.ordinal();
        this.bh_syncState().breedMixed = false;
    }

    @Unique
    private ListTag bh_writeContainer(SimpleContainer container) {
        ListTag list = new ListTag();
        Codec<BhSlotEntry> codec = BhSlotEntry.CODEC;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            codec.encodeStart(NbtOps.INSTANCE, new BhSlotEntry(i, stack))
                    .result().ifPresent(list::add);
        }
        return list;
    }

    @Unique
    private void bh_readContainer(CompoundTag input, String key, SimpleContainer container) {
        container.clearContent();
        if (!input.contains(key, Tag.TAG_LIST)) return;
        ListTag list = input.getList(key, Tag.TAG_COMPOUND);
        Codec<BhSlotEntry> codec = BhSlotEntry.CODEC;
        for (Tag t : list) {
            codec.parse(NbtOps.INSTANCE, t)
                    .result()
                    .ifPresent(entry -> {
                        int slot = entry.slot();
                        if (slot >= 0 && slot < container.getContainerSize()) {
                            container.setItem(slot, entry.stack());
                        }
                    });
        }
    }

    @Unique
    private void bh_restoreUpgradedSaddle(CompoundTag input) {
        if (inventory == null || !inventory.getItem(0).isEmpty()) {
            return;
        }
        if (!input.contains("SaddleItem", Tag.TAG_COMPOUND)) return;
        ItemStack saddle = ItemStack.of(input.getCompound("SaddleItem"));
        if (saddle.is(ModItems.UPGRADED_SADDLE.get())) {
            inventory.setItem(0, saddle);
        }
    }

    @Unique
    private static void bh_writeBlockPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if (pos == null) return;
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", pos.getX());
        posTag.putInt("Y", pos.getY());
        posTag.putInt("Z", pos.getZ());
        tag.put(key, posTag);
    }

    @Unique
    private static @Nullable BlockPos bh_readBlockPos(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) return null;
        CompoundTag posTag = tag.getCompound(key);
        if (!posTag.contains("X") || !posTag.contains("Y") || !posTag.contains("Z")) return null;
        return new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
    }

    @Unique
    private static @Nullable BlockPos bh_readLegacyBlockPos(CompoundTag input, String keyPrefix) {
        if (!input.contains(keyPrefix + "X") || !input.contains(keyPrefix + "Y") || !input.contains(keyPrefix + "Z")) {
            return null;
        }
        return new BlockPos(input.getInt(keyPrefix + "X"), input.getInt(keyPrefix + "Y"), input.getInt(keyPrefix + "Z"));
    }

    @Inject(method = "createInventory", at = @At("TAIL"))
    private void bh_onCreateInventory(CallbackInfo ci) {
        this.bh_syncGearFlags();
        AbstractHorse self = (AbstractHorse) (Object) this;
        for (HorseFeature feature : this.bh_features()) {
            feature.onInventoryChanged(self, this);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_tickFeatures(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (BhVanillaHorseSwap.trySwap(self)) {
            return;
        }
        for (HorseFeature feature : this.bh_features()) {
            feature.tick(self, this);
        }
    }

    @Override
    public @Nullable BreedAbility bh_currentAbility() {
        return this.bh_abilitiesFeature().current();
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void bh_combatOnHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            this.bh_combatFeature().onHurt((AbstractHorse) (Object) this, this, source);
        }
    }

    @Override
    public void bh_onRemoved() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        for (HorseFeature feature : this.bh_features()) {
            feature.onRemoved(self, this);
        }
    }

    @Inject(method = "handleEating", at = @At("HEAD"))
    private void bh_markGoldenAppleFeed(net.minecraft.world.entity.player.Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        this.bh_fedGoldenAppleThisTick = stack.is(Items.GOLDEN_APPLE);
    }

    @Inject(method = "handleEating", at = @At("RETURN"))
    private void bh_rewardGoldenAppleBond(net.minecraft.world.entity.player.Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        try {
            AbstractHorse self = (AbstractHorse) (Object) this;
            if (!this.bh_fedGoldenAppleThisTick || self.level().isClientSide() || !Boolean.TRUE.equals(cir.getReturnValue())) {
                return;
            }

            this.bh_setBond(this.bh_getBond() + 2);

            if (self.isInLove()) {
                HorseGender myGender = this.bh_getGender();
                java.util.List<AbstractHorse> nearby = self.level().getEntitiesOfClass(
                        AbstractHorse.class,
                        self.getBoundingBox().inflate(8.0D),
                        h -> h != self && h.isInLove() && ((IHorseData) h).bh_getGender() == myGender);
                if (!nearby.isEmpty()) {
                    self.resetLove();
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.sendSystemMessage(Component.translatable(
                                "message.icys-better-horses.same_gender_breed"));
                    }
                }
            }
        } finally {
            this.bh_fedGoldenAppleThisTick = false;
        }
    }

    @Inject(method = "doPlayerRide", at = @At("HEAD"), cancellable = true)
    private void bh_gateOwnerOnlyMount(net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide() || !BhConfig.horseExclusivityEnabled()) return;
        UUID owner = this.bh_getOwner();
        if (owner == null || owner.equals(player.getUUID())) return;
        if (bh_ownerIsPrimaryPassenger(self, owner)) return;
        self.playSound(net.minecraft.sounds.SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys-better-horses.not_owner"));
        }
        if (player.getVehicle() == self) {
            player.stopRiding();
        }
        ci.cancel();
    }

    @Inject(method = "doPlayerRide", at = @At("TAIL"))
    private void bh_trackLastRidden(net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide() || player.getVehicle() != self) {
            return;
        }
        UUID owner = this.bh_getOwner();
        if (owner == null || !owner.equals(player.getUUID())) {
            return;
        }
        HorseTracker.setLastRidden(owner, self);
    }

    @Inject(
            method = "doPlayerRide",
            at = @At("HEAD"),
            cancellable = true)
    private void bh_rotateHorseInsteadOfPlayer(net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }

        UUID owner = this.bh_getOwner();
        if (BhConfig.horseExclusivityEnabled()
                && owner != null
                && !owner.equals(player.getUUID())
                && !bh_ownerIsPrimaryPassenger(self, owner)) {
            ci.cancel();
            return;
        }
        self.setYRot(player.getYRot());
        self.yRotO = self.getYRot();
        self.setYHeadRot(player.getYHeadRot());
        self.setXRot(player.getXRot());

        player.startRiding(self);

        player.setYRot(self.getYRot());
        player.yRotO = self.yRotO;
        player.setXRot(self.getXRot());
        ci.cancel();
    }

    @Inject(method = "tameWithName", at = @At("RETURN"))
    private void bh_claimHorseOnTame(net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!cir.getReturnValueZ() || self.level().isClientSide() || player.getUUID().equals(this.bh_getOwner())) {
            return;
        }

        this.bh_setOwner(player.getUUID());
        this.bh_setWanderCenter(self.blockPosition());
        this.bh_setCommand(HorseCommand.WANDER);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys-better-horses.claimed"));
        }
    }

    @Inject(method = "openCustomInventoryScreen", at = @At("HEAD"), cancellable = true)
    private void bh_blockNonOwnerInventoryAccess(net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide() || !BhConfig.horseExclusivityEnabled()) {
            return;
        }

        UUID owner = this.bh_getOwner();
        if (owner == null || owner.equals(player.getUUID())) {
            return;
        }

        self.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys-better-horses.not_inventory_owner"));
        }
        ci.cancel();
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void bh_allowSecondPlayerRider(
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand,
            CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!self.isVehicle()
                || self.isBaby()
                || self.hasPassenger(player)
                || self.getPassengers().size() >= BhHorseSteering.bh_seatCount(this)) {
            return;
        }

        net.minecraft.world.InteractionResult animalResult = super.mobInteract(player, hand);
        if (animalResult.consumesAction()) {
            cir.setReturnValue(animalResult);
            return;
        }

        if (self.isTamed() && player.isSecondaryUseActive()) {
            self.openCustomInventoryScreen(player);
            cir.setReturnValue((self.level().isClientSide() ? net.minecraft.world.InteractionResult.SUCCESS : net.minecraft.world.InteractionResult.CONSUME));
            return;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            net.minecraft.world.InteractionResult heldItemResult = heldItem.interactLivingEntity(player, self, hand);
            if (heldItemResult.consumesAction()) {
                cir.setReturnValue(heldItemResult);
                return;
            }
        }

        if (!BhConfig.multiRidingEnabled()) {
            return;
        }

        this.doPlayerRide(player);
        cir.setReturnValue((self.level().isClientSide() ? net.minecraft.world.InteractionResult.SUCCESS : net.minecraft.world.InteractionResult.CONSUME));
    }

    @Override
    protected float getWaterSlowDown() {
        float vanillaSlowDown = super.getWaterSlowDown();
        float vanillaSpeedRatio = vanillaSlowDown / (1.0F - vanillaSlowDown);
        float boostedSpeedRatio = vanillaSpeedRatio * BH_WATER_SPEED_MULTIPLIER;
        return boostedSpeedRatio / (1.0F + boostedSpeedRatio);
    }

    @Override
    public Vec3 getFluidFallingAdjustedMovement(double gravity, boolean falling, Vec3 movement) {
        Vec3 adjustedMovement = super.getFluidFallingAdjustedMovement(gravity, falling, movement);
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!self.isInWater()) {
            return adjustedMovement;
        }
        return bh_applyWaterBuoyancy(adjustedMovement);
    }

    @Unique
    private Vec3 bh_applyWaterBuoyancy(Vec3 movement) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        double waterHeight = self.getFluidHeight(FluidTags.WATER);
        if (waterHeight <= 0.0D) {
            return movement;
        }

        double minVerticalSpeed =
                waterHeight > self.getFluidJumpThreshold() ? BH_WATER_RISE_SPEED : BH_WATER_SURFACE_SPEED;
        double verticalSpeed = Math.max(movement.y, minVerticalSpeed);
        return new Vec3(movement.x, Math.min(verticalSpeed, BH_WATER_MAX_RISE_SPEED), movement.z);
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void bh_adjustFallDamage(float distance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (this.bh_hasStabilizerGear()) {
            HorseStabilizerState landingState = HorseStabilizerLogic.resolveLandingState(
                    true,
                    distance,
                    this.bh_getStabilizerState());
            if (landingState == HorseStabilizerState.CLOSED) {
                return;
            }

            if (distance > 1.0F) {
                self.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
            }
            this.bh_setStabilizerState(landingState);
            this.fallDistance = 0.0F;
            cir.setReturnValue(false);
            return;
        }

        if (!this.bh_hasHoovesGear()) {
            return;
        }

        if (distance > 1.0F) {
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
        bh_dropContainerContents(self, level, bh_gearContainer);
        bh_dropContainerContents(self, level, bh_chestContainer);
        bh_syncGearFlags();
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void bh_onRegisterGoals(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        goalSelector.addGoal(3, new HorseStayGoal(self));
        goalSelector.addGoal(3, new HorseFollowOwnerGoal(self));
        goalSelector.addGoal(3, new HorseReturnHomeGoal(self));
        goalSelector.addGoal(3, new HorseWanderBoundsGoal(self));
        goalSelector.addGoal(1, new SpookGoal(self));
        goalSelector.addGoal(2, new DefendOwnerGoal(self));
    }

    @Redirect(
            method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity$MoveFunction;accept(Lnet/minecraft/world/entity/Entity;DDD)V"))
    private void bh_offsetSecondPassenger(
            net.minecraft.world.entity.Entity.MoveFunction moveFunction,
            Entity passenger, double x, double y, double z) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (this.bh_hasCartGear()) {
            if (self.level().isClientSide()) {
                BhRiderSeat.publish(self.getId(), Vec3.ZERO);
            }
            Vec3 bench = HorseCartEntity.benchSeatOffset(
                    self, BhHorseSteering.benchSeatIndex(self, passenger), self.yBodyRot);
            moveFunction.accept(passenger, self.getX() + bench.x, self.getY() + bench.y, self.getZ() + bench.z);
            return;
        }

        double lift = BhRiderSeat.seatLift(self);
        if (self.level().isClientSide()) {
            BhRiderSeat.publish(self.getId(), new Vec3(0.0D, lift, 0.0D));
        }
        y += lift;

        if (self instanceof BhBreedHorse) {
            y -= passenger.getMyRidingOffset();
        }

        Vec3 offset = BhHorseSteering.multiRiderOffset(self, passenger);
        if (offset != null) {
            x += offset.x;
            z += offset.z;
        }
        moveFunction.accept(passenger, x, y, z);
    }

    @Inject(method = "getRiddenRotation", at = @At("HEAD"), cancellable = true)
    private void bh_allowMountedFreeCamera(LivingEntity rider, CallbackInfoReturnable<Vec2> cir) {
        if (!(rider instanceof net.minecraft.world.entity.player.Player player)) {
            return;
        }
        Vec2 rotation = BhHorseSteering.riddenRotation((AbstractHorse) (Object) this, this, player);
        if (rotation != null) {
            cir.setReturnValue(rotation);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return BhHorseSteering.canAddPassenger((AbstractHorse) (Object) this, this, passenger);
    }

    @Unique
    private static boolean bh_ownerIsPrimaryPassenger(AbstractHorse horse, UUID owner) {
        java.util.List<Entity> passengers = horse.getPassengers();
        return !passengers.isEmpty() && passengers.get(0).getUUID().equals(owner);
    }

    @Unique
    private void bh_applyBondAttributes() {
        BhHorseTraits.applyBondAttributes((AbstractHorse) (Object) this, this.bh_getBond());
    }

    @Unique
    private boolean bh_hasHoovesGear() {
        return BhConfig.hoovesEnabled() && this.bh_hasGear(GearSlot.HOOVES);
    }

    @Unique
    private boolean bh_hasStabilizerGear() {
        return Stabilizer.hasStabilizerGear(this);
    }

    @Unique
    private void bh_freezeWaterAtSample(ServerLevel level, Vec3 sample, int radius) {
        BlockPos center = BlockPos.containing(sample.x, sample.y - 1.0D, sample.z);
        BlockState frostedIce = Blocks.FROSTED_ICE.defaultBlockState();
        int radiusSq = radius * radius;
        BlockPos.MutableBlockPos waterPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos abovePos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radiusSq) {
                    continue;
                }

                waterPos.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                BlockState waterState = level.getBlockState(waterPos);
                if (!waterState.is(Blocks.WATER) || !level.getFluidState(waterPos).isSourceOfType(Fluids.WATER)) {
                    continue;
                }

                abovePos.set(waterPos.getX(), waterPos.getY() + 1, waterPos.getZ());
                if (!level.getBlockState(abovePos).isAir()) {
                    continue;
                }

                BlockPos icePos = waterPos.immutable();
                level.setBlock(icePos, frostedIce, 3);
                level.scheduleTick(icePos, Blocks.FROSTED_ICE, Mth.nextInt(level.getRandom(), 60, 120));
            }
        }
    }

    @Unique
    private void bh_dropChestContents() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        bh_dropContainerContents(self, serverLevel, bh_chestContainer);
    }

    @Unique
    private void bh_dropContainerContents(AbstractHorse horse, ServerLevel level, SimpleContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                horse.spawnAtLocation(stack);
            }
        }
    }

    @Unique
    private void bh_syncGearFlags() {
        int flags = 0;
        for (GearSlot slot : GearSlot.values()) {
            if (slot.accepts(this.bh_gearContainer.getItem(slot.ordinal()))
                    && !this.bh_gearContainer.getItem(slot.ordinal()).isEmpty()) {
                flags |= 1 << slot.ordinal();
            }
        }

        this.bh_syncState().gearFlags = flags;
        this.bh_syncHorseData();
        boolean hadCart = this.entityData.get(BH_CART);
        boolean hasCart = this.bh_gearContainer.getItem(GearSlot.STABILIZER.ordinal())
                .is(ModItems.HORSE_CART.get());
        bh_push(BH_CART, hasCart);
        if (hasCart && !hadCart) {
            bh_setLargeCart(this.bh_mayUseLargeCart());
        }
        bh_push(BH_ENDER_CHEST, this.bh_gearContainer.getItem(GearSlot.CHEST.ordinal()).is(Items.ENDER_CHEST));
        bh_push(BH_UPGRADED_SADDLE, this.inventory != null
                && this.inventory.getItem(0).is(ModItems.UPGRADED_SADDLE.get()));
    }

    @Unique
    private ModAttachments.BhHorseSyncState bh_syncState() {
        if (this.bh_syncState == null) {
            this.bh_syncState = new ModAttachments.BhHorseSyncState();
        }
        return this.bh_syncState;
    }

    @Unique
    private void bh_syncHorseData() {
        if (((AbstractHorse) (Object) this).level().isClientSide()) {
            return;
        }
        this.entityData.set(BH_BOND, this.bh_syncState().bond);
        this.entityData.set(BH_STABILIZER_STATE, this.bh_syncState().stabilizerStateId);
        this.entityData.set(BH_GEAR_FLAGS, this.bh_syncState().gearFlags);
        this.entityData.set(BH_GENDER_ID, this.bh_syncState().genderId);
        this.entityData.set(BH_BREED_ID, this.bh_syncState().breedId);
        this.entityData.set(BH_BREED_MIXED, this.bh_syncState().breedMixed);
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
    public @Nullable ResourceKey<Level> bh_getHomeDimension() {
        return this.bh_homeDim;
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
    public long bh_getRescueReadyAt() {
        return this.bh_rescueReadyAt;
    }

    @Override
    public void bh_setRescueReadyAt(long value) {
        this.bh_rescueReadyAt = value;
    }

    @Override
    public int bh_getGear() {
        return level().isClientSide() ? this.entityData.get(BH_GEAR) : this.bh_gear;
    }

    @Override
    public void bh_setGear(int gear) {
        this.bh_gear = Mth.clamp(gear, 0, BhGears.TOP_GEAR);
        bh_push(BH_GEAR, this.bh_gear);
    }

    @Override
    public int bh_getGaitGear() {
        return this.entityData.get(BH_GAIT_GEAR);
    }

    @Override
    public void bh_setGaitGear(int gear) {
        bh_push(BH_GAIT_GEAR, Mth.clamp(gear, 0, BhGears.TOP_GEAR));
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
    public int bh_getCombatState() {
        return this.entityData.get(BH_COMBAT);
    }

    @Unique
    private void bh_syncCombatState() {
        bh_push(BH_COMBAT, this.bh_spookTicks > 0 ? 2 : this.bh_combatTarget != null ? 1 : 0);
    }

    @Override
    public int bh_getKickTicks() {
        return this.entityData.get(BH_KICK);
    }

    @Override
    public void bh_setKickTicks(int ticks) {
        bh_push(BH_KICK, Math.max(0, ticks));
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
        bh_push(BH_SURGE, packed);
    }

    @Override
    public int bh_getPerkSurge() {
        return this.entityData.get(BH_PERK);
    }

    @Override
    public void bh_setPerkSurge(int packed) {
        bh_push(BH_PERK, packed);
    }

    @Override
    public int bh_getPulse() {
        return this.entityData.get(BH_PULSE);
    }

    @Override
    public void bh_setPulse(int packed) {
        bh_push(BH_PULSE, packed);
    }

    @Override
    public int bh_getCharge() {
        return this.entityData.get(BH_CHARGE);
    }

    @Override
    public void bh_setCharge(int fill) {
        bh_push(BH_CHARGE, fill);
    }

    @Override
    public boolean bh_isFreeLook() {
        return this.entityData.get(BH_FREE_LOOK);
    }

    @Override
    public void bh_setFreeLook(boolean freeLook) {
        bh_push(BH_FREE_LOOK, freeLook);
    }

    @Override
    public @Nullable HorseCartEntity bh_getCartEntity() {
        bh_features();
        return this.bh_cartRig.cart();
    }

    @Override
    public boolean bh_hasCartGear() {
        return this.entityData.get(BH_CART);
    }

    @Override
    public boolean bh_hasEnderChestGear() {
        return this.entityData.get(BH_ENDER_CHEST);
    }

    @Override
    public @Nullable UUID bh_getCartId() {
        return this.bh_cartId;
    }

    @Override
    public void bh_setCartId(@Nullable UUID id) {
        this.bh_cartId = id;
    }

    @Override
    public boolean bh_hasLargeCart() {
        return this.entityData.get(BH_CART_LARGE);
    }

    @Override
    public void bh_setLargeCart(boolean large) {
        bh_push(BH_CART_LARGE, large && bh_getBreed().archetype() == BreedArchetype.DRAFT);
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
        if (this.bh_cartChestContainer == null) {
            this.bh_cartChestContainer = new SimpleContainer(BH_CART_CHEST_SIZE);
        }
        return this.bh_cartChestContainer;
    }

    @Override
    public void bh_dropCartChest() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel) || !bh_hasCartChest()) {
            return;
        }
        bh_setCartChest(false);
        if (this.bh_cartChestContainer != null) {
            BhHorseStorage.dropContainerContents(self, serverLevel, this.bh_cartChestContainer);
        }
        self.spawnAtLocation(new ItemStack(Items.CHEST));
    }

    @Override
    public boolean bh_hasCartPlough() {
        return this.entityData.get(BH_CART_PLOW);
    }

    @Override
    public ItemStack bh_getCartPlough() {
        return this.bh_cartPlow;
    }

    @Override
    public void bh_setCartPlough(ItemStack hoe) {
        this.bh_cartPlow = hoe;
        bh_push(BH_CART_PLOW, !hoe.isEmpty());
    }

    @Override
    public void bh_dropCartPlough() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel) || this.bh_cartPlow.isEmpty()) {
            return;
        }
        ItemStack hoe = this.bh_cartPlow;
        bh_setCartPlough(ItemStack.EMPTY);
        self.spawnAtLocation(hoe);
    }

    @Override
    public void bh_ridePlayer(net.minecraft.world.entity.player.Player player) {
        this.doPlayerRide(player);
    }

    @Override
    public void bh_clearStanding() {
        ((AbstractHorse) (Object) this).setStanding(false);
        this.standCounter = 0;
    }

    @Override
    public boolean bh_hasAnyEquipment() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.isSaddled()) {
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
}
