package icy.betterhorses.net.client.render;

import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.client.BhClientCaches;
import icy.betterhorses.net.feature.Stabilizer;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Iterator;
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

    private static final float EXERTION_IN_PER_SECOND = 1.0F / 6.0F;
    private static final float EXERTION_OUT_PER_SECOND = 1.0F / 10.0F;
    private static final float EAR_FLICK_DECAY_PER_SECOND = 3.0F;
    private static final float TAIL_SWISH_DECAY_PER_SECOND = 1.0F / 1.8F;

    private static final float SHAKE_TRIGGER = 0.999087F;
    private static final float STAMP_TRIGGER = 0.99625F;

    private static final float BANK_YAW_FULL = 70.0F;
    private static final float BANK_PER_SECOND = 3.5F;
    private static final float YAW_RATE_SMOOTHING = 0.35F;

    private static final float PIVOT_YAW_FULL = 55.0F;
    private static final float PIVOT_IN_PER_SECOND = 3.0F;
    private static final float PIVOT_OUT_PER_SECOND = 4.0F;
    private static final float PIVOT_WALK_SUPPRESS = 1.6F;
    private static final float PIVOT_STEPS_PER_DEGREE = 0.0055F;

    private static final float BACK_SPEED_TRIGGER = 0.012F;
    private static final float BACK_IN_PER_SECOND = 2.5F;
    private static final float BACK_OUT_PER_SECOND = 4.0F;

    private static final float SKID_PER_SECOND = 1.5F;
    private static final float SKID_DROP_TRIGGER = 4.0F;
    private static final float SKID_MIN_RUN = 0.55F;

    private static final float LIMP_HEALTH_START = 0.65F;
    private static final float LIMP_HEALTH_SPAN = 0.45F;
    private static final float LIMP_PER_SECOND = 0.6F;
    private static final float LIMP_SPEED_MAX = 0.45F;
    private static final float LIMP_SPEED_FADE = 0.15F;

    private static final float JUMP_FALL_FULL = 0.40F;
    private static final float JUMP_MIN_AIR_SECONDS = 0.12F;
    private static final float JUMP_IMPACT_SECONDS = 0.62F;
    private static final float JUMP_IMPACT_FIRST_SPAN = 0.55F;
    private static final float JUMP_IMPACT_SECOND_START = 0.30F;
    private static final float JUMP_IMPACT_FIRST_ATTACK = 0.40F;
    private static final float JUMP_IMPACT_SECOND_ATTACK = 0.42F;
    private static final float JUMP_IMPACT_POWER_MIN = 0.30F;
    private static final float JUMP_IMPACT_POWER_MAX = 1.35F;
    private static final float ARC_LAG_SECONDS = 0.10F;

    private static final int JUMP_NONE = 0;
    private static final int JUMP_HOLD = 1;
    private static final int JUMP_TAKEOFF = 2;
    private static final int JUMP_RISE = 3;
    private static final int JUMP_APEX = 4;
    private static final int JUMP_FALL = 5;
    private static final int JUMP_LAND = 6;

    private static final int JUMP_ARM_TICKS = 4;
    private static final float JUMP_LATE_SECONDS = 0.25F;
    private static final float JUMP_LAUNCH_SPEED = 0.05F;
    private static final float JUMP_MAX_RATE = 3.0F;
    private static final float JUMP_RATE_SLEW = 12.0F;
    private static final float JUMP_SEAM_SECONDS = 0.10F;
    private static final float JUMP_IN_PER_SECOND = 10.0F;
    private static final float JUMP_OUT_PER_SECOND = 6.0F;
    private static final float HOLD_IN_PER_SECOND = 6.0F;
    private static final float HOLD_OUT_PER_SECOND = 4.0F;
    private static final float LAND_FADE_SECONDS = JUMP_IMPACT_SECONDS * JUMP_IMPACT_FIRST_SPAN;
    private static final float WATER_FADE_SECONDS = 0.2F;
    private static final float FLAIL_START_SECONDS = 0.35F;
    private static final float FLAIL_FULL_SECONDS = 0.9F;
    private static final float PANIC_IN_PER_SECOND = 2.5F;
    private static final float PANIC_OUT_PER_SECOND = 3.0F;
    private static final float PANIC_MIN_LAND_SECONDS = 0.4F;
    private static final float FALL_DAMP_START_SECONDS = 0.3F;
    private static final float FALL_DAMP_MAX = 0.5F;
    private static final float FALL_DAMP_IN_PER_SECOND = 3.0F;
    private static final float FALL_DAMP_OUT_PER_SECOND = 6.0F;
    private static final float PLAIN_DROP_BLOCKS = 1.9F;
    private static final float PLAIN_IMPACT_SCALE = 0.5F;
    private static final float FIRST_PERSON_PITCH = 0.5F;
    private static final float CART_PITCH = 0.4F;
    private static final float GRAVITY = 0.08F;
    private static final float DRAG = 0.98F;
    private static final int GROUND_PROBE_BLOCKS = 48;
    private static final int PREDICT_TICKS = 100;

    private static final float KICK_SECONDS = 0.62F;
    private static final float STOMP_SECONDS = 0.5F;

    private static final float MOVE_EPSILON = 0.02F;


    private static final Map<Integer, BhEquineGait> ACTIVE = new HashMap<>();



    private final float random01;

    private final float leadSign;

    private float walk;
    private float trot;
    private float canter;
    private float run;
    private float swimRamp;
    private float stridePhase;
    private float strideOffset;
    private float lastWalkPos = Float.NaN;
    private float lastAgeInTicks = Float.NaN;

    private float landPhase;
    private boolean landOwed;
    private float lastJumpActive;
    private float shake;
    private float kickClock = Float.MAX_VALUE;
    private int lastKickTicks;
    private float stompClock = Float.MAX_VALUE;
    private int lastStompTicks;
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

    private int jumpPhase = JUMP_NONE;
    private int jumpClip = BhJumpClips.HOLD;
    private float jumpClipTime;
    private int jumpFromClip = BhJumpClips.HOLD;
    private float jumpFromTime;
    private float jumpFade = 1.0F;
    private float jumpWeight;
    private float jumpRate = 1.0F;
    private float holdPeak;
    private float landFrom;
    private float landClock;
    private float landFadeSeconds = LAND_FADE_SECONDS;
    private int lastJumpCue;
    private int jumpArmedUntil = Integer.MIN_VALUE;
    private float lastCharge;
    private float flailRamp;
    private float panic;
    private float fallDamp;
    private float takeoffY;
    private float pitchLag;
    private float jumpImpactClock = Float.MAX_VALUE;
    private float jumpAirSeconds;
    private float jumpImpactPower;
    private float jumpDeepestFall;
    private boolean jumpWasAirborne;

    private int senseTick = Integer.MIN_VALUE;
    private boolean senseAirborne;
    private float apexSeconds;
    private float landSeconds;

    private float lastEarSignalLeft = Float.NaN;
    private float lastEarSignalRight = Float.NaN;
    private float lastTailSignal = Float.NaN;

    private final int entityId;

    private boolean jumpSeeded;

    BhEquineGait(int entityId) {
        this.entityId = entityId;
        this.random01 = Math.abs(entityId * 0.6180339887F % 1.0F);
        this.leadSign = Math.abs(entityId * 0.7548776662F % 1.0F) > 0.5F ? 1.0F : -1.0F;
    }

    public static void fillJumpInputs(Entity entity,
                                      BhHorseRenderState state) {
        state.verticalSpeed = (float) (entity.getY() - entity.yOld);
        state.posY = (float) entity.getY();

        double dx = entity.getX() - entity.xOld;
        double dz = entity.getZ() - entity.zOld;
        float yawRad = entity.getYRot() * Mth.DEG_TO_RAD;
        state.forwardSpeed = (float) (dx * -Mth.sin(yawRad) + dz * Mth.cos(yawRad));

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        state.jumpChargeInput = player != null
                && player.getVehicle() == entity
                && client.options.keyJump.isDown()
                ? Mth.clamp(player.getJumpRidingScale(), 0.0F, 1.0F)
                : 0.0F;
        state.firstPersonRider = player != null
                && client.options.getCameraType().isFirstPerson()
                && entity.hasPassenger(player);
    }

    public static void advanceFor(Entity entity,
                                  BhHorseRenderState state) {
        fillJumpInputs(entity, state);
        if (entity instanceof AbstractHorse horse) {
            IHorseData data = IHorseData.of(horse);
            state.gear = data.bh_getGear();
            state.kickTicks = data.bh_getKickTicks();
            state.stompTicks = data.bh_getStompTicks();
            state.pullingCart = data.bh_hasCartGear();
            state.jumpCue = data.bh_getJumpCue();
            state.stabilizer = data.bh_getStabilizerState().ordinal();
        } else {
            state.gear = 0;
            state.kickTicks = 0;
            state.stompTicks = 0;
            state.pullingCart = false;
            state.jumpCue = 0;
            state.stabilizer = 0;
        }

        BhEquineGait gait = ACTIVE.computeIfAbsent(state.entityId, key -> new BhEquineGait(entity.getId()));
        gait.sense(entity, state);
        gait.advance(state, state.ageInTicks);
    }

    private void sense(Entity entity, BhHorseRenderState state) {
        int tick = (int) state.ageInTicks;
        boolean airborne = !state.onGround && !state.isInWater && !state.isPassenger;
        if (tick == senseTick && airborne == senseAirborne) {
            return;
        }
        senseTick = tick;
        senseAirborne = airborne;
        if (!airborne) {
            apexSeconds = 0.0F;
            landSeconds = 0.0F;
            return;
        }

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        BlockHitResult hit = entity.level().clip(new ClipContext(
                new Vec3(x, y + 0.01D, z), new Vec3(x, y - GROUND_PROBE_BLOCKS, z),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, entity));
        float drop = hit.getType() == HitResult.Type.MISS
                ? GROUND_PROBE_BLOCKS
                : (float) (y - hit.getLocation().y);

        float cap = state.stabilizer == HorseStabilizerState.OPEN.ordinal()
                ? (float) Stabilizer.MAX_DESCENT_SPEED
                : state.stabilizer == HorseStabilizerState.HALF_OPEN.ordinal()
                ? (float) Stabilizer.HALF_OPEN_DESCENT_SPEED
                : -Float.MAX_VALUE;

        float d = state.verticalSpeed;
        float pos = 0.0F;
        int apex = -1;
        int land = -1;
        for (int k = 1; k <= PREDICT_TICKS; k++) {
            d = Math.max((d - GRAVITY) * DRAG, cap);
            if (apex < 0 && d <= 0.0F) {
                apex = k;
            }
            pos += d;
            if (pos <= -drop) {
                land = k;
                break;
            }
        }
        apexSeconds = apex < 0 ? 0.0F : (apex - 0.5F) / 20.0F;
        landSeconds = land < 0 ? PREDICT_TICKS / 20.0F : land / 20.0F;
    }

    public static void reset() {
        ACTIVE.clear();
    }

    public static void remove(int id) {
        ACTIVE.remove(id);
    }

    public void advance(BhHorseRenderState state, float ageInTicks) {
        float deltaSeconds;
        if (Float.isNaN(lastAgeInTicks)) {
            deltaSeconds = 0.0F;
        } else {
            deltaSeconds = Mth.clamp((ageInTicks - lastAgeInTicks) / 20.0F, 0.0F, 0.25F);
        }
        if (lastAgeInTicks != ageInTicks) {
            state.poseRevision++;
        }
        lastAgeInTicks = ageInTicks;

        final float phase = state.phaseOffset;
        final float limbSpeed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
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
        float canterThreshold = state.isRidden ? 0.62F : (state.isBaby ? 0.55F : 0.78F);
        float trotThreshold = state.isRidden ? 0.4F : 0.6F;

        int gait = limbSpeed <= MOVE_EPSILON ? 0
                : state.gear > 0 ? Math.min(state.gear, BhGears.GALLOP_GEAR)
                : limbSpeed >= runThreshold ? BhGears.GALLOP_GEAR
                : limbSpeed >= canterThreshold ? BhGears.CANTER_GEAR
                : limbSpeed >= trotThreshold ? BhGears.TROT_GEAR
                : BhGears.WALK_GEAR;

        run = Mth.clamp(
                run + (gait == BhGears.GALLOP_GEAR ? RAMP_PER_SECOND : -RAMP_PER_SECOND) * deltaSeconds,
                0.0F, Math.max(0.0F, 1.0F - swim));
        canter = Mth.clamp(
                canter + (gait == BhGears.CANTER_GEAR ? RAMP_PER_SECOND : -RAMP_PER_SECOND) * deltaSeconds,
                0.0F, Math.max(0.0F, 1.0F - swim - run));
        trot = Mth.clamp(
                trot + (gait == BhGears.TROT_GEAR ? RAMP_PER_SECOND : -RAMP_PER_SECOND) * deltaSeconds,
                0.0F, Math.max(0.0F, 1.0F - swim - run - canter));
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
        float canterOut = canter * (1.0F - tolt);
        float trotOut = trot * (1.0F - tolt);
        walk = Math.max(0.0F, 1.0F - swim - runOut - canterOut);

        boolean reversing = state.forwardSpeed < -BACK_SPEED_TRIGGER && !swimming;
        back = Mth.clamp(back + (reversing ? BACK_IN_PER_SECOND : -BACK_OUT_PER_SECOND)
                * deltaSeconds, 0.0F, Math.max(0.0F, 1.0F - rear));
        state.backWeight = back;

        if (trotOut > 0.0F || canterOut > 0.0F || runOut > 0.0F || tolt > 0.0F) {
            strideOffset += deltaSeconds * limbSpeed
                    * (state.isRidden ? 1.0F - 0.7F * runOut - 0.45F * canterOut : 1.0F);
        }

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
        state.canterWeight = canterOut;
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
                * (state.isRidden && !state.pullingCart ? 1.0F : 0.0F);
        float bankStep = BANK_PER_SECOND * deltaSeconds;
        bank += Mth.clamp(bankTarget - bank, -bankStep, bankStep);
        state.bankWeight = bank;

        float pivotTarget = Mth.clamp(Math.abs(yawRate) / PIVOT_YAW_FULL, 0.0F, 1.0F)
                * Math.max(0.0F, 1.0F - move * PIVOT_WALK_SUPPRESS)
                * Math.max(0.0F, 1.0F - swim) * (1.0F - rear);
        pivot += Mth.clamp(pivotTarget - pivot,
                -PIVOT_OUT_PER_SECOND * deltaSeconds, PIVOT_IN_PER_SECOND * deltaSeconds);
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

        advanceJump(state, deltaSeconds, ageInTicks, move);
        state.jumpLeadSign = leadSign;
        lastJumpActive = state.jumpActive;

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

        if (state.kickTicks > lastKickTicks) {
            kickClock = 0.0F;
        }
        lastKickTicks = state.kickTicks;
        if (kickClock < KICK_SECONDS) {
            kickClock += deltaSeconds;
        }
        state.kickPhase = Mth.clamp(kickClock / KICK_SECONDS, 0.0F, 1.0F);

        if (state.stompTicks > lastStompTicks) {
            stompClock = 0.0F;
        }
        lastStompTicks = state.stompTicks;
        if (stompClock < STOMP_SECONDS) {
            stompClock += deltaSeconds;
        }
        state.stompPhase = Mth.clamp(stompClock / STOMP_SECONDS, 0.0F, 1.0F);

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

    private void advanceJump(BhHorseRenderState state, float dt, float ageInTicks, float move) {
        final float verticalSpeed = Mth.clamp(state.verticalSpeed, -3.0F, 3.0F);
        final boolean airborne = !state.onGround && !state.isInWater && !state.isPassenger;
        final int tick = (int) ageInTicks;
        final float sinceSense = Math.max(0.0F, (ageInTicks - senseTick) / 20.0F);
        final float apexLeft = Math.max(0.0F, apexSeconds - sinceSense);
        final float landLeft = Math.max(0.0F, landSeconds - sinceSense);

        if (!jumpSeeded) {
            jumpSeeded = true;
            jumpWasAirborne = airborne;
            lastJumpCue = state.jumpCue;
        }
        final boolean cued = state.jumpCue != lastJumpCue;
        lastJumpCue = state.jumpCue;

        final float charge = airborne || state.isInWater ? 0.0F : Mth.clamp(state.jumpChargeInput, 0.0F, 1.0F);
        if (lastCharge > 0.0F && charge <= 0.0F && !airborne) {
            jumpArmedUntil = Math.max(jumpArmedUntil, tick + JUMP_ARM_TICKS);
        }
        lastCharge = charge;

        if (airborne && !jumpWasAirborne) {
            jumpAirSeconds = 0.0F;
            jumpDeepestFall = 0.0F;
            flailRamp = 0.0F;
            takeoffY = state.posY;
            if (tick <= jumpArmedUntil && verticalSpeed > JUMP_LAUNCH_SPEED) {
                startJump(apexLeft);
            }
            jumpArmedUntil = Integer.MIN_VALUE;
        }

        if (cued) {
            if (!airborne) {
                jumpArmedUntil = tick + JUMP_ARM_TICKS;
            } else if (!inAir(jumpPhase) && jumpAirSeconds <= JUMP_LATE_SECONDS
                    && verticalSpeed > JUMP_LAUNCH_SPEED) {
                startJump(apexLeft);
            }
        }

        if (charge > 0.0F && (jumpPhase == JUMP_NONE || jumpPhase == JUMP_HOLD || jumpPhase == JUMP_LAND)) {
            if (jumpPhase != JUMP_HOLD) {
                crossTo(BhJumpClips.HOLD, 0.0F);
                jumpPhase = JUMP_HOLD;
            }
            holdPeak = Math.max(holdPeak, charge);
        } else if (jumpPhase == JUMP_HOLD && charge <= 0.0F && tick > jumpArmedUntil) {
            holdPeak = 0.0F;
        }

        if (airborne) {
            jumpAirSeconds += dt;
            jumpDeepestFall = Math.min(jumpDeepestFall, verticalSpeed);
            if (inAir(jumpPhase) && jumpPhase != JUMP_TAKEOFF) {
                flailRamp = Mth.clamp((jumpAirSeconds - FLAIL_START_SECONDS)
                        / (FLAIL_FULL_SECONDS - FLAIL_START_SECONDS), 0.0F, 1.0F);
            }
        } else if (jumpWasAirborne) {
            boolean jumped = inAir(jumpPhase);
            if (jumped) {
                landFrom = jumpWeight * (1.0F - panic);
                jumpWeight = landFrom;
                landClock = 0.0F;
                landFadeSeconds = state.isInWater ? WATER_FADE_SECONDS : LAND_FADE_SECONDS;
                jumpPhase = JUMP_LAND;
            }
            if (state.onGround) {
                float landingSpeed = Math.min(jumpDeepestFall, verticalSpeed);
                float power = Mth.clamp(-landingSpeed / JUMP_FALL_FULL,
                        JUMP_IMPACT_POWER_MIN, JUMP_IMPACT_POWER_MAX);
                if (jumped && jumpAirSeconds >= JUMP_MIN_AIR_SECONDS) {
                    jumpImpactClock = 0.0F;
                    jumpImpactPower = power;
                } else if (!jumped && takeoffY - state.posY >= PLAIN_DROP_BLOCKS) {
                    jumpImpactClock = 0.0F;
                    jumpImpactPower = power * PLAIN_IMPACT_SCALE;
                }
            }
            jumpAirSeconds = 0.0F;
        }
        jumpWasAirborne = airborne;

        if (jumpImpactClock < JUMP_IMPACT_SECONDS) {
            jumpImpactClock += dt;
        }

        final boolean panicking = airborne && state.stabilizer != 0
                && (panic > 0.0F || landLeft > PANIC_MIN_LAND_SECONDS);
        panic = Mth.clamp(panic + (panicking ? PANIC_IN_PER_SECOND : -PANIC_OUT_PER_SECOND) * dt,
                0.0F, 1.0F);

        final float dampTarget = airborne && !inAir(jumpPhase) && jumpAirSeconds > FALL_DAMP_START_SECONDS
                ? FALL_DAMP_MAX : 0.0F;
        fallDamp += Mth.clamp(dampTarget - fallDamp,
                -FALL_DAMP_OUT_PER_SECOND * dt, FALL_DAMP_IN_PER_SECOND * dt);

        stepJumpClock(dt, apexLeft, landLeft, airborne);

        switch (jumpPhase) {
            case JUMP_HOLD -> {
                float target = smoothstep(holdPeak);
                jumpWeight += Mth.clamp(target - jumpWeight,
                        -HOLD_OUT_PER_SECOND * dt, HOLD_IN_PER_SECOND * dt);
                if (holdPeak <= 0.0F && jumpWeight <= 0.0F) {
                    jumpPhase = JUMP_NONE;
                }
            }
            case JUMP_TAKEOFF, JUMP_RISE, JUMP_APEX, JUMP_FALL ->
                    jumpWeight = Math.min(1.0F, jumpWeight + JUMP_IN_PER_SECOND * dt);
            case JUMP_LAND -> {
                landClock += dt;
                float k = landClock / landFadeSeconds;
                jumpWeight = landFrom * (1.0F - smoothstep(k));
                if (k >= 1.0F) {
                    jumpWeight = 0.0F;
                    jumpPhase = JUMP_NONE;
                }
            }
            default -> jumpWeight = Math.max(0.0F, jumpWeight - JUMP_OUT_PER_SECOND * dt);
        }
        if (jumpFade < 1.0F) {
            jumpFade = Math.min(1.0F, jumpFade + dt / JUMP_SEAM_SECONDS);
        }

        final float weight = jumpPhase == JUMP_LAND ? jumpWeight : jumpWeight * (1.0F - panic);
        final float pitchScale = state.pullingCart ? CART_PITCH
                : state.firstPersonRider ? FIRST_PERSON_PITCH : 1.0F;

        state.jumpClip = jumpClip;
        state.jumpClipTime = jumpClipTime;
        state.jumpFromClip = jumpFromClip;
        state.jumpFromTime = jumpFromTime;
        state.jumpFade = smoothstep(jumpFade);
        state.jumpWeight = weight;
        state.jumpLegWeight = jumpPhase == JUMP_HOLD ? weight * (1.0F - move) : weight;
        state.jumpPitchScale = pitchScale;
        state.panicWeight = panic;
        state.fallDamp = fallDamp;

        state.jumpImpactProgress = jumpImpactClock / JUMP_IMPACT_SECONDS;
        state.jumpImpactPower = jumpImpactPower;
        state.jumpImpact = impactShifted(state, 0.0F);
        state.jumpImpactSecond = impactSecondShifted(state, 0.0F);

        final boolean flying = jumpPhase >= JUMP_TAKEOFF;
        state.jumpAir = flying ? weight : 0.0F;
        state.jumpFlail = (jumpPhase >= JUMP_RISE ? weight * flailRamp : 0.0F)
                * Math.max(0.0F, 1.0F - Mth.clamp(state.jumpImpact, 0.0F, 1.0F));
        if (jumpPhase == JUMP_TAKEOFF) {
            float span = BhJumpClips.length(BhJumpClips.TAKEOFF) - BhJumpClips.crouchEnd();
            float p = span > 0.0F ? Mth.clamp((jumpClipTime - BhJumpClips.crouchEnd()) / span, 0.0F, 1.0F) : 1.0F;
            state.jumpPush = Mth.sin(p * Mth.PI) * weight;
        } else {
            state.jumpPush = 0.0F;
        }

        state.jumpActive = Math.min(1.0F, Math.max(weight, Math.max(panic, state.jumpImpact)));

        float pitch = 0.0F;
        if (weight > 0.0F) {
            float now = BhJumpClips.bodyPitch(jumpClip, jumpClipTime);
            if (state.jumpFade < 1.0F) {
                float from = BhJumpClips.bodyPitch(jumpFromClip, jumpFromTime);
                now = from + (now - from) * state.jumpFade;
            }
            pitch = -now * Mth.DEG_TO_RAD * pitchScale * weight;
        }
        pitchLag += (pitch - pitchLag) * (1.0F - (float) Math.exp(-dt / ARC_LAG_SECONDS));
        state.jumpWhip = pitch - pitchLag;
    }

    private void stepJumpClock(float dt, float apexLeft, float landLeft, boolean airborne) {
        if (!inAir(jumpPhase)) {
            return;
        }
        final float apexAt = BhJumpClips.apexAt();
        final float takeoffEnd = BhJumpClips.length(BhJumpClips.TAKEOFF);
        final float apexEnd = BhJumpClips.length(BhJumpClips.APEX);

        float target = 1.0F;
        if (jumpPhase == JUMP_TAKEOFF) {
            target = (takeoffEnd - jumpClipTime + apexAt) / Math.max(apexLeft, 0.02F);
        } else if (jumpPhase == JUMP_APEX) {
            target = jumpClipTime < apexAt
                    ? (apexAt - jumpClipTime) / Math.max(apexLeft, 0.02F)
                    : (apexEnd - jumpClipTime) / Math.max(landLeft, 0.02F);
        }
        target = Mth.clamp(target, 1.0F, JUMP_MAX_RATE);
        jumpRate += Mth.clamp(target - jumpRate, -JUMP_RATE_SLEW * dt, JUMP_RATE_SLEW * dt);

        switch (jumpPhase) {
            case JUMP_TAKEOFF -> {
                jumpClipTime += dt * jumpRate;
                if (jumpClipTime >= takeoffEnd && airborne) {
                    jumpClipTime = takeoffEnd;
                    if (apexLeft > apexAt + JUMP_SEAM_SECONDS) {
                        crossTo(BhJumpClips.RISE, 0.0F);
                        jumpPhase = JUMP_RISE;
                    } else {
                        crossTo(BhJumpClips.APEX, 0.0F);
                        jumpPhase = JUMP_APEX;
                    }
                } else if (jumpClipTime >= takeoffEnd) {
                    jumpClipTime = takeoffEnd;
                }
            }
            case JUMP_RISE -> {
                jumpClipTime += dt;
                if (apexLeft <= apexAt) {
                    crossTo(BhJumpClips.APEX, 0.0F);
                    jumpPhase = JUMP_APEX;
                }
            }
            case JUMP_APEX -> {
                jumpClipTime += dt * jumpRate;
                if (jumpClipTime >= apexEnd) {
                    jumpClipTime = apexEnd;
                    crossTo(BhJumpClips.FALL, 0.0F);
                    jumpPhase = JUMP_FALL;
                }
            }
            default -> jumpClipTime += dt;
        }
    }

    private void startJump(float apexLeft) {
        crossTo(BhJumpClips.TAKEOFF, BhJumpClips.crouchEnd());
        jumpPhase = JUMP_TAKEOFF;
        holdPeak = 0.0F;
        jumpArmedUntil = Integer.MIN_VALUE;
        float remaining = BhJumpClips.length(BhJumpClips.TAKEOFF) - BhJumpClips.crouchEnd()
                + BhJumpClips.apexAt();
        jumpRate = Mth.clamp(remaining / Math.max(apexLeft, 0.02F), 1.0F, JUMP_MAX_RATE);
    }

    private void crossTo(int clip, float time) {
        jumpFromClip = jumpClip;
        jumpFromTime = jumpClipTime;
        jumpClip = clip;
        jumpClipTime = time;
        jumpFade = jumpWeight > 0.0F ? 0.0F : 1.0F;
    }

    private static boolean inAir(int phase) {
        return phase >= JUMP_TAKEOFF && phase <= JUMP_FALL;
    }

    public static float impactShifted(BhHorseRenderState state, float shiftSeconds) {
        float progress = state.jumpImpactProgress + shiftSeconds / JUMP_IMPACT_SECONDS;
        if (progress <= 0.0F || progress >= 1.0F) {
            return 0.0F;
        }
        return (impactFirst(progress) + 0.30F * impactSecond(progress)) * state.jumpImpactPower;
    }

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

    static {
        BhClientCaches.register(BhEquineGait::reset);
    }
}
