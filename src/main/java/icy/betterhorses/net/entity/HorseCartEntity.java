package icy.betterhorses.net.entity;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
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
    /** "Smaller than a horse" gate for carriage mobs (a horse is ~1.4 wide, ~1.6 tall). Tunable. */
    private static final float MAX_CARRIAGE_MOB_WIDTH = 1.2F;
    private static final float MAX_CARRIAGE_MOB_HEIGHT = 2.1F;
    /** How far above the bed floor to look for mobs standing in the carriage, to auto-board them. */
    private static final double BOARD_SCAN_HEIGHT = 1.6D;
    /**
     * How long a freshly spawned cart force-boards mobs found in its bed, ignoring vanilla's 60-tick
     * re-boarding cooldown. Long enough to cover the reload hand-off (see {@link #shouldBeSaved()}).
     */
    private static final int RESTORE_BOARD_TICKS = 80;

    // --- Wheel animation pacing ---
    /** Per-tick easing toward the measured speed; lower = longer spin-up ramp. */
    private static final double SPEED_SMOOTHING = 0.12D;
    /** Below this (blocks/tick) the cart counts as parked and the animation stops entirely. */
    private static final double STILL_SPEED = 0.004D;
    /** Speed (blocks/tick) at which the animation runs at its authored rate. */
    private static final double REFERENCE_SPEED = 0.35D;
    private static final double MIN_ANIM_SPEED = 0.15D;
    private static final double MAX_ANIM_SPEED = 1.5D;

    /** Animation key as it appears in {@code horse_cart.animation.json}. */
    private static final String WHEEL_ANIM_NAME = "wheel moving";
    private static final RawAnimation WHEELS_ROLLING = RawAnimation.begin().thenLoop(WHEEL_ANIM_NAME);

    // Network id of the bound horse, synced so the client can glue the cart to it directly (the
    // server-side UUID/ref aren't available client-side).
    private static final EntityDataAccessor<Integer> DATA_HORSE_ID =
            SynchedEntityData.defineId(HorseCartEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private @Nullable UUID horseUuid;
    private @Nullable AbstractHorse horse;

    /** Tick up to which this cart force-boards mobs in its bed; zeroed once cargo is unloaded by hand. */
    private int cargoRestoreDeadline = RESTORE_BOARD_TICKS;

    // Client-side movement measure (drives the wheel animation pacing).
    private double prevX;
    private double prevZ;
    private double smoothedSpeed;

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
            this.discard();
            return;
        }
        this.followHorse(boundHorse);
        this.tryBoardNearbyMobs();
        this.seatPassengers();
    }

    /**
     * Keeps carried animals in their sitting pose.
     *
     * <p>Humanoid riders (players, villagers, piglins…) fold their legs on their own — vanilla's
     * {@code HumanoidModel} does that for any passenger. Animals have no such pose: a pig or a sheep
     * stands in a vanilla boat too. The ones that <i>do</i> own a sit animation get it switched on
     * here, and it is re-asserted every tick because their AI keeps ticking while they ride and can
     * clear the flag underneath us.</p>
     */
    private void seatPassengers() {
        for (Entity passenger : this.getPassengers()) {
            setSeatedPose(passenger, true);
        }
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
            passenger.stopRiding();
            // Step them clear of the bed so they aren't standing in the scan box when the cooldown
            // lapses; without this a sneak-click on a parked cart just re-loads itself.
            Vec3 beside = this.position().add(
                    new Vec3(this.getBbWidth() * 0.5D + 0.6D, 0.0D, 0.0D)
                            .yRot(-this.getYRot() * ((float) Math.PI / 180.0F)));
            passenger.teleportTo(beside.x, this.getY(), beside.z);
        }
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
     * Measures how far the cart actually moved this tick and eases toward it, so the wheels spin up
     * gradually rather than snapping to full speed. Snaps to a hard zero below {@link #STILL_SPEED}
     * so a parked cart is perfectly still instead of creeping on positional jitter.
     */
    private void updateClientSpeed() {
        double dx = this.getX() - this.prevX;
        double dz = this.getZ() - this.prevZ;
        double instant = Math.sqrt(dx * dx + dz * dz);

        this.smoothedSpeed += (instant - this.smoothedSpeed) * SPEED_SMOOTHING;
        if (this.smoothedSpeed < STILL_SPEED) {
            this.smoothedSpeed = 0.0D;
        }

        this.prevX = this.getX();
        this.prevZ = this.getZ();
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

    /** Whether {@code candidate} may occupy a carriage seat: players always; mobs only if small enough. */
    private boolean canCarry(Entity candidate) {
        if (candidate instanceof Player) {
            return true;
        }
        if (!(candidate instanceof LivingEntity) || candidate instanceof AbstractHorse) {
            // No non-living riders, and never a horse/donkey/mule — those are the "big mobs" we exclude.
            return false;
        }
        if (candidate == this.boundHorse()) {
            return false;
        }
        return candidate.getBbWidth() < MAX_CARRIAGE_MOB_WIDTH
                && candidate.getBbHeight() < MAX_CARRIAGE_MOB_HEIGHT;
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
        if (this.getPassengers().size() >= REAR_SEAT_COUNT) {
            return;
        }
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, boardScanBox())) {
            if (this.getPassengers().size() >= REAR_SEAT_COUNT) {
                break;
            }
            if (candidate instanceof Player
                    || candidate.isPassenger()
                    || candidate.isVehicle()
                    || !candidate.isAlive()
                    || !this.canCarry(candidate)) {
                continue;
            }
            // Normally this respects boardingCooldown, so a mob you just shoved out isn't re-grabbed
            // instantly. The exception is a cart that has only just appeared: on a reload the mobs
            // in the bed are the ones the saved cart set down a moment ago, and vanilla stamped them
            // with a 60-tick cooldown on the way out. Force those aboard so a reload doesn't scatter
            // the cargo. Our seat/size gate still runs via canAddPassenger.
            candidate.startRiding(this, this.restoringCargo(), true);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hitLocation) {
        // Sneak-click unloads the back: the only way to get a mob out again, since mobs never
        // dismount on their own and the auto-boarder would grab anything standing in the bed.
        if (player.isSecondaryUseActive()) {
            if (this.getPassengers().isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!this.level().isClientSide()) {
                this.unloadPassengers();
            }
            return InteractionResult.SUCCESS;
        }
        if (this.level().isClientSide()) {
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
        if (this.getPassengers().size() < REAR_SEAT_COUNT) {
            player.startRiding(this);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
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
        return this.getPassengers().size() < REAR_SEAT_COUNT && this.canCarry(passenger);
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
        controllers.add(new AnimationController<>("wheels", 0, this::wheelPredicate));
    }

    private PlayState wheelPredicate(AnimationTest<HorseCartEntity> test) {
        if (this.smoothedSpeed <= 0.0D) {
            // Parked: no wheel spin, no bounce — the cart sits perfectly still.
            return PlayState.STOP;
        }
        test.setControllerSpeed(
                (float) Mth.clamp(this.smoothedSpeed / REFERENCE_SPEED, MIN_ANIM_SPEED, MAX_ANIM_SPEED));
        return test.setAndContinue(WHEELS_ROLLING);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
