package icy.betterhorses.net.entity;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.BhHorseSteering;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import icy.betterhorses.net.inventory.CartChestMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class HorseCartEntity extends Entity implements GeoEntity {

    public static final float WIDTH = 2.0F;
    public static final float HEIGHT = 1.5F;

    private static final double FOLLOW_OFFSET = 0.0D;
    private static final float YAW_OFFSET = 0.0F;

    private static final double BED_HALF_WIDTH = 0.95D;
    private static final double BED_FLOOR_HEIGHT = 0.8125D;

    private static final double SEAT_BEHIND = 1.4D;
    private static final double SEAT_SIDE = 0.45D;

    private static final int CHEST_SLOTS = CartChestMenu.SLOTS;
    private static final float CART_BREAK_DAMAGE = 40.0F;
    private static final double REAR_SEAT_SIDE = 0.45D;
    private static final double REAR_SEAT_HEIGHT = 0.75D;
    private static final float MAX_CARGO_WIDTH = EntityType.BOAT.getWidth();
    private static final double BOARD_SCAN_HEIGHT = 1.6D;
    private static final int RESTORE_BOARD_TICKS = 80;

    private static final TagKey<Block> PLOUGHABLE = TagKey.create(Registries.BLOCK,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "ploughable"));
    private static final TagKey<EntityType<?>> CARGO_BLOCKED = TagKey.create(Registries.ENTITY_TYPE,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "cart_cargo_blocked"));
    private static final TagKey<EntityType<?>> CARGO_ALLOWED = TagKey.create(Registries.ENTITY_TYPE,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "cart_cargo_allowed"));

    private static final double PLOW_BEHIND = 4.2D;
    private static final int PLOW_HALF_WIDTH = 1;
    private static final int PLOW_LIFT = 1;

    private static final double SPEED_SMOOTHING_UP = 0.12D;
    private static final int STOP_RAMP_TICKS = 16;
    private static final double STILL_SPEED = 0.02D;
    private static final int RESUME_TICKS = 2;
    private static final int SPEED_SYNC_STEPS = 128;
    private static final double REFERENCE_SPEED = 0.35D;
    private static final double MIN_ANIM_SPEED = 0.15D;
    private static final double MAX_ANIM_SPEED = 1.5D;

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
    private static final EntityDataAccessor<Boolean> DATA_HAS_PLOW =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_LARGE =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private @Nullable UUID horseUuid;
    private @Nullable AbstractHorse horse;

    private int cargoRestoreDeadline = RESTORE_BOARD_TICKS;

    private final List<ServerPlayer> chestViewers = new ArrayList<>();
    private final SimpleContainer placedChest = new SimpleContainer(CHEST_SLOTS);
    private ItemStack placedPlow = ItemStack.EMPTY;
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

    public CartSize size() {
        return CartSize.byLarge(this.entityData.get(DATA_LARGE));
    }

    public void setSize(CartSize size) {
        if (size == this.size()) {
            return;
        }
        this.entityData.set(DATA_LARGE, size.isLarge());
        this.setBoundingBox(this.makeBoundingBox());
        this.dropOverflowPassengers();
        this.closeChestViewers();
    }

    private void dropOverflowPassengers() {
        int seats = this.rearCapacity();
        List<Entity> riders = this.getPassengers();
        for (int i = riders.size() - 1; i >= seats; i--) {
            this.setDown(riders.get(i));
        }
    }

    public static boolean itemsBeyond(@Nullable SimpleContainer contents, int slots) {
        if (contents == null) {
            return false;
        }
        for (int slot = slots; slot < contents.getContainerSize(); slot++) {
            if (!contents.getItem(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private int rearCapacity() {
        return this.size().rearSeats(this.hasChest());
    }

    public @Nullable Component resizeRefusal(CartSize size) {
        if (size == this.size()) {
            return null;
        }
        if (this.getPassengers().size() > size.rearSeats(this.hasChest())) {
            return Component.translatable("message.icys-better-horses.cart_size_passengers");
        }
        if (this.hasPlough() && !size.takesPlough()) {
            return Component.translatable("message.icys-better-horses.cart_size_plough");
        }
        if (this.hasChest() && itemsBeyond(this.chestContainer(), size.chestSlots())) {
            return Component.translatable("message.icys-better-horses.cart_size_chest_full");
        }
        if (this.isPlaced()
                && !this.level().noCollision(this, boxFor(size, this.position(), this.getYRot()))) {
            return Component.translatable("message.icys-better-horses.cart_size_blocked");
        }
        return null;
    }

    public static HorseCartEntity preview(Level level, CartSize size) {
        HorseCartEntity cart = new HorseCartEntity(ModEntities.HORSE_CART.get(), level);
        cart.setId(-1);
        cart.entityData.set(DATA_PLACED, true);
        cart.entityData.set(DATA_LARGE, size.isLarge());
        return cart;
    }

    public static @Nullable HorseCartEntity place(ServerLevel level, Vec3 pos, float yaw, CartSize size) {
        HorseCartEntity cart = new HorseCartEntity(ModEntities.HORSE_CART.get(), level);
        cart.entityData.set(DATA_PLACED, true);
        cart.entityData.set(DATA_LARGE, size.isLarge());
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
        return new ItemStack(ModItems.HORSE_CART.get());
    }

    public static @Nullable HorseCartEntity spawnFor(AbstractHorse horse) {
        if (!(horse.level() instanceof ServerLevel level)) {
            return null;
        }
        HorseCartEntity cart = new HorseCartEntity(ModEntities.HORSE_CART.get(), level);
        cart.entityData.set(DATA_LARGE, IHorseData.of(horse).bh_hasLargeCart());
        cart.bindTo(horse);
        cart.followHorse(horse);
        return level.addFreshEntity(cart) ? cart : null;
    }

    public void bindTo(AbstractHorse boundHorse) {
        this.horse = boundHorse;
        this.horseUuid = boundHorse.getUUID();
        this.entityData.set(DATA_HORSE_ID, boundHorse.getId());
        IHorseData.of(boundHorse).bh_setCartId(this.getUUID());
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
        if (boundHorse == null && this.horseUuid != null) {
            this.closeChestViewers();
            if (this.horse != null && this.horse.getRemovalReason() != null
                    && this.horse.getRemovalReason().shouldDestroy()) this.discard();
            return;
        }
        if (boundHorse == null || !boundHorse.isAlive() || boundHorse.isRemoved()
                || !IHorseData.of(boundHorse).bh_hasCartGear()) {
            this.closeChestViewers();
            this.discard();
            return;
        }
        this.followHorse(boundHorse);
        this.updateRollSpeed();
        IHorseData data = IHorseData.of(boundHorse);
        if (data.bh_getCartId() != null && !getUUID().equals(data.bh_getCartId())
                && level() instanceof ServerLevel serverLevel
                && serverLevel.getEntity(data.bh_getCartId()) instanceof HorseCartEntity replacement) {
            for (Entity passenger : List.copyOf(getPassengers())) passenger.startRiding(replacement, true);
            discard();
            return;
        }
        data.bh_setCartId(getUUID());
        this.entityData.set(DATA_HAS_CHEST, data.bh_hasCartChest());
        this.entityData.set(DATA_HAS_PLOW, data.bh_hasCartPlough());
        this.setSize(CartSize.byLarge(data.bh_hasLargeCart()));
        this.tillGround();
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
        float yaw = boundHorse.yBodyRot + YAW_OFFSET;
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        Vec3 target = cartPosFor(boundHorse.getX(), boundHorse.getY(), boundHorse.getZ(), boundHorse.yBodyRot);
        this.setPos(target.x, target.y, target.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void glueToHorse(AbstractHorse boundHorse) {
        this.followHorse(boundHorse);

        Vec3 previous = cartPosFor(boundHorse.xo, boundHorse.yo, boundHorse.zo, boundHorse.yBodyRotO);
        this.xo = this.xOld = previous.x;
        this.yo = this.yOld = previous.y;
        this.zo = this.zOld = previous.z;
        this.yRotO = boundHorse.yBodyRotO + YAW_OFFSET;
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

    public @Nullable AbstractHorse boundHorse() {
        return this.level().isClientSide() ? this.clientHorse() : this.horse;
    }

    public static Vec3 benchSeatOffset(AbstractHorse boundHorse, int seatIndex, float horseYaw) {
        double side = benchShared(boundHorse) ? (seatIndex <= 0 ? -SEAT_SIDE : SEAT_SIDE) : 0.0D;
        double up = CartSize.byLarge(IHorseData.of(boundHorse).bh_hasLargeCart()).benchHeight();
        return new Vec3(side, up, FOLLOW_OFFSET - SEAT_BEHIND)
                .yRot(-horseYaw * ((float) Math.PI / 180.0F));
    }

    private static boolean benchShared(AbstractHorse boundHorse) {
        return boundHorse.getPassengers().size() > 1;
    }

    private Vec3 carriageSeatOffset(int seatIndex, float cartYaw) {
        CartSize size = this.size();
        seatIndex += size.rearSeatCount() - this.rearCapacity();
        double side = seatIndex % 2 == 0 ? -REAR_SEAT_SIDE : REAR_SEAT_SIDE;
        double rowShift = seatIndex < 2 ? -size.rearRowSpacing() : size.rearRowSpacing();
        return new Vec3(side, REAR_SEAT_HEIGHT, -(size.rearSeatBehind() + rowShift))
                .yRot(-cartYaw * ((float) Math.PI / 180.0F));
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        int seatIndex = Math.max(0, this.getPassengers().indexOf(passenger));
        Vec3 seat = this.carriageSeatOffset(seatIndex, this.getYRot());
        move.accept(passenger, this.getX() + seat.x, this.getY() + seat.y, this.getZ() + seat.z);
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
        if (!(candidate instanceof LivingEntity)
                || candidate instanceof Player
                || candidate instanceof AbstractHorse
                || candidate.getType().is(CARGO_BLOCKED)) {
            return false;
        }
        return candidate.getType().is(CARGO_ALLOWED) || candidate.getBbWidth() < MAX_CARGO_WIDTH;
    }

    private AABB boardScanBox() {
        AABB bed = this.getBoundingBox();
        return new AABB(bed.minX, bed.minY, bed.minZ, bed.maxX, bed.minY + BOARD_SCAN_HEIGHT, bed.maxZ)
                .inflate(0.1D);
    }

    private void tryBoardNearbyMobs() {
        if (!BhConfig.cartPickupEnabled()) {
            return;
        }
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
                candidate.startRiding(this, this.restoringCargo());
            } else {
                candidate.startRiding(boundHorse, false);
            }
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitLocation, InteractionHand hand) {
        boolean clientSide = this.level().isClientSide();
        ItemStack held = player.getItemInHand(hand);

        if (held.is(ItemTags.HOES) && !this.hasPlough() && this.size().takesPlough()) {
            if (clientSide) {
                return InteractionResult.SUCCESS;
            }
            return this.attachPlough(player, held) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (held.is(Items.CHEST) && !this.hasChest()) {
            if (clientSide) {
                return InteractionResult.SUCCESS;
            }
            return this.attachChest(player, held) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (held.is(Items.SHEARS) && (this.hasPlough() || this.hasChest())) {
            if (clientSide) {
                return InteractionResult.SUCCESS;
            }
            if (this.hasPlough()) {
                this.shearPlough(player, hand);
            } else {
                this.shearChest(player, hand);
            }
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

        boolean benchHasRoom = boundHorse.getPassengers().size()
                < BhHorseSteering.bh_seatCount(IHorseData.of(boundHorse));
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

    public boolean hasPlough() {
        return this.entityData.get(DATA_HAS_PLOW);
    }

    private ItemStack ploughItem() {
        if (this.isPlaced()) {
            return this.placedPlow;
        }
        AbstractHorse boundHorse = this.resolveHorse();
        return boundHorse == null ? ItemStack.EMPTY : IHorseData.of(boundHorse).bh_getCartPlough();
    }

    private void setPlough(ItemStack hoe) {
        if (this.isPlaced()) {
            this.placedPlow = hoe;
        } else {
            AbstractHorse boundHorse = this.resolveHorse();
            if (boundHorse != null) {
                IHorseData.of(boundHorse).bh_setCartPlough(hoe);
            }
        }
        this.entityData.set(DATA_HAS_PLOW, !hoe.isEmpty());
    }

    private boolean attachPlough(Player player, ItemStack held) {
        if (!this.playerMayHandleCargo(player)) {
            return false;
        }
        this.setPlough(held.copyWithCount(1));
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        this.playSound(SoundEvents.ARMOR_EQUIP_IRON, 1.0F, 1.0F);
        return true;
    }

    private void shearPlough(Player player, InteractionHand hand) {
        if (!this.playerMayHandleCargo(player)) {
            return;
        }
        this.dropPlough();
        player.getItemInHand(hand).hurtAndBreak(1, player,
                broken -> broken.broadcastBreakEvent(hand));
        this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
    }

    private void dropPlough() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack hoe = this.ploughItem();
        if (hoe.isEmpty()) {
            return;
        }
        this.setPlough(ItemStack.EMPTY);
        this.spawnAtLocation(hoe);
    }

    private void tillGround() {
        if (!(this.level() instanceof ServerLevel level) || !this.hasPlough()) {
            return;
        }
        if (this.entityData.get(DATA_ROLL_SPEED) < STILL_SPEED) {
            return;
        }
        ItemStack hoe = this.ploughItem();
        if (hoe.isEmpty()) {
            return;
        }

        AbstractHorse boundHorse = this.resolveHorse();
        LivingEntity driver = boundHorse == null ? null : boundHorse.getControllingPassenger();

        float rad = -this.getYRot() * ((float) Math.PI / 180.0F);
        int turned = 0;
        for (int lane = -PLOW_HALF_WIDTH; lane <= PLOW_HALF_WIDTH; lane++) {
            Vec3 spot = this.position().add(new Vec3(lane, 0.0D, -PLOW_BEHIND).yRot(rad));
            BlockPos furrow = BlockPos.containing(spot.x, this.getY(), spot.z).below();
            for (int lift = PLOW_LIFT; lift >= 0; lift--) {
                if (this.turnOver(level, furrow.above(lift), driver)) {
                    turned++;
                    break;
                }
            }
        }
        if (turned == 0) {
            return;
        }

        this.playSound(SoundEvents.HOE_TILL, 1.0F, 1.0F);
        if (hoe.hurt(turned, this.random, driver instanceof ServerPlayer sp ? sp : null)) {
            hoe.shrink(1);
        }
        if (hoe.isEmpty()) {
            this.setPlough(ItemStack.EMPTY);
            this.playSound(SoundEvents.ITEM_BREAK, 0.8F, 0.9F);
        }
    }

    private boolean turnOver(ServerLevel level, BlockPos pos, @Nullable LivingEntity driver) {
        BlockState ground = level.getBlockState(pos);
        if (!tillable(ground) || !level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        if (driver instanceof ServerPlayer sp && !level.mayInteract(sp, pos)) {
            return false;
        }
        if (ground.is(Blocks.ROOTED_DIRT)) {
            Block.popResource(level, pos, new ItemStack(Items.HANGING_ROOTS));
        }
        level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
        return true;
    }

    private static boolean tillable(BlockState state) {
        return state.is(PLOUGHABLE);
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

        this.setChestAttached(true);
        this.dropOverflowPassengers();
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
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
        player.getItemInHand(hand).hurtAndBreak(1, player,
                broken -> broken.broadcastBreakEvent(hand));
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
                this.spawnAtLocation(stack);
            }
        }
        this.spawnAtLocation(new ItemStack(Items.CHEST));
    }

    private void openChestMenu(Player player) {
        SimpleContainer contents = this.chestContainer();
        if (contents == null || !this.playerMayHandleCargo(player)) {
            return;
        }

        boolean wide = this.size().isLarge();
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, opener) -> wide
                        ? new CartChestMenu(containerId, inventory, contents) {
                            @Override public boolean stillValid(Player viewer) { return mayKeepChestOpen(viewer); }
                        }
                        : new ChestMenu(net.minecraft.world.inventory.MenuType.GENERIC_9x6, containerId, inventory, contents, 6) {
                            @Override public boolean stillValid(Player viewer) { return mayKeepChestOpen(viewer); }
                        },
                this.getDisplayName()));
        if (player instanceof ServerPlayer serverPlayer && isViewing(serverPlayer, contents)) {
            this.chestViewers.add(serverPlayer);
        }
    }

    private void updateChestViewers() {
        if (!this.chestViewers.isEmpty()) {
            SimpleContainer contents = this.chestContainer();
            this.chestViewers.removeIf(viewer -> {
                if (!isViewing(viewer, contents)) return true;
                if (mayKeepChestOpen(viewer)) return false;
                viewer.closeContainer();
                return true;
            });
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

    private boolean mayKeepChestOpen(Player player) {
        if (!this.isAlive() || !player.isAlive() || player.level() != level()
                || player.distanceToSqr(this) > 64.0D || !hasChest()) return false;
        if (isPlaced()) return true;
        AbstractHorse horse = resolveHorse();
        return horse != null && horse.isAlive() && (!BhConfig.horseExclusivityEnabled()
                || IHorseData.of(horse).bh_mayHandle(player.getUUID()));
    }

    private void setChestOpen(boolean open) {
        if (open == this.entityData.get(DATA_CHEST_OPEN)) {
            return;
        }
        this.entityData.set(DATA_CHEST_OPEN, open);
        this.playSound(open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, 0.5F, 1.0F);
    }

    private static boolean isViewing(Player viewer, @Nullable SimpleContainer contents) {
        if (contents == null || !viewer.isAlive() || viewer.isRemoved()) {
            return false;
        }
        if (viewer.containerMenu instanceof CartChestMenu wide) {
            return wide.getContainer() == contents;
        }
        return viewer.containerMenu instanceof ChestMenu menu && menu.getContainer() == contents;
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
        return !this.isPlaced() && this.getPassengers().size() < this.rearCapacity();
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
    protected AABB makeBoundingBox() {
        return boxFor(this.size(), this.position(), this.getYRot());
    }

    private static AABB boxFor(CartSize size, Vec3 pos, float yaw) {
        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double centerX = pos.x + sin * size.bedCenterBehind();
        double centerZ = pos.z - cos * size.bedCenterBehind();

        double absSin = Math.abs(sin);
        double absCos = Math.abs(cos);
        double halfX = size.bedHalfLength() * absSin + BED_HALF_WIDTH * absCos;
        double halfZ = size.bedHalfLength() * absCos + BED_HALF_WIDTH * absSin;

        return new AABB(
                centerX - halfX, pos.y, centerZ - halfZ,
                centerX + halfX, pos.y + BED_FLOOR_HEIGHT, centerZ + halfZ);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return this.bh_collidesWith(entity);
    }

    public boolean bh_collidesWith(Entity mover) {
        AbstractHorse bound = this.boundHorse();
        if (bound != null && (mover == bound || mover.getVehicle() == bound)) {
            return false;
        }
        return !this.hasPassenger(mover);
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
    public boolean hurt(DamageSource source, float amount) {
        if (!this.isPlaced() || this.isRemoved()) {
            return false;
        }

        boolean instant = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
        this.damageTaken += amount * 10.0F;
        this.markHurt();
        if ((instant || this.damageTaken > CART_BREAK_DAMAGE) && this.level() instanceof ServerLevel serverLevel) {
            this.breakIntoItems(serverLevel, !instant);
        }
        return true;
    }

    private void breakIntoItems(ServerLevel level, boolean dropCart) {
        this.closeChestViewers();
        this.dropChest();
        this.dropPlough();
        if (dropCart) {
            this.spawnAtLocation(new ItemStack(ModItems.HORSE_CART.get()));
        }
        this.playSound(SoundEvents.WOOD_BREAK, 1.0F, 1.0F);
        this.discard();
    }

    @Override
    public boolean shouldBeSaved() {
        return this.isPlaced() || this.horseUuid != null || !this.getPassengers().isEmpty();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_LARGE.equals(key)) {
            this.setBoundingBox(this.makeBoundingBox());
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_HORSE_ID, -1);
        this.entityData.define(DATA_CHEST_OPEN, false);
        this.entityData.define(DATA_ROLL_SPEED, 0.0F);
        this.entityData.define(DATA_PLACED, false);
        this.entityData.define(DATA_HAS_CHEST, false);
        this.entityData.define(DATA_HAS_PLOW, false);
        this.entityData.define(DATA_LARGE, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag input) {
        this.horseUuid = input.hasUUID("BhHorse") ? input.getUUID("BhHorse") : null;
        this.entityData.set(DATA_PLACED, input.getBoolean("BhPlaced"));
        if (this.isPlaced()) {
            this.setNoGravity(false);
        }
        this.entityData.set(DATA_HAS_CHEST, input.getBoolean("BhHasChest"));
        this.placedPlow = input.contains("BhPlow", Tag.TAG_COMPOUND)
                ? ItemStack.of(input.getCompound("BhPlow"))
                : ItemStack.EMPTY;
        this.entityData.set(DATA_HAS_PLOW, !this.placedPlow.isEmpty());
        this.entityData.set(DATA_LARGE, input.getBoolean("BhLarge"));
        this.damageTaken = input.getFloat("BhDamage");
        this.placedChest.clearContent();
        var items = input.getList("BhChestItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot >= 0 && slot < this.placedChest.getContainerSize()) {
                this.placedChest.setItem(slot,
                        ItemStack.of(entry));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag output) {
        if (this.horseUuid != null) output.putUUID("BhHorse", this.horseUuid);
        output.putBoolean("BhPlaced", this.isPlaced());
        output.putBoolean("BhHasChest", this.hasChest());
        if (!this.placedPlow.isEmpty()) {
            output.put("BhPlow", this.placedPlow.save(new CompoundTag()));
        }
        output.putBoolean("BhLarge", this.size().isLarge());
        output.putFloat("BhDamage", this.damageTaken);
        net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();
        for (int slot = 0; slot < this.placedChest.getContainerSize(); slot++) {
            ItemStack stack = this.placedChest.getItem(slot);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) slot);
                items.add(stack.save(entry));
            }
        }
        output.put("BhChestItems", items);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "wheels", 0, this::wheelPredicate));
        controllers.add(new AnimationController<>(this, "chest", 0, this::chestPredicate));
        controllers.add(new AnimationController<>(this, "pose", 0, this::posePredicate));
        controllers.add(new AnimationController<>(this, "plough", 0, this::ploughPredicate));
    }

    private PlayState posePredicate(AnimationState<HorseCartEntity> test) {
        if (!this.isPlaced()) {
            if (test.getController().getCurrentRawAnimation() != null) {
                test.resetCurrentAnimation();
            }
            return PlayState.STOP;
        }
        return test.setAndContinue(this.size().standing());
    }

    private PlayState ploughPredicate(AnimationState<HorseCartEntity> test) {
        RawAnimation dragging = this.size().tilling();
        if (dragging == null || !this.hasPlough() || this.smoothedSpeed <= 0.0D) {
            if (test.getController().getCurrentRawAnimation() != null) {
                test.resetCurrentAnimation();
            }
            return PlayState.STOP;
        }
        test.setControllerSpeed(
                (float) Mth.clamp(this.smoothedSpeed / REFERENCE_SPEED, MIN_ANIM_SPEED, MAX_ANIM_SPEED));
        return test.setAndContinue(dragging);
    }

    private PlayState chestPredicate(AnimationState<HorseCartEntity> test) {
        if (!this.hasChest()) {
            this.chestAnimPrimed = false;
            if (test.getController().getCurrentRawAnimation() != null) {
                test.resetCurrentAnimation();
            }
            return PlayState.STOP;
        }
        if (this.entityData.get(DATA_CHEST_OPEN)) {
            this.chestAnimPrimed = true;
            return test.setAndContinue(this.size().chestOpening());
        }
        if (!this.chestAnimPrimed) {
            return PlayState.STOP;
        }
        return test.setAndContinue(this.size().chestClosing());
    }

    private PlayState wheelPredicate(AnimationState<HorseCartEntity> test) {
        if (this.smoothedSpeed <= 0.0D) {
            test.setControllerSpeed(0.0F);
            return PlayState.STOP;
        }
        double floor = this.coastTicks > 0 ? 0.0D : MIN_ANIM_SPEED;
        test.setControllerSpeed(
                (float) Mth.clamp(this.smoothedSpeed / REFERENCE_SPEED, floor, MAX_ANIM_SPEED));
        return test.setAndContinue(this.size().wheelsRolling());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
