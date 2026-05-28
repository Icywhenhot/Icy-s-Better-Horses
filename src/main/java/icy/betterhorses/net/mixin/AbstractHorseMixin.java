package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.HorseGender;
import icy.betterhorses.net.HorseStabilizerLogic;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModAttachments;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.item.HitchpostBlock;
import icy.betterhorses.net.goal.HorseFollowOwnerGoal;
import icy.betterhorses.net.goal.HorseReturnHomeGoal;
import icy.betterhorses.net.goal.HorseStayGoal;
import icy.betterhorses.net.goal.HorseWanderBoundsGoal;
import icy.betterhorses.net.inventory.BhSlotEntry;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements IHorseData {

    @Shadow
    protected SimpleContainer inventory;

    @Shadow
    protected abstract void doPlayerRide(net.minecraft.world.entity.player.Player player);

    @Unique private @Nullable UUID bh_owner = null;
    @Unique private HorseCommand bh_command = HorseCommand.FOLLOW;
    @Unique private @Nullable BlockPos bh_home = null;
    @Unique private @Nullable BlockPos bh_wanderCenter = null;
    @Unique private @Nullable BlockPos bh_hitchpostPos = null;
    @Unique private @Nullable Vec3 bh_hitchAnchor = null;
    @Unique private boolean bh_nameTagBondReceived = false;
    @Unique private final ModAttachments.BhHorseSyncState bh_syncState = new ModAttachments.BhHorseSyncState();
    @Unique
    private final SimpleContainer bh_gearContainer = new SimpleContainer(GearSlot.COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            AbstractHorseMixin.this.bh_syncGearFlags();
        }
    };
    @Unique private final SimpleContainer bh_chestContainer = new SimpleContainer(27);
    @Unique private boolean bh_hadUpgradedSaddle = false;
    @Unique private boolean bh_fedGoldenAppleThisTick = false;
    @Unique private @Nullable Vec3 bh_lastFrostWalkerPos = null;

    @Unique
    private static final ResourceLocation BH_SPEED_ID =
            new ResourceLocation("icys_better_horses", "bond_speed");
    @Unique
    private static final ResourceLocation BH_JUMP_ID =
            new ResourceLocation("icys_better_horses", "bond_jump");
    @Unique private static final float BH_HOOVES_FALL_DAMAGE_MULTIPLIER = 0.5F;
    @Unique private static final double BH_STABILIZER_HALF_OPEN_DESCENT_SPEED = -0.35D;
    @Unique private static final double BH_STABILIZER_MAX_DESCENT_SPEED = -0.125D;
    @Unique private static final double BH_STABILIZER_SMOOTHING = 0.35D;
    @Unique private static final double BH_STABILIZER_HALF_OPEN_SMOOTHING = 0.2D;
    // Horse bbox is 1.39625 wide (±0.698 from center). The 2nd-passenger player hitbox is
    // ±0.3 around their attachment point, so any rear offset more negative than -0.398 pushes
    // the rear of their hitbox past the horse's bbox — when the horse backs into a wall, the
    // rider clips into the block and takes in-wall (suffocation) damage. -0.35 keeps the rear
    // edge at -0.65, leaving ~0.05 of buffer against the horse's rear edge. Front offset is
    // mirrored for visual balance and to keep the 1st passenger symmetric with the 2nd.
    @Unique private static final double BH_FRONT_PASSENGER_Z_OFFSET = 0.35D;
    @Unique private static final double BH_REAR_PASSENGER_Z_OFFSET = -0.35D;
    @Unique private static final float BH_FREE_CAMERA_ANGLE_THRESHOLD = 90.0F;
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

    protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void bh_defineSynchedData(CallbackInfo ci) {
        this.entityData.define(BH_BOND, this.bh_syncState.bond);
        this.entityData.define(BH_STABILIZER_STATE, this.bh_syncState.stabilizerStateId);
        this.entityData.define(BH_GEAR_FLAGS, this.bh_syncState.gearFlags);
        this.entityData.define(BH_GENDER_ID, this.bh_syncState.genderId);
        this.entityData.define(BH_BREED_ID, this.bh_syncState.breedId);
        this.entityData.define(BH_BREED_MIXED, this.bh_syncState.breedMixed);
    }

    @Override
    public @Nullable UUID bh_getOwner() {
        return bh_owner;
    }

    @Override
    public void bh_setOwner(@Nullable UUID owner) {
        this.bh_owner = owner;
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
        return bh_command;
    }

    @Override
    public void bh_setCommand(HorseCommand command) {
        this.bh_command = command;
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
        this.bh_hitchAnchor = this.bh_hitchpostPos == null
                ? null
                : ((AbstractHorse) (Object) this).position();
    }

    @Override
    public int bh_getBond() {
        return this.entityData.get(BH_BOND);
    }

    @Override
    public void bh_setBond(int level) {
        this.bh_syncState.bond = Math.max(0, Math.min(100, level));
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
        this.bh_syncState.genderId = gender.ordinal();
        this.bh_syncHorseData();
    }

    @Override
    public HorseBreed bh_getBreed() {
        return HorseBreed.fromId(this.entityData.get(BH_BREED_ID));
    }

    @Override
    public void bh_setBreed(HorseBreed breed) {
        this.bh_syncState.breedId = breed.ordinal();
        this.bh_syncHorseData();
    }

    @Override
    public boolean bh_isMixedBreed() {
        return this.entityData.get(BH_BREED_MIXED);
    }

    @Override
    public void bh_setMixedBreed(boolean mixed) {
        this.bh_syncState.breedMixed = mixed;
        this.bh_syncHorseData();
    }

    @Override
    public HorseStabilizerState bh_getStabilizerState() {
        return HorseStabilizerState.fromId(this.entityData.get(BH_STABILIZER_STATE));
    }

    @Override
    public void bh_setStabilizerState(HorseStabilizerState state) {
        this.bh_syncState.stabilizerStateId = state.ordinal();
        this.bh_syncHorseData();
    }

    @Override
    public int bh_getGearFlags() {
        return this.entityData.get(BH_GEAR_FLAGS);
    }

    @Override
    public boolean bh_hasUpgradedSaddle() {
        return inventory != null && inventory.getItem(0).is(ModItems.UPGRADED_SADDLE);
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
        bh_writeBlockPos(output, "BH_Home", bh_home);
        bh_writeBlockPos(output, "BH_WanderCenter", bh_wanderCenter);
        bh_writeBlockPos(output, "BH_Hitchpost", bh_hitchpostPos);
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
        bh_home = bh_readBlockPos(input, "BH_Home");
        bh_wanderCenter = bh_readBlockPos(input, "BH_WanderCenter");
        bh_hitchpostPos = bh_readBlockPos(input, "BH_Hitchpost");
        if (bh_home == null) bh_home = bh_readLegacyBlockPos(input, "BH_Home");
        if (bh_wanderCenter == null) bh_wanderCenter = bh_readLegacyBlockPos(input, "BH_WanderCenter");
        if (bh_hitchpostPos == null) bh_hitchpostPos = bh_readLegacyBlockPos(input, "BH_Hitchpost");
        bh_hitchAnchor = null;
        bh_applyBondAttributes();
        bh_readContainer(input, "BH_Gear", bh_gearContainer);
        bh_readContainer(input, "BH_Chest", bh_chestContainer);
        bh_restoreUpgradedSaddle(input);
        bh_syncGearFlags();
        bh_hadUpgradedSaddle = this.bh_hasUpgradedSaddle();

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
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void bh_assignTraitsOnSpawn(net.minecraft.world.level.ServerLevelAccessor level,
                                        net.minecraft.world.DifficultyInstance difficulty,
                                        net.minecraft.world.entity.MobSpawnType reason,
                                        @Nullable net.minecraft.world.entity.SpawnGroupData groupData,
                                        CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> cir) {
        // Always randomize gender on fresh spawn — default int 0 doesn't distinguish "unset" from MALE.
        this.bh_syncState().genderId = this.random.nextBoolean() ? 0 : 1;

        if (this.bh_getBreed() != HorseBreed.UNKNOWN_SPECIES) {
            return;
        }

        // Real horses are handled by HorseFinalizeSpawnMixin so we can read the original
        // BhHorseGroupData passed from sibling spawns (vanilla Horse.finalizeSpawn clobbers
        // its groupData arg before super, so we can't see the wrapper from here).
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
        HorseBreed picked = HorseBreed.MUSTANG; // fallback for unmapped coats
        if (self instanceof Horse horse) {
            java.util.List<HorseBreed> matches = HorseBreed.breedsMatchingCoat(horse.getVariant(), horse.getMarkings());
            if (!matches.isEmpty()) {
                picked = matches.get(this.random.nextInt(matches.size()));
            }
        }
        this.bh_syncState().breedId = picked.ordinal();
        this.bh_syncState().breedMixed = false;
        // Intentionally do NOT touch the coat — pre-existing horses keep the look they had.
    }

    @Unique
    private ListTag bh_writeContainer(SimpleContainer container) {
        ListTag list = new ListTag();
        Codec<BhSlotEntry> codec = BhSlotEntry.CODEC;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            codec.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), new BhSlotEntry(i, stack))
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
            codec.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), t)
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
        ItemStack saddle = ItemStack.parse(this.registryAccess(), input.getCompound("SaddleItem")).orElse(ItemStack.EMPTY);
        if (saddle.is(ModItems.UPGRADED_SADDLE)) {
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
        this.bh_hadUpgradedSaddle = this.bh_hasUpgradedSaddle();
        this.bh_syncGearFlags();
    }

    /**
     * 1.21.11 dropped {@code AbstractHorse.containerChanged(Container)} (the old
     * {@code ContainerListener} hook). Watch for upgraded-saddle removal from a tick poll instead.
     * Cheap: one item-slot check per horse per tick on the server.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_pollUpgradedSaddleRemoval(CallbackInfo ci) {
        if (((AbstractHorse) (Object) this).level().isClientSide()) {
            return;
        }
        boolean hasUpgradedSaddle = this.bh_hasUpgradedSaddle();
        if (this.bh_hadUpgradedSaddle && !hasUpgradedSaddle) {
            this.bh_onUpgradedSaddleRemoved(ItemStack.EMPTY);
        }
        this.bh_hadUpgradedSaddle = hasUpgradedSaddle;
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

            // If horse just entered love mode and a same-gender horse is already in love nearby, cancel and warn.
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
                                "message.icys_better_horses.same_gender_breed"));
                    }
                }
            }
        } finally {
            this.bh_fedGoldenAppleThisTick = false;
        }
    }

    /**
     * Block non-owners from becoming the primary rider of an owned horse. A non-owner is allowed
     * to mount only when the owner is already the primary rider (the 2-rider scenario the
     * second-passenger feature enables). Wild/untamed horses fall through to vanilla so taming
     * still works. We hook {@code doPlayerRide} rather than {@code mobInteract} because vanilla,
     * commands like {@code /ride}, and some other mods all funnel through this method — gating
     * here covers every path. Server-side only: clients don't have authoritative owner state.
     */
    @Inject(method = "doPlayerRide", at = @At("HEAD"), cancellable = true)
    private void bh_gateOwnerOnlyMount(net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide() || !BhConfig.horseExclusivityEnabled()) return;
        UUID owner = this.bh_getOwner();
        if (owner == null || owner.equals(player.getUUID())) return;
        if (bh_ownerIsPrimaryPassenger(self, owner)) return;
        self.playSound(net.minecraft.sounds.SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys_better_horses.not_owner"));
        }
        // Belt-and-suspenders force-eject — covers the case where another mod/path already
        // attached the player as a passenger before our gate ran, or where the client
        // optimistically predicted a mount. Idempotent if they aren't actually riding.
        if (player.getVehicle() == self) {
            player.stopRiding();
        }
        ci.cancel();
    }

    /**
     * Catch-all: if at any tick the primary rider isn't the owner of an owned horse, eject
     * every passenger. Covers owner-dismount-while-friend-was-secondary (friend slides into the
     * primary slot), forced mounts from plugins/datapacks, and any future path we don't gate
     * explicitly at mount time. Cheap — only runs when the horse is being ridden.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_enforceOwnerPrimaryRider(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide()) return;
        java.util.List<Entity> passengers = self.getPassengers();
        if (passengers.isEmpty()) return;

        if (!BhConfig.multiRidingEnabled() && passengers.size() > 1) {
            for (int i = 1; i < passengers.size(); i++) {
                passengers.get(i).stopRiding();
            }
            passengers = self.getPassengers();
            if (passengers.isEmpty()) {
                return;
            }
        }

        if (!BhConfig.horseExclusivityEnabled()) {
            return;
        }

        UUID owner = this.bh_getOwner();
        if (owner == null) return;
        Entity primary = passengers.get(0);
        if (!(primary instanceof net.minecraft.world.entity.player.Player)) return;
        if (primary.getUUID().equals(owner)) return;
        self.playSound(net.minecraft.sounds.SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        for (Entity passenger : new java.util.ArrayList<>(passengers)) {
            passenger.stopRiding();
        }
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
        // Owner data only exists server-side. Let the client run vanilla's doPlayerRide body.
        if (self.level().isClientSide()) {
            return;
        }

        // Defense in depth: even if HEAD-cancel from bh_gateOwnerOnlyMount didn't suppress
        // this injector for some mixin-ordering reason, never mount a non-owner here.
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
        this.bh_setWanderCommand(self.blockPosition());
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys_better_horses.claimed"));
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
            serverPlayer.sendSystemMessage(Component.translatable("message.icys_better_horses.not_inventory_owner"));
        }
        ci.cancel();
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void bh_allowSecondPlayerRider(
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand,
            CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        // Ctrl+rightclick fires both our radial-open packet AND vanilla's interact packet.
        // The radial packet arrives first and arms a suppression flag; consume it here so the
        // mount/inventory/heldItem branches below never run for that click.
        if (!self.level().isClientSide()
                && HorseTracker.consumeInteractSuppression(player.getUUID(), self.getId())) {
            cir.setReturnValue(net.minecraft.world.InteractionResult.CONSUME);
            return;
        }
        if (!self.isVehicle()
                || self.isBaby()
                || self.hasPassenger(player)
                || self.getPassengers().size() >= 2) {
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_tickStabilizer(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        HorseStabilizerState state = this.bh_computeStabilizerState(self);

        if (state == HorseStabilizerState.OPEN || state == HorseStabilizerState.HALF_OPEN) {
            Vec3 motion = self.getDeltaMovement();
            double targetSpeed = state == HorseStabilizerState.OPEN
                    ? BH_STABILIZER_MAX_DESCENT_SPEED
                    : BH_STABILIZER_HALF_OPEN_DESCENT_SPEED;
            double smoothing = state == HorseStabilizerState.OPEN
                    ? BH_STABILIZER_SMOOTHING
                    : BH_STABILIZER_HALF_OPEN_SMOOTHING;

            if (motion.y < targetSpeed) {
                double smoothedY = Mth.lerp(smoothing, motion.y, targetSpeed);
                if (smoothedY > targetSpeed) {
                    smoothedY = targetSpeed;
                }
                self.setDeltaMovement(motion.x, smoothedY, motion.z);
                self.hurtMarked = true;
            }
            if (state == HorseStabilizerState.OPEN) {
                this.fallDistance = 0.0F;
            }
        }

        this.bh_setStabilizerState(state);
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_freezeWaterWithFrostWalkerHooves(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        Vec3 currentPos = self.position();
        Vec3 previousPos = this.bh_lastFrostWalkerPos;
        this.bh_lastFrostWalkerPos = currentPos;

        if (!(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int frostWalkerLevel = this.bh_getHoovesFrostWalkerLevel();
        if (frostWalkerLevel <= 0 || self.isInLava() || (!self.onGround() && !self.isInWater())) {
            return;
        }

        if (previousPos == null
                || previousPos.distanceToSqr(currentPos) > BH_FROST_WALKER_RESET_DISTANCE * BH_FROST_WALKER_RESET_DISTANCE) {
            previousPos = currentPos;
        }

        this.bh_applyFrostWalkerTrail(serverLevel, previousPos, currentPos, frostWalkerLevel);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_tickHitchpost(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (this.bh_hitchpostPos == null) {
            return;
        }

        if (!BhConfig.hitchpostEnabled()) {
            if (self.level() instanceof ServerLevel serverLevel) {
                HitchpostBlock.releaseHorse(serverLevel, self, true);
            }
            return;
        }

        if (self.level() instanceof ServerLevel serverLevel
                && !HitchpostBlock.isValidTether(serverLevel, self, this.bh_hitchpostPos)) {
            HitchpostBlock.releaseHorse(serverLevel, self, true);
            return;
        }

        if (this.bh_hitchAnchor == null) {
            this.bh_hitchAnchor = self.position();
        }

        this.bh_applyHitchpostConstraint(self);
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
        if (this.bh_hitchpostPos != null) {
            HitchpostBlock.releaseHorse(level, self, false);
        }
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
    }

    @Inject(method = "getPassengerAttachmentPoint", at = @At("RETURN"), cancellable = true)
    private void bh_offsetSecondPassenger(
            Entity passenger,
            net.minecraft.world.entity.EntityDimensions dimensions,
            float scaleFactor,
            CallbackInfoReturnable<Vec3> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!BhConfig.multiRidingEnabled() || self.getPassengers().size() <= 1) {
            return;
        }

        int passengerIndex = self.getPassengers().indexOf(passenger);
        if (passengerIndex < 0) {
            return;
        }

        double zOffset = passengerIndex == 0 ? BH_FRONT_PASSENGER_Z_OFFSET : BH_REAR_PASSENGER_Z_OFFSET;
        Vec3 offset = new Vec3(0.0D, 0.0D, zOffset).yRot(-self.getYRot() * ((float) Math.PI / 180.0F));
        cir.setReturnValue(cir.getReturnValue().add(offset));
    }

    @Inject(method = "getRiddenRotation", at = @At("HEAD"), cancellable = true)
    private void bh_allowMountedFreeCamera(LivingEntity rider, CallbackInfoReturnable<Vec2> cir) {
        if (!(rider instanceof net.minecraft.world.entity.player.Player player)
                || player.xxa != 0.0F
                || player.zza != 0.0F) {
            return;
        }

        AbstractHorse self = (AbstractHorse) (Object) this;
        float playerYRot = Mth.wrapDegrees(player.getYRot());
        float rotationDifference = Mth.wrapDegrees(playerYRot - self.getYRot());

        if (Math.abs(rotationDifference) > BH_FREE_CAMERA_ANGLE_THRESHOLD) {
            float horseYRot = Mth.wrapDegrees(
                    playerYRot - Math.signum(rotationDifference) * BH_FREE_CAMERA_ANGLE_THRESHOLD);
            cir.setReturnValue(new Vec2(player.getXRot() * 0.5F, horseYRot));
            return;
        }

        cir.setReturnValue(new Vec2(player.getXRot() * 0.5F, self.getYRot()));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        java.util.List<Entity> passengers = this.getPassengers();
        boolean multiRidingEnabled = BhConfig.multiRidingEnabled();
        boolean horseExclusivityEnabled = BhConfig.horseExclusivityEnabled();
        if (passengers.size() >= (multiRidingEnabled ? 2 : 1)) {
            return false;
        }

        UUID owner = this.bh_getOwner();
        if (owner == null || !horseExclusivityEnabled) {
            if (passengers.isEmpty()) {
                return true;
            }
            return multiRidingEnabled && passenger instanceof net.minecraft.world.entity.player.Player;
        }

        if (!(passenger instanceof net.minecraft.world.entity.player.Player player)) {
            return false;
        }
        boolean isOwner = owner.equals(player.getUUID());
        if (passengers.isEmpty()) {
            return isOwner;
        }
        if (!multiRidingEnabled) {
            return false;
        }
        if (isOwner) {
            return true;
        }
        return passengers.get(0).getUUID().equals(owner);
    }

    @Unique
    private static boolean bh_ownerIsPrimaryPassenger(AbstractHorse horse, UUID owner) {
        java.util.List<Entity> passengers = horse.getPassengers();
        return !passengers.isEmpty() && passengers.get(0).getUUID().equals(owner);
    }

    @Unique
    private void bh_applyBondAttributes() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        int bondLevel = Math.min(this.bh_getBond() / 20, 5);
        double bonus = bondLevel * 0.15;

        AttributeInstance speed = self.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(BH_SPEED_ID);
            if (bondLevel > 0) {
                speed.addTransientModifier(new AttributeModifier(
                        BH_SPEED_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        AttributeInstance jump = self.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) {
            jump.removeModifier(BH_JUMP_ID);
            if (bondLevel > 0) {
                jump.addTransientModifier(new AttributeModifier(
                        BH_JUMP_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    @Unique
    private HorseStabilizerState bh_computeStabilizerState(AbstractHorse horse) {
        return HorseStabilizerLogic.computeState(
                this.bh_hasStabilizerGear(),
                horse.onGround(),
                horse.isInWater(),
                horse.isInLava(),
                horse.isPassenger(),
                horse.getDeltaMovement().y,
                (float) this.fallDistance,
                this.bh_getStabilizerState());
    }

    @Unique
    private boolean bh_hasHoovesGear() {
        return BhConfig.hoovesEnabled() && this.bh_hasGear(GearSlot.HOOVES);
    }

    @Unique
    private boolean bh_hasStabilizerGear() {
        return BhConfig.stabilizerEnabled() && this.bh_hasGear(GearSlot.STABILIZER);
    }

    @Unique
    private int bh_getHoovesFrostWalkerLevel() {
        if (!BhConfig.hoovesEnabled()) {
            return 0;
        }
        ItemStack hooves = this.bh_gearContainer.getItem(GearSlot.HOOVES.ordinal());
        if (hooves.isEmpty()) {
            return 0;
        }

        for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>> entry
                : hooves.getEnchantments().entrySet()) {
            if (entry.getKey().is(Enchantments.FROST_WALKER)) {
                return entry.getIntValue();
            }
        }

        return 0;
    }

    @Unique
    private void bh_applyFrostWalkerTrail(ServerLevel level, Vec3 start, Vec3 end, int frostWalkerLevel) {
        int radius = Math.min(16, 3 + frostWalkerLevel);
        double dx = end.x - start.x;
        double dz = end.z - start.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDistance < 1.0E-6D) {
            this.bh_freezeWaterAtSample(level, end, radius);
            return;
        }
        int samples = Math.max(1, Mth.ceil(horizontalDistance / BH_FROST_WALKER_SAMPLE_STEP));

        for (int i = 0; i <= samples; i++) {
            double progress = (double) i / (double) samples;
            this.bh_freezeWaterAtSample(level, new Vec3(
                    Mth.lerp(progress, start.x, end.x),
                    Mth.lerp(progress, start.y, end.y),
                    Mth.lerp(progress, start.z, end.z)),
                    radius);
        }
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
    private void bh_applyHitchpostConstraint(AbstractHorse horse) {
        if (this.bh_hitchAnchor == null) {
            return;
        }

        horse.getNavigation().stop();
        Vec3 currentPos = horse.position();
        double horizontalDistanceSq = (currentPos.x - this.bh_hitchAnchor.x) * (currentPos.x - this.bh_hitchAnchor.x)
                + (currentPos.z - this.bh_hitchAnchor.z) * (currentPos.z - this.bh_hitchAnchor.z);
        if (horizontalDistanceSq > 0.04D || Math.abs(currentPos.y - this.bh_hitchAnchor.y) > 1.25D) {
            horse.teleportTo(this.bh_hitchAnchor.x, this.bh_hitchAnchor.y, this.bh_hitchAnchor.z);
        }

        horse.setDeltaMovement(Vec3.ZERO);
        horse.hurtMarked = true;
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
    }

    @Unique
    private ModAttachments.BhHorseSyncState bh_syncState() {
        return this.bh_syncState;
    }

    @Unique
    private void bh_syncHorseData() {
        this.entityData.set(BH_BOND, this.bh_syncState.bond);
        this.entityData.set(BH_STABILIZER_STATE, this.bh_syncState.stabilizerStateId);
        this.entityData.set(BH_GEAR_FLAGS, this.bh_syncState.gearFlags);
        this.entityData.set(BH_GENDER_ID, this.bh_syncState.genderId);
        this.entityData.set(BH_BREED_ID, this.bh_syncState.breedId);
        this.entityData.set(BH_BREED_MIXED, this.bh_syncState.breedMixed);
    }
}
