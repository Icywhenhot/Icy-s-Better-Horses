package icy.betterhorses.net.client.render;

import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

public final class BhEquineGait {

    private static final float RAMP_PER_SECOND = 6.0F;
    private static final float SWIM_IN_PER_SECOND = 0.8F;
    private static final float SWIM_OUT_PER_SECOND = 2.0F;

    private static final float SHAKE_IN_PER_SECOND = 1.6F;
    private static final float SHAKE_OUT_PER_SECOND = 0.8F;
    private static final float STAMP_PER_SECOND = 2.0F;
    private static final float LAND_PER_SECOND = 1.8F;
    private static final float LAND_PER_SECOND_BABY = 2.4F;
    private static final float MOUNT_PER_SECOND = 2.5F;

    private static final float BREATH_RADIANS_PER_SECOND = Mth.TWO_PI / 3.5F;
    private static final float BREATH_RATE_BLOWN = 2.6F;

    private static final float WETNESS_IN_PER_SECOND = 0.5F;
    private static final float WETNESS_OUT_PER_SECOND = 1.0F / 8.0F;
    private static final float WETNESS_WORTH_SHAKING = 0.5F;
    private static final float WATER_SHAKE_SECONDS = 0.9F;

    private static final float STAY_IN_PER_SECOND = 1.5F;
    private static final float STAY_OUT_PER_SECOND = 3.0F;

    private static final float REST_SETTLE_SECONDS = 6.0F;
    private static final float REST_IN_PER_SECOND = 0.25F;
    private static final float REST_OUT_PER_SECOND = 1.2F;
    private static final float REST_SWAP_TICKS = 700.0F;
    /** How much faster the stillness counter unwinds than it builds. */
    private static final float REST_DECAY_MULTIPLIER = 3.0F;

    private static final float EXERTION_IN_PER_SECOND = 1.0F / 6.0F;
    private static final float EXERTION_OUT_PER_SECOND = 1.0F / 10.0F;
    private static final float EAR_FLICK_DECAY_PER_SECOND = 3.0F;
    private static final float TAIL_SWISH_DECAY_PER_SECOND = 1.0F / 1.8F;

    private static final float SHAKE_TRIGGER = 0.999087F;
    private static final float STAMP_TRIGGER = 0.99625F;

    private static final float BANK_YAW_FULL = 70.0F;
    private static final float BANK_PER_SECOND = 3.5F;
    private static final float YAW_RATE_SMOOTHING = 0.35F;

    /** Degrees per second of body yaw counted as a full-speed pivot. */
    private static final float PIVOT_YAW_FULL = 55.0F;
    private static final float PIVOT_IN_PER_SECOND = 3.0F;
    private static final float PIVOT_OUT_PER_SECOND = 4.0F;
    /** How fast the pivot fades as the horse starts actually travelling instead of turning. */
    private static final float PIVOT_WALK_SUPPRESS = 1.6F;
    /** Step cycles per degree turned. 0.0055 is roughly one step per 45 degrees. */
    private static final float PIVOT_STEPS_PER_DEGREE = 0.0055F;

    /** Blocks per tick of backwards travel before the backing pose engages. */
    private static final float BACK_SPEED_TRIGGER = 0.012F;
    private static final float BACK_IN_PER_SECOND = 2.5F;
    private static final float BACK_OUT_PER_SECOND = 4.0F;




    private static final float SKID_PER_SECOND = 1.5F;
    private static final float SKID_DROP_TRIGGER = 4.0F;
    private static final float SKID_MIN_RUN = 0.55F;

    private static final float LIMP_HEALTH_START = 0.65F;
    private static final float LIMP_HEALTH_SPAN = 0.45F;
    private static final float LIMP_PER_SECOND = 0.6F;
    // A walk sits below the 0.4-0.6 trot threshold, and first gear runs at 20% speed, so 0.30 cut
    // the limp off across most of the range it should be visible in. Full below 0.45, gone by 0.60
    // - it still hides once the horse is trotting or galloping, where a limp reads as a stumble.
    private static final float LIMP_SPEED_MAX = 0.45F;
    private static final float LIMP_SPEED_FADE = 0.15F;

