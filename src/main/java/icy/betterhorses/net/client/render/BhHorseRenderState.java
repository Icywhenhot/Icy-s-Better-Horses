package icy.betterhorses.net.client.render;

public class BhHorseRenderState {

    private static final java.util.Map<Integer, BhHorseRenderState> ACTIVE = new java.util.HashMap<>();

    private static final int PREVIEW_BASE = Integer.MIN_VALUE;
    private static boolean previewing;

    public static void beginPreview() {
        previewing = true;
    }

    public static void endPreview() {
        previewing = false;
    }

    public static int renderId(int entityId) {
        return previewing ? PREVIEW_BASE + entityId : entityId;
    }

    public static int previewId(int entityId) {
        return PREVIEW_BASE + entityId;
    }

    static BhHorseRenderState forEntity(int id) {
        return ACTIVE.computeIfAbsent(id, ignored -> new BhHorseRenderState());
    }

    public static void remove(int id) {
        ACTIVE.remove(id);
        ACTIVE.remove(PREVIEW_BASE + id);
    }

    public static void reset() {
        ACTIVE.clear();
    }

    static {
        icy.betterhorses.net.client.BhClientCaches.register(BhHorseRenderState::reset);
    }

    final java.util.Map<BhHorseModel.PoseKey, BhHorseModel.Pose> poses = new java.util.HashMap<>();
    int poseRevision;
    public float ageInTicks;
    public float walkAnimationSpeed;
    public float walkAnimationPos;
    public float eatAnimation;
    public float standAnimation;
    public float feedingAnimation;
    public float xRot;
    public float yRot;
    public float phaseOffset;
    public float random01;
    public int entityId;
    public boolean onGround;
    public boolean isPassenger;
    public boolean isRidden;
    public boolean isBaby;
    public boolean isInWater;
    public float hurt;
    public float bodyYaw;
    public float healthFraction = 1.0F;
    public float walkWeight;
    public float trotWeight;
    public float canterWeight;
    public float runWeight;
    public float swimWeight;
    public int gear;
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
    public float rearWeight;
    public int kickTicks;
    public float kickPhase = 1.0F;
    public int stompTicks;
    public float stompPhase = 1.0F;
    public float verticalSpeed;
    public float posY;
    public float jumpChargeInput;
    public int jumpCue;
    public int stabilizer;
    public boolean firstPersonRider;

    public int jumpClip;
    public float jumpClipTime;
    public int jumpFromClip;
    public float jumpFromTime;
    public float jumpFade = 1.0F;
    public float jumpWeight;
    public float jumpLegWeight;
    public float jumpPitchScale = 1.0F;
    public float jumpAir;
    public float jumpPush;
    public float jumpFlail;
    public float jumpWhip;
    public float panicWeight;
    public float fallDamp;

    public float jumpImpact;
    public float jumpImpactSecond;
    public float jumpActive;

    public float jumpImpactProgress = Float.MAX_VALUE;
    public float jumpImpactPower;
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
    public float mountSettle;
    public boolean pullingCart;
    public float bankWeight;
    public float skidWeight;
    public float limpWeight;
    public float forwardSpeed;
    public float pivotWeight;
    public float pivotPhase;
    public float pivotDir;
    public float backWeight;
}
