package icy.betterhorses.net.entity;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
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

/**
 * A standalone cart entity that is pulled behind the horse that owns it.
 *
 * <p>The cart is <b>derived state</b>: it is spawned/despawned by the horse's gear
 * ({@code AbstractHorseMixin.bh_tickCart}) and is never written to disk
 * ({@link #shouldBeSaved()} returns {@code false}). Each tick it snaps to a fixed offset along its
 * bound horse's facing and copies the horse's yaw. This keeps it from ever duplicating or orphaning
 * across chunk loads / the whistle-respawn system.</p>
 *
 * <p><b>Geometry note:</b> the model's bed sits ~{@link #BED_CENTER_BEHIND} blocks <i>behind</i> the
 * entity origin (the origin is up at the harness end). So the collision box is built over the bed in
 * {@link #makeBoundingBox(Vec3)} rather than around the origin — otherwise you'd hit an invisible
 * wall in front of the cart and still fall through the bed itself.</p>
 *
 * <p>Rendering is handled by {@code HorseCartRenderer} (a GeckoLib {@code GeoEntityRenderer}).
 * GeckoLib reads movement from {@code getDeltaMovement()}, which isn't meaningful for an entity
 * whose position we snap manually, so the wheel animation is driven by a smoothed client-side
 * measure of how far the cart actually moved per tick: fully stopped when parked, easing from slow
 * up to the authored speed as the horse gets going.</p>
 */
public final class HorseCartEntity extends Entity implements GeoEntity {

    /** Entity-type dimensions. The real collision box comes from {@link #makeBoundingBox(Vec3)}. */
    public static final float WIDTH = 2.0F;
    public static final float HEIGHT = 1.5F;

    /**
     * Offset from the horse center along the horse's facing, in blocks. Positive = ahead of the
     * horse (the model bed extends backward from its origin, so placing the entity ahead glues the
     * visible cart right behind the horse). Tunable.
     */
    private static final double FOLLOW_OFFSET = 0.0D;
    /** Added to the horse yaw for the cart's facing; flip to 180 if the model faces the wrong way. */
    private static final float YAW_OFFSET = 0.0F;

    // --- Bed collision box (model units / 16). Bed floor cube is [-15,12,18]..[15,13,54]. ---
    /** Distance behind the entity origin to the center of the cart bed. */
    private static final double BED_CENTER_BEHIND = 2.2D;
    private static final double BED_HALF_WIDTH = 0.95D;
    private static final double BED_HALF_LENGTH = 1.15D;
    /** Top of the bed floor — the surface you stand on. */
    private static final double BED_FLOOR_HEIGHT = 0.8125D;

    // --- Bench seats (two riders, side by side). Bench cube is [-15,21,17]..[15,23,28]. ---
    private static final double SEAT_HEIGHT = 1.15D;
    /** Distance behind the entity origin to the bench. */
    private static final double SEAT_BEHIND = 1.4D;
    /** Sideways spacing of the two seats from the cart center line. */
    private static final double SEAT_SIDE = 0.45D;

    // --- Carriage seats (two occupants in the bed behind the bench: players or small mobs). ---
    private static final int REAR_SEAT_COUNT = 2;
    /** Distance behind the entity origin to the carriage seats (further back than the bench). */
    private static final double REAR_SEAT_BEHIND = 2.55D;
    /** Sideways spacing of the two carriage seats from the cart center line. */
    private static final double REAR_SEAT_SIDE = 0.45D;
    /**
     * Seat height above the entity origin. Set flush with the underside of the bed (the floor cube
     * runs y 12..13 in the model, so 12/16 = 0.75) rather than on top of it: riders drop into the
     * cart the way a minecart passenger does, and the side rails — which top out at 21/16 — hide
     * their legs. 0.75 is as low as this can go before feet poke out beneath the cart.
     */
    private static final double REAR_SEAT_HEIGHT = 0.75D;
    /**
     * Width limit for anything riding in the back, taken from an actual boat so the cart carries
     * exactly what a boat carries.
     *
     * <p>Vanilla's rule (in {@code AbstractBoat.tick}) is simply
     * {@code passenger.getBbWidth() < boat.getBbWidth()} — width only, height is never considered,
     * which is why a boat will happily take a two-and-a-half block tall enderman but refuses a horse.
     * Reading the number off the boat's own entity type rather than hard-coding it means the cart
     * keeps matching if Mojang ever resizes boats.</p>
     */
    private static final float MAX_CARGO_WIDTH = EntityTypes.OAK_BOAT.getWidth();
    /** How far above the bed floor to look for mobs standing in the carriage, to auto-board them. */
    private static final double BOARD_SCAN_HEIGHT = 1.6D;
    /**
     * How long a freshly spawned cart force-boards mobs found in its bed, ignoring vanilla's 60-tick
     * re-boarding cooldown. Long enough to cover the reload hand-off (see {@link #shouldBeSaved()}).
     */
    private static final int RESTORE_BOARD_TICKS = 80;