    // ---- jump ---------------------------------------------------------------------------
    // Blocks per tick. A horse leaves the ground at roughly 0.4-0.9 depending on jump strength,
    // and a walk down a slab edge is well under 0.1, which is what this separates.
    private static final float JUMP_TAKEOFF_SPEED = 0.16F;
    private static final float JUMP_RISE_FULL = 0.34F;
    private static final float JUMP_FALL_FULL = 0.40F;
    // Below this it was a kerb, not a jump: no push-off pulse, no landing spring.
    private static final float JUMP_MIN_AIR_SECONDS = 0.12F;
    private static final float JUMP_THRUST_SECONDS = 0.24F;
    private static final float JUMP_THRUST_ATTACK = 0.34F;
    private static final float JUMP_IMPACT_SECONDS = 0.62F;
    // The two landing beats deliberately OVERLAP. Butt them up end to end instead and the pose
    // passes exactly through rest between them, which reads as two separate landings - the one
    // thing this whole design exists to avoid. First beat owns [0, 0.55], second [0.30, 1.0].
    private static final float JUMP_IMPACT_FIRST_SPAN = 0.55F;
    private static final float JUMP_IMPACT_SECOND_START = 0.30F;
    private static final float JUMP_IMPACT_FIRST_ATTACK = 0.40F;
    private static final float JUMP_IMPACT_SECOND_ATTACK = 0.42F;
    private static final float JUMP_GATHER_IN_PER_SECOND = 6.0F;
    private static final float JUMP_GATHER_OUT_PER_SECOND = 14.0F;
    private static final float JUMP_FLIGHT_PER_SECOND = 10.0F;
    private static final float JUMP_REACH_IN_PER_SECOND = 5.0F;
    private static final float JUMP_REACH_OUT_PER_SECOND = 4.5F;
    private static final float JUMP_IMPACT_POWER_MIN = 0.30F;
    private static final float JUMP_IMPACT_POWER_MAX = 1.35F;
    // verticalSpeed only changes on a tick boundary, so anything derived from it directly is a
    // 20 Hz staircase held across every render frame in between. Gravity moves it by a fixed
    // ~0.078/tick, i.e. the rise target steps by ~0.23 every 0.05 s, so a limiter at about that
    // same rate draws the straight line through the staircase for one tick of lag and no lost
    // amplitude. In has to stay ahead of JUMP_FLIGHT_PER_SECOND or it would slow the takeoff.
    private static final float JUMP_RISE_IN_PER_SECOND = 14.0F;
    private static final float JUMP_RISE_OUT_PER_SECOND = 5.0F;
    /**
     * Time constant of the lag trailing the body arc, in seconds. 0.10 puts the whip in a
     * -14 .. +7 degree band on a charged jump with a worst per-frame change under 4 degrees;
     * 0.15 widens it to -17 .. +8 and starts to read as the head being on a string.
     */
    private static final float ARC_LAG_SECONDS = 0.10F;

    private static final Map<Integer, BhEquineGait> ACTIVE = new HashMap<>();

    private final float random01;

    /**
     * Which foreleg this horse leads with, +1 or -1. Hashed off a different irrational to
     * {@link #random01} on purpose: that one already picks the sore side for the limp, and a horse
     * that always leads with the leg it favours would read as a limp rather than a preference.
     */
    private final float leadSign;

    private float walk;
    private float trot;
    private float run;
    private float swimRamp;
    private float stridePhase;
    private float strideOffset;
    private float lastWalkPos = Float.NaN;
    private float lastAgeInTicks = Float.NaN;

    private float landPhase;
    private boolean landOwed;
    /** Previous frame's {@code jumpActive}, so the rear can be clamped against it. */
    private float lastJumpActive;
    private float shake;
    private float frontLeftStamp;
    private float backRightStamp;
    private float earFlickLeft;
    private float earFlickRight;
    private float tailSwish;
    private float ridden;
    private float toltRamp;
    private float exertion;
    private float breathPhase;
    private float wetness;
    private boolean wasInWater;
    private boolean waterShakeOwed;
    private float waterShakeTimer;
    private float waterShake;
    private float stay;
    private float restStillSeconds;
    private float restLeftHind;
    private float restRightHind;
    private float bank;
    private float pivot;
    private float pivotPhase;
    private float back;
    private float lastYaw = Float.NaN;
    private int lastYawTick = Integer.MIN_VALUE;
    private float yawRate;
    private float skidPhase = 1.0F;
    private float skidPower;
    private float previousRun;
    private float limp;

    private float jumpGather;
    private float jumpFlight;
    private float jumpReach;
    private float jumpRiseSmooth;
    private float arcLag;
    private float jumpThrustClock = Float.MAX_VALUE;
    private float jumpImpactClock = Float.MAX_VALUE;
    private float jumpAirSeconds;
    private float jumpEventSeconds;
    private float jumpLaunchPower;
    private float jumpImpactPower;
    private float jumpPeakRise;
    private float jumpDeepestFall;
    private boolean jumpWasAirborne;
    private boolean jumpApexLogged;
    private boolean jumpEventOpen;

    private float lastEarSignalLeft = Float.NaN;
    private float lastEarSignalRight = Float.NaN;
    private float lastTailSignal = Float.NaN;

    private final int entityId;

    private boolean jumpSeeded;

    private BhEquineGait(int entityId) {
        this.entityId = entityId;
        this.random01 = Math.abs(entityId * 0.6180339887F % 1.0F);
        this.leadSign = Math.abs(entityId * 0.7548776662F % 1.0F) > 0.5F ? 1.0F : -1.0F;
    }

