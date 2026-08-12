package icy.betterhorses.net.client.render;

import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-entity gait state, integrated over time.
 *
 * <p>Most animation values can be computed fresh every frame from the entity's current
 * speed. Gait weights cannot: they have to <em>ramp</em>, so that a horse breaking into a
 * gallop blends over a fraction of a second instead of snapping. That means remembering
 * last frame's value, which means state, which means one of these per entity.
 *
 * <p>Thresholds and ramp rates follow the Fresh Animations horse, whose feel this is meant
 * to reproduce. The per-breed {@code GaitProfile} hook is where a Shire eventually gets a
 * slower, heavier step without any of this logic being duplicated.
 */
public final class BhEquineGait {

    /** Gait weights slide at this many units per second. ~0.17s for a full change. */
    private static final float RAMP_PER_SECOND = 6.0F;
    /** Swimming fades in slowly and out quickly, so surfacing looks decisive. */
    private static final float SWIM_IN_PER_SECOND = 0.8F;
    private static final float SWIM_OUT_PER_SECOND = 2.0F;

    private static final Map<Integer, BhEquineGait> ACTIVE = new HashMap<>();

    private float walk;
    private float trot;
    private float run;
    private float swimRamp;
    private float stridePhase;
    private float lastAgeInTicks = Float.NaN;

    private BhEquineGait() {}

    public static BhEquineGait get(int entityId) {
        return ACTIVE.computeIfAbsent(entityId, id -> new BhEquineGait());
    }

    /** Called on world change so stale entity ids don't accumulate across sessions. */
    public static void reset() {
        ACTIVE.clear();
    }

    /**
     * Advance the integrated state and write the result onto the render state.
     *
     * @param ageInTicks the entity's age, used only to derive the time delta - deriving it
     *                   rather than trusting a frame timer keeps the ramp speed correct when
     *                   the game is paused or running slowly.
     */
    public void advance(BhHorseRenderState state, float ageInTicks) {
        float deltaSeconds;
        if (Float.isNaN(lastAgeInTicks)) {
            deltaSeconds = 0.0F;
        } else {
            // ticks -> seconds, guarded against pauses and backwards jumps
            deltaSeconds = Mth.clamp((ageInTicks - lastAgeInTicks) / 20.0F, 0.0F, 0.25F);
        }
        lastAgeInTicks = ageInTicks;

        float limbSpeed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
        boolean swimming = !state.isPassenger && !state.onGround && state.isInWater;

        swimRamp = Mth.clamp(
                swimRamp + (swimming ? SWIM_IN_PER_SECOND : -SWIM_OUT_PER_SECOND) * deltaSeconds,
                0.0F, 1.0F);
        float swim = Mth.clamp(-0.5F + swimRamp * 2.0F, 0.0F, 1.0F);

        // a ridden horse commits to a gait sooner; a foal has shorter legs and so a
        // higher stride rate for the same ground speed
        float runThreshold = state.isRidden ? 0.8F : (state.isBaby ? 0.7F : 0.97F);
        float trotThreshold = state.isRidden ? 0.4F : 0.6F;

        run = Mth.clamp(
                run + (limbSpeed >= runThreshold ? RAMP_PER_SECOND : -RAMP_PER_SECOND) * deltaSeconds,
                0.0F, Math.max(0.0F, 1.0F - swim));
        trot = Mth.clamp(
                trot + (limbSpeed >= trotThreshold ? RAMP_PER_SECOND : -RAMP_PER_SECOND) * deltaSeconds,
                0.0F, Math.max(0.0F, 1.0F - swim - run));
        walk = Math.max(0.0F, 1.0F - swim - run);

        // the stride timer runs off the walk animation position, but a ridden gallop
        // covers ground faster than the legs cycle, so it is eased back a little
        stridePhase = state.walkAnimationPos * 0.8F * (state.isRidden ? 1.0F - 0.3F * run : 1.0F);
        if (state.isBaby) {
            stridePhase /= 1.8F;
        }

        state.walkWeight = walk;
        state.trotWeight = trot;
        state.runWeight = run;
        state.swimWeight = swim;
        state.idleWeight = Math.max(0.0F, 1.0F - limbSpeed * 6.0F);
        state.moveWeight = Math.min(1.0F, limbSpeed * 6.0F);
        state.stridePhase = stridePhase;
    }
}