    // --- Wheel animation pacing ---
    /** Per-tick easing toward the measured speed while speeding up; lower = longer spin-up ramp. */
    private static final double SPEED_SMOOTHING_UP = 0.12D;
    /**
     * Ticks the wheels take to coast from rolling to a dead stop once the horse stops — 0.8 seconds.
     *
     * <p>The spin-<i>down</i> is a fixed linear ramp rather than the easing used on the way up, and
     * that asymmetry is the whole point: an exponential decay approaches zero without ever arriving,
     * so the wheels were left creeping round almost indefinitely after the cart had visibly parked.
     * A ramp lands on exactly zero at a known tick, whatever speed the cart was doing when it
     * stopped. This is also why the single looping clip needs no separate start/stop animations —
     * the controller's playback speed is what starts and stops it.</p>
     */
    private static final int STOP_RAMP_TICKS = 16;
    /**
     * Below this (blocks/tick) the cart counts as parked and the animation stops entirely.
     *
     * <p>Kept well clear of zero as a second line of defence behind {@link #DATA_ROLL_SPEED}. Any
     * amount of phantom movement that gets past this threshold is enough to keep restarting the
     * coast-down, and since {@link #MIN_ANIM_SPEED} puts a floor under the playback rate, the result
     * is wheels that turn at a steady 15% speed forever rather than ever arriving at a stop. 0.02
     * blocks/tick is 0.4 blocks per second — an order of magnitude below the slowest real walk (a
     * horse at a walk covers roughly 0.24 blocks/tick), so nothing genuinely rolling reads as
     * parked.</p>
     */
    private static final double STILL_SPEED = 0.02D;
    /**
     * Consecutive moving ticks needed to call off a coast-down already under way. A lone twitchy
     * tick must never restart the wheels — that is the other half of why the halt never arrived.
     */
    private static final int RESUME_TICKS = 2;
    /** Resolution of the synced speed, in steps per block/tick. */
    private static final int SPEED_SYNC_STEPS = 128;
    /** Speed (blocks/tick) at which the animation runs at its authored rate. */
    private static final double REFERENCE_SPEED = 0.35D;
    private static final double MIN_ANIM_SPEED = 0.15D;
    private static final double MAX_ANIM_SPEED = 1.5D;

    // --- Animation keys, exactly as they appear in horse_cart.animation.json ---
    private static final String WHEEL_ANIM_NAME = "wheel moving2";
    private static final String CHEST_OPEN_ANIM_NAME = "chest";
    private static final String CHEST_CLOSE_ANIM_NAME = "chest close";

    private static final RawAnimation WHEELS_ROLLING = RawAnimation.begin().thenLoop(WHEEL_ANIM_NAME);
    // Play-and-hold: both lid clips end on the pose they were aiming for and stay there, so the lid
    // sits open for as long as someone is browsing rather than springing back on its own.
    private static final RawAnimation CHEST_OPENING = RawAnimation.begin().thenPlayAndHold(CHEST_OPEN_ANIM_NAME);
    private static final RawAnimation CHEST_CLOSING = RawAnimation.begin().thenPlayAndHold(CHEST_CLOSE_ANIM_NAME);