    /**
     * Fills the two jump inputs that have to come off the entity. Called from every breed
     * renderer immediately before {@link #advance}.
     *
     * <p>{@code getY() - yOld} rather than {@code getDeltaMovement()} on purpose: for a horse
     * someone else is riding the client never simulates physics, it interpolates positions, and
     * only the position delta reflects what is actually being drawn.
     *
     * <p>The charge is a different matter. Vanilla keeps it entirely on the rider's own client
     * ({@code LocalPlayer.jumpRidingScale}); the server is told nothing until the key is
     * released, so no other client can know a jump is coming. The gather therefore plays for
     * whoever is riding and is simply absent for onlookers.
     */
    public static void fillJumpInputs(net.minecraft.world.entity.Entity entity,
                                      BhHorseRenderState state) {
        state.verticalSpeed = (float) (entity.getY() - entity.yOld);

        // Travel along the horse's own facing, so negative is backing up. Position deltas rather
        // than getDeltaMovement for the same reason as the vertical: for a horse someone else is
        // riding, this client never simulates physics, it interpolates positions.
        double dx = entity.getX() - entity.xOld;
        double dz = entity.getZ() - entity.zOld;
        float yawRad = entity.getYRot() * Mth.DEG_TO_RAD;
        state.forwardSpeed = (float) (dx * -Mth.sin(yawRad) + dz * Mth.cos(yawRad));

        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.client.player.LocalPlayer player = client.player;
        state.jumpChargeInput = player != null
                && player.getVehicle() == entity
                && client.options.keyJump.isDown()
                ? Mth.clamp(player.getJumpRidingScale(), 0.0F, 1.0F)
                : 0.0F;
    }

    public static BhEquineGait get(int entityId) {
        return ACTIVE.computeIfAbsent(entityId, BhEquineGait::new);
    }

    public static void reset() {
        ACTIVE.clear();
        BhJumpDebug.reset();
    }

