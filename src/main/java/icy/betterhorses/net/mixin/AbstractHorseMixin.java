package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.HorseStabilizerLogic;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.item.HitchpostBlock;
import icy.betterhorses.net.goal.HorseFollowOwnerGoal;
import icy.betterhorses.net.goal.HorseReturnHomeGoal;
import icy.betterhorses.net.goal.HorseStayGoal;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private static final EntityDataAccessor<Optional<BlockPos>> BH_HITCHPOST_POS_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    @Unique private @Nullable UUID bh_owner = null;
    @Unique private HorseCommand bh_command = HorseCommand.FOLLOW;
    @Unique private @Nullable BlockPos bh_home = null;
    @Unique private @Nullable BlockPos bh_hitchpostPos = null;
    @Unique private @Nullable Vec3 bh_hitchAnchor = null;
    @Unique private int bh_bond = 0;
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

    @Unique
    private static final Identifier BH_SPEED_ID =
            Identifier.fromNamespaceAndPath("icys-better-horses", "bond_speed");
    @Unique
    private static final Identifier BH_JUMP_ID =
            Identifier.fromNamespaceAndPath("icys-better-horses", "bond_jump");
    @Unique private static final float BH_WATER_SPEED_MULTIPLIER = 1.5F;
    @Unique private static final double BH_WATER_RISE_SPEED = 0.006D;
    @Unique private static final double BH_WATER_SURFACE_SPEED = 0.001D;
    @Unique private static final double BH_WATER_MAX_RISE_SPEED = 0.015D;
    @Unique private static final float BH_HOOVES_FALL_DAMAGE_MULTIPLIER = 0.5F;
    @Unique private static final double BH_STABILIZER_HALF_OPEN_DESCENT_SPEED = -0.35D;
    @Unique private static final double BH_STABILIZER_MAX_DESCENT_SPEED = -0.125D;
    @Unique private static final double BH_STABILIZER_SMOOTHING = 0.35D;
    @Unique private static final double BH_STABILIZER_HALF_OPEN_SMOOTHING = 0.2D;
    @Unique private static final double BH_FRONT_PASSENGER_Z_OFFSET = 0.2D;
    @Unique private static final double BH_REAR_PASSENGER_Z_OFFSET = -0.55D;

    protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
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
    public @Nullable BlockPos bh_getHitchpostPos() {
        return bh_hitchpostPos;
    }

    @Override
    public void bh_setHitchpostPos(@Nullable BlockPos pos) {
        this.bh_hitchpostPos = pos == null ? null : pos.immutable();
        this.bh_hitchAnchor = this.bh_hitchpostPos == null
                ? null
                : ((AbstractHorse) (Object) this).position();
        this.entityData.set(BH_HITCHPOST_POS_SYNCED, Optional.ofNullable(this.bh_hitchpostPos));
    }

    @Override
    public int bh_getBond() {
        return this.entityData.get(BH_BOND_SYNCED);
    }

    @Override
    public void bh_setBond(int level) {
        this.bh_bond = Math.max(0, Math.min(100, level));
        this.entityData.set(BH_BOND_SYNCED, this.bh_bond);
        bh_applyBondAttributes();
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
        return chestGear.is(Items.CHEST);
    }

    @Override
    public void bh_onChestGearRemoved(ItemStack previousChestGear) {
        bh_dropChestContents();
    }

    @Override
    public void bh_onUpgradedSaddleRemoved(ItemStack previousSaddle) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        bh_dropContainerContents(self, serverLevel, bh_gearContainer);
        bh_dropChestContents();
        bh_syncGearFlags();
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void bh_defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BH_BOND_SYNCED, 0);
        builder.define(BH_STABILIZER_STATE_SYNCED, HorseStabilizerState.CLOSED.ordinal());
        builder.define(BH_GEAR_FLAGS_SYNCED, 0);
        builder.define(BH_HITCHPOST_POS_SYNCED, Optional.empty());
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void bh_onWrite(ValueOutput output, CallbackInfo ci) {
        if (bh_owner != null) {
            output.store("BH_Owner", UUIDUtil.CODEC, bh_owner);
        }
        output.putInt("BH_Command", bh_command.ordinal());
        output.putInt("BH_Bond", bh_bond);
        if (bh_home != null) {
            output.store("BH_Home", BlockPos.CODEC, bh_home);
        }
        if (bh_hitchpostPos != null) {
            output.store("BH_Hitchpost", BlockPos.CODEC, bh_hitchpostPos);
        }
        bh_writeContainer(output.list("BH_Gear", BhSlotEntry.CODEC), bh_gearContainer);
        bh_writeContainer(output.list("BH_Chest", BhSlotEntry.CODEC), bh_chestContainer);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void bh_onRead(ValueInput input, CallbackInfo ci) {
        bh_owner = input.read("BH_Owner", UUIDUtil.CODEC).orElse(null);
        if (bh_owner == null) {
            EntityReference<LivingEntity> ownerRef = ((AbstractHorse) (Object) this).getOwnerReference();
            bh_owner = ownerRef == null ? null : ownerRef.getUUID();
        }
        bh_command = HorseCommand.fromId(input.getIntOr("BH_Command", HorseCommand.FOLLOW.ordinal()));
        bh_bond = input.getIntOr("BH_Bond", 0);
        this.entityData.set(BH_BOND_SYNCED, bh_bond);
        bh_home = input.read("BH_Home", BlockPos.CODEC).orElse(null);
        bh_hitchpostPos = input.read("BH_Hitchpost", BlockPos.CODEC).orElse(null);
        if (bh_home == null) {
            bh_home = bh_readLegacyBlockPos(input, "BH_Home");
        }
        if (bh_hitchpostPos == null) {
            bh_hitchpostPos = bh_readLegacyBlockPos(input, "BH_Hitchpost");
        }
        bh_hitchAnchor = null;
        this.entityData.set(BH_HITCHPOST_POS_SYNCED, Optional.ofNullable(bh_hitchpostPos));
        bh_applyBondAttributes();
        bh_readContainer(input.listOrEmpty("BH_Gear", BhSlotEntry.CODEC), bh_gearContainer);
        bh_readContainer(input.listOrEmpty("BH_Chest", BhSlotEntry.CODEC), bh_chestContainer);
        bh_restoreUpgradedSaddle(input);
        bh_syncGearFlags();
        bh_hadUpgradedSaddle = this.bh_hasUpgradedSaddle();
    }

    @Unique
    private void bh_writeContainer(ValueOutput.TypedOutputList<BhSlotEntry> list, SimpleContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            list.add(new BhSlotEntry(i, stack));
        }
    }

    @Unique
    private void bh_readContainer(ValueInput.TypedInputList<BhSlotEntry> list, SimpleContainer container) {
        container.clearContent();
        for (BhSlotEntry entry : list) {
            int slot = entry.slot();
            if (slot < 0 || slot >= container.getContainerSize()) continue;
            container.setItem(slot, entry.stack());
        }
    }

    @Unique
    private void bh_restoreUpgradedSaddle(ValueInput input) {
        if (inventory == null || !inventory.getItem(0).isEmpty()) {
            return;
        }
        ItemStack saddle = input.read("SaddleItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (saddle.is(ModItems.UPGRADED_SADDLE)) {
            inventory.setItem(0, saddle);
        }
    }

    @Unique
    private static @Nullable BlockPos bh_readLegacyBlockPos(ValueInput input, String keyPrefix) {
        Optional<Integer> x = input.getInt(keyPrefix + "X");
        Optional<Integer> y = input.getInt(keyPrefix + "Y");
        Optional<Integer> z = input.getInt(keyPrefix + "Z");
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return null;
        }

        return new BlockPos(x.get(), y.get(), z.get());
    }

    /** Codec-friendly slot/stack pair used for {@code BH_Gear}/{@code BH_Chest} list entries. */
    @Unique
    public record BhSlotEntry(int slot, ItemStack stack) {
        public static final com.mojang.serialization.Codec<BhSlotEntry> CODEC =
                com.mojang.serialization.codecs.RecordCodecBuilder.create(instance -> instance.group(
                        com.mojang.serialization.Codec.INT.fieldOf("Slot").forGetter(BhSlotEntry::slot),
                        ItemStack.CODEC.fieldOf("Item").forGetter(BhSlotEntry::stack)
                ).apply(instance, BhSlotEntry::new));
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

    @Inject(method = "fedFood", at = @At("HEAD"))
    private void bh_markGoldenAppleFeed(net.minecraft.world.entity.player.Player player, ItemStack stack, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        this.bh_fedGoldenAppleThisTick = stack.is(Items.GOLDEN_APPLE);
    }

    @Inject(method = "fedFood", at = @At("RETURN"))
    private void bh_rewardGoldenAppleBond(net.minecraft.world.entity.player.Player player, ItemStack stack, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        try {
            AbstractHorse self = (AbstractHorse) (Object) this;
            if (!this.bh_fedGoldenAppleThisTick || self.level().isClientSide() || !cir.getReturnValue().consumesAction()) {
                return;
            }

            this.bh_setBond(this.bh_getBond() + 2);
        } finally {
            this.bh_fedGoldenAppleThisTick = false;
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

    @Inject(method = "tameWithName", at = @At("RETURN"))
    private void bh_claimHorseOnTame(net.minecraft.world.entity.player.Player player, CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (!cir.getReturnValueZ() || self.level().isClientSide() || player.getUUID().equals(this.bh_getOwner())) {
            return;
        }

        this.bh_setOwner(player.getUUID());
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys-better-horses.claimed"));
        }
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
                this.fallDistance = 0.0D;
            }
        }

        this.bh_setStabilizerState(state);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_tickHitchpost(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (this.bh_hitchpostPos == null) {
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
    private void bh_adjustFallDamage(double distance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (this.bh_hasStabilizerGear()) {
            HorseStabilizerState landingState = HorseStabilizerLogic.resolveLandingState(
                    true,
                    (float) distance,
                    this.bh_getStabilizerState());
            if (landingState == HorseStabilizerState.CLOSED) {
                return;
            }

            if (distance > 1.0D) {
                self.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
            }
            this.bh_setStabilizerState(landingState);
            this.fallDistance = 0.0D;
            cir.setReturnValue(false);
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
    }

    @Inject(method = "getPassengerAttachmentPoint", at = @At("RETURN"), cancellable = true)
    private void bh_offsetSecondPassenger(
            Entity passenger,
            net.minecraft.world.entity.EntityDimensions dimensions,
            float scaleFactor,
            CallbackInfoReturnable<Vec3> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.getPassengers().size() <= 1) {
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

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        if (this.getPassengers().isEmpty()) {
            return true;
        }

        return this.getPassengers().size() < 2
                && passenger instanceof net.minecraft.world.entity.player.Player;
    }

    @Unique
    private void bh_applyBondAttributes() {
        AbstractHorse self = (AbstractHorse) (Object) this;
        int bondLevel = Math.min(bh_bond / 20, 5);
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
        return this.bh_hasGear(GearSlot.HOOVES);
    }

    @Unique
    private boolean bh_hasStabilizerGear() {
        return this.bh_hasGear(GearSlot.STABILIZER);
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
                horse.spawnAtLocation(level, stack);
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

        this.entityData.set(BH_GEAR_FLAGS_SYNCED, flags);
    }
}
