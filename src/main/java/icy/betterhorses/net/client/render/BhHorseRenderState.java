package icy.betterhorses.net.client.render;

import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;

public class BhHorseRenderState extends EquineRenderState {

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

    public float rearWeight;

    public float verticalSpeed;
    public float jumpChargeInput;

    public float jumpGather;
    public float jumpThrust;
    public float jumpFlight;
    public float jumpRise;
    public float jumpFall;
    public float jumpReach;
    public float jumpImpact;
    public float jumpImpactSecond;
    public float jumpActive;

    public float arcPitch;
    public float arcWhip;

    public float jumpThrustProgress = Float.MAX_VALUE;
    public float jumpImpactProgress = Float.MAX_VALUE;
    public float jumpLaunchPower;
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

    public float bankWeight;

    public float skidWeight;

    public float limpWeight;

    public float forwardSpeed;
    public float pivotWeight;
    public float pivotPhase;
    public float pivotDir;
    public float backWeight;

    private BhEquineGait gait;

    BhEquineGait gaitFor(int entityId) {
        if (gait == null || gait.entityId() != entityId) {
            gait = new BhEquineGait(entityId);
        }
        return gait;
    }
}
