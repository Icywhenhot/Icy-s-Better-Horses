package icy.betterhorses.net.client.render;

import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;

public class BhHorseRenderState extends EquineRenderState {

    final java.util.Map<BhHorseModel.PoseKey, BhHorseModel.Pose> poses = new java.util.HashMap<>();
    int poseRevision;

    public Identifier coatTexture;

    public float phaseOffset;

    public float random01;

    public int entityId;

    public boolean onGround;
    public boolean isPassenger;

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
