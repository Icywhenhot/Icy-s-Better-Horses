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

// a standalone cart entity that is pulled behind the horse that owns
public final class HorseCartEntity extends Entity implements GeoEntity {

    // entity-type dimensions. the real collision box comes from makeBoundingBox(Vec3)
    public static final float WIDTH = 2.0F;
    public static final float HEIGHT = 1.5F;

    // offset from the horse center along the horse's facing, in blocks
    private static final double FOLLOW_OFFSET = 0.0D;
    // added to the horse yaw for the cart's facing; flip to 180 if the model faces the wrong way
    private static final float YAW_OFFSET = 0.0F;

    // bed collision box (model units / 16)
    // distance behind the entity origin to the center of the cart bed
    private static final double BED_CENTER_BEHIND = 2.2D;
    private static final double BED_HALF_WIDTH = 0.95D;
    private static final double BED_HALF_LENGTH = 1.15D;
    // top of the bed floor, the surface you stand
    private static final double BED_FLOOR_HEIGHT = 0.8125D;

    // bench seats (two riders, side by side)
    private static final double SEAT_HEIGHT = 1.15D;
    // distance behind the entity origin to the bench
    private static final double SEAT_BEHIND = 1.4D;
    // sideways spacing of the two seats from the cart center line
    private static final double SEAT_SIDE = 0.45D;

    // carriage seats (two occupants in the bed behind the bench: players or small mobs)
    private static final int REAR_SEAT_COUNT = 2;
    // double chest's worth of storage
    private static final int CHEST_SLOTS = 54;
    // damage a placed cart soaks up before it breaks, in the same units vanilla boats use
    private static final float CART_BREAK_DAMAGE = 40.0F;
    // distance behind the entity origin to the carriage seats (further back than the bench)
    private static final double REAR_SEAT_BEHIND = 2.55D;
    // sideways spacing of the two carriage seats from the cart center line
    private static final double REAR_SEAT_SIDE = 0.45D;
    // seat height above the entity origin
    private static final double REAR_SEAT_HEIGHT = 0.75D;
    // width limit for anything riding in the back
    private static final float MAX_CARGO_WIDTH = EntityTypes.OAK_BOAT.getWidth();
    // how far above the bed floor to look for mobs standing in the carriage, to auto-board them
    private static final double BOARD_SCAN_HEIGHT = 1.6D;
    // how long a freshly spawned cart force-boards mobs found in its bed
    private static final int RESTORE_BOARD_TICKS = 80;

    // wheel animation pacing
    // per-tick easing toward the measured speed while speeding up; lower = longer spin-up ramp
    private static final double SPEED_SMOOTHING_UP = 0.12D;
    // ticks the wheels take to coast from rolling to a dead stop once the horse stops, 0.8 seconds
    private static final int STOP_RAMP_TICKS = 16;
    // below this (blocks/tick) the cart counts as parked and the animation stops entirely
    private static final double STILL_SPEED = 0.02D;
    // consecutive moving ticks needed to call off a coast-down already under way
    private static final int RESUME_TICKS = 2;
    // resolution of the synced speed, in steps per block/tick
    private static final int SPEED_SYNC_STEPS = 128;
    // speed (blocks/tick) at which the animation runs at its authored rate
    private static final double REFERENCE_SPEED = 0.35D;
    private static final double MIN_ANIM_SPEED = 0.15D;
    private static final double MAX_ANIM_SPEED = 1.5D;

    // animation keys, exactly as they appear in horse_cart.animation.json
    private static final String WHEEL_ANIM_NAME = "wheel moving2";
    private static final String CHEST_OPEN_ANIM_NAME = "chest";
    private static final String CHEST_CLOSE_ANIM_NAME = "chest close";
    private static final String STANDING_ANIM_NAME = "stand alone";