    public void advance(BhHorseRenderState state, float ageInTicks) {
        float deltaSeconds;
        if (Float.isNaN(lastAgeInTicks)) {
            deltaSeconds = 0.0F;
        } else {
            deltaSeconds = Mth.clamp((ageInTicks - lastAgeInTicks) / 20.0F, 0.0F, 0.25F);
        }
        lastAgeInTicks = ageInTicks;

        final float phase = state.phaseOffset;
        final float limbSpeed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
        // Rearing and the jump both pose the whole animal, and the jump wins. Vanilla's rear is
        // already redirected off the jump itself and the server will not start one in mid-air, but
        // an ambient or hurt rear can still be part way through when the rider starts charging -
        // and every rear term in the model would then fight the gather it sits on top of.
        //
        // Clamped rather than switched: jumpActive is itself a ramped weight that rises with the
        // gather and decays behind the landing, so the rear fades out and back in with no step at
        // either end. The same idiom the gait weights use on each other.
        //
        // Last frame's jumpActive, because this frame's is not computed until the jump block far
        // below. One frame of lag on a weight that ramps over tenths of a second is not visible,
        // and it buys a single definition of "rearing" that the whole animator agrees on.
        final float rear = Math.min(state.standAnimation,
                Math.max(0.0F, 1.0F - lastJumpActive));
        state.rearWeight = rear;
        final float graze = state.eatAnimation;
        boolean swimming = !state.isPassenger && !state.onGround && state.isInWater;

        swimRamp = Mth.clamp(
                swimRamp + (swimming ? SWIM_IN_PER_SECOND : -SWIM_OUT_PER_SECOND) * deltaSeconds,
                0.0F, 1.0F);
        float swim = Mth.clamp(-0.5F + swimRamp * 2.0F, 0.0F, 1.0F);

        float runThreshold = state.isRidden ? 0.8F : (state.isBaby ? 0.7F : 0.97F);
        float trotThreshold = state.isRidden ? 0.4F : 0.6F;

        run = Mth.clamp(
                run + (limbSpeed >= runThreshold ? RAMP_PER_SECOND : -RAMP_PER_SECOND) * deltaSeconds,
                0.0F, Math.max(0.0F, 1.0F - swim));
        trot = Mth.clamp(
                trot + (limbSpeed >= trotThreshold ? RAMP_PER_SECOND : -RAMP_PER_SECOND) * deltaSeconds,
                0.0F, Math.max(0.0F, 1.0F - swim - run));
        ridden = Mth.clamp(
                ridden + (state.isRidden ? MOUNT_PER_SECOND : -MOUNT_PER_SECOND) * deltaSeconds,
                0.0F, 1.0F);
        state.mountSettle = Mth.sin(ridden * Mth.PI);

        toltRamp = Mth.clamp(
                toltRamp + (state.toltRequest > 0.5F ? RAMP_PER_SECOND : -RAMP_PER_SECOND)
                        * deltaSeconds,
                0.0F, 1.0F);
        float tolt = Math.min(
                state.gaitedBlend * toltRamp * Math.min(1.0F, limbSpeed * 6.0F),
                Math.max(0.0F, 1.0F - swim));

        float runOut = run * (1.0F - tolt);
        float trotOut = trot * (1.0F - tolt);
        walk = Math.max(0.0F, 1.0F - swim - runOut);

        // ---- backing up ---------------------------------------------------------------------
        // Read off the horse's own travel rather than rider input, so a led or shoved horse backs
        // properly too, and it costs nothing on a wild one.
        boolean reversing = state.forwardSpeed < -BACK_SPEED_TRIGGER && !swimming;
        back = Mth.clamp(back + (reversing ? BACK_IN_PER_SECOND : -BACK_OUT_PER_SECOND)
                * deltaSeconds, 0.0F, Math.max(0.0F, 1.0F - rear));
        state.backWeight = back;

        if (trotOut > 0.0F || runOut > 0.0F || tolt > 0.0F) {
            // subtract the offset, never scale the walk position: scaling jumps the phase
            strideOffset += deltaSeconds * limbSpeed
                    * (state.isRidden ? 1.0F - 0.7F * runOut : 1.0F);
        }

        // Backing runs the stride cycle BACKWARDS, and it has to be done here, to the phase.
        //
        // The obvious version - negating the leg reach in the model - is wrong and detaches the
        // legs. reach and rot are a matched pair: the walk carries reach 6.5 against rot 1/1.8, a
        // ratio of ~11.7, and that ratio IS the lever relation that makes the leg swing about the
        // shoulder rather than about the hoof it is pivoted at. Invert one without the other and
        // the leg top drifts by twice the reach. Reversing the phase instead leaves every channel
        // deriving from one cycle, so they cannot disagree.
        //
        // Scaled by the REAL walkAnimationPos delta rather than a guess at its rate, so the
        // reversal is exact: net phase rate becomes (1 - 2*back) times normal, which is +1 at
        // back 0, frozen at 0.5 and a clean -1 at 1.
        float walkPosDelta = Float.isNaN(lastWalkPos) ? 0.0F : state.walkAnimationPos - lastWalkPos;
        lastWalkPos = state.walkAnimationPos;
        if (back > 0.0F && walkPosDelta > 0.0F) {
            strideOffset += walkPosDelta * 1.6F * back / (state.isBaby ? 12.0F : 3.0F);
        }

        stridePhase = -0.3F
                + (state.walkAnimationPos * 0.8F - strideOffset * (state.isBaby ? 12.0F : 3.0F))
                  / (state.isBaby ? 1.8F : 1.0F);

        final float idle = Math.max(0.0F, 1.0F - limbSpeed * 6.0F);
        final float move = Math.min(1.0F, limbSpeed * 6.0F);


        state.walkWeight = walk;
        state.trotWeight = trotOut;
        state.runWeight = runOut;
        state.toltWeight = tolt;

        exertion = Mth.clamp(
                exertion + (run > 0.5F ? EXERTION_IN_PER_SECOND : -EXERTION_OUT_PER_SECOND)
                        * deltaSeconds,
                0.0F, 1.0F);
        state.exertion = exertion;

        breathPhase = (breathPhase + deltaSeconds * BREATH_RADIANS_PER_SECOND
                * (1.0F + (BREATH_RATE_BLOWN - 1.0F) * exertion)) % Mth.TWO_PI;
        state.breathPhase = phase + breathPhase;
        state.swimWeight = swim;
        state.idleWeight = idle;
        state.moveWeight = move;
        state.stridePhase = stridePhase;
        state.random01 = random01;
        state.riddenWeight = ridden;

        int tick = (int) ageInTicks;
        if (tick != lastYawTick) {
            if (!Float.isNaN(lastYaw)) {
                int elapsed = Math.max(1, tick - lastYawTick);
                float sampled = Mth.degreesDifference(lastYaw, state.bodyYaw) * 20.0F / elapsed;
                yawRate += (sampled - yawRate) * YAW_RATE_SMOOTHING;
            }
            lastYaw = state.bodyYaw;
            lastYawTick = tick;
        }

        float bankTarget = Mth.clamp(yawRate / BANK_YAW_FULL, -1.0F, 1.0F)
                * move * Math.max(0.0F, 1.0F - swim) * (1.0F - rear)
                * (state.isRidden ? 1.0F : 0.0F);
        float bankStep = BANK_PER_SECOND * deltaSeconds;
        bank += Mth.clamp(bankTarget - bank, -bankStep, bankStep);
        state.bankWeight = bank;

        // ---- turning on the spot -----------------------------------------------------------
        // Deliberately NOT gated on isRidden the way the bank above is. yawRate comes off the
        // rendered body yaw, which every horse has whether or not anyone is on it, and a loose
        // horse swinging round to face something new is exactly when this should play.
        float pivotTarget = Mth.clamp(Math.abs(yawRate) / PIVOT_YAW_FULL, 0.0F, 1.0F)
                * Math.max(0.0F, 1.0F - move * PIVOT_WALK_SUPPRESS)
                * Math.max(0.0F, 1.0F - swim) * (1.0F - rear);
        pivot += Mth.clamp(pivotTarget - pivot,
                -PIVOT_OUT_PER_SECOND * deltaSeconds, PIVOT_IN_PER_SECOND * deltaSeconds);
        // The phase advances with degrees turned, not with time. A foot steps because the horse
        // rotated, so when the yaw stalls the feet stop rather than paddling on the spot.
        pivotPhase += Math.abs(yawRate) * deltaSeconds * PIVOT_STEPS_PER_DEGREE;
        state.pivotWeight = pivot;
        state.pivotPhase = pivotPhase;
        state.pivotDir = Mth.clamp(yawRate / PIVOT_YAW_FULL, -1.0F, 1.0F);

        float runLost = deltaSeconds > 0.0F ? (previousRun - run) / deltaSeconds : 0.0F;
        if (state.onGround && previousRun >= SKID_MIN_RUN
                && runLost >= SKID_DROP_TRIGGER && skidPhase >= 1.0F) {
            skidPhase = 0.0F;
            skidPower = previousRun;
        }
        previousRun = run;
        if (skidPhase < 1.0F) {
            skidPhase = Math.min(1.0F, skidPhase + SKID_PER_SECOND * deltaSeconds);
        }
        state.skidWeight = Mth.sin(skidPhase * Mth.PI) * skidPower;

        float limpTarget = Mth.clamp(
                (LIMP_HEALTH_START - state.healthFraction) / LIMP_HEALTH_SPAN,
                0.0F, 1.0F);
        float limpStep = LIMP_PER_SECOND * deltaSeconds;
        limp += Mth.clamp(limpTarget - limp, -limpStep, limpStep);
        float limpSpeedGate = Mth.clamp(
                (LIMP_SPEED_MAX - limbSpeed) / LIMP_SPEED_FADE, 0.0F, 1.0F);
        state.limpWeight = limp * limpSpeedGate * Math.max(0.0F, 1.0F - swim);

        // ---- jump -------------------------------------------------------------------------
        // Gather -> thrust -> flight -> reach -> impact. Not five clips: five weights, each of
        // which is already at (or near) zero when the next one comes up, so the pose is
        // continuous across every boundary. That is the whole reason the landing needs no
        // cross-fade, and therefore costs no frames: the impact curve starts at 0 on the exact
        // frame the hoof touches, while jumpReach is still holding the flight pose underneath it.
        final float verticalSpeed = Mth.clamp(state.verticalSpeed, -3.0F, 3.0F);
        final boolean airborne = !state.onGround && !state.isInWater && !state.isPassenger;

        final float gatherTarget = airborne || state.isInWater
                ? 0.0F
                : Mth.clamp(state.jumpChargeInput, 0.0F, 1.0F);
        jumpGather += Mth.clamp(gatherTarget - jumpGather,
                -JUMP_GATHER_OUT_PER_SECOND * deltaSeconds,
                JUMP_GATHER_IN_PER_SECOND * deltaSeconds);

        if (!jumpSeeded) {
            // A horse that spawns mid-air must not fire a takeoff on its first rendered frame
            jumpSeeded = true;
            jumpWasAirborne = airborne;
        }

        if (airborne && !jumpWasAirborne) {
            jumpAirSeconds = 0.0F;
            jumpPeakRise = verticalSpeed;
            jumpDeepestFall = 0.0F;
            jumpApexLogged = false;
            jumpEventSeconds = 0.0F;
            jumpEventOpen = true;
            if (verticalSpeed >= JUMP_TAKEOFF_SPEED || jumpGather > 0.15F) {
                jumpThrustClock = 0.0F;
                jumpLaunchPower = Mth.clamp(
                        Math.max(verticalSpeed / JUMP_RISE_FULL, jumpGather), 0.35F, 1.0F);
                BhJumpDebug.takeoff(entityId, verticalSpeed, jumpGather, jumpLaunchPower,
                        state.isRidden);
            } else {
                // stepped off a ledge: no push-off pulse, but it still has to land properly
                jumpLaunchPower = 0.6F;
            }
        }

        if (airborne) {
            jumpAirSeconds += deltaSeconds;
            jumpPeakRise = Math.max(jumpPeakRise, verticalSpeed);
            jumpDeepestFall = Math.min(jumpDeepestFall, verticalSpeed);
            if (!jumpApexLogged && verticalSpeed <= 0.0F && jumpAirSeconds > 0.0F) {
                jumpApexLogged = true;
                BhJumpDebug.apex(entityId, jumpAirSeconds, jumpPeakRise);
            }
        } else if (jumpWasAirborne) {
            // onGround, not merely "no longer airborne": dropping into water ends the flight but
            // is not a landing, and a spring there would fight the swim pose.
            if (state.onGround && jumpAirSeconds >= JUMP_MIN_AIR_SECONDS) {
                float landingSpeed = Math.min(jumpDeepestFall, verticalSpeed);
                jumpImpactClock = 0.0F;
                jumpImpactPower = Mth.clamp(-landingSpeed / JUMP_FALL_FULL,
                        JUMP_IMPACT_POWER_MIN, JUMP_IMPACT_POWER_MAX);
                BhJumpDebug.touchdown(entityId, jumpAirSeconds, landingSpeed, jumpImpactPower,
                        jumpReach, deltaSeconds);
            }
            jumpAirSeconds = 0.0F;
        }
        jumpWasAirborne = airborne;

        if (jumpThrustClock < JUMP_THRUST_SECONDS) {
            jumpThrustClock += deltaSeconds;
        }
        if (jumpImpactClock < JUMP_IMPACT_SECONDS) {
            jumpImpactClock += deltaSeconds;
        }

        // One frame of held-back ramp: a hoof that clips a slab edge for a single tick is not a
        // jump, and letting that flicker the whole system is worse than a frame of latency on
        // something the thrust pulse has already covered.
        jumpFlight = airborne && jumpAirSeconds > 0.0F
                ? Math.min(1.0F, jumpFlight + JUMP_FLIGHT_PER_SECOND * deltaSeconds)
                : 0.0F;

        // Published before the weights are evaluated, because the weights are now evaluated *from*
        // them - once here at shift 0 for everything that shares one pose, and again per leg in
        // the model at a small offset. One curve, several clocks.
        state.jumpThrustProgress = jumpThrustClock / JUMP_THRUST_SECONDS;
        state.jumpImpactProgress = jumpImpactClock / JUMP_IMPACT_SECONDS;
        state.jumpLaunchPower = jumpLaunchPower;
        state.jumpImpactPower = jumpImpactPower;
        state.jumpLeadSign = leadSign;

        state.jumpThrust = thrustShifted(state, 0.0F);

        state.jumpGather = jumpGather;
        state.jumpFlight = jumpFlight;

        // jumpRise carries the largest amplitudes in the whole animator - 44 degrees on the
        // forelegs, 34 on the hinds, 16 of arc - and used raw off verticalSpeed it inherits that
        // signal's 20 Hz staircase. Measured in jump_sim: a 10.6 degree foreleg jerk on one frame
        // followed by two perfectly frozen ones, three times a second, for the whole ascent. That
        // is what read as stiff, and it was worst on the strongest jumps, because a weak hop is
        // still inside the thrust pulse by the time the steps get big.
        //
        // jumpFall is deliberately left raw: its only consumer is the jumpReach ramp below, which
        // already rate-limits it, and putting a second limiter in front of that would add lag to
        // the one weight the touchdown handoff depends on.
        final float riseTarget = Mth.clamp(verticalSpeed / JUMP_RISE_FULL, 0.0F, 1.0F) * jumpFlight;
        jumpRiseSmooth += Mth.clamp(riseTarget - jumpRiseSmooth,
                -JUMP_RISE_OUT_PER_SECOND * deltaSeconds,
                JUMP_RISE_IN_PER_SECOND * deltaSeconds);
        state.jumpRise = jumpRiseSmooth;
        state.jumpFall = Mth.clamp(-verticalSpeed / JUMP_FALL_FULL, 0.0F, 1.0F) * jumpFlight;

        // Deliberately *not* multiplied by jumpFlight. Touchdown zeroes jumpFlight outright, and
        // this is the weight that has to survive that frame and carry the reaching forelegs into
        // the landing. Take the gate away and the legs snap back to rest as the hoof lands.
        jumpReach += Mth.clamp(state.jumpFall - jumpReach,
                -JUMP_REACH_OUT_PER_SECOND * deltaSeconds,
                JUMP_REACH_IN_PER_SECOND * deltaSeconds);
        state.jumpReach = jumpReach;

        state.jumpImpact = impactShifted(state, 0.0F);
        state.jumpImpactSecond = impactSecondShifted(state, 0.0F);

        float jumpTail = Math.max(Math.max(jumpFlight, jumpReach),
                Math.max(state.jumpThrust, state.jumpImpact));
        state.jumpActive = Math.min(1.0F, Math.max(jumpGather, jumpTail));
        // Feeds the rear clamp at the top of the next frame.
        lastJumpActive = state.jumpActive;

        // Nothing else in this animator has inertia. Every term is a pure function of the current
        // weights, so every bone reaches its pose on the same frame and the horse moves like one
        // rigid diagram - which is a large part of what still read as stiff after the staircase
        // was gone. A one-pole lag trailing the arc gives the missing property back: the gap
        // between the arc and its lag is proportional to how fast the arc is moving, so a bone
        // that subtracts a share of it arrives late in exact proportion to how hard the body just
        // moved, and settles onto its authored pose as soon as the motion stops.
        //
        // Exponential rather than the rate limiters used everywhere else in this file, and that is
        // the whole point: a limiter saturates and hands back the same gap whether the body is
        // drifting or snapping, which is precisely backwards for inertia. Written through
        // exp(-dt/tau) so the time constant is real seconds and does not drift with frame rate.
        final float arc = arcPitch(state);
        arcLag += (arc - arcLag) * (1.0F - (float) Math.exp(-deltaSeconds / ARC_LAG_SECONDS));
        state.arcPitch = arc;
        state.arcWhip = arc - arcLag;

        if (jumpEventOpen) {
            jumpEventSeconds += deltaSeconds;
            if (jumpTail <= 0.0F && !airborne) {
                jumpEventOpen = false;
                BhJumpDebug.settled(entityId, jumpEventSeconds);
            } else {
                BhJumpDebug.sample(entityId, state, jumpAirSeconds);
            }
        }

        // The clamped weight, not the raw flag: a rear the jump suppressed never appeared, so it
        // must not queue a recovery spring to play once the horse has landed.
        if (rear > 0.2F) {
            landPhase = 0.0F;
            landOwed = true;
        } else if (landOwed) {
            landPhase += (state.isBaby ? LAND_PER_SECOND_BABY : LAND_PER_SECOND) * deltaSeconds;
            if (landPhase >= 1.0F) {
                landPhase = 1.0F;
                landOwed = false;
            }
        }
        state.landPhase = landPhase;
        state.landWeight = Mth.sin((landPhase - landPhase * landPhase / 2.0F) * Mth.TWO_PI);

        stay = Mth.clamp(
                stay + (state.commandedToStay ? STAY_IN_PER_SECOND : -STAY_OUT_PER_SECOND)
                        * deltaSeconds,
                0.0F, Math.max(0.0F, 1.0F - move));
        state.stayWeight = stay;

        boolean canRest = idle > 0.9F && state.onGround && !state.isInWater
                && !state.isRidden && rear <= 0.0F && graze <= 0.0F;
        // Decays rather than resetting to zero. A single frame where limbSpeed blips above 1/60
        // or onGround flickers would otherwise throw away the whole count, which made the hip-shot
        // rest far rarer than intended. Decaying at 3x the build rate still clears it promptly
        // when the horse genuinely moves off.
        restStillSeconds = canRest
                ? restStillSeconds + deltaSeconds
                : Math.max(0.0F, restStillSeconds - deltaSeconds * REST_DECAY_MULTIPLIER);
        boolean settled = restStillSeconds > REST_SETTLE_SECONDS;
        boolean rightsTurn = ((int) (ageInTicks / REST_SWAP_TICKS) & 1) == 0;
        restLeftHind = Mth.clamp(restLeftHind
                + ((settled && !rightsTurn) ? REST_IN_PER_SECOND : -REST_OUT_PER_SECOND)
                        * deltaSeconds, 0.0F, 1.0F);
        restRightHind = Mth.clamp(restRightHind
                + ((settled && rightsTurn) ? REST_IN_PER_SECOND : -REST_OUT_PER_SECOND)
                        * deltaSeconds, 0.0F, 1.0F);
        state.restLeftHind = restLeftHind;
        state.restRightHind = restRightHind;


        wetness = Mth.clamp(
                wetness + (state.isInWater ? WETNESS_IN_PER_SECOND : -WETNESS_OUT_PER_SECOND)
                        * deltaSeconds,
                0.0F, 1.0F);
        if (wasInWater && !state.isInWater && wetness > WETNESS_WORTH_SHAKING) {
            waterShakeOwed = true;
            wetness = 0.0F;
        }
        wasInWater = state.isInWater;
        if (waterShakeOwed && state.onGround && !state.isInWater) {
            waterShakeOwed = false;
            waterShakeTimer = WATER_SHAKE_SECONDS;
        }
        if (waterShakeTimer > 0.0F) {
            waterShakeTimer -= deltaSeconds;
        }

        float shakeSignal = Mth.sin(phase + (ageInTicks + state.walkAnimationPos) / 400.0F);
        boolean shaking = waterShakeTimer > 0.0F
                || (state.onGround && shakeSignal > SHAKE_TRIGGER);
        shake = Mth.clamp(
                shake + (shaking ? SHAKE_IN_PER_SECOND : -SHAKE_OUT_PER_SECOND) * deltaSeconds,
                0.0F, Math.max(0.0F, 1.0F - 0.7F * run - rear - graze));
        state.shakeRaw = shake;

        waterShake = Mth.clamp(
                waterShake + (waterShakeTimer > 0.0F ? SHAKE_IN_PER_SECOND : -SHAKE_OUT_PER_SECOND)
                        * deltaSeconds,
                0.0F, Math.min(shake, Math.max(0.0F, 1.0F - rear)));
        state.waterShakeRaw = waterShake;

        float stampSignal = phase + (ageInTicks + state.walkAnimationPos) / 130.0F;
        float stampCeiling = Math.max(0.0F, idle - rear);
        boolean grounded = !state.isInWater && state.onGround;
        frontLeftStamp = Mth.clamp(frontLeftStamp
                        + (grounded && Mth.cos(stampSignal) > STAMP_TRIGGER
                                ? STAMP_PER_SECOND : -STAMP_PER_SECOND) * deltaSeconds,
                0.0F, stampCeiling);
        backRightStamp = Mth.clamp(backRightStamp
                        + (grounded && Mth.sin(stampSignal) < -STAMP_TRIGGER
                                ? STAMP_PER_SECOND : -STAMP_PER_SECOND) * deltaSeconds,
                0.0F, stampCeiling);
        state.frontLeftStampRaw = frontLeftStamp;
        state.backRightStampRaw = backRightStamp;

        float er = phase + ageInTicks / 210.0F;
        float el = phase + ageInTicks / 180.0F;
        float earSignalRight = Mth.cos(er + Mth.cos(er * 1.3F) * 6.0F / (Mth.sin(er * 1.7F) + 5.0F));
        float earSignalLeft = Mth.sin(el + Mth.sin(el * 1.3F) * 6.0F / (Mth.cos(el * 1.7F) + 5.0F));
        earFlickRight = triggerOrDecay(earFlickRight, lastEarSignalRight, earSignalRight,
                EAR_FLICK_DECAY_PER_SECOND, deltaSeconds);
        earFlickLeft = triggerOrDecay(earFlickLeft, lastEarSignalLeft, earSignalLeft,
                EAR_FLICK_DECAY_PER_SECOND, deltaSeconds);
        lastEarSignalRight = earSignalRight;
        lastEarSignalLeft = earSignalLeft;
        state.earFlickRightRaw = earFlickRight;
        state.earFlickLeftRaw = earFlickLeft;

        float ts = phase * 1.31F + ageInTicks / 260.0F;
        float tailSignal = Mth.sin(ts + Mth.cos(ts * 1.7F) * 6.0F / (Mth.sin(ts * 1.1F) + 5.0F));
        if (state.onGround && idle > 0.5F && rear <= 0.0F) {
            tailSwish = triggerOrDecay(tailSwish, lastTailSignal, tailSignal,
                    TAIL_SWISH_DECAY_PER_SECOND, deltaSeconds);
        } else {
            tailSwish = Math.max(0.0F, tailSwish - TAIL_SWISH_DECAY_PER_SECOND * deltaSeconds);
        }
        lastTailSignal = tailSignal;
        state.tailSwishRaw = tailSwish;

        float t = ageInTicks + state.walkAnimationPos;
        float breadth = Mth.clamp(-1.0F + Mth.sin(phase + t / 73.0F) * 3.0F, 0.0F, 1.0F);
        float gate = Mth.clamp(1.0F + Mth.sin(phase + t / 117.0F) * 5.0F,
                0.0F, Math.max(0.0F, 1.0F - run));
        state.idleEnergy = (0.3F + 0.7F * (0.5F - 0.5F * Mth.cos(breadth * Mth.PI)))
                * (0.5F - 0.5F * Mth.cos(gate * Mth.PI));

        float ageK = ageInTicks * (state.isBaby ? 1.3F : 1.0F) * (0.7F + 0.3F * random01);
        state.idleTimer = (phase + ageK + Mth.sin(phase + ageK / 33.0F) * 9.0F) / 18.0F;

    }


