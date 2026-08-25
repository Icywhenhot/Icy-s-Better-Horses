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

    private static final float JUMP_TAKEOFF_SPEED = 0.16F;
    private static final float JUMP_RISE_FULL = 0.34F;
    private static final float JUMP_FALL_FULL = 0.40F;
    private static final float JUMP_MIN_AIR_SECONDS = 0.12F;
    private static final float JUMP_THRUST_SECONDS = 0.24F;
    private static final float JUMP_THRUST_ATTACK = 0.34F;
    private static final float JUMP_IMPACT_SECONDS = 0.62F;
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
    private static final float JUMP_RISE_IN_PER_SECOND = 14.0F;
    private static final float JUMP_RISE_OUT_PER_SECOND = 5.0F;
    private static final float ARC_LAG_SECONDS = 0.10F;

    private static final Map<Integer, BhEquineGait> ACTIVE = new HashMap<>();

    private final float random01;

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

    public static void fillJumpInputs(net.minecraft.world.entity.Entity entity,
                                      BhHorseRenderState state) {
        state.verticalSpeed = (float) (entity.getY() - entity.yOld);

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

        boolean reversing = state.forwardSpeed < -BACK_SPEED_TRIGGER && !swimming;
        back = Mth.clamp(back + (reversing ? BACK_IN_PER_SECOND : -BACK_OUT_PER_SECOND)
                * deltaSeconds, 0.0F, Math.max(0.0F, 1.0F - rear));
        state.backWeight = back;

        if (trotOut > 0.0F || runOut > 0.0F || tolt > 0.0F) {
            strideOffset += deltaSeconds * limbSpeed
                    * (state.isRidden ? 1.0F - 0.7F * runOut : 1.0F);
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

        final float verticalSpeed = Mth.clamp(state.verticalSpeed, -3.0F, 3.0F);
        final boolean airborne = !state.onGround && !state.isInWater && !state.isPassenger;

        final float gatherTarget = airborne || state.isInWater
                ? 0.0F
                : Mth.clamp(state.jumpChargeInput, 0.0F, 1.0F);
        jumpGather += Mth.clamp(gatherTarget - jumpGather,
                -JUMP_GATHER_OUT_PER_SECOND * deltaSeconds,
                JUMP_GATHER_IN_PER_SECOND * deltaSeconds);

        if (!jumpSeeded) {
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

        jumpFlight = airborne && jumpAirSeconds > 0.0F
                ? Math.min(1.0F, jumpFlight + JUMP_FLIGHT_PER_SECOND * deltaSeconds)
                : 0.0F;

        state.jumpThrustProgress = jumpThrustClock / JUMP_THRUST_SECONDS;
        state.jumpImpactProgress = jumpImpactClock / JUMP_IMPACT_SECONDS;
        state.jumpLaunchPower = jumpLaunchPower;
        state.jumpImpactPower = jumpImpactPower;
        state.jumpLeadSign = leadSign;

        state.jumpThrust = thrustShifted(state, 0.0F);

        state.jumpGather = jumpGather;
        state.jumpFlight = jumpFlight;

        final float riseTarget = Mth.clamp(verticalSpeed / JUMP_RISE_FULL, 0.0F, 1.0F) * jumpFlight;
        jumpRiseSmooth += Mth.clamp(riseTarget - jumpRiseSmooth,
                -JUMP_RISE_OUT_PER_SECOND * deltaSeconds,
                JUMP_RISE_IN_PER_SECOND * deltaSeconds);
        state.jumpRise = jumpRiseSmooth;
        state.jumpFall = Mth.clamp(-verticalSpeed / JUMP_FALL_FULL, 0.0F, 1.0F) * jumpFlight;

        jumpReach += Mth.clamp(state.jumpFall - jumpReach,
                -JUMP_REACH_OUT_PER_SECOND * deltaSeconds,
                JUMP_REACH_IN_PER_SECOND * deltaSeconds);
        state.jumpReach = jumpReach;

        state.jumpImpact = impactShifted(state, 0.0F);
        state.jumpImpactSecond = impactSecondShifted(state, 0.0F);

        float jumpTail = Math.max(Math.max(jumpFlight, jumpReach),
                Math.max(state.jumpThrust, state.jumpImpact));
        state.jumpActive = Math.min(1.0F, Math.max(jumpGather, jumpTail));
        lastJumpActive = state.jumpActive;

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


    public static float arcPitch(BhHorseRenderState state) {
        return (-16.0F * state.jumpRise
              - 10.0F * state.jumpThrust
              + 9.0F * state.jumpReach
              + 5.0F * state.jumpImpact
              - 7.0F * state.jumpImpactSecond) * Mth.DEG_TO_RAD;
    }

    public static float thrustShifted(BhHorseRenderState state, float shiftSeconds) {
        float progress = state.jumpThrustProgress + shiftSeconds / JUMP_THRUST_SECONDS;
        if (progress <= 0.0F || progress >= 1.0F) {
            return 0.0F;
        }
        return smoothPulse(progress, JUMP_THRUST_ATTACK) * state.jumpLaunchPower;
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
}
