package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseCommand;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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

    @Unique
    private static final EntityDataAccessor<Integer> BH_BOND_SYNCED =
            SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> BH_STABILIZER_STATE_SYNCED =
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
    @Unique private final SimpleContainer bh_gearContainer = new SimpleContainer(GearSlot.COUNT);
    @Unique private final SimpleContainer bh_chestContainer = new SimpleContainer(27);
    @Unique private boolean bh_hadUpgradedSaddle = false;
    @Unique private boolean bh_fedGoldenAppleThisTick = false;
    @Unique private boolean bh_usedNameTagThisInteract = false;
    @Unique private @Nullable Component bh_customNameBeforeInteract = null;

    @Unique
    private static final ResourceLocation BH_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath("icys-better-horses", "bond_speed");
    @Unique
    private static final ResourceLocation BH_JUMP_ID =
            ResourceLocation.fromNamespaceAndPath("icys-better-horses", "bond_jump");
    @Unique private static final float BH_WATER_SPEED_MULTIPLIER = 1.5F;
    @Unique private static final double BH_WATER_RISE_SPEED = 0.006D;
    @Unique private static final double BH_WATER_SURFACE_SPEED = 0.001D;
    @Unique private static final double BH_WATER_MAX_RISE_SPEED = 0.015D;
    @Unique private static final float BH_HOOVES_FALL_DAMAGE_MULTIPLIER = 0.5F;
    @Unique private static final float BH_STABILIZER_HALF_OPEN_FALL_DISTANCE = 4.0F;
    @Unique private static final float BH_STABILIZER_OPEN_FALL_DISTANCE = 4.0F;
    @Unique private static final double BH_STABILIZER_HALF_OPEN_DESCENT_SPEED = -0.35D;
    @Unique private static final double BH_STABILIZER_MAX_DESCENT_SPEED = -0.125D;
    @Unique private static final double BH_STABILIZER_SMOOTHING = 0.35D;
    @Unique private static final double BH_STABILIZER_HALF_OPEN_SMOOTHING = 0.2D;

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
        return chestGear.is(Items.CHEST) || chestGear.is(ModItems.HORSE_CHEST_GEAR);
    }

    @Override
    public void bh_onChestGearRemoved(ItemStack previousChestGear) {
        bh_dropChestContents();
    }

    @Override
    public void bh_onUpgradedSaddleRemoved(ItemStack previousSaddle) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (self.level().isClientSide()) return;
        bh_dropContainerContents(self, bh_gearContainer);
        bh_dropChestContents();
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void bh_defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BH_BOND_SYNCED, 0);
        builder.define(BH_STABILIZER_STATE_SYNCED, HorseStabilizerState.CLOSED.ordinal());
        builder.define(BH_HITCHPOST_POS_SYNCED, Optional.empty());
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void bh_onWrite(CompoundTag tag, CallbackInfo ci) {
        if (bh_owner != null) {
            tag.putUUID("BH_Owner", bh_owner);
        }
        tag.putInt("BH_Command", bh_command.ordinal());
        tag.putInt("BH_Bond", bh_bond);
        if (bh_home != null) {
            tag.putInt("BH_HomeX", bh_home.getX());
            tag.putInt("BH_HomeY", bh_home.getY());
            tag.putInt("BH_HomeZ", bh_home.getZ());
        }
        if (bh_hitchpostPos != null) {
            tag.putInt("BH_HitchpostX", bh_hitchpostPos.getX());
            tag.putInt("BH_HitchpostY", bh_hitchpostPos.getY());
            tag.putInt("BH_HitchpostZ", bh_hitchpostPos.getZ());
        }
        tag.put("BH_Gear", bh_writeContainer(bh_gearContainer));
        tag.put("BH_Chest", bh_writeContainer(bh_chestContainer));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void bh_onRead(CompoundTag tag, CallbackInfo ci) {
        bh_owner = tag.hasUUID("BH_Owner") ? tag.getUUID("BH_Owner") : null;
        bh_command = HorseCommand.fromId(tag.getInt("BH_Command"));
        bh_bond = tag.getInt("BH_Bond");
        this.entityData.set(BH_BOND_SYNCED, bh_bond);
        if (tag.contains("BH_HomeX")) {
            bh_home = new BlockPos(tag.getInt("BH_HomeX"), tag.getInt("BH_HomeY"), tag.getInt("BH_HomeZ"));
        }
        bh_hitchpostPos = tag.contains("BH_HitchpostX")
                ? new BlockPos(tag.getInt("BH_HitchpostX"), tag.getInt("BH_HitchpostY"), tag.getInt("BH_HitchpostZ"))
                : null;
        bh_hitchAnchor = null;
        this.entityData.set(BH_HITCHPOST_POS_SYNCED, Optional.ofNullable(bh_hitchpostPos));
        bh_applyBondAttributes();
        bh_readContainer(bh_gearContainer, tag.getList("BH_Gear", Tag.TAG_COMPOUND));
        bh_readContainer(bh_chestContainer, tag.getList("BH_Chest", Tag.TAG_COMPOUND));
        bh_restoreUpgradedSaddle(tag);
        bh_migrateLegacyGear();
        bh_hadUpgradedSaddle = this.bh_hasUpgradedSaddle();
    }

    @Unique
    private ListTag bh_writeContainer(SimpleContainer container) {
        ListTag list = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putByte("Slot", (byte) i);
            entry.put("Item", stack.save(registryAccess()));
            list.add(entry);
        }
        return list;
    }

    @Unique
    private void bh_readContainer(SimpleContainer container, ListTag list) {
        container.clearContent();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 0xFF;
            if (slot < 0 || slot >= container.getContainerSize()) continue;
            ItemStack stack = ItemStack.parse(registryAccess(), entry.getCompound("Item")).orElse(ItemStack.EMPTY);
            container.setItem(slot, stack);
        }
    }

    @Unique
    private void bh_restoreUpgradedSaddle(CompoundTag tag) {
        if (inventory == null || !inventory.getItem(0).isEmpty() || !tag.contains("SaddleItem", Tag.TAG_COMPOUND)) {
            return;
        }

        ItemStack saddle = ItemStack.parse(registryAccess(), tag.getCompound("SaddleItem")).orElse(ItemStack.EMPTY);
        if (saddle.is(ModItems.UPGRADED_SADDLE)) {
            inventory.setItem(0, saddle);
        }
    }

    @Inject(method = "createInventory", at = @At("TAIL"))
    private void bh_onCreateInventory(CallbackInfo ci) {
        this.bh_hadUpgradedSaddle = this.bh_hasUpgradedSaddle();
    }

    @Inject(method = "containerChanged", at = @At("TAIL"))
    private void bh_onContainerChanged(net.minecraft.world.Container container, CallbackInfo ci) {
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

    @Inject(method = "mobInteract", at = @At("HEAD"))
    private void bh_trackNameTagUse(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        ItemStack heldItem = player.getItemInHand(hand);
        this.bh_usedNameTagThisInteract = heldItem.is(Items.NAME_TAG);
        this.bh_customNameBeforeInteract = self.getCustomName();
    }

    @Inject(method = "mobInteract", at = @At("RETURN"))
    private void bh_rewardNameTagBond(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        try {
            AbstractHorse self = (AbstractHorse) (Object) this;
            if (!this.bh_usedNameTagThisInteract || self.level().isClientSide() || !cir.getReturnValue().consumesAction()) {
                return;
            }

            Component currentName = self.getCustomName();
            if (currentName != null && !currentName.equals(this.bh_customNameBeforeInteract)) {
                this.bh_setBond(this.bh_getBond() + 10);
            }
        } finally {
            this.bh_usedNameTagThisInteract = false;
            this.bh_customNameBeforeInteract = null;
        }
    }

    @Unique
    private void bh_migrateLegacyGear() {
        ItemStack chestGear = bh_gearContainer.getItem(GearSlot.CHEST.ordinal());
        if (chestGear.is(ModItems.HORSE_CHEST_GEAR)) {
            bh_gearContainer.setItem(GearSlot.CHEST.ordinal(), new ItemStack(Items.CHEST));
        }
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
                this.fallDistance = 0.0F;
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
    private void bh_adjustFallDamage(float distance, float damageMultiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        if (this.bh_hasStabilizerGear()) {
            if (distance > 1.0F) {
                self.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
            }
            this.bh_setStabilizerState(HorseStabilizerState.OPEN);
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
        if (self.level().isClientSide()) return;
        if (self.level() instanceof ServerLevel serverLevel && this.bh_hitchpostPos != null) {
            HitchpostBlock.releaseHorse(serverLevel, self, false);
        }
        bh_dropContainerContents(self, bh_gearContainer);
        bh_dropContainerContents(self, bh_chestContainer);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void bh_onRegisterGoals(CallbackInfo ci) {
        AbstractHorse self = (AbstractHorse) (Object) this;
        goalSelector.addGoal(3, new HorseStayGoal(self));
        goalSelector.addGoal(3, new HorseFollowOwnerGoal(self));
        goalSelector.addGoal(3, new HorseReturnHomeGoal(self));
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
        if (!this.bh_hasStabilizerGear()
                || horse.onGround()
                || horse.isInWater()
                || horse.isInLava()
                || horse.isPassenger()) {
            return HorseStabilizerState.CLOSED;
        }

        HorseStabilizerState currentState = this.bh_getStabilizerState();
        double verticalSpeed = horse.getDeltaMovement().y;
        if (currentState == HorseStabilizerState.OPEN) {
            return HorseStabilizerState.OPEN;
        }
        if (currentState == HorseStabilizerState.HALF_OPEN && verticalSpeed < -0.02D) {
            return this.fallDistance >= BH_STABILIZER_OPEN_FALL_DISTANCE
                    ? HorseStabilizerState.OPEN
                    : HorseStabilizerState.HALF_OPEN;
        }
        if (verticalSpeed >= -0.08D) {
            return HorseStabilizerState.CLOSED;
        }
        if (this.fallDistance >= BH_STABILIZER_OPEN_FALL_DISTANCE) {
            return HorseStabilizerState.OPEN;
        }
        if (this.fallDistance >= BH_STABILIZER_HALF_OPEN_FALL_DISTANCE) {
            return HorseStabilizerState.HALF_OPEN;
        }
        return HorseStabilizerState.CLOSED;
    }

    @Unique
    private boolean bh_hasHoovesGear() {
        return bh_gearContainer.getItem(GearSlot.HOOVES.ordinal()).is(ModItems.HORSE_HOOVES);
    }

    @Unique
    private boolean bh_hasStabilizerGear() {
        return bh_gearContainer.getItem(GearSlot.STABILIZER.ordinal()).is(ModItems.HORSE_STABILIZER);
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
        if (self.level().isClientSide()) return;
        bh_dropContainerContents(self, bh_chestContainer);
    }

    @Unique
    private void bh_dropContainerContents(AbstractHorse horse, SimpleContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                horse.spawnAtLocation(stack);
            }
        }
    }
}