    /**
     * The body arc for a jump, radians, + is nose-down.
     *
     * <p>Lives here rather than in the model because it is called twice from two places - once by
     * {@code advance} so the inertia lag has something to chase, once by {@code setupAnim} as the
     * pose itself. Copying these five coefficients into the model instead would let the pose and
     * the thing pretending to trail it drift quietly apart.
     */
    public static float arcPitch(BhHorseRenderState state) {
        return (-16.0F * state.jumpRise
              - 10.0F * state.jumpThrust
              + 9.0F * state.jumpReach
              + 5.0F * state.jumpImpact
              - 7.0F * state.jumpImpactSecond) * Mth.DEG_TO_RAD;
    }

    /**
     * The push-off pulse as a leg standing {@code shiftSeconds} ahead of (+) or behind (-) the
     * horse would feel it.
     *
     * <p>A real horse does not leave the ground on both forelegs at the same instant: one side
     * leads and the other follows a fraction of a beat later, with the diagonal hind matching it.
     * Shifting each leg's <em>clock</em> rather than scaling its amplitude means all four legs
     * trace the identical curve, which is what makes the asymmetry read as a lead leg rather than
     * as one leg being weaker than the other.
     */
    public static float thrustShifted(BhHorseRenderState state, float shiftSeconds) {
        float progress = state.jumpThrustProgress + shiftSeconds / JUMP_THRUST_SECONDS;
        if (progress <= 0.0F || progress >= 1.0F) {
            return 0.0F;
        }
        return smoothPulse(progress, JUMP_THRUST_ATTACK) * state.jumpLaunchPower;
    }

