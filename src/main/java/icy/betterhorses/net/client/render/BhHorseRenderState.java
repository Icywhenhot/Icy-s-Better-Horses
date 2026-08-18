package icy.betterhorses.net.client.render;

import net.minecraft.client.renderer.entity.state.EquineRenderState;

public abstract class BhHorseRenderState extends EquineRenderState {

    public net.minecraft.resources.Identifier coatTexture;

    public float phaseOffset;

    public float random01;

    /**
     * The horse's entity id. Needed because the rider is a separate entity rendered by a separate
     * renderer, so the only way to hand it this horse's animated saddle transform is through a
     * map keyed by id - the model instance is shared across the whole breed and this render state
     * object is a single instance refilled per entity per frame.
     */
    public int entityId;

    public boolean onGround;
    public boolean isPassenger;

    public float hurt;

    public float bodyYaw;
    public float healthFraction = 1.0F;

    public float walkWeight;
    public float trotWeight;
    public float runWeight;
    public float swimWeight;

    public float idleWeight;
    public float moveWeight;

    public float gaitedBlend;
    public float toltRequest;
    public float toltWeight;

    public float stridePhase;

    public float riddenHeadDrop;
    public float riddenWeight;

    public float landPhase;
    public float landWeight;

    /**
     * Rearing, 0..1 - vanilla's {@code standAnimation} after the jump has had its say.
     *
     * <p>Read this, never {@code standAnimation} directly. The rear and the jump both pose the
     * entire animal and they were never meant to run together; this is where the jump wins.
     */
    public float rearWeight;

    /** Blocks per tick, + is up. {@code getY() - yOld}, so it works for remote horses too. */
    public float verticalSpeed;
    /** 0..1 jump charge. Only the local rider's own mount can see this; 0 for everyone else. */
    public float jumpChargeInput;

    /** Crouch while a rider charges the jump. */
    public float jumpGather;
    /** Push-off pulse, fires the frame the hooves leave the ground. */
    public float jumpThrust;
    /** 1 while airborne. Hard-zeroed on touchdown - nothing that must survive it hangs off this. */
    public float jumpFlight;
    /** Climbing part of the arc. */
    public float jumpRise;
    /** Descending part of the arc. */
    public float jumpFall;
    /**
     * Forelegs reaching for the ground. Rises during the descent and decays *through* touchdown,
     * which is what makes the landing continuous with the flight instead of a second animation.
     */
    public float jumpReach;
    /** Landing compression. Starts at 0 on the exact touchdown frame, so it can never pop. */
    public float jumpImpact;
    /** Second landing beat - the hind legs coming down after the forehand. */
    public float jumpImpactSecond;
    /** Max of every jump weight. Used to suppress ground-only motion. */
    public float jumpActive;

    /** The body arc, radians, + is nose-down. Authored in {@code BhEquineGait.arcPitch}. */
    public float arcPitch;
    /**
     * How far the arc has outrun a one-pole lag trailing it, i.e. how fast it is moving right now.
     * A bone that should feel heavy subtracts a share of this and so arrives at its pose late, in
     * proportion to how violently the body just moved. Zero whenever the arc is holding still, so
     * it costs nothing on the ground.
     */
    public float arcWhip;

    /**
     * Raw 0..1 progress through the push-off and landing pulses, so a leg can evaluate the same
     * curve a fraction of a beat early or late. {@code >= 1} means the pulse is over.
     *
     * <p>The evaluated weights above are the shared ones - body, neck, tail, ears all read those.
     * Only the legs re-evaluate, because only the legs need to disagree with each other.
     */
    public float jumpThrustProgress = Float.MAX_VALUE;
    public float jumpImpactProgress = Float.MAX_VALUE;
    /** Amplitudes the two pulses are scaled by, needed to re-evaluate them per leg. */
    public float jumpLaunchPower;
    public float jumpImpactPower;
    /**
     * +1 or -1, fixed per horse. Which foreleg leads the jump - real horses take off and land one
     * side before the other, and it is the same side every time for a given animal.
     */
    public float jumpLeadSign;

    public float idleTimer;
    public float idleEnergy;

    public float shakeRaw;
    public float waterShakeRaw;

    public float frontLeftStampRaw;
    public float backRightStampRaw;

    public float earFlickLeftRaw;
    public float earFlickRightRaw;

    public float tailSwishRaw;

    public float exertion;
    public float breathPhase;

    public boolean commandedToStay;
    public float stayWeight;

    public float restLeftHind;
    public float restRightHind;

    public float mountSettle;

    public float bankWeight;


    public float skidWeight;

    public float limpWeight;

    /** Blocks per tick along the horse's own facing. Negative means it is backing up. */
    public float forwardSpeed;
    /** 0..1 turning on the spot. Not ridden-gated - wild horses pivot too. */
    public float pivotWeight;
    /** Step cycle for the pivot. Advances with degrees turned, not with time. */
    public float pivotPhase;
    /** -1..1 which way it is turning. */
    public float pivotDir;
    /** 0..1 backing up. */
    public float backWeight;
}
