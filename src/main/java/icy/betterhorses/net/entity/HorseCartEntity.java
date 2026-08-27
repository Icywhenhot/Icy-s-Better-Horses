package icy.betterhorses.net.entity;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class HorseCartEntity extends Entity implements GeoEntity {

    public static final float WIDTH = 2.0F;
    public static final float HEIGHT = 1.5F;

    private static final double FOLLOW_OFFSET = 0.0D;
    private static final float YAW_OFFSET = 0.0F;

    private static final double BED_CENTER_BEHIND = 2.2D;
    private static final double BED_HALF_WIDTH = 0.95D;
    private static final double BED_HALF_LENGTH = 1.15D;
    private static final double BED_FLOOR_HEIGHT = 0.8125D;

    private static final double SEAT_HEIGHT = 1.15D;
    private static final double SEAT_BEHIND = 1.4D;
    private static final double SEAT_SIDE = 0.45D;

    private static final int REAR_SEAT_COUNT = 2;
    private static final int CHEST_SLOTS = 54;
    private static final float CART_BREAK_DAMAGE = 40.0F;
    private static final double REAR_SEAT_BEHIND = 2.55D;
    private static final double REAR_SEAT_SIDE = 0.45D;
    private static final double REAR_SEAT_HEIGHT = 0.75D;
    private static final float MAX_CARGO_WIDTH = EntityTypes.OAK_BOAT.getWidth();
    private static final double BOARD_SCAN_HEIGHT = 1.6D;
    private static final int RESTORE_BOARD_TICKS = 80;

    private static final double SPEED_SMOOTHING_UP = 0.12D;
    private static final int STOP_RAMP_TICKS = 16;
    private static final double STILL_SPEED = 0.02D;
    private static final int RESUME_TICKS = 2;
    private static final int SPEED_SYNC_STEPS = 128;
    private static final double REFERENCE_SPEED = 0.35D;
    private static final double MIN_ANIM_SPEED = 0.15D;
    private static final double MAX_ANIM_SPEED = 1.5D;

    private static final String WHEEL_ANIM_NAME = "wheel moving2";
    private static final String CHEST_OPEN_ANIM_NAME = "chest";
    private static final String CHEST_CLOSE_ANIM_NAME = "chest close";
    private static final String STANDING_ANIM_NAME = "stand alone";

    private static final RawAnimation WHEELS_ROLLING = RawAnimation.begin().thenLoop(WHEEL_ANIM_NAME);
    private static final RawAnimation CHEST_OPENING = RawAnimation.begin().thenPlayAndHold(CHEST_OPEN_ANIM_NAME);
    private static final RawAnimation CHEST_CLOSING = RawAnimation.begin().thenPlayAndHold(CHEST_CLOSE_ANIM_NAME);
    private static final RawAnimation STANDING = RawAnimation.begin().thenLoop(STANDING_ANIM_NAME);

    private static final EntityDataAccessor<Integer> DATA_HORSE_ID =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CHEST_OPEN =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_ROLL_SPEED =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_PLACED =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_CHEST =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private @Nullable UUID horseUuid;
    private @Nullable AbstractHorse horse;

    private int cargoRestoreDeadline = RESTORE_BOARD_TICKS;

    private final List<ServerPlayer> chestViewers = new ArrayList<>();
    private final SimpleContainer placedChest = new SimpleContainer(CHEST_SLOTS);
    private float damageTaken;
    private boolean chestAnimPrimed = false;

    private double prevX;
    private double prevZ;

    private double smoothedSpeed;
    private double coastFromSpeed;
    private int coastTicks;
    private int movingTicks;

    public HorseCartEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.prevX = this.getX();
        this.prevZ = this.getZ();
    }

    public boolean isPlaced() {
        return this.entityData.get(DATA_PLACED);
    }

    public static @Nullable HorseCartEntity place(ServerLevel level, Vec3 pos, float yaw) {
        HorseCartEntity cart = new HorseCartEntity(ModEntities.HORSE_CART, level);
        cart.entityData.set(DATA_PLACED, true);
        cart.setNoGravity(false);
        cart.setYRot(yaw);
        cart.setYBodyRot(yaw);
        cart.setYHeadRot(yaw);
        cart.setPos(pos.x, pos.y, pos.z);
        if (!level.noCollision(cart)) {
            return null;
        }
        if (!level.addFreshEntity(cart)) {
            return null;
        }
        cart.playSound(SoundEvents.WOOD_PLACE, 1.0F, 1.0F);
        return cart;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(ModItems.HORSE_CART);
    }

    public static @Nullable HorseCartEntity spawnFor(AbstractHorse horse) {
        if (!(horse.level() instanceof ServerLevel level)) {
            return null;
        }
        HorseCartEntity cart = new HorseCartEntity(ModEntities.HORSE_CART, level);
        cart.bindTo(horse);
        cart.followHorse(horse);
        return level.addFreshEntity(cart) ? cart : null;
    }

    public void bindTo(AbstractHorse boundHorse) {
        this.horse = boundHorse;
        this.horseUuid = boundHorse.getUUID();
        this.entityData.set(DATA_HORSE_ID, boundHorse.getId());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            AbstractHorse boundHorse = this.clientHorse();
            if (boundHorse != null) {
                this.glueToHorse(boundHorse);
            }
            this.updateClientSpeed();
            return;
        }

        if (this.isPlaced()) {
            this.settleOnGround();
            this.updateChestViewers();
            return;
        }

        AbstractHorse boundHorse = this.resolveHorse();
        if (boundHorse == null || !boundHorse.isAlive() || boundHorse.isRemoved()
                || !IHorseData.of(boundHorse).bh_hasCartGear()) {
            this.closeChestViewers();
            this.discard();
            return;
        }
        this.followHorse(boundHorse);
        this.updateRollSpeed();
        this.entityData.set(DATA_HAS_CHEST, IHorseData.of(boundHorse).bh_hasCartChest());
        this.updateChestViewers();
        this.tryBoardNearbyMobs();
        this.tendPassengers();
    }

    private void settleOnGround() {
        if (this.onGround()) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        this.setDeltaMovement(0.0D, Math.max(this.getDeltaMovement().y - 0.04D, -0.5D), 0.0D);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    private void tendPassengers() {
        for (Entity passenger : this.getPassengers()) {
            setSeatedPose(passenger, true);
            disarmPassenger(passenger);
        }
        for (Entity passenger : this.benchCargo()) {
            setSeatedPose(passenger, true);
            disarmPassenger(passenger);
        }
    }

    private static void disarmPassenger(Entity passenger) {
        if (!(passenger instanceof Mob mob)) {
            return;
        }
        mob.setTarget(null);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }

    private void unloadPassengers() {
        this.cargoRestoreDeadline = 0;
        for (Entity passenger : List.copyOf(this.getPassengers())) {
            this.setDown(passenger);
        }
        for (Entity passenger : List.copyOf(this.benchCargo())) {
            this.setDown(passenger);
        }
    }

    public void setDown(Entity passenger) {
        passenger.stopRiding();
        setSeatedPose(passenger, false);
        Vec3 beside = this.position().add(
                new Vec3(this.getBbWidth() * 0.5D + 0.6D, 0.0D, 0.0D)
                        .yRot(-this.getYRot() * ((float) Math.PI / 180.0F)));
        passenger.teleportTo(beside.x, this.getY(), beside.z);
    }

    private boolean restoringCargo() {
        return this.tickCount <= this.cargoRestoreDeadline;
    }

    private static void setSeatedPose(Entity passenger, boolean seated) {
        if (passenger instanceof TamableAnimal tamable) {
            tamable.setInSittingPose(seated || tamable.isOrderedToSit());
        } else if (passenger instanceof Fox fox) {
            fox.setSitting(seated);
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide()) {
            setSeatedPose(passenger, false);
        }
    }

    private void updateRollSpeed() {
        double dx = this.getX() - this.prevX;
        double dz = this.getZ() - this.prevZ;
        this.prevX = this.getX();
        this.prevZ = this.getZ();

        if (this.tickCount <= 1) {
            return;
        }

        float speed = Math.round(Math.sqrt(dx * dx + dz * dz) * SPEED_SYNC_STEPS) / (float) SPEED_SYNC_STEPS;
        this.entityData.set(DATA_ROLL_SPEED, speed);
    }

    private void updateClientSpeed() {
        double instant = this.entityData.get(DATA_ROLL_SPEED);

        this.movingTicks = instant >= STILL_SPEED ? Math.min(this.movingTicks + 1, RESUME_TICKS) : 0;

        if (this.movingTicks >= RESUME_TICKS) {
            this.smoothedSpeed += (instant - this.smoothedSpeed) * SPEED_SMOOTHING_UP;
            this.coastFromSpeed = this.smoothedSpeed;
            this.coastTicks = 0;
            return;
        }

        if (this.coastTicks < STOP_RAMP_TICKS) {
            this.coastTicks++;
        }
        this.smoothedSpeed = this.coastTicks >= STOP_RAMP_TICKS
                ? 0.0D
                : this.coastFromSpeed * (1.0D - (double) this.coastTicks / STOP_RAMP_TICKS);
    }

    private void followHorse(AbstractHorse boundHorse) {
        float yaw = boundHorse.getYRot() + YAW_OFFSET;
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        Vec3 target = cartPosFor(boundHorse.getX(), boundHorse.getY(), boundHorse.getZ(), boundHorse.getYRot());
        this.setPos(target.x, target.y, target.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void glueToHorse(AbstractHorse boundHorse) {
        InterpolationHandler interpolation = this.getInterpolation();
        if (interpolation != null) {
            interpolation.cancel();
        }

        this.followHorse(boundHorse);

        Vec3 previous = cartPosFor(boundHorse.xo, boundHorse.yo, boundHorse.zo, boundHorse.yRotO);
        this.xo = previous.x;
        this.yo = previous.y;
        this.zo = previous.z;
        this.yRotO = boundHorse.yRotO + YAW_OFFSET;
    }

    private static Vec3 cartPosFor(double horseX, double horseY, double horseZ, float horseYaw) {
        double rad = Math.toRadians(horseYaw);
        return new Vec3(
                horseX - Math.sin(rad) * FOLLOW_OFFSET,
                horseY,
                horseZ + Math.cos(rad) * FOLLOW_OFFSET);
    }

    public @Nullable Vec3 gluedRenderPosition(float partialTick) {
        AbstractHorse boundHorse = this.clientHorse();
        if (boundHorse == null) {
            return null;
        }
        Vec3 horsePos = boundHorse.getPosition(partialTick);
        return cartPosFor(horsePos.x, horsePos.y, horsePos.z, renderBodyYaw(boundHorse, partialTick));
    }

    public float gluedRenderYaw(float partialTick) {
        AbstractHorse boundHorse = this.clientHorse();
        return boundHorse == null ? this.getYRot() : renderBodyYaw(boundHorse, partialTick) + YAW_OFFSET;
    }

    private static float renderBodyYaw(AbstractHorse boundHorse, float partialTick) {
        return Mth.rotLerp(partialTick, boundHorse.yBodyRotO, boundHorse.yBodyRot);
    }

    private @Nullable AbstractHorse resolveHorse() {
        if (this.horse != null && this.horse.isAlive() && !this.horse.isRemoved()) {
            return this.horse;
        }
        if (this.horseUuid != null && this.level() instanceof ServerLevel serverLevel
                && serverLevel.getEntity(this.horseUuid) instanceof AbstractHorse resolved) {
            this.horse = resolved;
            this.entityData.set(DATA_HORSE_ID, resolved.getId());
            return resolved;
        }
        return null;
    }

    private @Nullable AbstractHorse clientHorse() {
        int id = this.entityData.get(DATA_HORSE_ID);
        return id != -1 && this.level().getEntity(id) instanceof AbstractHorse boundHorse
                ? boundHorse
                : null;
    }

    private @Nullable AbstractHorse boundHorse() {
        return this.level().isClientSide() ? this.clientHorse() : this.horse;
    }

    public static Vec3 benchSeatOffset(int seatIndex, float horseYaw) {
        double side = seatIndex <= 0 ? -SEAT_SIDE : SEAT_SIDE;
        return new Vec3(side, SEAT_HEIGHT, FOLLOW_OFFSET - SEAT_BEHIND)
                .yRot(-horseYaw * ((float) Math.PI / 180.0F));
    }

    private static Vec3 carriageSeatOffset(int seatIndex, float cartYaw) {
        double side = seatIndex <= 0 ? -REAR_SEAT_SIDE : REAR_SEAT_SIDE;
        return new Vec3(side, REAR_SEAT_HEIGHT, -REAR_SEAT_BEHIND)
                .yRot(-cartYaw * ((float) Math.PI / 180.0F));
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        int seatIndex = Math.max(0, this.getPassengers().indexOf(passenger));
        return carriageSeatOffset(seatIndex, this.getYRot());
    }

    private boolean canCarry(Entity candidate) {
        if (candidate instanceof Player) {
            return true;
        }
        if (candidate == this.boundHorse()) {
            return false;
        }
        return isCarriableCargo(candidate);
    }

    public static boolean isCarriableCargo(Entity candidate) {
        return candidate instanceof LivingEntity
                && !(candidate instanceof Player)
                && !(candidate instanceof AbstractHorse)
                && candidate.getBbWidth() < MAX_CARGO_WIDTH
                && !candidate.is(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS);
    }

    private AABB boardScanBox() {
        AABB bed = this.getBoundingBox();
        return new AABB(bed.minX, bed.minY, bed.minZ, bed.maxX, bed.minY + BOARD_SCAN_HEIGHT, bed.maxZ)
                .inflate(0.1D);
    }

    private void tryBoardNearbyMobs() {
        AbstractHorse boundHorse = this.resolveHorse();
        if (!this.rearSeatsFree() && !this.benchSeatFree(boundHorse)) {
            return;
        }
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, boardScanBox())) {
            if (!this.rearSeatsFree() && !this.benchSeatFree(boundHorse)) {
                break;
            }
            if (candidate instanceof Player
                    || candidate.isPassenger()
                    || candidate.isVehicle()
                    || !candidate.isAlive()
                    || !this.canCarry(candidate)) {
                continue;
            }
            if (this.rearSeatsFree()) {
                candidate.startRiding(this, this.restoringCargo(), true);
            } else {
                candidate.startRiding(boundHorse, false, true);
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation) {
        boolean clientSide = this.level().isClientSide();
        ItemStack held = player.getItemInHand(hand);

        if (held.is(Items.CHEST) && !this.hasChest()) {
            if (clientSide) {
                return InteractionResult.SUCCESS;
            }
            return this.attachChest(player, held) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (held.is(Items.SHEARS) && this.hasChest()) {
            if (clientSide) {
                return InteractionResult.SUCCESS;
            }
            this.shearChest(player, hand);
            return InteractionResult.CONSUME;
        }

        if (player.isSecondaryUseActive()) {
            if (this.hasChest()) {
                if (clientSide) {
                    return InteractionResult.SUCCESS;
                }
                this.openChestMenu(player);
                return InteractionResult.CONSUME;
            }
            if (this.getPassengers().isEmpty() && this.benchCargo().isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!clientSide) {
                this.unloadPassengers();
            }
            return InteractionResult.SUCCESS;
        }
        if (clientSide) {
            return this.clientHorse() != null ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        AbstractHorse boundHorse = this.resolveHorse();
        if (boundHorse == null) {
            return InteractionResult.PASS;
        }

        boolean benchHasRoom = boundHorse.getPassengers().size() < 2;
        if (benchHasRoom && this.playerMayTakeBench(boundHorse, player)) {
            IHorseData.of(boundHorse).bh_ridePlayer(player);
            return InteractionResult.CONSUME;
        }
        if (this.rearSeatsFree()) {
            player.startRiding(this);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public boolean hasChest() {
        return this.entityData.get(DATA_HAS_CHEST);
    }

    private @Nullable SimpleContainer chestContainer() {
        if (this.isPlaced()) {
            return this.placedChest;
        }
        AbstractHorse boundHorse = this.resolveHorse();
        return boundHorse == null ? null : IHorseData.of(boundHorse).bh_getCartChestContainer();
    }

    private void setChestAttached(boolean attached) {
        if (!this.isPlaced()) {
            AbstractHorse boundHorse = this.resolveHorse();
            if (boundHorse != null) {
                IHorseData.of(boundHorse).bh_setCartChest(attached);
            }
        }
        this.entityData.set(DATA_HAS_CHEST, attached);
    }

    private boolean attachChest(Player player, ItemStack held) {
        if (!this.playerMayHandleCargo(player)) {
            return false;
        }

        this.unloadPassengers();
        this.setChestAttached(true);
        held.consume(1, player);
        this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, 1.0F);
        return true;
    }

    private void shearChest(Player player, InteractionHand hand) {
        SimpleContainer contents = this.chestContainer();
        if (contents == null || !this.playerMayHandleCargo(player)) {
            return;
        }

        if (!contents.isEmpty()) {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.icys-better-horses.cart_chest_not_empty"));
            }
            return;
        }

        this.closeChestViewers();
        this.dropChest();
        player.getItemInHand(hand).hurtAndBreak(1, player, hand);
        this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
    }

    private void dropChest() {
        if (!this.hasChest() || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (!this.isPlaced()) {
            AbstractHorse boundHorse = this.resolveHorse();
            if (boundHorse != null) {
                IHorseData.of(boundHorse).bh_dropCartChest();
            }
            this.entityData.set(DATA_HAS_CHEST, false);
            return;
        }

        this.setChestAttached(false);
        for (int slot = 0; slot < this.placedChest.getContainerSize(); slot++) {
            ItemStack stack = this.placedChest.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(level, stack);
            }
        }
        this.spawnAtLocation(level, new ItemStack(Items.CHEST));
    }

    private void openChestMenu(Player player) {
        SimpleContainer contents = this.chestContainer();
        if (contents == null || !this.playerMayHandleCargo(player)) {
            return;
        }

        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, opener) -> ChestMenu.sixRows(containerId, inventory, contents),
                this.getDisplayName()));
        if (player instanceof ServerPlayer serverPlayer && isViewing(serverPlayer, contents)) {
            this.chestViewers.add(serverPlayer);
        }
    }

    private void updateChestViewers() {
        if (!this.chestViewers.isEmpty()) {
            SimpleContainer contents = this.chestContainer();
            this.chestViewers.removeIf(viewer -> !isViewing(viewer, contents));
        }
        this.setChestOpen(!this.chestViewers.isEmpty());
    }

    private void closeChestViewers() {
        for (ServerPlayer viewer : List.copyOf(this.chestViewers)) {
            viewer.closeContainer();
        }
        this.chestViewers.clear();
        this.setChestOpen(false);
    }

    private void setChestOpen(boolean open) {
        if (open == this.entityData.get(DATA_CHEST_OPEN)) {
            return;
        }
        this.entityData.set(DATA_CHEST_OPEN, open);
        this.playSound(open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, 0.5F, 1.0F);
    }

    private static boolean isViewing(Player viewer, @Nullable SimpleContainer contents) {
        return contents != null
                && viewer.isAlive()
                && !viewer.isRemoved()
                && viewer.containerMenu instanceof ChestMenu menu
                && menu.getContainer() == contents;
    }

    private boolean playerMayHandleCargo(Player player) {
        AbstractHorse boundHorse = this.resolveHorse();
        if (boundHorse == null || !BhConfig.horseExclusivityEnabled()) {
            return true;
        }
        if (IHorseData.of(boundHorse).bh_mayHandle(player.getUUID())) {
            return true;
        }

        boundHorse.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.icys-better-horses.not_inventory_owner"));
        }
        return false;
    }

    private boolean playerMayTakeBench(AbstractHorse boundHorse, Player player) {
        if (!BhConfig.horseExclusivityEnabled()) {
            return true;
        }
        IHorseData data = IHorseData.of(boundHorse);
        if (data.bh_maySaddleUp(player.getUUID())) {
            return true;
        }
        List<Entity> passengers = boundHorse.getPassengers();
        return !passengers.isEmpty() && data.bh_maySaddleUp(passengers.get(0).getUUID());
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.rearSeatsFree() && this.canCarry(passenger);
    }

    private boolean rearSeatsFree() {
        return !this.isPlaced() && !this.hasChest() && this.getPassengers().size() < REAR_SEAT_COUNT;
    }

    private boolean benchSeatFree(@Nullable AbstractHorse boundHorse) {
        if (boundHorse == null) {
            return false;
        }
        List<Entity> passengers = boundHorse.getPassengers();
        return passengers.size() == 1 && passengers.get(0) instanceof Player;
    }

    private List<Entity> benchCargo() {
        AbstractHorse boundHorse = this.level().isClientSide() ? this.clientHorse() : this.resolveHorse();
        if (boundHorse == null) {
            return List.of();
        }
        return boundHorse.getPassengers().stream().filter(passenger -> !(passenger instanceof Player)).toList();
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return null;
    }

    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        double rad = Math.toRadians(this.getYRot());
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double centerX = pos.x + sin * BED_CENTER_BEHIND;
        double centerZ = pos.z - cos * BED_CENTER_BEHIND;

        double absSin = Math.abs(sin);
        double absCos = Math.abs(cos);
        double halfX = BED_HALF_LENGTH * absSin + BED_HALF_WIDTH * absCos;
        double halfZ = BED_HALF_LENGTH * absCos + BED_HALF_WIDTH * absSin;

        return new AABB(
                centerX - halfX, pos.y, centerZ - halfZ,
                centerX + halfX, pos.y + BED_FLOOR_HEIGHT, centerZ + halfZ);
    }

    @Override
    public boolean canBeCollidedWith(Entity entity) {
        if (entity == null) {
            return true;
        }
        AbstractHorse bound = this.boundHorse();
        if (bound != null && (entity == bound || entity.getVehicle() == bound)) {
            return false;
        }
        return !this.hasPassenger(entity);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return this.canBeCollidedWith(entity);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (!this.isPlaced() || this.isRemoved()) {
            return false;
        }

        boolean instant = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
        this.damageTaken += amount * 10.0F;
        this.markHurt();
        if (instant || this.damageTaken > CART_BREAK_DAMAGE) {
            this.breakIntoItems(level, !instant);
        }
        return true;
    }

    private void breakIntoItems(ServerLevel level, boolean dropCart) {
        this.closeChestViewers();
        this.dropChest();
        if (dropCart) {
            this.spawnAtLocation(level, new ItemStack(ModItems.HORSE_CART));
        }
        this.playSound(SoundEvents.WOOD_BREAK, 1.0F, 1.0F);
        this.discard();
    }

    @Override
    public boolean shouldBeSaved() {
        return this.isPlaced() || !this.getPassengers().isEmpty();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HORSE_ID, -1);
        builder.define(DATA_CHEST_OPEN, false);
        builder.define(DATA_ROLL_SPEED, 0.0F);
        builder.define(DATA_PLACED, false);
        builder.define(DATA_HAS_CHEST, false);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.entityData.set(DATA_PLACED, input.getBooleanOr("BhPlaced", false));
        if (this.isPlaced()) {
            this.setNoGravity(false);
        }
        this.entityData.set(DATA_HAS_CHEST, input.getBooleanOr("BhHasChest", false));
        this.damageTaken = input.getFloatOr("BhDamage", 0.0F);
        this.placedChest.clearContent();
        for (BhCartSlot entry : input.listOrEmpty("BhChestItems", BhCartSlot.CODEC)) {
            if (entry.slot() >= 0 && entry.slot() < this.placedChest.getContainerSize()) {
                this.placedChest.setItem(entry.slot(), entry.stack());
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putBoolean("BhPlaced", this.isPlaced());
        output.putBoolean("BhHasChest", this.hasChest());
        output.putFloat("BhDamage", this.damageTaken);
        ValueOutput.TypedOutputList<BhCartSlot> items = output.list("BhChestItems", BhCartSlot.CODEC);
        for (int slot = 0; slot < this.placedChest.getContainerSize(); slot++) {
            ItemStack stack = this.placedChest.getItem(slot);
            if (!stack.isEmpty()) {
                items.add(new BhCartSlot(slot, stack));
            }
        }
    }

    public record BhCartSlot(int slot, ItemStack stack) {
        public static final Codec<BhCartSlot> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.fieldOf("Slot").forGetter(BhCartSlot::slot),
                        ItemStack.CODEC.fieldOf("Item").forGetter(BhCartSlot::stack)
                ).apply(instance, BhCartSlot::new));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("wheels", 0, this::wheelPredicate));
        controllers.add(new AnimationController<>("chest", 0, this::chestPredicate));
        controllers.add(new AnimationController<>("pose", 0, this::posePredicate));
    }

    private PlayState posePredicate(AnimationTest<HorseCartEntity> test) {
        if (!this.isPlaced()) {
            if (test.controller().getCurrentRawAnimation() != null) {
                test.controller().reset();
            }
            return PlayState.STOP;
        }
        return test.setAndContinue(STANDING);
    }

    private PlayState chestPredicate(AnimationTest<HorseCartEntity> test) {
        if (!this.hasChest()) {
            this.chestAnimPrimed = false;
            if (test.controller().getCurrentRawAnimation() != null) {
                test.controller().reset();
            }
            return PlayState.STOP;
        }
        if (this.entityData.get(DATA_CHEST_OPEN)) {
            this.chestAnimPrimed = true;
            return test.setAndContinue(CHEST_OPENING);
        }
        if (!this.chestAnimPrimed) {
            return PlayState.STOP;
        }
        return test.setAndContinue(CHEST_CLOSING);
    }

    private PlayState wheelPredicate(AnimationTest<HorseCartEntity> test) {
        if (this.smoothedSpeed <= 0.0D) {
            test.setControllerSpeed(0.0F);
            return PlayState.STOP;
        }
        double floor = this.coastTicks > 0 ? 0.0D : MIN_ANIM_SPEED;
        test.setControllerSpeed(
                (float) Mth.clamp(this.smoothedSpeed / REFERENCE_SPEED, floor, MAX_ANIM_SPEED));
        return test.setAndContinue(WHEELS_ROLLING);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