    private static final RawAnimation WHEELS_ROLLING = RawAnimation.begin().thenLoop(WHEEL_ANIM_NAME);
    // play-and-hold: both lid clips end on the pose they were aiming for and stay there
    private static final RawAnimation CHEST_OPENING = RawAnimation.begin().thenPlayAndHold(CHEST_OPEN_ANIM_NAME);
    private static final RawAnimation CHEST_CLOSING = RawAnimation.begin().thenPlayAndHold(CHEST_CLOSE_ANIM_NAME);
    // looped rather than played once: the clip's first and last frames are the same pose, so looping
    // holds the cart in it for as long as it stands there
    private static final RawAnimation STANDING = RawAnimation.begin().thenLoop(STANDING_ANIM_NAME);

    // network id of the bound horse, synced so the client can glue the cart to it directly (the
    private static final EntityDataAccessor<Integer> DATA_HORSE_ID =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.INT);
    // whether anyone currently has the cart's chest open
    private static final EntityDataAccessor<Boolean> DATA_CHEST_OPEN =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.BOOLEAN);
    // how far the cart travelled last tick, in blocks, measured and synced by the server
    private static final EntityDataAccessor<Float> DATA_ROLL_SPEED =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.FLOAT);
    // set on a cart standing in the world under its own steam rather than hitched to a horse. saved,
    // so it survives a reload, unlike a hitched cart which the horse respawns
    private static final EntityDataAccessor<Boolean> DATA_PLACED =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.BOOLEAN);
    // whether a chest is fitted, whichever mode we're in. the renderer reads only this
    private static final EntityDataAccessor<Boolean> DATA_HAS_CHEST =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private @Nullable UUID horseUuid;
    private @Nullable AbstractHorse horse;

    // tick up to which this cart force-boards mobs in its bed; zeroed once cargo is unloaded by hand
    private int cargoRestoreDeadline = RESTORE_BOARD_TICKS;

    // players with this cart's chest screen open
    private final List<ServerPlayer> chestViewers = new ArrayList<>();
    // storage for a placed cart's chest. a hitched cart keeps its on the horse instead, since the
    // cart entity is discarded and respawned on every reload and would lose it
    private final SimpleContainer placedChest = new SimpleContainer(CHEST_SLOTS);
    // damage taken since it was last put down, boat style. see CART_BREAK_DAMAGE
    private float damageTaken;
    // client-side latch: false until this client has actually seen the lid open
    private boolean chestAnimPrimed = false;

    // server-side previous position, for measuring the cart's real per-tick travel
    private double prevX;
    private double prevZ;

    // client-side animation pacing, derived from the server's measure
    private double smoothedSpeed;
    // wheel speed at the moment the cart stopped, the top of the coast-down ramp
    private double coastFromSpeed;
    // ticks into the coast-down ramp; 0 whenever the cart is actually moving
    private int coastTicks;
    // consecutive ticks of real movement, capped at RESUME_TICKS
    private int movingTicks;

    public HorseCartEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.prevX = this.getX();
        this.prevZ = this.getZ();
    }

    // spawns a cart bound to horse, positioned relative
    // true for a cart standing in the world on its own rather than hitched behind a horse
    public boolean isPlaced() {
        return this.entityData.get(DATA_PLACED);
    }

    // puts a cart down in the world, facing the way the player is looking. server side only
    public static @Nullable HorseCartEntity place(ServerLevel level, Vec3 pos, float yaw) {
        HorseCartEntity cart = new HorseCartEntity(ModEntities.HORSE_CART, level);
        cart.entityData.set(DATA_PLACED, true);
        // a hitched cart is teleported around by its horse and wants no gravity; one standing on its
        // own has to be able to fall
        cart.setNoGravity(false);
        cart.setYRot(yaw);
        cart.setYBodyRot(yaw);
        cart.setYHeadRot(yaw);
        cart.setPos(pos.x, pos.y, pos.z);
        // the cart is long and its box sits well behind the origin, so check it actually fits rather
        // than letting one clip halfway into a wall
        if (!level.noCollision(cart)) {
            return null;
        }
        if (!level.addFreshEntity(cart)) {
            return null;
        }
        cart.playSound(SoundEvents.WOOD_PLACE, 1.0F, 1.0F);
        return cart;
    }

    // middle-clicking a placed cart hands back the item that made it
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
            // a parked cart just sits there. no horse to follow, no wheels turning, no cargo to tend
            this.settleOnGround();
            this.updateChestViewers();
            return;
        }

        AbstractHorse boundHorse = this.resolveHorse();
        if (boundHorse == null || !boundHorse.isAlive() || boundHorse.isRemoved()
                || !((IHorseData) boundHorse).bh_hasCartGear()) {
            // nobody should be left staring into the storage of a cart that is about to stop existing
            this.closeChestViewers();
            this.discard();
            return;
        }
        this.followHorse(boundHorse);
        this.updateRollSpeed();
        // mirror the horse's chest flag onto ours so the renderer only ever reads one field
        this.entityData.set(DATA_HAS_CHEST, ((IHorseData) boundHorse).bh_hasCartChest());
        this.updateChestViewers();
        this.tryBoardNearbyMobs();
        this.tendPassengers();
    }

    // a placed cart drops until it finds ground, so mining out from under one doesn't leave it hanging
    // in the air. purely vertical: nothing pushes it sideways
    private void settleOnGround() {
        if (this.onGround()) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        this.setDeltaMovement(0.0D, Math.max(this.getDeltaMovement().y - 0.04D, -0.5D), 0.0D);
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    // per-tick upkeep on whoever is riding in the back: hold carried animals in their sitting pose
    private void tendPassengers() {
        for (Entity passenger : this.getPassengers()) {
            setSeatedPose(passenger, true);
            disarmPassenger(passenger);
        }
        // cargo riding the bench is a passenger of the horse rather than of us
        for (Entity passenger : this.benchCargo()) {
            setSeatedPose(passenger, true);
            disarmPassenger(passenger);
        }
    }

    // takes a carried mob's attack target away from it, every tick
    private static void disarmPassenger(Entity passenger) {
        if (!(passenger instanceof Mob mob)) {
            return;
        }
        mob.setTarget(null);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }

    // sets everyone in the back down beside the cart
    private void unloadPassengers() {
        // closes the restore window too, so a sneak-click right after the cart appears isn't undone
        this.cargoRestoreDeadline = 0;
        for (Entity passenger : List.copyOf(this.getPassengers())) {
            this.setDown(passenger);
        }
        // whoever is riding shotgun comes off too, they're a passenger of the horse
        for (Entity passenger : List.copyOf(this.benchCargo())) {
            this.setDown(passenger);
        }
    }

    // puts one rider on the ground beside the cart
    public void setDown(Entity passenger) {
        passenger.stopRiding();
        setSeatedPose(passenger, false);
        // step them clear of the bed so they aren't standing in the scan box when the cooldown lapses
        Vec3 beside = this.position().add(
                new Vec3(this.getBbWidth() * 0.5D + 0.6D, 0.0D, 0.0D)
                        .yRot(-this.getYRot() * ((float) Math.PI / 180.0F)));
        passenger.teleportTo(beside.x, this.getY(), beside.z);
    }

    // true for the first moments of a cart's life, see RESTORE_BOARD_TICKS
    private boolean restoringCargo() {
        return this.tickCount <= this.cargoRestoreDeadline;
    }

    private static void setSeatedPose(Entity passenger, boolean seated) {
        if (passenger instanceof TamableAnimal tamable) {
            // only the pose, never orderedToSit, a dog told to stay put must still be told to stay put once
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

    // server-side: measures how far the cart really travelled this tick and publishes it for the animation
    private void updateRollSpeed() {
        double dx = this.getX() - this.prevX;
        double dz = this.getZ() - this.prevZ;
        this.prevX = this.getX();
        this.prevZ = this.getZ();

        // on the very first tick prevX/prevZ have never held a real position
        if (this.tickCount <= 1) {
            return;
        }

        // quantised, so a cart rolling at a near-constant speed stops re-syncing every tick over differences
        float speed = Math.round(Math.sqrt(dx * dx + dz * dz) * SPEED_SYNC_STEPS) / (float) SPEED_SYNC_STEPS;
        // set() is a no-op when the value is unchanged, so a parked cart syncs nothing at all
        this.entityData.set(DATA_ROLL_SPEED, speed);
    }

    // turns the server's measured speed into the wheel animation's pace
    private void updateClientSpeed() {
        double instant = this.entityData.get(DATA_ROLL_SPEED);

        this.movingTicks = instant >= STILL_SPEED ? Math.min(this.movingTicks + 1, RESUME_TICKS) : 0;

        if (this.movingTicks >= RESUME_TICKS) {
            this.smoothedSpeed += (instant - this.smoothedSpeed) * SPEED_SMOOTHING_UP;
            // remembered as the height to start the ramp from, should this be the last moving tick
            this.coastFromSpeed = this.smoothedSpeed;
            this.coastTicks = 0;
            return;
        }

        // stopped, or one stray drifting tick, which deliberately does not count as moving
        if (this.coastTicks < STOP_RAMP_TICKS) {
            this.coastTicks++;
        }
        this.smoothedSpeed = this.coastTicks >= STOP_RAMP_TICKS
                ? 0.0D
                : this.coastFromSpeed * (1.0D - (double) this.coastTicks / STOP_RAMP_TICKS);
    }

    private void followHorse(AbstractHorse boundHorse) {
        float yaw = boundHorse.getYRot() + YAW_OFFSET;
        // rotation first: setPos rebuilds the bounding box, which is yaw-dependent (see makeBoundingBox)
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        Vec3 target = cartPosFor(boundHorse.getX(), boundHorse.getY(), boundHorse.getZ(), boundHorse.getYRot());
        this.setPos(target.x, target.y, target.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    // client-side glue. mirrors the horse's previous and current transform
    private void glueToHorse(AbstractHorse boundHorse) {
        // null for plain entities, only entities that opt into interpolation have a handler
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

    // cart position for a given horse transform: FOLLOW_OFFSET along the horse's facing
    private static Vec3 cartPosFor(double horseX, double horseY, double horseZ, float horseYaw) {
        double rad = Math.toRadians(horseYaw);
        // horse forward is (-sin, 0, cos)
        return new Vec3(
                horseX - Math.sin(rad) * FOLLOW_OFFSET,
                horseY,
                horseZ + Math.cos(rad) * FOLLOW_OFFSET);
    }

    // where the cart should be drawn this frame, glued to the horse's interpolated transform
    public @Nullable Vec3 gluedRenderPosition(float partialTick) {
        AbstractHorse boundHorse = this.clientHorse();
        if (boundHorse == null) {
            return null;
        }
        Vec3 horsePos = boundHorse.getPosition(partialTick);
        return cartPosFor(horsePos.x, horsePos.y, horsePos.z, renderBodyYaw(boundHorse, partialTick));
    }

    // the cart's render yaw, glued to the horse's interpolated body yaw
    public float gluedRenderYaw(float partialTick) {
        AbstractHorse boundHorse = this.clientHorse();
        return boundHorse == null ? this.getYRot() : renderBodyYaw(boundHorse, partialTick) + YAW_OFFSET;
    }

    private static float renderBodyYaw(AbstractHorse boundHorse, float partialTick) {
        return Mth.rotLerp(partialTick, boundHorse.yBodyRotO, boundHorse.yBodyRot);
    }

    // server-side lookup of the bound horse (by ref, then by stored UUID)
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

    // client-side lookup of the bound horse via the synced network id
    private @Nullable AbstractHorse clientHorse() {
        int id = this.entityData.get(DATA_HORSE_ID);
        return id != -1 && this.level().getEntity(id) instanceof AbstractHorse boundHorse
                ? boundHorse
                : null;
    }

    // cheap both-sides handle on the bound horse, for collision filtering
    private @Nullable AbstractHorse boundHorse() {
        return this.level().isClientSide() ? this.clientHorse() : this.horse;
    }

    // bench seating

    // seat position for a bench rider, as a world-space offset from the horse's position
    public static Vec3 benchSeatOffset(int seatIndex, float horseYaw) {
        double side = seatIndex <= 0 ? -SEAT_SIDE : SEAT_SIDE;
        // local space: +z is the facing direction, so the bench (behind) is negative z
        return new Vec3(side, SEAT_HEIGHT, FOLLOW_OFFSET - SEAT_BEHIND)
                .yRot(-horseYaw * ((float) Math.PI / 180.0F));
    }

    // carriage seating (players or small mobs, boat-style boarding)

    // seat offset (from the cart origin) for a carriage occupant, rotated into world space
    private static Vec3 carriageSeatOffset(int seatIndex, float cartYaw) {
        double side = seatIndex <= 0 ? -REAR_SEAT_SIDE : REAR_SEAT_SIDE;
        // local space: +z is the facing direction, so the carriage (behind the bench) is negative z
        return new Vec3(side, REAR_SEAT_HEIGHT, -REAR_SEAT_BEHIND)
                .yRot(-cartYaw * ((float) Math.PI / 180.0F));
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        int seatIndex = Math.max(0, this.getPassengers().indexOf(passenger));
        return carriageSeatOffset(seatIndex, this.getYRot());
    }

    // whether candidate may ride in the cart: players always
    private boolean canCarry(Entity candidate) {
        if (candidate instanceof Player) {
            return true;
        }
        if (candidate == this.boundHorse()) {
            return false;
        }
        return isCarriableCargo(candidate);
    }

    // the cargo half of canCarry on its own, for callers that have no cart to hand
    public static boolean isCarriableCargo(Entity candidate) {
        return candidate instanceof LivingEntity
                && !(candidate instanceof Player)
                && !(candidate instanceof AbstractHorse)
                && candidate.getBbWidth() < MAX_CARGO_WIDTH
                && !candidate.is(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS);
    }

    // box over the bed (up to BOARD_SCAN_HEIGHT tall) used to catch mobs standing in the carriage
    private AABB boardScanBox() {
        AABB bed = this.getBoundingBox();
        return new AABB(bed.minX, bed.minY, bed.minZ, bed.maxX, bed.minY + BOARD_SCAN_HEIGHT, bed.maxZ)
                .inflate(0.1D);
    }

    // boat-style boarding: any small mob that ends up standing in the carriage climbs aboard
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
                // normally this respects boardingCooldown, so a mob you just shoved out isn't re-grabbed instantly
                candidate.startRiding(this, this.restoringCargo(), true);
            } else {
                // the bed is full, or given over to a chest, but the driver has an empty seat beside them
                candidate.startRiding(boundHorse, false, true);
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation) {
        boolean clientSide = this.level().isClientSide();
        ItemStack held = player.getItemInHand(hand);

        // chest fitting and removal come first: a chest or shears in hand is never a request to sit down
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
            // with a chest fitted the bed is full of chest, so there is nothing to unload
            if (this.hasChest()) {
                if (clientSide) {
                    return InteractionResult.SUCCESS;
                }
                this.openChestMenu(player);
                return InteractionResult.CONSUME;
            }
            // sneak-click unloads the back: the only way to get a mob out again
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

        // prefer the bench (which drives the horse) when the player is allowed there and it has room
        boolean benchHasRoom = boundHorse.getPassengers().size() < 2;
        if (benchHasRoom && this.playerMayTakeBench(boundHorse, player)) {
            ((IHorseData) boundHorse).bh_ridePlayer(player);
            return InteractionResult.CONSUME;
        }
        if (this.rearSeatsFree()) {
            player.startRiding(this);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    // cart chest

    // whether a chest is fitted to the cart. synced either way, so it answers on both sides and in
    // both modes without having to find a horse first
    public boolean hasChest() {
        return this.entityData.get(DATA_HAS_CHEST);
    }

    // the fitted chest's slots. a placed cart owns its storage; a hitched one keeps it on the horse,
    // because the cart entity itself is thrown away and respawned on every reload
    private @Nullable SimpleContainer chestContainer() {
        if (this.isPlaced()) {
            return this.placedChest;
        }
        AbstractHorse boundHorse = this.resolveHorse();
        return boundHorse == null ? null : ((IHorseData) boundHorse).bh_getCartChestContainer();
    }

    // records a chest going on or coming off, wherever this cart keeps that flag
    private void setChestAttached(boolean attached) {
        if (!this.isPlaced()) {
            AbstractHorse boundHorse = this.resolveHorse();
            if (boundHorse != null) {
                ((IHorseData) boundHorse).bh_setCartChest(attached);
            }
        }
        this.entityData.set(DATA_HAS_CHEST, attached);
    }

    // fits a chest from the player's hand
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

    // shears the chest back off, dropping it along with anything still inside
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

        // anyone still browsing gets the screen shut first
        this.closeChestViewers();
        this.dropChest();
        player.getItemInHand(hand).hurtAndBreak(1, player, hand);
        this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
    }

    // sets the chest down along with everything in it, and clears the flag
    private void dropChest() {
        if (!this.hasChest() || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (!this.isPlaced()) {
            AbstractHorse boundHorse = this.resolveHorse();
            if (boundHorse != null) {
                ((IHorseData) boundHorse).bh_dropCartChest();
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

    // opens the fitted chest's storage, a double chest's worth of room
    private void openChestMenu(Player player) {
        SimpleContainer contents = this.chestContainer();
        if (contents == null || !this.playerMayHandleCargo(player)) {
            return;
        }

        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, opener) -> ChestMenu.sixRows(containerId, inventory, contents),
                this.getDisplayName()));
        // openMenu can be refused (a screen already open, the player being removed)
        if (player instanceof ServerPlayer serverPlayer && isViewing(serverPlayer, contents)) {
            this.chestViewers.add(serverPlayer);
        }
    }

    // keeps DATA_CHEST_OPEN in step with who really has the chest screen up
    private void updateChestViewers() {
        if (!this.chestViewers.isEmpty()) {
            SimpleContainer contents = this.chestContainer();
            this.chestViewers.removeIf(viewer -> !isViewing(viewer, contents));
        }
        this.setChestOpen(!this.chestViewers.isEmpty());
    }

    // shuts the screen on anyone browsing, for when the chest (or the whole cart) is going away
    private void closeChestViewers() {
        for (ServerPlayer viewer : List.copyOf(this.chestViewers)) {
            viewer.closeContainer();
        }
        this.chestViewers.clear();
        this.setChestOpen(false);
    }

    // flips the synced lid state, with the vanilla chest sounds on each edge
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

    // cargo handling follows the horse's own inventory gating: with exclusivity on, only the owner may fit
    private boolean playerMayHandleCargo(Player player) {
        AbstractHorse boundHorse = this.resolveHorse();
        // a cart standing on its own belongs to nobody, so there is no owner to check against
        if (boundHorse == null || !BhConfig.horseExclusivityEnabled()) {
            return true;
        }
        UUID owner = ((IHorseData) boundHorse).bh_getOwner();
        if (owner == null || owner.equals(player.getUUID())) {
            return true;
        }

        boundHorse.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.icys-better-horses.not_inventory_owner"));
        }
        return false;
    }

    // bench eligibility mirrors the horse's own rider-gating: the owner and anyone they trust drive
    private boolean playerMayTakeBench(AbstractHorse boundHorse, Player player) {
        if (!BhConfig.horseExclusivityEnabled()) {
            return true;
        }
        IHorseData data = (IHorseData) boundHorse;
        if (data.bh_maySaddleUp(player.getUUID())) {
            return true;
        }
        List<Entity> passengers = boundHorse.getPassengers();
        return !passengers.isEmpty() && data.bh_maySaddleUp(passengers.get(0).getUUID());
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        // the two front bench seats belong to the horse
        return this.rearSeatsFree() && this.canCarry(passenger);
    }

    // whether the carriage can take another occupant
    private boolean rearSeatsFree() {
        // a placed cart is scenery you can store things in, not a ride
        return !this.isPlaced() && !this.hasChest() && this.getPassengers().size() < REAR_SEAT_COUNT;
    }

    // whether an animal may ride shotgun: only once a player has the reins and the seat beside them
    private boolean benchSeatFree(@Nullable AbstractHorse boundHorse) {
        if (boundHorse == null) {
            return false;
        }
        List<Entity> passengers = boundHorse.getPassengers();
        return passengers.size() == 1 && passengers.get(0) instanceof Player;
    }

    // everything riding the bench that isn't a person, i.e
    private List<Entity> benchCargo() {
        // same both-sides lookup as hasChest(): the sneak-click that unloads this is evaluated on the client
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

    // collision

    // collision box built over the cart bed (which sits behind the entity origin)
    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        double rad = Math.toRadians(this.getYRot());
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        // behind the origin = origin - forward * distance, with forward = (-sin, 0, cos)
        double centerX = pos.x + sin * BED_CENTER_BEHIND;
        double centerZ = pos.z - cos * BED_CENTER_BEHIND;

        // axis-aligned extents of the rotated bed rectangle
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
        // never block the horse pulling us (it stands at the cart's front edge) or anyone riding
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

    // entity plumbing

    @Override
    public boolean isPickable() {
        // must be pickable so the player can right-click it to sit down
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        // its position is driven entirely by the horse; being shoved would just fight
        return false;
    }

    // a placed cart is knocked apart the way a boat is: a few hits and it comes back as items. a
    // hitched one is untouchable, since it is the horse's gear rather than a thing in its own right
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

    // drops the cart itself plus the chest and everything inside it, then removes the entity
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
        // a placed cart is a real thing in the world and has to persist. a hitched one stays pure
        // derived state: the horse's gear re-spawns it on load
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

    // only a placed cart ever reaches these: a hitched one returns false from shouldBeSaved
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

    // slot/stack pair for the placed chest's saved contents
    public record BhCartSlot(int slot, ItemStack stack) {
        public static final com.mojang.serialization.Codec<BhCartSlot> CODEC =
                com.mojang.serialization.codecs.RecordCodecBuilder.create(instance -> instance.group(
                        com.mojang.serialization.Codec.INT.fieldOf("Slot").forGetter(BhCartSlot::slot),
                        ItemStack.CODEC.fieldOf("Item").forGetter(BhCartSlot::stack)
                ).apply(instance, BhCartSlot::new));
    }

    // GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // two controllers over disjoint bones: the roll drives cart/axle/wheels/brace
        controllers.add(new AnimationController<>("wheels", 0, this::wheelPredicate));
        controllers.add(new AnimationController<>("chest", 0, this::chestPredicate));
        controllers.add(new AnimationController<>("pose", 0, this::posePredicate));
    }

    // a placed cart holds the stand alone pose: shafts down, resting on the ground. the clip is a
    // single held pose rather than movement, so looping it just keeps the cart sat there
    private PlayState posePredicate(AnimationTest<HorseCartEntity> test) {
        if (!this.isPlaced()) {
            if (test.controller().getCurrentRawAnimation() != null) {
                test.controller().reset();
            }
            return PlayState.STOP;
        }
        return test.setAndContinue(STANDING);
    }

    // lid state follows whether anyone has the storage screen open: the open clip while it's up
    private PlayState chestPredicate(AnimationTest<HorseCartEntity> test) {
        if (!this.hasChest()) {
            this.chestAnimPrimed = false;
            // cleared outright rather than frozen (see the wheel controller for why the two differ)
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
            // never seen open on this client, leave the lid in the model's closed rest pose rather than playing
            return PlayState.STOP;
        }
        return test.setAndContinue(CHEST_CLOSING);
    }

    private PlayState wheelPredicate(AnimationTest<HorseCartEntity> test) {
        if (this.smoothedSpeed <= 0.0D) {
            // parked. returning STOP is NOT enough on its own
            test.setControllerSpeed(0.0F);
            return PlayState.STOP;
        }
        // MIN_ANIM_SPEED keeps the wheels legibly turning behind a dawdling horse
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
