package icy.betterhorses.net.entity;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.BhHorseSteering;
import icy.betterhorses.net.BhWaterline;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.CartMenu;
import icy.betterhorses.net.item.HorseCartItem;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import icy.betterhorses.net.mixin.ShovelItemAccessor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.component.DataComponents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class HorseCartEntity extends Entity implements GeoEntity {

    public static final float WIDTH = 2.0F;
    public static final float HEIGHT = 1.5F;

    private static final double FOLLOW_OFFSET = 0.0D;
    private static final float YAW_OFFSET = 0.0F;

    private static final float SLACK_CATCHUP = 0.3F;
    private static final float MAX_SLACK = 8.0F;
    private static final float MAX_TILT = 25.0F;
    private static final float TILT_EASE = 0.3F;
    private static final float MAX_TILT_STEP = 3.0F;
    private static final double WHEEL_SIDE = 1.16D;
    private static final double PROBE_UP = 1.0D;
    private static final double PROBE_DOWN = 3.0D;
    private static final double FLOAT_DRAFT = 0.6D;
    private static final float FLOAT_EASE = 0.25F;
    private static final double MAX_FLOAT_SPEED = 0.15D;

    private static final double BED_HALF_WIDTH = 0.95D;
    private static final double BED_FLOOR_HEIGHT = 0.8125D;

    private static final double SEAT_BEHIND = 1.4D;
    private static final double SEAT_SIDE = 0.45D;

    private static final int CHEST_SLOTS = CartType.CHEST_SLOTS;
    private static final float CART_BREAK_DAMAGE = 40.0F;
    private static final double REAR_SEAT_SIDE = 0.45D;
    private static final double REAR_SEAT_HEIGHT = 0.75D;
    private static final float MAX_CARGO_WIDTH = EntityType.BOAT.getWidth();
    private static final double BOARD_SCAN_HEIGHT = 1.6D;
    private static final int RESTORE_BOARD_TICKS = 80;

    private static final TagKey<Block> PLOUGHABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "ploughable"));
    private static final TagKey<EntityType<?>> CARGO_BLOCKED = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "cart_cargo_blocked"));
    private static final TagKey<EntityType<?>> CARGO_ALLOWED = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "cart_cargo_allowed"));
    private static final TagKey<EntityType<?>> NEUTRAL = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "cart_pickup_neutral"));

    public static final int PICKUP_ON = 1;
    public static final int PICKUP_PASSIVE = 2;
    public static final int PICKUP_NEUTRAL = 4;
    public static final int PICKUP_HOSTILE = 8;
    public static final int PICKUP_DEFAULT = PICKUP_ON | PICKUP_PASSIVE | PICKUP_NEUTRAL;

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
    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_HIDDEN =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PICKUP =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private @Nullable UUID horseUuid;
    private @Nullable AbstractHorse horse;

    private int cargoRestoreDeadline = RESTORE_BOARD_TICKS;

    private final List<ServerPlayer> viewers = new ArrayList<>();
    private final SimpleContainer placedChest = new SimpleContainer(CHEST_SLOTS);
    private final Rig rig = new Rig();
    private ItemStack placedCart = ItemStack.EMPTY;
    private ItemStack placedChestItem = ItemStack.EMPTY;
    private ItemStack placedPlow = ItemStack.EMPTY;
    private float damageTaken;
    private boolean chestAnimPrimed = false;

    private double prevX;
    private double prevZ;

    private boolean settled;
    private float slackYaw;
    private float slackYawO;
    private float tilt;
    private float tiltO;
    private float lift;
    private float liftO;

    private float bedBounce;
    private float bedRock;

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

    public CartType type() {
        return CartType.byOrdinal(this.entityData.get(DATA_TYPE));
    }

    private void applyType(CartType type) {
        if (type == this.type()) {
            return;
        }
        this.entityData.set(DATA_TYPE, type.ordinal());
        this.setBoundingBox(this.makeBoundingBox());
        this.dropOverflowPassengers();
    }

    private ItemStack setupStack() {
        if (this.isPlaced()) {
            return this.placedCart;
        }
        AbstractHorse boundHorse = this.resolveHorse();
        return boundHorse == null ? ItemStack.EMPTY
                : IHorseData.of(boundHorse).bh_getGearContainer().getItem(GearSlot.STABILIZER.ordinal());
    }

    private void mirrorSetup(ItemStack setup) {
        int hidden = 0;
        List<CartType.Part> parts = this.type().parts();
        for (int i = 0; i < parts.size(); i++) {
            if (HorseCartItem.partHidden(setup, parts.get(i).key())) {
                hidden |= 1 << i;
            }
        }
        this.entityData.set(DATA_HIDDEN, hidden);
        this.entityData.set(DATA_PICKUP, HorseCartItem.pickup(setup));
        Component name = setup.get(DataComponents.CUSTOM_NAME);
        if (!Objects.equals(name, this.getCustomName())) {
            this.setCustomName(name);
        }
    }

    public boolean partHidden(int index) {
        return (this.entityData.get(DATA_HIDDEN) & (1 << index)) != 0;
    }

    public boolean shaded() {
        List<CartType.Part> parts = this.type().parts();
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).shades() && !this.partHidden(i)) {
                return true;
            }
        }
        return false;
    }

    public int pickup() {
        return this.entityData.get(DATA_PICKUP);
    }

    private void dropOverflowPassengers() {
        int seats = this.rearCapacity();
        List<Entity> riders = this.getPassengers();
        for (int i = riders.size() - 1; i >= seats; i--) {
            this.setDown(riders.get(i));
        }
    }

    private int rearCapacity() {
        return this.type().rearSeats(this.hasChest());
    }

    public enum Refusal {
        DRAFT_ONLY("cart_size_draft_only", "draft"),
        RIDERS("cart_size_passengers", "riders"),
        PLOUGH("cart_size_plough", "plough"),
        CHEST("cart_size_chest_full", "chest"),
        BLOCKED("cart_size_blocked", "blocked");

        private final String message;
        private final String label;

        Refusal(String message, String label) {
            this.message = message;
            this.label = label;
        }

        public Component message() {
            return Component.translatable("message.icys-better-horses." + this.message);
        }

        public Component label() {
            return Component.translatable("gui.icys-better-horses.cart.refuse." + this.label);
        }
    }

    public @Nullable Refusal switchRefusal(CartType type, @Nullable Container chest) {
        if (type == this.type()) {
            return null;
        }
        if (type.isLarge() && !this.isPlaced()) {
            AbstractHorse boundHorse = this.boundHorse();
            if (boundHorse == null || !IHorseData.of(boundHorse).bh_mayUseLargeCart()) {
                return Refusal.DRAFT_ONLY;
            }
        }
        if (this.getPassengers().size() > type.rearSeats(this.hasChest())) {
            return Refusal.RIDERS;
        }
        if (this.hasPlough() && !type.takesPlough()) {
            return Refusal.PLOUGH;
        }
        if (this.hasChest() && CartType.itemsOutside(chest, type)) {
            return Refusal.CHEST;
        }
        if (this.isPlaced()
                && !this.level().noCollision(this, boxFor(type, this.position(), this.getYRot()))) {
            return Refusal.BLOCKED;
        }
        return null;
    }

    public void switchType(ServerPlayer player, CartType type) {
        if (type == this.type() || !this.playerMayHandleCargo(player)) {
            return;
        }
        Refusal refusal = this.switchRefusal(type, this.chestContainer());
        if (refusal != null) {
            player.sendSystemMessage(refusal.message());
            return;
        }
        ItemStack setup = this.setupStack();
        if (setup.isEmpty()) {
            return;
        }
        HorseCartItem.setType(setup, type);
        AbstractHorse boundHorse = this.isPlaced() ? null : this.resolveHorse();
        if (boundHorse != null) {
            IHorseData.of(boundHorse).bh_syncCartType();
        }
        this.applyType(type);
        this.mirrorSetup(setup);
        this.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
    }

    public void togglePart(int index) {
        List<CartType.Part> parts = this.type().parts();
        ItemStack setup = this.setupStack();
        if (index < 0 || index >= parts.size() || setup.isEmpty()) {
            return;
        }
        String key = parts.get(index).key();
        HorseCartItem.setPartHidden(setup, key, !HorseCartItem.partHidden(setup, key));
        this.mirrorSetup(setup);
        this.playSound(SoundEvents.WOOL_PLACE, 0.8F, 1.0F);
    }

    public void togglePickup(int flag) {
        ItemStack setup = this.setupStack();
        if (setup.isEmpty() || !BhConfig.cartPickupEnabled()) {
            return;
        }
        HorseCartItem.setPickup(setup, HorseCartItem.pickup(setup) ^ flag);
        this.mirrorSetup(setup);
    }

    public static HorseCartEntity preview(Level level, CartType type, boolean standing) {
        HorseCartEntity cart = new HorseCartEntity(ModEntities.HORSE_CART, level);
        cart.setId(-1);
        cart.entityData.set(DATA_PLACED, standing);
        cart.entityData.set(DATA_TYPE, type.ordinal());
        return cart;
    }

    public void dressPreview(boolean chest, boolean plough, int hidden) {
        this.entityData.set(DATA_HAS_CHEST, chest);
        this.entityData.set(DATA_HAS_PLOW, plough);
        this.entityData.set(DATA_HIDDEN, hidden);
    }

    public static @Nullable HorseCartEntity place(ServerLevel level, Vec3 pos, float yaw, ItemStack stack) {
        HorseCartEntity cart = new HorseCartEntity(ModEntities.HORSE_CART, level);
        CartType stored = HorseCartItem.storedType(stack);
        cart.placedCart = stack.copyWithCount(1);
        cart.entityData.set(DATA_PLACED, true);
        cart.entityData.set(DATA_TYPE, (stored == null ? CartType.BUGGY : stored).ordinal());
        cart.mirrorSetup(cart.placedCart);
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
        cart.entityData.set(DATA_TYPE, IHorseData.of(horse).bh_getCartType().ordinal());
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
            this.updateViewers();
            return;
        }

        AbstractHorse boundHorse = this.resolveHorse();
        if (boundHorse == null && this.horseUuid != null) {
            this.closeViewers();
            if (this.horse != null && this.horse.getRemovalReason() != null
                    && this.horse.getRemovalReason().shouldDestroy()) this.discard();
            return;
        }
        if (boundHorse == null || !boundHorse.isAlive() || boundHorse.isRemoved()
                || !IHorseData.of(boundHorse).bh_hasCartGear()) {
            this.closeViewers();
            this.discard();
            return;
        }
        this.followHorse(boundHorse);
        this.updateRollSpeed();
        IHorseData data = IHorseData.of(boundHorse);
        if (data.bh_getCartId() != null && !getUUID().equals(data.bh_getCartId())
                && this.level() instanceof ServerLevel serverLevel
                && serverLevel.getEntity(data.bh_getCartId()) instanceof HorseCartEntity replacement) {
            for (Entity passenger : List.copyOf(getPassengers())) passenger.startRiding(replacement, true);
            discard();
            return;
        }
        data.bh_setCartId(getUUID());
        this.entityData.set(DATA_HAS_CHEST, data.bh_hasCartChest());
        this.entityData.set(DATA_HAS_PLOW, data.bh_hasCartPlough());
        this.applyType(data.bh_getCartType());
        this.mirrorSetup(this.setupStack());
        this.tillGround();
        this.updateViewers();
        this.tryBoardNearbyMobs();
        this.tendPassengers();
    }

    private void settleOnGround() {
        double surface = BhWaterline.surface(this.level(), this.getX(), this.getY(), this.getZ());
        if (!Double.isNaN(surface)) {
            double rise = Mth.clamp((surface - FLOAT_DRAFT - this.getY()) * FLOAT_EASE, -MAX_FLOAT_SPEED, MAX_FLOAT_SPEED);
            this.setDeltaMovement(0.0D, rise, 0.0D);
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }
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

    public void unloadPassengers() {
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
        Vec3 target = cartPosFor(boundHorse.getX(), boundHorse.getY(), boundHorse.getZ(), boundHorse.yBodyRot);
        this.settle(boundHorse, target);
        this.setYRot(this.slackYaw);
        this.setYBodyRot(this.slackYaw);
        this.setYHeadRot(this.slackYaw);

        this.setPos(target.x, target.y + this.lift, target.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void settle(AbstractHorse boundHorse, Vec3 hitch) {
        float horseYaw = boundHorse.yBodyRot + YAW_OFFSET;
        if (!this.settled) {
            this.settled = true;
            this.slackYaw = horseYaw;
            this.tilt = 0.0F;
            this.lift = 0.0F;
        }
        this.slackYawO = this.slackYaw;
        this.tiltO = this.tilt;
        this.liftO = this.lift;

        this.slackYaw += Mth.wrapDegrees(horseYaw - this.slackYaw) * SLACK_CATCHUP;
        float lag = Mth.wrapDegrees(horseYaw - this.slackYaw);
        if (Math.abs(lag) > MAX_SLACK) {
            this.slackYaw = horseYaw - Math.copySign(MAX_SLACK, lag);
        }

        boolean swimming = boundHorse.isInWater() && !boundHorse.onGround();
        double surface = swimming ? BhWaterline.surface(this.level(), hitch.x, hitch.y, hitch.z) : Double.NaN;
        float floatTarget = Double.isNaN(surface) ? 0.0F : (float) Math.max(0.0D, surface - FLOAT_DRAFT - hitch.y);
        this.lift += (floatTarget - this.lift) * FLOAT_EASE;

        if (swimming || boundHorse.onGround()) {
            float target = swimming ? 0.0F : this.groundTilt(hitch);
            this.tilt += Mth.clamp((target - this.tilt) * TILT_EASE, -MAX_TILT_STEP, MAX_TILT_STEP);
        }
    }

    private float groundTilt(Vec3 hitch) {
        double behind = this.type().axleBehind();
        float rad = -this.slackYaw * Mth.DEG_TO_RAD;
        double ground = Math.max(
                this.groundBelow(hitch.add(new Vec3(-WHEEL_SIDE, 0.0D, -behind).yRot(rad))),
                this.groundBelow(hitch.add(new Vec3(WHEEL_SIDE, 0.0D, -behind).yRot(rad))));
        float angle = (float) Math.toDegrees(Math.atan2(hitch.y - ground, behind));
        return Mth.clamp(angle, -MAX_TILT, MAX_TILT);
    }

    private double groundBelow(Vec3 wheel) {
        Vec3 top = new Vec3(wheel.x, wheel.y + PROBE_UP, wheel.z);
        Vec3 bottom = new Vec3(wheel.x, wheel.y - PROBE_DOWN, wheel.z);
        BlockHitResult hit = this.level().clip(new ClipContext(top, bottom,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.MISS ? bottom.y : hit.getLocation().y;
    }

    private void glueToHorse(AbstractHorse boundHorse) {
        this.followHorse(boundHorse);
        IHorseData.of(boundHorse).bh_bindCartEntity(this);

        Vec3 previous = cartPosFor(boundHorse.xo, boundHorse.yo, boundHorse.zo, boundHorse.yBodyRotO);
        this.xo = this.xOld = previous.x;
        this.yo = this.yOld = previous.y + this.liftO;
        this.zo = this.zOld = previous.z;
        this.yRotO = this.slackYawO;
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
        return cartPosFor(horsePos.x, horsePos.y + this.renderLift(partialTick), horsePos.z,
                renderBodyYaw(boundHorse, partialTick));
    }

    public float gluedRenderYaw(float partialTick) {
        return this.settled ? Mth.rotLerp(partialTick, this.slackYawO, this.slackYaw) : this.getYRot();
    }

    public float renderTilt(float partialTick) {
        return Mth.lerp(partialTick, this.tiltO, this.tilt);
    }

    public void recordBed(float bounce, float rock) {
        this.bedBounce = bounce;
        this.bedRock = rock;
    }

    public float bedBounce() {
        return this.bedBounce;
    }

    public float bedRock() {
        return this.bedRock;
    }

    public float renderLift(float partialTick) {
        return Mth.lerp(partialTick, this.liftO, this.lift);
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

    public static Vec3 benchSeatOffset(AbstractHorse boundHorse, int seatIndex) {
        HorseCartEntity cart = IHorseData.of(boundHorse).bh_getCartEntity();
        return cart != null && cart.settled && !cart.isRemoved()
                ? benchSeatOffset(boundHorse, seatIndex, cart.slackYaw, cart.tilt, cart.lift)
                : benchSeatOffset(boundHorse, seatIndex, boundHorse.yBodyRot, 0.0F, 0.0F);
    }

    public static Vec3 benchSeatOffset(AbstractHorse boundHorse, int seatIndex, float cartYaw, float cartTilt,
                                       float cartLift) {
        double side = benchShared(boundHorse) ? (seatIndex <= 0 ? -SEAT_SIDE : SEAT_SIDE) : 0.0D;
        double up = IHorseData.of(boundHorse).bh_getCartType().benchHeight();
        return new Vec3(side, up, FOLLOW_OFFSET - SEAT_BEHIND)
                .xRot(cartTilt * Mth.DEG_TO_RAD)
                .yRot(-cartYaw * ((float) Math.PI / 180.0F))
                .add(0.0D, cartLift, 0.0D);
    }

    private static boolean benchShared(AbstractHorse boundHorse) {
        return boundHorse.getPassengers().size() > 1;
    }

    public Vec3 renderSeat(Entity passenger, float partialTick) {
        float yaw = this.gluedRenderYaw(partialTick);
        float cartTilt = this.renderTilt(partialTick);
        Vec3 riding = passenger.getVehicle() == null ? Vec3.ZERO : passenger.getVehicleAttachmentPoint(passenger.getVehicle());
        if (this.hasPassenger(passenger)) {
            Vec3 base = this.gluedRenderPosition(partialTick);
            if (base == null) {
                base = this.getPosition(partialTick);
            }
            return base.add(this.carriageSeatOffset(Math.max(0, this.getPassengers().indexOf(passenger)), yaw, cartTilt))
                    .subtract(riding);
        }
        AbstractHorse boundHorse = this.clientHorse();
        if (boundHorse == null) {
            return passenger.getPosition(partialTick);
        }
        Vec3 seat = benchSeatOffset(boundHorse, BhHorseSteering.benchSeatIndex(boundHorse, passenger),
                yaw, cartTilt, this.renderLift(partialTick));
        return boundHorse.getPosition(partialTick).add(seat).subtract(riding);
    }

    private Vec3 carriageSeatOffset(int seatIndex, float cartYaw) {
        return this.carriageSeatOffset(seatIndex, cartYaw, this.tilt);
    }

    private Vec3 carriageSeatOffset(int seatIndex, float cartYaw, float cartTilt) {
        CartType size = this.type();
        seatIndex += size.rearSeatCount() - this.rearCapacity();
        double side = seatIndex % 2 == 0 ? -REAR_SEAT_SIDE : REAR_SEAT_SIDE;
        double rowShift = seatIndex < 2 ? -size.rearRowSpacing() : size.rearRowSpacing();
        return new Vec3(side, REAR_SEAT_HEIGHT, -(size.rearSeatBehind() + rowShift))
                .xRot(cartTilt * Mth.DEG_TO_RAD)
                .yRot(-cartYaw * ((float) Math.PI / 180.0F));
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        int seatIndex = Math.max(0, this.getPassengers().indexOf(passenger));
        return this.carriageSeatOffset(seatIndex, this.getYRot());
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

    private static boolean wanted(LivingEntity mob, int pickup) {
        int kind;
        if (mob instanceof NeutralMob || mob.getType().is(NEUTRAL)) {
            kind = PICKUP_NEUTRAL;
        } else if (mob instanceof Enemy) {
            kind = PICKUP_HOSTILE;
        } else {
            kind = PICKUP_PASSIVE;
        }
        return (pickup & kind) != 0;
    }

    private void tryBoardNearbyMobs() {
        if (!BhConfig.cartPickupEnabled()) {
            return;
        }
        boolean restoring = this.restoringCargo();
        int pickup = this.pickup();
        if (!restoring && (pickup & PICKUP_ON) == 0) {
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
                    || !this.canCarry(candidate)
                    || !restoring && !wanted(candidate, pickup)) {
                continue;
            }
            if (this.rearSeatsFree()) {
                candidate.startRiding(this, restoring);
            } else {
                candidate.startRiding(boundHorse, false);
            }
        }
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitLocation, InteractionHand hand) {
        boolean clientSide = this.level().isClientSide();
        ItemStack held = player.getItemInHand(hand);

        if ((held.is(ItemTags.HOES) || held.is(ItemTags.SHOVELS))
                && !this.hasPlough() && this.type().takesPlough()) {
            if (clientSide) {
                return InteractionResult.SUCCESS;
            }
            return this.attachPlough(player, held) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (GearSlot.isStorageChest(held) && !this.hasChest()) {
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
            if (player instanceof ServerPlayer serverPlayer) {
                this.openMenu(serverPlayer);
            }
            return clientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
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

    public Container rig() {
        return this.rig;
    }

    private ItemStack chestItem() {
        if (this.isPlaced()) {
            return this.placedChestItem;
        }
        AbstractHorse boundHorse = this.resolveHorse();
        return boundHorse == null ? ItemStack.EMPTY : IHorseData.of(boundHorse).bh_getCartChestItem();
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

    private void fitPlough(ItemStack hoe) {
        this.setPlough(hoe);
        if (!hoe.isEmpty()) {
            this.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 1.0F);
        }
    }

    private boolean attachPlough(Player player, ItemStack held) {
        if (!this.playerMayHandleCargo(player)) {
            return false;
        }
        this.fitPlough(held.copyWithCount(1));
        held.consume(1, player);
        return true;
    }

    private void shearPlough(Player player, InteractionHand hand) {
        if (!this.playerMayHandleCargo(player)) {
            return;
        }
        this.dropPlough();
        player.getItemInHand(hand).hurtAndBreak(1, player,
                hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
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
        if (!(this.level() instanceof ServerLevel level) || !this.hasPlough() || !this.type().takesPlough()) {
            return;
        }
        if (this.entityData.get(DATA_ROLL_SPEED) < STILL_SPEED) {
            return;
        }
        ItemStack tool = this.ploughItem();
        if (tool.isEmpty()) {
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
                if (this.turnOver(level, furrow.above(lift), driver, tool)) {
                    turned++;
                    break;
                }
            }
        }
        if (turned == 0) {
            return;
        }

        this.playSound(tool.is(ItemTags.SHOVELS) ? SoundEvents.SHOVEL_FLATTEN : SoundEvents.HOE_TILL, 1.0F, 1.0F);
        tool.hurtAndBreak(turned, level, null, item -> {});
        if (tool.isEmpty()) {
            this.setPlough(ItemStack.EMPTY);
            this.playSound(SoundEvents.ITEM_BREAK, 0.8F, 0.9F);
        }
    }

    private boolean turnOver(ServerLevel level, BlockPos pos, @Nullable LivingEntity driver, ItemStack tool) {
        BlockState ground = level.getBlockState(pos);
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }
        if (driver instanceof ServerPlayer sp && !level.mayInteract(sp, pos)) {
            return false;
        }
        BlockState turned;
        if (tool.is(ItemTags.SHOVELS)) {
            turned = ShovelItemAccessor.bh_flattenables().get(ground.getBlock());
            if (turned == null || turned == ground) return false;
        } else {
            if (!tillable(ground)) return false;
            if (ground.is(Blocks.ROOTED_DIRT)) {
                Block.popResource(level, pos, new ItemStack(Items.HANGING_ROOTS));
            }
            turned = Blocks.FARMLAND.defaultBlockState();
        }
        level.setBlockAndUpdate(pos, turned);
        return true;
    }

    private static boolean tillable(BlockState state) {
        return state.is(PLOUGHABLE);
    }

    public @Nullable SimpleContainer chestContainer() {
        if (this.isPlaced()) {
            return this.placedChest;
        }
        AbstractHorse boundHorse = this.resolveHorse();
        return boundHorse == null ? null : IHorseData.of(boundHorse).bh_getCartChestContainer();
    }

    private void setChest(ItemStack chest) {
        if (this.isPlaced()) {
            this.placedChestItem = chest;
        } else {
            AbstractHorse boundHorse = this.resolveHorse();
            if (boundHorse != null) {
                IHorseData.of(boundHorse).bh_setCartChest(chest);
            }
        }
        this.entityData.set(DATA_HAS_CHEST, !chest.isEmpty());
    }

    private void fitChest(ItemStack chest) {
        this.setChest(chest);
        if (!chest.isEmpty()) {
            this.dropOverflowPassengers();
            this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, 1.0F);
        }
    }

    private boolean attachChest(Player player, ItemStack held) {
        if (!this.playerMayHandleCargo(player)) {
            return false;
        }

        this.fitChest(held.copyWithCount(1));
        held.consume(1, player);
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

        this.closeViewers();
        this.dropChest();
        player.getItemInHand(hand).hurtAndBreak(1, player,
                hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
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

        ItemStack chest = this.placedChestItem.isEmpty() ? new ItemStack(Items.CHEST) : this.placedChestItem;
        this.setChest(ItemStack.EMPTY);
        for (int slot = 0; slot < this.placedChest.getContainerSize(); slot++) {
            ItemStack stack = this.placedChest.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack);
            }
        }
        this.spawnAtLocation(chest);
    }

    public void openMenu(ServerPlayer player) {
        SimpleContainer contents = this.chestContainer();
        if (contents == null || !this.playerMayHandleCargo(player)) {
            return;
        }

        int tab = this.hasChest() ? CartMenu.TAB_CARGO : CartMenu.TAB_CART;
        HorseCartEntity cart = this;
        player.openMenu(new ExtendedScreenHandlerFactory<CartMenu.Opening>() {
            @Override
            public CartMenu.Opening getScreenOpeningData(ServerPlayer opener) {
                return new CartMenu.Opening(cart.getId(), tab);
            }

            @Override
            public Component getDisplayName() {
                return cart.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player opener) {
                return new CartMenu(containerId, inventory, cart, contents, tab);
            }
        });
        if (isViewing(player) && !this.viewers.contains(player)) {
            this.viewers.add(player);
        }
    }

    private void updateViewers() {
        if (!this.viewers.isEmpty()) {
            this.viewers.removeIf(viewer -> {
                if (!isViewing(viewer)) return true;
                if (mayKeepMenuOpen(viewer)) return false;
                viewer.closeContainer();
                return true;
            });
        }
        this.setChestOpen(this.hasChest() && !this.viewers.isEmpty());
    }

    private void closeViewers() {
        for (ServerPlayer viewer : List.copyOf(this.viewers)) {
            viewer.closeContainer();
        }
        this.viewers.clear();
        this.setChestOpen(false);
    }

    public boolean mayKeepMenuOpen(Player player) {
        if (!this.isAlive() || !player.isAlive() || player.level() != level()
                || player.distanceToSqr(this.getBoundingBox().getCenter()) > 64.0D) return false;
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

    private boolean isViewing(Player viewer) {
        return viewer.isAlive() && !viewer.isRemoved()
                && viewer.containerMenu instanceof CartMenu menu && menu.cart() == this;
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

    public int cargoCount() {
        return this.getPassengers().size() + this.benchCargo().size();
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
        return boxFor(this.type(), this.position(), this.getYRot());
    }

    private static AABB boxFor(CartType size, Vec3 pos, float yaw) {
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
        this.closeViewers();
        this.dropChest();
        this.dropPlough();
        if (dropCart) {
            this.spawnAtLocation(this.placedCart.isEmpty()
                    ? new ItemStack(ModItems.HORSE_CART) : this.placedCart.copy());
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
        if (DATA_TYPE.equals(key)) {
            this.setBoundingBox(this.makeBoundingBox());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HORSE_ID, -1);
        builder.define(DATA_CHEST_OPEN, false);
        builder.define(DATA_ROLL_SPEED, 0.0F);
        builder.define(DATA_PLACED, false);
        builder.define(DATA_HAS_CHEST, false);
        builder.define(DATA_HAS_PLOW, false);
        builder.define(DATA_TYPE, CartType.BUGGY.ordinal());
        builder.define(DATA_HIDDEN, 0);
        builder.define(DATA_PICKUP, PICKUP_DEFAULT);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag input) {
        this.horseUuid = input.hasUUID("BhHorse") ? input.getUUID("BhHorse") : null;
        this.entityData.set(DATA_PLACED, input.getBoolean("BhPlaced"));
        if (this.isPlaced()) {
            this.setNoGravity(false);
        }
        this.placedChestItem = !input.getBoolean("BhHasChest") ? ItemStack.EMPTY
                : input.contains("BhChestItem", Tag.TAG_COMPOUND)
                ? ItemStack.parse(this.registryAccess(), input.getCompound("BhChestItem")).orElseGet(() -> new ItemStack(Items.CHEST))
                : new ItemStack(Items.CHEST);
        this.entityData.set(DATA_HAS_CHEST, !this.placedChestItem.isEmpty());
        this.placedPlow = input.contains("BhPlow", Tag.TAG_COMPOUND)
                ? ItemStack.parse(this.registryAccess(), input.getCompound("BhPlow")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        this.entityData.set(DATA_HAS_PLOW, !this.placedPlow.isEmpty());
        boolean legacyLarge = input.getBoolean("BhLarge");
        this.placedCart = input.contains("BhCart", Tag.TAG_COMPOUND)
                ? ItemStack.parse(this.registryAccess(), input.getCompound("BhCart")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        if (this.isPlaced() && this.placedCart.isEmpty()) {
            this.placedCart = new ItemStack(ModItems.HORSE_CART);
            HorseCartItem.setType(this.placedCart, CartType.defaultFor(legacyLarge));
        }
        CartType stored = HorseCartItem.storedType(this.placedCart);
        this.entityData.set(DATA_TYPE, (stored == null ? CartType.defaultFor(legacyLarge) : stored).ordinal());
        if (!this.placedCart.isEmpty()) {
            this.mirrorSetup(this.placedCart);
        }
        this.damageTaken = input.getFloat("BhDamage");
        this.placedChest.clearContent();
        var items = input.getList("BhChestItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot >= 0 && slot < this.placedChest.getContainerSize()) {
                this.placedChest.setItem(slot,
                        ItemStack.parse(this.registryAccess(), entry).orElse(ItemStack.EMPTY));
            }
        }
        if (!input.getBoolean("BhChestWide") && !legacyLarge) {
            CartType.widenLegacyRows(this.placedChest);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag output) {
        if (this.horseUuid != null) output.putUUID("BhHorse", this.horseUuid);
        output.putBoolean("BhPlaced", this.isPlaced());
        output.putBoolean("BhHasChest", this.hasChest());
        if (!this.placedChestItem.isEmpty()) {
            output.put("BhChestItem", this.placedChestItem.save(this.registryAccess()));
        }
        if (!this.placedPlow.isEmpty()) {
            output.put("BhPlow", this.placedPlow.save(this.registryAccess()));
        }
        if (!this.placedCart.isEmpty()) {
            output.put("BhCart", this.placedCart.save(this.registryAccess()));
        }
        output.putBoolean("BhChestWide", true);
        output.putFloat("BhDamage", this.damageTaken);
        net.minecraft.nbt.ListTag items = new net.minecraft.nbt.ListTag();
        for (int slot = 0; slot < this.placedChest.getContainerSize(); slot++) {
            ItemStack stack = this.placedChest.getItem(slot);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) slot);
                items.add(stack.save(this.registryAccess(), entry));
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
        return test.setAndContinue(this.type().standing());
    }

    private PlayState ploughPredicate(AnimationState<HorseCartEntity> test) {
        RawAnimation dragging = this.type().tilling();
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
            return test.setAndContinue(this.type().chestOpening());
        }
        if (!this.chestAnimPrimed) {
            return PlayState.STOP;
        }
        return test.setAndContinue(this.type().chestClosing());
    }

    private PlayState wheelPredicate(AnimationState<HorseCartEntity> test) {
        if (this.smoothedSpeed <= 0.0D) {
            test.setControllerSpeed(0.0F);
            return PlayState.STOP;
        }
        double floor = this.coastTicks > 0 ? 0.0D : MIN_ANIM_SPEED;
        test.setControllerSpeed(
                (float) Mth.clamp(this.smoothedSpeed / REFERENCE_SPEED, floor, MAX_ANIM_SPEED));
        return test.setAndContinue(this.type().wheelsRolling());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    private final class Rig implements Container {

        @Override
        public int getContainerSize() {
            return CartType.Attachment.values().length;
        }

        @Override
        public boolean isEmpty() {
            return !hasChest() && !hasPlough();
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot == CartType.Attachment.CHEST.ordinal() ? chestItem() : ploughItem();
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return this.removeItemNoUpdate(slot);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack was = this.getItem(slot);
            this.setItem(slot, ItemStack.EMPTY);
            return was;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot == CartType.Attachment.CHEST.ordinal()) {
                fitChest(stack);
            } else {
                fitPlough(stack);
            }
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }
    }
}