    /**
     * The landing compression, likewise shifted. Two beats, both positive, both starting at zero:
     * the forehand takes the weight, then the hind end comes down. Positive-only is not a
     * stylistic choice - a negative lobe would lift the barrel above its rest height and open a
     * gap at the shoulders, because the legs hang off the root and do not follow the body up.
     */
    public static float impactShifted(BhHorseRenderState state, float shiftSeconds) {
        float progress = state.jumpImpactProgress + shiftSeconds / JUMP_IMPACT_SECONDS;
        if (progress <= 0.0F || progress >= 1.0F) {
            return 0.0F;
        }
        return (impactFirst(progress) + 0.30F * impactSecond(progress)) * state.jumpImpactPower;
    }

    /** The second landing beat alone - the hind legs coming down after the forehand. */
    public static float impactSecondShifted(BhHorseRenderState state, float shiftSeconds) {
        float progress = state.jumpImpactProgress + shiftSeconds / JUMP_IMPACT_SECONDS;
        if (progress <= 0.0F || progress >= 1.0F) {
            return 0.0F;
        }
        return impactSecond(progress) * state.jumpImpactPower;
    }

    private static float impactFirst(float progress) {
        return smoothPulse(Mth.clamp(progress / JUMP_IMPACT_FIRST_SPAN, 0.0F, 1.0F),
                JUMP_IMPACT_FIRST_ATTACK);
    }

