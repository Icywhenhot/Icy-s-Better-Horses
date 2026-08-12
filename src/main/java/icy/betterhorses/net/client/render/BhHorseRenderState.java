package icy.betterhorses.net.client.render;

import net.minecraft.client.renderer.entity.state.EquineRenderState;

/**
 * Render state shared by every dedicated breed horse.
 *
 * <p>{@link EquineRenderState} already carries most of what the animator needs
 * ({@code walkAnimationPos}, {@code walkAnimationSpeed}, {@code ageInTicks}, {@code yRot},
 * {@code xRot}, {@code isBaby}, {@code isInWater}, {@code isRidden}) plus — usefully —
 * {@code standAnimation} and {@code eatAnimation}. Fresh Animations has to reconstruct
 * those last two by reading an invisible neck bone, because a resource pack cannot see
 * entity state. Running in Java they are simply handed to us.
 *
 * <p>What is added here is the handful of inputs vanilla does not expose on the state,
 * plus the gait weights, which have to be integrated over time rather than computed
 * fresh each frame.
 *
 * <p>Nothing here is breed-specific, and nothing here should become breed-specific:
 * {@link BhHorseModel} reads these fields and drives every breed from them. A breed
 * subclasses this only so its renderer has a distinct state type; if one ever needs an
 * extra input, that is where it goes.
 */
public abstract class BhHorseRenderState extends EquineRenderState {

    /** Which coat texture this horse rolled. Set from the entity every frame. */
    public net.minecraft.resources.Identifier coatTexture;

    /** Stable per-entity phase offset, so a herd never breathes or blinks in unison. */
    public float phaseOffset;

    public boolean onGround;
    public boolean isPassenger;

    /** 0 when unhurt, rising towards 1 immediately after a hit. */
    public float hurt;

    /**
     * Gait weights. These cross-fade rather than switch, and are clamped so they can never
     * sum past 1 — every bone is then a weighted sum of per-gait terms. This is the single
     * idea that makes the motion read as continuous instead of stepped.
     */
    public float walkWeight;
    public float trotWeight;
    public float runWeight;
    public float swimWeight;

    /** {@code 1} at a standstill, falling to {@code 0} as soon as the horse is really moving. */
    public float idleWeight;
    /** The inverse of {@link #idleWeight}, saturating early. */
    public float moveWeight;

    /** Stride phase, adjusted so the gait cycle stays continuous across gait changes. */
    public float stridePhase;
}