    // Network id of the bound horse, synced so the client can glue the cart to it directly (the
    // server-side UUID/ref aren't available client-side).
    private static final EntityDataAccessor<Integer> DATA_HORSE_ID =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.INT);
    // Whether anyone currently has the cart's chest open. Synced so the lid animation plays for
    // every nearby client, not just the player browsing it.
    private static final EntityDataAccessor<Boolean> DATA_CHEST_OPEN =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.BOOLEAN);
    /**
     * How far the cart travelled last tick, in blocks, measured and synced by the <b>server</b>.
     *
     * <p>This is the one signal the wheel animation trusts, and it has to come from the server. The
     * client's copy of the cart is pinned to a horse whose position is itself being interpolated and
     * corrected against the server every tick, so measuring displacement there reports a parked cart
     * as permanently creeping — which is exactly what kept the wheels turning after the cart had
     * stopped. Server-side there is no interpolation: a parked horse's position is bit-for-bit
     * identical tick to tick, so a stopped cart measures a true zero.</p>
     */
    private static final EntityDataAccessor<Float> DATA_ROLL_SPEED =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private @Nullable UUID horseUuid;
    private @Nullable AbstractHorse horse;

    /** Tick up to which this cart force-boards mobs in its bed; zeroed once cargo is unloaded by hand. */
    private int cargoRestoreDeadline = RESTORE_BOARD_TICKS;

    /**
     * Players with this cart's chest screen open. Server-side only, so these are always
     * {@link ServerPlayer}s — which is also the side that can close a screen back down again.
     * Drives {@link #DATA_CHEST_OPEN}.
     */
    private final List<ServerPlayer> chestViewers = new ArrayList<>();
    /**
     * Client-side latch: false until this client has actually seen the lid open. A cart that comes
     * into view with its chest already shut has nothing to animate — the model's rest pose is closed
     * — so without this every cart would slam its lid shut the moment it was rendered.
     */
    private boolean chestAnimPrimed = false;

    // Server-side previous position, for measuring the cart's real per-tick travel.
    private double prevX;
    private double prevZ;

    // Client-side animation pacing, derived from the server's measure.
    private double smoothedSpeed;
    /** Wheel speed at the moment the cart stopped — the top of the coast-down ramp. */
    private double coastFromSpeed;
    /** Ticks into the coast-down ramp; 0 whenever the cart is actually moving. */
    private int coastTicks;
    /** Consecutive ticks of real movement, capped at {@link #RESUME_TICKS}. */
    private int movingTicks;

    public HorseCartEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.prevX = this.getX();
        this.prevZ = this.getZ();
    }

    /** Spawns a cart bound to {@code horse}, positioned relative to it. Server-side only. */
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

        AbstractHorse boundHorse = this.resolveHorse();
        if (boundHorse == null || !boundHorse.isAlive() || boundHorse.isRemoved()
                || !((IHorseData) boundHorse).bh_hasCartGear()) {
            // Nobody should be left staring into the storage of a cart that is about to stop existing.
            this.closeChestViewers();
            this.discard();
            return;
        }
        this.followHorse(boundHorse);
        this.updateRollSpeed();
        this.updateChestViewers();
        this.tryBoardNearbyMobs();
        this.tendPassengers();
    }

    /**
     * Per-tick upkeep on whoever is riding in the back: hold carried animals in their sitting pose,
     * and keep anything with a grudge disarmed.
     *
     * <p>Humanoid riders (players, villagers, piglins…) fold their legs on their own — vanilla's
     * {@code HumanoidModel} does that for any passenger. Animals have no such pose: a pig or a sheep
     * stands in a vanilla boat too. The ones that <i>do</i> own a sit animation get it switched on
     * here, and it is re-asserted every tick because their AI keeps ticking while they ride and can
     * clear the flag underneath us.</p>
     */
    private void tendPassengers() {
        for (Entity passenger : this.getPassengers()) {
            setSeatedPose(passenger, true);
            disarmPassenger(passenger);
        }
        // Cargo riding the bench is a passenger of the horse rather than of us, so it needs the same
        // treatment applied from here — nothing else is watching it.
        for (Entity passenger : this.benchCargo()) {
            setSeatedPose(passenger, true);
            disarmPassenger(passenger);
        }
    }

    /**
     * Takes a carried mob's attack target away from it, every tick.
     *
     * <p>Most of the "cargo can't fight back" work is done by {@code MobCartPassengerMixin}, which
     * catches the whole {@code Mob} hierarchy at once. This covers the hole in it: brain-driven mobs
     * keep their target in a memory rather than the field the mixin blanks, and read it back through
     * their <i>own</i> {@code getTarget} override, so the mixin never sees them. Piglins and breezes
     * are the ones that matter here — both are small enough to ride in the bed and both attack at
     * range, so without this a piglin would keep firing its crossbow from the back of the cart.
     * Erasing an absent memory is a no-op, so this is safe on animals too.</p>
     */
    private static void disarmPassenger(Entity passenger) {
        if (!(passenger instanceof Mob mob)) {
            return;
        }
        mob.setTarget(null);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }

    /**
     * Sets everyone in the back down beside the cart. Vanilla's 60-tick boarding cooldown, stamped
     * on each of them by {@code removePassenger}, is what stops {@link #tryBoardNearbyMobs()} from
     * immediately hauling them back in.
     */
    private void unloadPassengers() {
        // Closes the restore window too, so a sneak-click right after the cart appears isn't undone
        // by the force-boarding below.
        this.cargoRestoreDeadline = 0;
        for (Entity passenger : List.copyOf(this.getPassengers())) {
            this.setDown(passenger);
        }
        // Whoever is riding shotgun comes off too — they're a passenger of the horse, so the loop
        // above never sees them, and a sneak-click that emptied the bed but left an animal sat next
        // to the driver would look like it half-worked.
        for (Entity passenger : List.copyOf(this.benchCargo())) {
            this.setDown(passenger);
        }
    }

    /** Puts one rider on the ground beside the cart. */
    private void setDown(Entity passenger) {
        passenger.stopRiding();
        setSeatedPose(passenger, false);
        // Step them clear of the bed so they aren't standing in the scan box when the cooldown
        // lapses; without this a sneak-click on a parked cart just re-loads itself.
        Vec3 beside = this.position().add(
                new Vec3(this.getBbWidth() * 0.5D + 0.6D, 0.0D, 0.0D)
                        .yRot(-this.getYRot() * ((float) Math.PI / 180.0F)));
        passenger.teleportTo(beside.x, this.getY(), beside.z);
    }

    /** True for the first moments of a cart's life — see {@link #RESTORE_BOARD_TICKS}. */
    private boolean restoringCargo() {
        return this.tickCount <= this.cargoRestoreDeadline;
    }

    private static void setSeatedPose(Entity passenger, boolean seated) {
        if (passenger instanceof TamableAnimal tamable) {
            // Only the pose, never orderedToSit — a dog told to stay put must still be told to stay
            // put once it hops back out.
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

    /**
     * Server-side: measures how far the cart really travelled this tick and publishes it for the
     * animation. See {@link #DATA_ROLL_SPEED} for why this can't be done on the client.
     */
    private void updateRollSpeed() {
        double dx = this.getX() - this.prevX;
        double dz = this.getZ() - this.prevZ;
        this.prevX = this.getX();
        this.prevZ = this.getZ();

        // On the very first tick prevX/prevZ have never held a real position, so the "displacement"
        // would be the cart's whole distance from the world origin.
        if (this.tickCount <= 1) {
            return;
        }

        // Quantised, so a cart rolling at a near-constant speed stops re-syncing every tick over
        // differences far too small to see.
        float speed = Math.round(Math.sqrt(dx * dx + dz * dz) * SPEED_SYNC_STEPS) / (float) SPEED_SYNC_STEPS;
        // set() is a no-op when the value is unchanged, so a parked cart syncs nothing at all.
        this.entityData.set(DATA_ROLL_SPEED, speed);
    }

    /**
     * Turns the server's measured speed into the wheel animation's pace.
     *
     * <p>Rolling eases toward the reported speed, so the wheels spin up gradually rather than
     * snapping to full pelt. The moment the cart stops it switches to a fixed
     * {@link #STOP_RAMP_TICKS} ramp down to exactly zero, so the roll always ends 0.8s after the
     * horse does — see {@link #STOP_RAMP_TICKS} for why this half isn't eased too.</p>
     */
    private void updateClientSpeed() {
        double instant = this.entityData.get(DATA_ROLL_SPEED);

        this.movingTicks = instant >= STILL_SPEED ? Math.min(this.movingTicks + 1, RESUME_TICKS) : 0;

        if (this.movingTicks >= RESUME_TICKS) {
            this.smoothedSpeed += (instant - this.smoothedSpeed) * SPEED_SMOOTHING_UP;
            // Remembered as the height to start the ramp from, should this be the last moving tick.
            this.coastFromSpeed = this.smoothedSpeed;
            this.coastTicks = 0;
            return;
        }

        // Stopped — or one stray drifting tick, which deliberately does not count as moving.
        if (this.coastTicks < STOP_RAMP_TICKS) {
            this.coastTicks++;
        }
        this.smoothedSpeed = this.coastTicks >= STOP_RAMP_TICKS
                ? 0.0D
                : this.coastFromSpeed * (1.0D - (double) this.coastTicks / STOP_RAMP_TICKS);
    }

    private void followHorse(AbstractHorse boundHorse) {
        float yaw = boundHorse.getYRot() + YAW_OFFSET;
        // Rotation first: setPos rebuilds the bounding box, which is yaw-dependent (see makeBoundingBox).
        this.setYRot(yaw);
        this.setYBodyRot(yaw);
        this.setYHeadRot(yaw);

        Vec3 target = cartPosFor(boundHorse.getX(), boundHorse.getY(), boundHorse.getZ(), boundHorse.getYRot());
        this.setPos(target.x, target.y, target.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * Client-side glue. Mirrors the horse's <i>previous and current</i> transform, so the cart's
     * render interpolation is bit-for-bit the horse's — it moves as one piece with it instead of
     * chasing a position of its own. Server position packets are cancelled first: left active they
     * lerp the cart toward the server's copy while we snap it to the horse, which is what made it
     * jitter.
     */
    private void glueToHorse(AbstractHorse boundHorse) {
        // Null for plain entities — only entities that opt into interpolation have a handler.
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

    /** Cart position for a given horse transform: FOLLOW_OFFSET along the horse's facing. */
    private static Vec3 cartPosFor(double horseX, double horseY, double horseZ, float horseYaw) {
        double rad = Math.toRadians(horseYaw);
        // Horse forward is (-sin, 0, cos).
        return new Vec3(
                horseX - Math.sin(rad) * FOLLOW_OFFSET,
                horseY,
                horseZ + Math.cos(rad) * FOLLOW_OFFSET);
    }

    /**
     * Where the cart should be drawn this frame, glued to the horse's interpolated transform.
     * Used by the renderer to set the render-state position directly, so the cart never lags or
     * jitters regardless of client entity tick order. Returns null if the horse isn't loaded.
     */
    public @Nullable Vec3 gluedRenderPosition(float partialTick) {
        AbstractHorse boundHorse = this.clientHorse();
        if (boundHorse == null) {
            return null;
        }
        Vec3 horsePos = boundHorse.getPosition(partialTick);
        return cartPosFor(horsePos.x, horsePos.y, horsePos.z, renderBodyYaw(boundHorse, partialTick));
    }

    /**
     * The cart's render yaw, glued to the horse's interpolated <b>body</b> yaw.
     *
     * <p>Two subtleties combine here. GeckoLib derives a non-living entity's model rotation from
     * {@code getVisualRotationYInDegrees()} — the raw {@code getYRot()} with no interpolation — so
     * the cart's facing would step once per tick. And {@code getYRot()} on a player-<i>controlled</i>
     * horse is updated per frame from the mouse and snaps at tick boundaries. Because the cart's bed
     * sits far from its rotation pivot, either of those turns a small yaw discontinuity into a big
     * positional lurch of the visible geometry — the "microteleport" on steering. Matching the
     * horse's smoothly interpolated body yaw (exactly what the horse's own body render uses) makes
     * the cart track the horse's visible facing with no discontinuity.</p>
     */
    public float gluedRenderYaw(float partialTick) {
        AbstractHorse boundHorse = this.clientHorse();
        return boundHorse == null ? this.getYRot() : renderBodyYaw(boundHorse, partialTick) + YAW_OFFSET;
    }

    private static float renderBodyYaw(AbstractHorse boundHorse, float partialTick) {
        return Mth.rotLerp(partialTick, boundHorse.yBodyRotO, boundHorse.yBodyRot);
    }

    /** Server-side lookup of the bound horse (by ref, then by stored UUID). */
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

    /** Client-side lookup of the bound horse via the synced network id. */
    private @Nullable AbstractHorse clientHorse() {
        int id = this.entityData.get(DATA_HORSE_ID);
        return id != -1 && this.level().getEntity(id) instanceof AbstractHorse boundHorse
                ? boundHorse
                : null;
    }

    /** Cheap both-sides handle on the bound horse, for collision filtering. */
    private @Nullable AbstractHorse boundHorse() {
        return this.level().isClientSide() ? this.clientHorse() : this.horse;
    }

    // --- Bench seating ------------------------------------------------------

    /**
     * Seat position for a bench rider, as a world-space offset from the <b>horse's</b> position.
     *
     * <p>Bench riders are passengers of the <b>horse</b>, not the cart — they're merely
     * <i>attached</i> here. That way steering, speed, jumping and all the vehicle networking stay
     * exactly vanilla, and whoever takes the driver's seat simply drives the horse from the cart.
     * The cart sits {@link #FOLLOW_OFFSET} along the horse's facing and the bench is
     * {@link #SEAT_BEHIND} back from the cart origin, so the two combine into one offset.</p>
     */
    public static Vec3 benchSeatOffset(int seatIndex, float horseYaw) {
        double side = seatIndex <= 0 ? -SEAT_SIDE : SEAT_SIDE;
        // Local space: +z is the facing direction, so the bench (behind) is negative z.
        return new Vec3(side, SEAT_HEIGHT, FOLLOW_OFFSET - SEAT_BEHIND)
                .yRot(-horseYaw * ((float) Math.PI / 180.0F));
    }

    // --- Carriage seating (players or small mobs, boat-style boarding) ---------

    /** Seat offset (from the cart origin) for a carriage occupant, rotated into world space. */
    private static Vec3 carriageSeatOffset(int seatIndex, float cartYaw) {
        double side = seatIndex <= 0 ? -REAR_SEAT_SIDE : REAR_SEAT_SIDE;
        // Local space: +z is the facing direction, so the carriage (behind the bench) is negative z.
        return new Vec3(side, REAR_SEAT_HEIGHT, -REAR_SEAT_BEHIND)
                .yRot(-cartYaw * ((float) Math.PI / 180.0F));
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        int seatIndex = Math.max(0, this.getPassengers().indexOf(passenger));
        return carriageSeatOffset(seatIndex, this.getYRot());
    }

    /**
     * Whether {@code candidate} may ride in the cart: players always, everything else by exactly the
     * rule a boat uses.
     *
     * <p>Vanilla boats gate on three things and nothing else — the rider must be a
     * {@link LivingEntity}, it must be narrower than the boat, and it must not carry the
     * {@code minecraft:cannot_be_pushed_onto_boats} tag (which is what keeps fish, squid and dolphins
     * from being scooped up as you sail past). Deferring to that tag rather than inventing a list
     * means data packs and other mods can adjust what the cart hauls the same way they adjust boats.
     * Horses and their relatives fall out of the width test on their own, but they are also excluded
     * outright: a horse riding in the cart its twin is pulling is not a thing worth allowing.</p>
     */
    private boolean canCarry(Entity candidate) {
        if (candidate instanceof Player) {
            return true;
        }
        if (candidate == this.boundHorse()) {
            return false;
        }
        return isCarriableCargo(candidate);
    }

    /**
     * The cargo half of {@link #canCarry} on its own, for callers that have no cart to hand — the
     * horse's bench seat gate lives over in {@code AbstractHorseMixin.canAddPassenger}.
     */
    public static boolean isCarriableCargo(Entity candidate) {
        return candidate instanceof LivingEntity
                && !(candidate instanceof Player)
                && !(candidate instanceof AbstractHorse)
                && candidate.getBbWidth() < MAX_CARGO_WIDTH
                && !candidate.is(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS);
    }

    /** Box over the bed (up to {@link #BOARD_SCAN_HEIGHT} tall) used to catch mobs standing in the carriage. */
    private AABB boardScanBox() {
        AABB bed = this.getBoundingBox();
        return new AABB(bed.minX, bed.minY, bed.minZ, bed.maxX, bed.minY + BOARD_SCAN_HEIGHT, bed.maxZ)
                .inflate(0.1D);
    }

    /**
     * Boat-style boarding: any small mob that ends up standing in the carriage climbs aboard, until
     * both back seats are taken. Players board by right-clicking (see {@link #interact}). Server-side.
     */
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
                // Normally this respects boardingCooldown, so a mob you just shoved out isn't
                // re-grabbed instantly. The exception is a cart that has only just appeared: on a
                // reload the mobs in the bed are the ones the saved cart set down a moment ago, and
                // vanilla stamped them with a 60-tick cooldown on the way out. Force those aboard so
                // a reload doesn't scatter the cargo — the loop's own seat check bounds how many
                // get on, because forcing skips canAddPassenger entirely.
                candidate.startRiding(this, this.restoringCargo(), true);
            } else {
                // The bed is full, or given over to a chest — but the driver has an empty seat
                // beside them, so this one rides shotgun. Never forced: the seat gate in the horse's
                // canAddPassenger is the only thing keeping cargo out of the driver's seat.
                candidate.startRiding(boundHorse, false, true);
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation) {
        boolean clientSide = this.level().isClientSide();
        ItemStack held = player.getItemInHand(hand);

        // Chest fitting and removal come first: a chest or shears in hand is never a request to sit
        // down. Both must claim the click on the client too, or the chest would be placed as a block
        // against the cart instead.
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
            // With a chest fitted the bed is full of chest, so there is nothing to unload — the same
            // sneak-click opens its storage instead.
            if (this.hasChest()) {
                if (clientSide) {
                    return InteractionResult.SUCCESS;
                }
                this.openChestMenu(player);
                return InteractionResult.CONSUME;
            }
            // Sneak-click unloads the back: the only way to get a mob out again, since mobs never
            // dismount on their own and the auto-boarder would grab anything standing in the bed.
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

        // Prefer the bench (which drives the horse) when the player is allowed there and it has room;
        // otherwise drop into a carriage seat in the back. Bench riders are passengers of the horse
        // (routed through its normal ride path so ownership gating applies); carriage riders are ours.
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

    // --- Cart chest ---------------------------------------------------------

    /**
     * Whether a chest is fitted to the cart. The flag lives on the bound horse (the cart is derived
     * state that never survives a reload), and is synced, so this answers correctly on both sides.
     */
    public boolean hasChest() {
        // Resolve rather than read the cached ref on the server: this gates boarding, and answering
        // "no chest" for a horse we merely haven't looked up yet would let a mob into a full bed.
        AbstractHorse boundHorse = this.level().isClientSide() ? this.clientHorse() : this.resolveHorse();
        return boundHorse != null && ((IHorseData) boundHorse).bh_hasCartChest();
    }

    /** The fitted chest's 27 slots, or null when there is no chest or no horse to hang them off. */
    private @Nullable SimpleContainer chestContainer() {
        AbstractHorse boundHorse = this.resolveHorse();
        return boundHorse == null ? null : ((IHorseData) boundHorse).bh_getCartChestContainer();
    }

    /**
     * Fits a chest from the player's hand. Takes one chest, sets everyone in the back down (the
     * chest occupies the whole bed) and flags the horse. Server-side.
     */
    private boolean attachChest(Player player, ItemStack held) {
        AbstractHorse boundHorse = this.resolveHorse();
        if (boundHorse == null || !this.playerMayHandleCargo(boundHorse, player)) {
            return false;
        }

        this.unloadPassengers();
        ((IHorseData) boundHorse).bh_setCartChest(true);
        held.consume(1, player);
        this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, 1.0F);
        return true;
    }

    /**
     * Shears the chest back off, dropping it along with anything still inside. Refused while the
     * chest holds items — emptying it first is what keeps a stack from being scattered across the
     * ground by a stray click. Server-side.
     */
    private void shearChest(Player player, InteractionHand hand) {
        AbstractHorse boundHorse = this.resolveHorse();
        if (boundHorse == null || !this.playerMayHandleCargo(boundHorse, player)) {
            return;
        }

        SimpleContainer contents = ((IHorseData) boundHorse).bh_getCartChestContainer();
        if (!contents.isEmpty()) {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(
                        Component.translatable("message.icys-better-horses.cart_chest_not_empty"));
            }
            return;
        }

        // Anyone still browsing gets the screen shut first: past this point the container is no
        // longer reachable, so items dropped into it would be stranded.
        this.closeChestViewers();
        // Drops the chest item and — defensively, in case anything slipped in behind the check
        // above — whatever the container still holds.
        ((IHorseData) boundHorse).bh_dropCartChest();
        player.getItemInHand(hand).hurtAndBreak(1, player, hand);
        this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
    }

    /** Opens the fitted chest's storage — a double chest's worth of room. Server-side. */
    private void openChestMenu(Player player) {
        AbstractHorse boundHorse = this.resolveHorse();
        SimpleContainer contents = this.chestContainer();
        if (boundHorse == null || contents == null || !this.playerMayHandleCargo(boundHorse, player)) {
            return;
        }

        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, opener) -> ChestMenu.sixRows(containerId, inventory, contents),
                this.getDisplayName()));
        // openMenu can be refused (a screen already open, the player being removed), so take the
        // menu that actually ended up in front of them rather than assuming ours did.
        if (player instanceof ServerPlayer serverPlayer && isViewing(serverPlayer, contents)) {
            this.chestViewers.add(serverPlayer);
        }
    }

    /**
     * Keeps {@link #DATA_CHEST_OPEN} in step with who really has the chest screen up, which is what
     * drives the lid animation. A viewer drops off the moment their open menu is no longer this
     * cart's chest — closing the screen, opening something else, dying and disconnecting all land
     * here, so there is no separate close hook to miss.
     */
    private void updateChestViewers() {
        if (!this.chestViewers.isEmpty()) {
            SimpleContainer contents = this.chestContainer();
            this.chestViewers.removeIf(viewer -> !isViewing(viewer, contents));
        }
        this.setChestOpen(!this.chestViewers.isEmpty());
    }

    /** Shuts the screen on anyone browsing, for when the chest (or the whole cart) is going away. */
    private void closeChestViewers() {
        for (ServerPlayer viewer : List.copyOf(this.chestViewers)) {
            viewer.closeContainer();
        }
        this.chestViewers.clear();
        this.setChestOpen(false);
    }

    /** Flips the synced lid state, with the vanilla chest sounds on each edge. */
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

    /**
     * Cargo handling follows the horse's own inventory gating: with exclusivity on, only the owner
     * may fit, open or shear off the chest. Refusal is announced the same way the horse announces a
     * refused inventory, so the two read as one rule.
     */
    private boolean playerMayHandleCargo(AbstractHorse boundHorse, Player player) {
        if (!BhConfig.horseExclusivityEnabled()) {
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

    /** Bench eligibility mirrors the horse's own owner-gating: only the owner drives, unless exclusivity is off. */
    private boolean playerMayTakeBench(AbstractHorse boundHorse, Player player) {
        if (!BhConfig.horseExclusivityEnabled()) {
            return true;
        }
        UUID owner = ((IHorseData) boundHorse).bh_getOwner();
        if (owner == null || owner.equals(player.getUUID())) {
            return true;
        }
        List<Entity> passengers = boundHorse.getPassengers();
        return !passengers.isEmpty() && passengers.get(0).getUUID().equals(owner);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        // The two front bench seats belong to the horse; the cart itself only seats the two carriage
        // occupants behind it. Players always fit; mobs must be smaller than a horse.
        return this.rearSeatsFree() && this.canCarry(passenger);
    }

    /**
     * Whether the carriage can take another occupant. A fitted chest fills the bed, so the two back
     * seats are gone entirely while it is on — a loaded cart carries cargo or passengers, not both.
     */
    private boolean rearSeatsFree() {
        return !this.hasChest() && this.getPassengers().size() < REAR_SEAT_COUNT;
    }

    /**
     * Whether an animal may ride shotgun: only once a player has the reins and the seat beside them
     * is empty. Both halves matter — vanilla hands control to the first passenger and only if it is
     * a player, so cargo in that seat would leave the horse unsteerable.
     */
    private boolean benchSeatFree(@Nullable AbstractHorse boundHorse) {
        if (boundHorse == null) {
            return false;
        }
        List<Entity> passengers = boundHorse.getPassengers();
        return passengers.size() == 1 && passengers.get(0) instanceof Player;
    }

    /** Everything riding the bench that isn't a person — i.e. cargo sat beside the driver. */
    private List<Entity> benchCargo() {
        // Same both-sides lookup as hasChest(): the sneak-click that unloads this is evaluated on
        // the client too, where resolveHorse() has nothing to resolve against.
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

    // --- Collision ----------------------------------------------------------

    /**
     * Collision box built over the cart <i>bed</i> (which sits behind the entity origin), sized to
     * the yaw-rotated bed footprint and capped at the bed floor so you can walk around on it.
     */
    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        double rad = Math.toRadians(this.getYRot());
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        // Behind the origin = origin - forward * distance, with forward = (-sin, 0, cos).
        double centerX = pos.x + sin * BED_CENTER_BEHIND;
        double centerZ = pos.z - cos * BED_CENTER_BEHIND;

        // Axis-aligned extents of the rotated bed rectangle.
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
        // Never block the horse pulling us (it stands at the cart's front edge) or anyone riding it,
        // and never block our own passengers — otherwise the horse would shove against its own cart.
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

    // --- Entity plumbing ----------------------------------------------------

    @Override
    public boolean isPickable() {
        // Must be pickable so the player can right-click it to sit down.
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        // Its position is driven entirely by the horse; being shoved would just fight that.
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        // An empty cart stays pure derived state: the horse's gear re-spawns it on load, so writing
        // it would only risk orphans and duplicates.
        //
        // A loaded one has to be written, though. Vanilla stores passengers *inside* their vehicle
        // (Entity.save returns false for anything that is riding), so a cart that skipped the save
        // took its riders down with it — mobs left in the back simply ceased to exist on reload.
        // The restored cart still has no horse binding, so its first tick discards it and sets the
        // riders down exactly where they were; the horse's own tick spawns the real cart and
        // tryBoardNearbyMobs picks them straight back up.
        return !this.getPassengers().isEmpty();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HORSE_ID, -1);
        builder.define(DATA_CHEST_OPEN, false);
        builder.define(DATA_ROLL_SPEED, 0.0F);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    // --- GeckoLib -----------------------------------------------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Two controllers over disjoint bones: the roll drives cart/axle/wheels/brace, the lid drives
        // chest/top. Keeping them apart lets the chest open and shut while the cart is rolling.
        controllers.add(new AnimationController<>("wheels", 0, this::wheelPredicate));
        controllers.add(new AnimationController<>("chest", 0, this::chestPredicate));
    }

    /**
     * Lid state follows whether anyone has the storage screen open: the open clip while it's up, the
     * close clip once the last viewer leaves. Both hold on their last frame, so between clips the lid
     * simply stays where the animation left it.
     */
    private PlayState chestPredicate(AnimationTest<HorseCartEntity> test) {
        if (!this.hasChest()) {
            this.chestAnimPrimed = false;
            // Cleared outright rather than frozen (see the wheel controller for why the two differ):
            // there is no chest bone on screen to snap, and this leaves the lid genuinely closed for
            // the next chest fitted to this cart instead of resuming a held-open pose.
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
            // Never seen open on this client — leave the lid in the model's closed rest pose rather
            // than playing a close clip that starts from a lid that was already down.
            return PlayState.STOP;
        }
        return test.setAndContinue(CHEST_CLOSING);
    }

    private PlayState wheelPredicate(AnimationTest<HorseCartEntity> test) {
        if (this.smoothedSpeed <= 0.0D) {
            // Parked. Returning STOP is NOT enough on its own: GeckoLib records the play state but
            // goes on applying the last animation at the last speed it was given, so the wheels kept
            // creeping round at whatever rate the coast-down happened to end on — the whole
            // "wheels never stop" bug. Zeroing the controller speed is what actually halts them.
            //
            // Zeroed rather than reset(): reset() clears the animation outright and snaps every bone
            // back to its rest pose, which would jerk the wheel up to an eighth of a turn backwards
            // at the exact moment it settles. Freezing holds the pose it stopped in, which is what a
            // real wheel does.
            test.setControllerSpeed(0.0F);
            return PlayState.STOP;
        }
        // MIN_ANIM_SPEED keeps the wheels legibly turning behind a dawdling horse, but it has to be
        // lifted during the coast-down: held there, the roll would run at a fixed rate for the whole
        // 0.8s and then blink to a halt, instead of winding down into one.
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