    private static float impactSecond(float progress) {
        return smoothPulse(Mth.clamp((progress - JUMP_IMPACT_SECOND_START)
                        / (1.0F - JUMP_IMPACT_SECOND_START), 0.0F, 1.0F),
                JUMP_IMPACT_SECOND_ATTACK);
    }

    /**
     * A 0 -> 1 -> 0 bump that peaks at {@code attack} and has zero slope at 0, at the peak and
     * at 1.
     *
     * <p>{@code sin(pow(p, k) * pi)}, the shape FA reaches for, starts with an infinite slope:
     * on the first frame after a trigger it is already past half its value, which pops. Two
     * smoothsteps back to back cost the same and start from actually standing still.
     */
    private static float smoothPulse(float progress, float attack) {
        return progress <= attack
                ? smoothstep(progress / attack)
                : 1.0F - smoothstep((progress - attack) / (1.0F - attack));
    }

    private static float smoothstep(float x) {
        float t = Mth.clamp(x, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float triggerOrDecay(float current, float previous, float signal,
                                        float decayPerSecond, float deltaSeconds) {
        if (!Float.isNaN(previous) && (previous < 0.0F) != (signal < 0.0F)) {
            return 1.0F;
        }
        return Math.max(0.0F, current - decayPerSecond * deltaSeconds);
    }
}
