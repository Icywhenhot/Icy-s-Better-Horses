package icy.betterhorses.net.client.render;

import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.entity.FriesianHorse;
import icy.betterhorses.net.entity.IcelandicHorse;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;

public abstract class BhHorseModel<T extends BhBreedHorse> extends EntityModel<T>
        implements BhHorseModelAccess {

    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart snout;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart mane;
    private final ModelPart maneTip;
    private final ModelPart tail;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart leftRein;
    private final ModelPart rightRein;
    private final ModelPart rootPart;
    private float partialTick;

    private final Rest rootRest;
    private final Rest bodyRest;
    private final Rest neckRest;
    private final Rest headRest;
    private final Rest tailRest;
    private final Rest maneRest;
    private final Rest maneTipRest;
    private final Rest snoutRest;
    private final Rest leftEarRest;
    private final Rest rightEarRest;
    private final Rest[] legRest;
    private final ModelPart[] legs;

    record Rest(float x, float y, float z, float xRot, float yRot, float zRot) {
        static Rest of(ModelPart p) {
            return new Rest(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot);
        }
    }

    record PoseKey(List<net.minecraft.client.model.geom.PartPose> rests, List<Float> shape) {}

    static final class Pose {
        final float[] parts;
        int revision;
        float age;
        BhRiderMotion rider;

        Pose(int size) { parts = new float[size]; }
    }

    private PoseKey poseKey;
    ModelPart[] posedParts;

    private void preparePose() {
        posedParts = new ModelPart[]{rootPart, body, neck, head, snout, leftEar, rightEar,
                mane, maneTip, tail, frontLeftLeg, frontRightLeg, backLeftLeg, backRightLeg};
        poseKey = new PoseKey(java.util.Arrays.stream(posedParts).map(ModelPart::storePose).toList(),
                List.of(legLever[0], legLever[1], legLever[2], legLever[3], frameScale, grazeNeck, grazeHeadRel,
                        gaitScale(true), gaitScale(false), gaitShoulderHold(true), gaitShoulderHold(false),
                        gaitReachScale(true), gaitReachScale(false)));
    }

    private void restorePose(float[] pose) {
        int i = 0;
        for (ModelPart part : posedParts) {
            part.x = pose[i++]; part.y = pose[i++]; part.z = pose[i++];
            part.xRot = pose[i++]; part.yRot = pose[i++]; part.zRot = pose[i++];
            part.xScale = pose[i++]; part.yScale = pose[i++]; part.zScale = pose[i++];
        }
    }

    private void savePose(BhHorseRenderState state) {
        Pose saved = state.poses.computeIfAbsent(poseKey, key -> new Pose(posedParts.length * 9));
        float[] pose = saved.parts;
        int i = 0;
        for (ModelPart part : posedParts) {
            pose[i++] = part.x; pose[i++] = part.y; pose[i++] = part.z;
            pose[i++] = part.xRot; pose[i++] = part.yRot; pose[i++] = part.zRot;
            pose[i++] = part.xScale; pose[i++] = part.yScale; pose[i++] = part.zScale;
        }
        saved.revision = state.poseRevision;
        saved.age = state.ageInTicks;
        saved.rider = BhRiderMotion.get(state.entityId);
    }

    private float[] legLever;

    private float frameScale;

    private float grazeNeck;
    private float grazeHeadRel;

    private BhJumpRig jumpRig;
    private final float[] clipPose = new float[BhJumpClips.POSE_SIZE];
    private final float[] clipFrom = new float[BhJumpClips.POSE_SIZE];

    private static float bhHeadDrop(BhBreedHorse horse) {
        if (horse instanceof IcelandicHorse || horse instanceof FriesianHorse) {
            return 0.0F;
        }
        return BhBreedData.of(horse.bhFixedBreed()).archetype() == BhContent.DRAFT.get()
                ? 20.0F * Mth.DEG_TO_RAD
                : 25.0F * Mth.DEG_TO_RAD;
    }

    private static ModelPart childOrEmpty(ModelPart parent, String name) {
        return parent.hasChild(name)
                ? parent.getChild(name)
                : new ModelPart(List.of(), Map.of());
    }

    private static float measureSnoutReach(ModelPart snout, float snoutZ, float fallback) {
        final float[] minZ = {Float.MAX_VALUE};
        snout.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.contains("rein")) {
                return;
            }
            minZ[0] = Math.min(minZ[0], cube.minZ);
        });
        return minZ[0] == Float.MAX_VALUE ? fallback : -(snoutZ + minZ[0]);
    }

    protected float gaitScale(boolean front) {
        return 1.0F;
    }

    protected float gaitShoulderHold(boolean front) {
        return 0.0F;
    }

    protected float gaitReachScale(boolean front) {
        return 1.0F;
    }

    private static float canterSweep(float p) {
        if (p < CANTER_DUTY) {
            return -1.0F + 2.0F * (p / CANTER_DUTY);
        }
        float u = (p - CANTER_DUTY) / (1.0F - CANTER_DUTY);
        return 1.0F - 2.0F * (u * u * (3.0F - 2.0F * u));
    }

    private static float measureBottom(ModelPart part, float fallback) {
        final float[] low = {-Float.MAX_VALUE};
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty()) {
                low[0] = Math.max(low[0], cube.maxY);
            }
        });
        return low[0] == -Float.MAX_VALUE ? fallback : low[0];
    }

    private static float measureLegLever(ModelPart part, float fallback) {
        final float[] top = {Float.MAX_VALUE};
        part.visit(new PoseStack(),
                (pose, path, index, cube) -> top[0] = Math.min(top[0], cube.minY));
        return top[0] == Float.MAX_VALUE ? fallback : -top[0];
    }

    protected BhHorseModel(ModelPart root) {
        this.body = childOrEmpty(root, "body");
        this.neck = childOrEmpty(this.body, "neck2");
        this.head = childOrEmpty(this.neck, "head2");
        this.snout = childOrEmpty(this.head, "snout2");
        this.leftEar = childOrEmpty(this.head, "left_ear2");
        this.rightEar = childOrEmpty(this.head, "right_ear2");
        this.mane = childOrEmpty(this.neck, "mane2");
        this.maneTip = childOrEmpty(this.head, "mane3");
        this.tail = childOrEmpty(this.body, "tail2");
        this.frontLeftLeg = childOrEmpty(root, "front_left_leg");
        this.frontRightLeg = childOrEmpty(root, "front_right_leg");
        this.backLeftLeg = childOrEmpty(root, "back_left_leg");
        this.backRightLeg = childOrEmpty(root, "back_right_leg");
        this.leftRein = childOrEmpty(this.snout, "left_rein2");
        this.rightRein = childOrEmpty(this.snout, "right_rein2");

        this.rootPart = root;
        this.rootRest = Rest.of(root);
        this.bodyRest = Rest.of(this.body);
        this.neckRest = Rest.of(this.neck);
        this.headRest = Rest.of(this.head);
        this.tailRest = Rest.of(this.tail);
        this.maneRest = Rest.of(this.mane);
        this.maneTipRest = Rest.of(this.maneTip);
        this.snoutRest = Rest.of(this.snout);
        this.leftEarRest = Rest.of(this.leftEar);
        this.rightEarRest = Rest.of(this.rightEar);
        this.legs = new ModelPart[] {frontLeftLeg, frontRightLeg, backLeftLeg, backRightLeg};
        this.legRest = new Rest[legs.length];
        this.legLever = new float[legs.length];
        for (int i = 0; i < legs.length; i++) {
            this.legRest[i] = Rest.of(legs[i]);
            this.legLever[i] = measureLegLever(legs[i], i < 2 ? 12.0F : 15.0F);
        }
        this.frameScale = this.legLever[0] / 12.0F;
        this.jumpRig = new BhJumpRig(this.frameScale, measureBottom(this.body, 5.0F),
                this.bodyRest, this.neckRest, this.headRest, this.leftEarRest, this.rightEarRest,
                this.tailRest, this.legRest, this.legLever);

        final float snoutReach = measureSnoutReach(this.snout, this.snoutRest.z(), 10.0F);
        final float grazeDrop = GRAZE_DROP_PIXELS * this.frameScale;
        final float neckBaseY = this.bodyRest.y() + this.neckRest.y()
                + grazeDrop + (-this.neckRest.z()) * Mth.sin(GRAZE_PITCH);

        final float a = this.headRest.y();
        final float b = -this.headRest.z();
        final float r = (float) Math.sqrt(a * a + b * b);
        float neck = GRAZE_NECK_MAX;
        if (r > 1.0E-4F) {
            float k = Mth.clamp((GRAZE_MUZZLE_Y - neckBaseY - snoutReach) / r, -1.0F, 1.0F);
            neck = (float) (Math.atan2(b, a) - Math.acos(k));
        }
        this.grazeNeck = Mth.clamp(neck, GRAZE_NECK_MIN, GRAZE_NECK_MAX);
        this.grazeHeadRel = Mth.PI / 2.0F - this.grazeNeck;
    }

    void matchFrame(BhHorseModel horse) {
        this.legLever = horse.legLever;
        this.frameScale = horse.frameScale;
        this.grazeNeck = horse.grazeNeck;
        this.grazeHeadRel = horse.grazeHeadRel;
        this.jumpRig = horse.jumpRig;
    }

    @Override
    public ModelPart bh_getBody() {
        return this.body;
    }

    public ModelPart root() {
        return rootPart;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer,
                               int packedLight, int packedOverlay, int color) {
        rootPart.render(poseStack, consumer, packedLight, packedOverlay, color);
    }

    Rest bhBodyRest() {
        return bodyRest;
    }

    private static final float BANK_ROLL = 11.0F * Mth.DEG_TO_RAD;

    private static final float GROUND_Y = 24.0F;

    private static final float NECK_REST_TILT = 12.0F * Mth.DEG_TO_RAD;

    private static final float GRAZE_DROP_PIXELS = 3.5F;
    private static final float GRAZE_PITCH = 12.0F * Mth.DEG_TO_RAD;
    private static final float GRAZE_MUZZLE_Y = GROUND_Y - 0.5F;
    private static final float GRAZE_NECK_MIN = 70.0F * Mth.DEG_TO_RAD;
    private static final float GRAZE_NECK_MAX = 145.0F * Mth.DEG_TO_RAD;

    private static final float CANTER_DUTY = 0.55F;
    private static final float CANTER_ROT_FRONT = 0.52F;
    private static final float CANTER_ROT_BACK = 0.46F;
    private static final float CANTER_REACH_FRONT = 7.0F;
    private static final float CANTER_REACH_BACK = 6.0F;
    private static final float CANTER_LIFT_FRONT = 2.6F;
    private static final float CANTER_LIFT_BACK = 2.2F;

    private static final float[] CANTER_RIGHT_LEAD = {1.0F / 3.0F, 0.0F, 2.0F / 3.0F, 1.0F / 3.0F};
    private static final float[] CANTER_LEFT_LEAD = {0.0F, 1.0F / 3.0F, 1.0F / 3.0F, 2.0F / 3.0F};

    private static final float[] LEG_LAG_SECONDS = {0.020F, 0.020F, 0.013F, 0.013F};

    private static final float[] LEG_DIAGONAL = {1.0F, -1.0F, -1.0F, 1.0F};

    private static final float FLAIL_FRONT_DEG = 5.0F;
    private static final float FLAIL_BACK_DEG = 7.0F;

    private static final float RIDER_JUMP_FOLLOW = 0.3F;

    private static final float PANIC_HZ = 2.1F;
    private static final float PANIC_STROKE = 1.3F;
    private static final float[] PANIC_LEG_PHASE = {0.0F, Mth.PI, Mth.PI * 0.5F, -Mth.PI * 0.5F};

    private static final float[] PIVOT_LEG_PHASE =
            {0.0F, Mth.PI, Mth.PI * 0.5F, Mth.PI * 1.5F};

    private static final float PIVOT_FRONT_DEG = 11.0F;
    private static final float PIVOT_BACK_DEG = 5.0F;

    @Override
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        this.partialTick = partialTick;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        BhHorseRenderState state = BhHorseRenderState.forEntity(BhHorseRenderState.renderId(entity.getId()));
        state.ageInTicks = ageInTicks;
        state.walkAnimationPos = limbSwing;
        state.walkAnimationSpeed = limbSwingAmount;
        state.eatAnimation = entity.getEatAnim(partialTick);
        state.standAnimation = entity.getStandAnim(partialTick);
        state.feedingAnimation = entity.getMouthAnim(partialTick);
        state.xRot = headPitch;
        state.yRot = netHeadYaw;
        state.onGround = entity.onGround();
        state.isPassenger = entity.isPassenger();
        state.isRidden = entity.isVehicle();
        state.isBaby = entity.isBaby();
        state.isInWater = entity.isInWater();
        state.hurt = entity.hurtTime > 0 ? entity.hurtTime / 10.0F : 0.0F;
        state.bodyYaw = entity.getYRot();
        state.healthFraction = entity.getMaxHealth() > 0.0F
                ? Mth.clamp(entity.getHealth() / entity.getMaxHealth(), 0.0F, 1.0F)
                : 1.0F;
        state.phaseOffset = (entity.getId() * 0.6180339887F % 1.0F) * Mth.TWO_PI;
        state.riddenHeadDrop = bhHeadDrop(entity);
        state.commandedToStay = IHorseData.of(entity).bh_getCommand().equals(BhContent.COMMAND_STAY.getKey());
        state.entityId = BhHorseRenderState.renderId(entity.getId());
        if (entity instanceof IcelandicHorse) {
            state.gaitedBlend = 1.0F;
            IHorseData data = IHorseData.of(entity);
            state.toltRequest = state.isRidden
                    ? (data.bh_getGaitGear() == BhGears.TOLT_GEAR ? 1.0F : 0.0F)
                    : (data.bh_isOwned() && data.bh_getCommand().equals(BhContent.COMMAND_FOLLOW.getKey()) ? 1.0F : 0.0F);
        }
        BhEquineGait.advanceFor(entity, state);
        setupState(state);
    }

    void setupState(BhHorseRenderState state) {
        if (poseKey == null) preparePose();
        Pose pose = state.poses.get(poseKey);
        if (pose != null && pose.revision == state.poseRevision && pose.age == state.ageInTicks) {
            restorePose(pose.parts);
            BhRiderMotion.publish(state.entityId, pose.rider);
            leftRein.visible = state.isRidden;
            rightRein.visible = state.isRidden;
            return;
        }

        final float phase = state.phaseOffset;
        final float age = state.ageInTicks;
        final float stride = state.stridePhase;
        final float speed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);

        final float walk = state.walkWeight;
        final float trot = state.trotWeight;
        final float run = state.runWeight;
        final float swim = state.swimWeight;
        final float canter = state.canterWeight;
        final float idle = state.idleWeight;
        final float move = state.moveWeight;
        final float tolt = state.toltWeight;

        final float rear = state.rearWeight;
        final float graze = state.eatAnimation;

        final float blown = state.exertion;
        final float breathDepth = 1.0F + 5.0F * blown;

        final float breath = state.breathPhase;
        final float swimT = phase + age / 20.0F * Mth.TWO_PI + stride / 3.0F;
        final float rearRaw = age / 2.0F + state.walkAnimationPos / 20.0F;
        final float rearT = phase + rearRaw;
        final float rearT25 = phase + rearRaw / 2.5F;
        final float rearT15 = phase + rearRaw / 1.5F;
        final float chewT = phase + age / 2.0F - Mth.cos(phase + age / 2.0F) / 3.0F;
        final float shakeT = phase + age / 1.2F;
        final float stampT = phase + age / 3.0F;
        final float idleT = state.idleTimer;
        final float alive = state.idleEnergy;
        final float landT = state.landPhase;

        final float jHit = state.jumpImpact;
        final float jHit2 = state.jumpImpactSecond;
        final float jAny = state.jumpActive;
        final float lead = state.jumpLeadSign;
        final float panic = state.panicWeight;
        final float panicT = phase + age / 20.0F * Mth.TWO_PI * PANIC_HZ
              + Mth.sin(phase + age / 7.3F) * 0.8F;

        final float land = state.landWeight * (1.0F - jAny);
        final float shake = 0.5F - 0.5F * Mth.cos(
                state.shakeRaw * state.shakeRaw * state.shakeRaw * Mth.PI);
        final float stampFL = 0.5F - 0.5F * Mth.cos(state.frontLeftStampRaw * Mth.PI);
        final float stampBR = 0.5F - 0.5F * Mth.cos(state.backRightStampRaw * Mth.PI);
        final float wetShake = 0.5F - 0.5F * Mth.cos(
                state.waterShakeRaw * state.waterShakeRaw * state.waterShakeRaw * Mth.PI);
        final float flickL = Mth.sin(state.earFlickLeftRaw * state.earFlickLeftRaw * Mth.PI);
        final float flickR = Mth.sin(state.earFlickRightRaw * state.earFlickRightRaw * Mth.PI);
        final float swish = Mth.sin(state.tailSwishRaw * state.tailSwishRaw
                * state.tailSwishRaw * Mth.PI);

        final float stay = state.stayWeight;
        final float settle = state.mountSettle;
        final float feed = Mth.clamp(state.feedingAnimation, 0.0F, 1.0F);
        final float chewing = Math.max(graze, feed);
        final float flinch = state.hurt * state.hurt
                * Mth.clamp((1.0F - state.hurt) * 8.0F, 0.0F, 1.0F);

        final float notRearingEarly = 1.0F - rear;
        final float bank = state.bankWeight * (0.55F + 0.45F * run) * notRearingEarly;
        final float skid = state.skidWeight;
        final float limp = state.limpWeight * Math.max(0.45F, move) * notRearingEarly;
        final float limpStand = state.limpWeight * (1.0F - move) * notRearingEarly;

        final float pivot = state.pivotWeight;
        final float pivotDir = state.pivotDir;
        final float pivotT = state.pivotPhase * Mth.TWO_PI;
        final float back = state.backWeight;

        final float kickRaw = state.kickPhase;
        final float kickCoil = (0.5F - 0.5F * Mth.cos(
                Mth.clamp(kickRaw / 0.18F, 0.0F, 1.0F)
              * Mth.clamp((0.34F - kickRaw) / 0.16F, 0.0F, 1.0F) * Mth.PI)) * notRearingEarly;
        final float kickSnap = (0.5F - 0.5F * Mth.cos(
                Mth.clamp((kickRaw - 0.30F) / 0.14F, 0.0F, 1.0F)
              * Mth.clamp((1.0F - kickRaw) / 0.56F, 0.0F, 1.0F) * Mth.PI)) * notRearingEarly;

        final float stompRaw = state.stompPhase;
        final float stompLift = 0.5F - 0.5F * Mth.cos(
                Mth.clamp(stompRaw / 0.30F, 0.0F, 1.0F)
              * Mth.clamp((0.52F - stompRaw) / 0.22F, 0.0F, 1.0F) * Mth.PI);
        final float stompDrive = 0.5F - 0.5F * Mth.cos(
                Mth.clamp((stompRaw - 0.46F) / 0.10F, 0.0F, 1.0F)
              * Mth.clamp((1.0F - stompRaw) / 0.44F, 0.0F, 1.0F) * Mth.PI);
        final int stompLeg = state.random01 > 0.5F ? 0 : 1;

        final float soreSign = state.random01 > 0.5F ? 1.0F : -1.0F;
        final float limpNod = Mth.cos(stride) * soreSign * limp;

        float bodyDrop =
                Mth.sin(breath) / 6.0F * idle * breathDepth
              + ((-Mth.sin(stride * 2.0F) + 2.0F) / 3.0F * Math.max(0.3F, move) * (1.0F - trot)
                   * (1.0F - 0.88F * tolt)
                 + (0.1F + Mth.cos(Mth.PI / 4.0F + stride * 2.0F) / 2.0F) * trot) * walk
              + Mth.cos(-Mth.PI / 3.0F + stride) * speed * run
                * (1.0F - rear / (state.onGround ? 2.5F : 0.75F))
              + Mth.cos(-Mth.PI / 5.0F + stride) * speed * 0.8F * canter
                * (1.0F - rear / (state.onGround ? 2.5F : 0.75F))
              + speed / 2.0F
              + (-4.7F - Mth.cos(rearT) / 6.0F) * rear * frameScale
              + 2.0F * land
              + 1.3F * settle
              + 0.5F * limpStand
              + GRAZE_DROP_PIXELS * graze * frameScale
              + 1.0F * skid
              + 0.8F * Math.abs(limpNod)
              + (0.9F * kickCoil - 2.1F * kickSnap) * frameScale
              + (0.7F * stompLift - 0.5F * stompDrive) * frameScale;

        float bodyPitch =
                Mth.sin(-Mth.PI / 4.0F + breath) / 60.0F * idle
              - Mth.cos(stride * 2.0F) / 40.0F * Math.max(0.3F, move) * walk * (1.0F - 0.75F * tolt)
              + (Mth.sin(stride) / 20.0F - Mth.cos(stride) / 13.0F) * speed * 0.7F * run
                * (1.0F - rear / (state.onGround ? 1.2F : 3.0F))
              + (Mth.sin(stride) / 18.0F) * speed * 0.9F * canter
                * (1.0F - rear / (state.onGround ? 1.2F : 3.0F))
              + Mth.sin(Mth.PI / 4.0F + swimT * 2.0F) / 20.0F * swim
              + (-0.7F + Mth.sin(rearT) / 25.0F) * rear
              + (10.0F * Mth.cos(landT * 3.0F)) * Mth.DEG_TO_RAD * land
              - 0.22F * skid
              + GRAZE_PITCH * graze
              + (-5.0F * kickCoil + 16.0F * kickSnap) * Mth.DEG_TO_RAD
              + (-6.0F * Mth.DEG_TO_RAD) * panic;

        final float swimSink = (state.isRidden ? 4.0F : 8.0F) * swim;

        body.y = bodyRest.y() + bodyDrop + swimSink;
        body.z = bodyRest.z() + (4.0F - Mth.sin(rearT) / 4.0F) * rear * frameScale;
        body.xRot = bodyRest.xRot() + bodyPitch;
        body.yRot = bodyRest.yRot() + (-1.0F * Mth.cos(stampT)) * Mth.DEG_TO_RAD * stampBR
              + Mth.sin(shakeT * 0.85F - Mth.PI / 3.0F) * 0.06F * wetShake
              + 0.09F * flinch;
        body.zRot = bodyRest.zRot()
              + (-1.5F * Mth.cos(Mth.PI / 4.0F + stampT)) * Mth.DEG_TO_RAD * (stampBR - stampFL)
              + Mth.sin(shakeT * 0.85F) * 0.11F * wetShake
              + (5.0F * Mth.DEG_TO_RAD) * pivotDir * pivot
              + (4.0F * Mth.DEG_TO_RAD) * soreSign * limp
              + (5.5F * Mth.DEG_TO_RAD) * soreSign * limpStand
              + (3.0F * Mth.DEG_TO_RAD) * Mth.cos(rearT25) * rear
              + (4.0F * Mth.DEG_TO_RAD) * Mth.sin(panicT * 0.5F) * panic;

        final float bankAngle = -BANK_ROLL * bank;
        rootPart.zRot = rootRest.zRot() + bankAngle;
        rootPart.xRot = rootRest.xRot();
        rootPart.x = rootRest.x() + GROUND_Y * Mth.sin(bankAngle);
        rootPart.y = rootRest.y() + GROUND_Y * (1.0F - Mth.cos(bankAngle));
        rootPart.z = rootRest.z();

        final float viewDuck = state.riddenHeadDrop * state.riddenWeight
              * (1.0F - graze) * (1.0F - rear);

        float neckPitch = -bodyPitch
              + NECK_REST_TILT * (1.0F - graze)
              + viewDuck
              + (20.0F * speed * (1.0F - 0.4F * tolt) * Mth.DEG_TO_RAD)
              + Mth.cos(breath) / 80.0F
              + (14.0F * Mth.DEG_TO_RAD + Mth.sin(breath) * 0.055F)
                * blown * idle * (1.0F - graze)
              + Mth.sin(Mth.PI / 4.0F + stride * 2.0F) / 20.0F * Math.max(0.3F, move) * walk
              + Mth.cos(stride) / 6.0F * speed * (1.0F - land) * run
              + ((-20.0F * Mth.DEG_TO_RAD) - Mth.cos(rearT) / 25.0F) * rear
              + grazeNeck * graze
              + (20.0F * Mth.DEG_TO_RAD) * land
              + (-8.0F * Mth.DEG_TO_RAD) * tolt
              + (3.0F * Mth.sin(idleT * 1.5F + Mth.sin(-Mth.PI / 4.0F + idleT / 1.5F) * 2.0F)
                 * alive * (1.0F - graze)) * Mth.DEG_TO_RAD
              + (10.0F * Mth.DEG_TO_RAD) * shake
              - (16.0F * Mth.DEG_TO_RAD) * back
              + (22.0F * Mth.DEG_TO_RAD) * stay * (1.0F - graze)
              + (30.0F * Mth.DEG_TO_RAD) * feed
              - (14.0F * Mth.DEG_TO_RAD) * flinch
              - (16.0F * Mth.DEG_TO_RAD) * skid
              - (16.0F * Mth.DEG_TO_RAD) * limpNod
              + (-22.0F + 7.0F * Mth.sin(panicT * 1.3F + 0.7F)) * Mth.DEG_TO_RAD * panic;

        float neckYaw = (16.0F * Mth.DEG_TO_RAD) * bank
              + (14.0F * Mth.DEG_TO_RAD) * pivotDir * pivot
              + Mth.clamp(state.yRot * Mth.DEG_TO_RAD, -0.6F, 0.6F) * 0.35F
              + (3.0F * Mth.DEG_TO_RAD)
                * Mth.sin(-Mth.PI / 7.0F + idleT + Mth.sin(-Mth.PI / 7.0F + idleT * 2.0F) / 2.0F)
                * (1.0F - Mth.cos(idleT / 2.0F + Mth.cos(idleT) / 2.0F)) * alive;

        neck.xRot = neckRest.xRot() + neckPitch;
        neck.yRot = neckRest.yRot() + neckYaw;
        neck.zRot = neckRest.zRot()
              + Mth.cos(shakeT) / 8.0F * shake
              + (2.0F * Mth.cos(Mth.clamp(0.5F - Mth.sin(-Mth.PI / 12.0F + idleT) * 0.7F,
                                          0.0F, 1.0F) * Mth.PI)
                 * (0.8F - Mth.cos(idleT / 4.0F + Mth.sin(idleT / 4.0F))) * alive) * Mth.DEG_TO_RAD;

        head.xRot = headRest.xRot()
              - viewDuck
              + Mth.clamp(state.xRot * Mth.DEG_TO_RAD, -0.7F, 0.7F) * (1.0F - graze)
                * (1.0F - 0.7F * rear)
              + ((-10.0F * Mth.DEG_TO_RAD)
                 - Mth.sin(Mth.PI / 6.0F + stride) / 7.0F * (1.0F - land))
                * speed * run * (1.0F - 0.7F * rear)
              + grazeHeadRel * graze
              + (7.0F * Mth.DEG_TO_RAD) * tolt
              + (-10.0F - 20.0F * Mth.cos(landT * 4.0F)) * Mth.DEG_TO_RAD * land
              + (-1.5F + 1.5F * Mth.sin(idleT * 1.5F + Mth.sin(idleT / 1.5F) / 2.0F))
                * Mth.DEG_TO_RAD * alive * (1.0F - graze)
              - (10.0F * Mth.DEG_TO_RAD) * shake
              - (8.0F * Mth.DEG_TO_RAD) * stay
              + (7.0F * Mth.DEG_TO_RAD) * back
              - (10.0F * Mth.DEG_TO_RAD) * feed
              - (18.0F * Mth.DEG_TO_RAD) * flinch
              - (8.0F * Mth.DEG_TO_RAD) * skid
              + (-6.0F + 6.0F * Mth.sin(panicT * 1.7F + 2.1F)) * Mth.DEG_TO_RAD * panic;

        float headYaw = Mth.clamp(state.yRot * Mth.DEG_TO_RAD, -0.6F, 0.6F) * 0.5F
              + (10.0F * Mth.DEG_TO_RAD) * bank
              + (5.0F * Mth.DEG_TO_RAD)
                * Mth.cos(Mth.clamp(0.5F - Mth.sin(idleT) * 1.5F, 0.0F, 1.0F) * Mth.PI) * alive
              + (-Mth.sin(shakeT) / 3.0F + Mth.cos(shakeT) / 8.0F) * shake
              + (10.0F * Mth.DEG_TO_RAD) * Mth.sin(panicT * 0.9F) * panic;

        head.yRot = headRest.yRot() + headYaw;
        head.zRot = headRest.zRot()
              + BANK_ROLL * 0.45F * bank
              + (2.0F * Mth.DEG_TO_RAD)
                * Mth.cos(Mth.clamp(0.5F - Mth.sin(idleT) * 1.5F, 0.0F, 1.0F) * Mth.PI)
                * alive * (1.0F - graze)
              + (-Mth.sin(shakeT) / 3.0F) * shake;
        head.x = headRest.x() + Mth.sin(shakeT) / 1.3F * shake;

        snout.xRot = snoutRest.xRot() + Mth.sin(chewT) * 0.06F * chewing;
        snout.zRot = snoutRest.zRot()
              + (-1.0F + Mth.sin(-Mth.PI / 6.0F + chewT)) / 14.0F * chewing
                * (state.random01 > 0.5F ? -1.0F : 1.0F);
        snout.yScale = 1.0F - (0.4F - 1.3F * Mth.sin(Mth.PI / 4.0F + chewT)) / 20.0F * chewing;
        snout.xScale = 1.0F + 0.18F * (0.5F + 0.5F * Mth.sin(breath)) * blown;

        leftRein.visible = state.isRidden;
        rightRein.visible = state.isRidden;

        final float earGait = (-Mth.sin(Mth.PI / 4.0F + breath) / 20.0F
              + Mth.sin(-Mth.PI / 3.0F + stride * 2.0F) / 4.0F * speed * walk * (1.0F - trot)
              + Mth.cos(Mth.PI / 4.0F + stride * 2.0F) / 9.0F * trot
              + Mth.sin(stride) / 3.0F * speed * run) * (1.0F - rear);
        final float earLand = (-40.0F * Mth.cos(landT * 8.0F)) * Mth.DEG_TO_RAD * land;
        final float earChew = (3.0F * Mth.sin(chewT)) * Mth.DEG_TO_RAD * graze;
        final float earFixed = (-12.0F * Mth.DEG_TO_RAD) * skid + (-30.0F * Mth.DEG_TO_RAD) * panic;

        leftEar.xRot = leftEarRest.xRot()
              + (10.0F - 40.0F * Mth.cos(age * 1.5F) * flickL * (1.0F - shake) * walk)
                * Mth.DEG_TO_RAD
              + earGait
              - Mth.sin(rearT) / 6.0F * rear
              + earChew + earLand + earFixed
              + (14.0F * Mth.DEG_TO_RAD) * stay
              - (10.0F * Mth.DEG_TO_RAD) * feed;
        rightEar.xRot = rightEarRest.xRot()
              + (10.0F + 25.0F * Mth.sin(age * 1.5F) * flickR * (1.0F - shake) * walk)
                * Mth.DEG_TO_RAD
              + earGait
              - Mth.sin(Mth.PI / 6.0F + rearT) / 6.0F * rear
              + earChew + earLand + earFixed
              + (14.0F * Mth.DEG_TO_RAD) * stay
              - (10.0F * Mth.DEG_TO_RAD) * feed;

        leftEar.yRot = leftEarRest.yRot()
              + (70.0F * Mth.cos(Mth.PI / 6.0F + age * 1.5F) * flickL * (1.0F - shake) * walk
                 - 50.0F * graze - 25.0F * shake) * Mth.DEG_TO_RAD;
        rightEar.yRot = rightEarRest.yRot()
              + (-70.0F * Mth.sin(Mth.PI / 6.0F + age * 1.5F) * flickR * (1.0F - shake) * walk
                 + 50.0F * graze + 25.0F * shake) * Mth.DEG_TO_RAD;

        leftEar.zRot = leftEarRest.zRot()
              - 0.15F * run - 0.30F * flinch
              + (-3.0F * Mth.sin(chewT) * graze + 10.0F * Mth.cos(landT * 3.0F) * land)
                * Mth.DEG_TO_RAD
              + ((8.0F * Mth.DEG_TO_RAD) + Mth.cos(-Mth.PI / 4.0F + shakeT) / 1.2F) * shake;
        rightEar.zRot = rightEarRest.zRot()
              + 0.15F * run + 0.30F * flinch
              + (3.0F * Mth.sin(chewT) * graze - 10.0F * Mth.cos(landT * 3.0F) * land)
                * Mth.DEG_TO_RAD
              + ((-8.0F * Mth.DEG_TO_RAD) + Mth.cos(-Mth.PI / 4.0F + shakeT) / 1.2F) * shake;

        final float maneFly = Mth.sin(phase + age * 1.7F) * 0.6F
              + Mth.sin(phase + age * 0.71F) * 0.4F;

        final float hairDrive = Math.min(1.5F,
                state.jumpAir + 0.8F * state.jumpPush + 0.6F * jHit + 0.8F * panic);
        final float maneWhip = Mth.sin(phase + age * 3.1F)
              * Math.min(1.4F, state.jumpPush + 0.7F * jHit + 0.5F * panic);

        final float maneGait = Mth.sin(stride * 2.0F - Mth.PI / 4.0F) / 12.0F * move * walk
              + Mth.sin(stride * 2.0F - Mth.PI / 5.0F) / 10.0F * trot
              + Mth.sin(stride - Mth.PI / 3.0F) / 7.0F * speed * run;
        final float maneWind = speed * speed * move;

        mane.yRot = maneRest.yRot() + maneGait
              + Mth.sin(shakeT) / 4.0F * shake
              + maneFly * (0.10F * hairDrive + 0.13F * maneWind) + maneWhip * 0.07F;
        mane.zRot = maneRest.zRot()
              + maneFly * 0.08F * maneWind;
        maneTip.yRot = maneTipRest.yRot() - headYaw + Mth.sin(shakeT) / 4.0F * shake
              + maneGait * 0.7F
              + maneFly * (0.13F * hairDrive + 0.16F * maneWind) + maneWhip * 0.10F;
        maneTip.zRot = maneTipRest.zRot() + Mth.sin(shakeT) / 3.0F * shake
              + maneFly * (0.10F * hairDrive + 0.12F * maneWind) + maneWhip * 0.08F;
        maneTip.x = maneTipRest.x() - headYaw * 3.0F;
        maneTip.yScale = 1.0F + 0.1F * shake;

        tail.xRot = tailRest.xRot()
              + Mth.cos(breath) / 30.0F
              + (10.0F + 40.0F * speed * speed) * Mth.DEG_TO_RAD
              + Mth.sin(Mth.PI / 4.0F + stride * 2.0F) / 16.0F * Math.max(0.3F, move) * walk
              - Mth.sin(stride - Mth.PI / 4.0F) / 5.0F * speed * run
              + (-7.0F + 3.0F * Mth.cos(-Mth.PI / 4.0F + rearT)) * Mth.DEG_TO_RAD * rear
              - (26.0F * Mth.DEG_TO_RAD) * flinch
              + (30.0F * Mth.sin(landT * 5.0F)) * Mth.DEG_TO_RAD * land
              + (15.0F + 25.0F * Mth.sin(age / 1.5F)) * Mth.DEG_TO_RAD * swish
              + (12.0F * Mth.DEG_TO_RAD) * skid
              - (18.0F * Mth.DEG_TO_RAD) * panic;
        tail.yRot = tailRest.yRot() + Mth.sin(stride - Mth.PI / 3.0F) / 8.0F * move * walk
              + Mth.sin(stride - Mth.PI / 3.0F) / 6.0F * speed * run
              + (5.0F * Mth.DEG_TO_RAD) * Mth.sin(-Mth.PI / 4.0F + rearT15) * rear
              + maneFly * (0.09F * hairDrive + 0.11F * maneWind) + maneWhip * 0.06F
              + 0.3F * Mth.sin(panicT * 1.9F) * panic;
        tail.zRot = tailRest.zRot() + (40.0F * Mth.sin(age / 3.0F)) * Mth.DEG_TO_RAD * swish;
        tail.yScale = 1.0F - (0.4F - 1.3F * Mth.sin(Mth.PI / 4.0F + age / 3.0F)) / 20.0F * swish;

        final float ls = stride;
        final float st = swimT;
        final float moveClamped = Math.max(0.3F, move);
        final float notRearing = 1.0F - rear;
        final float trotInner = 2.5F + 2.0F * trot;
        final float FIVE_DEG = 5.0F * Mth.DEG_TO_RAD;
        final float SIXTH_PI = Mth.PI / 6.0F;
        final float NINTH_PI = Mth.PI / 9.0F;
        final float landFront = (10.0F * Mth.cos(landT)) * Mth.DEG_TO_RAD * land;
        final float landBack = (-7.0F * Mth.cos(landT)) * Mth.DEG_TO_RAD * land;
        final float skidFront = (-14.0F * Mth.DEG_TO_RAD) * skid;
        final float skidBack = (-16.0F * Mth.DEG_TO_RAD) * skid;
        final float stampSwing = Mth.sin(stampT);
        final float stampRoll = Mth.cos(stampT - stampSwing / 2.0F);
        final float toltKneeGain = 1.0F + 0.2F * tolt;
        final float toltKneeCeil = SIXTH_PI + Mth.PI / 36.0F * tolt;
        final float toltLift = 1.0F + 2.0F * tolt;
        final float toltLiftFloor = -4.0F - 3.0F * tolt;
        final float toltReach = 1.0F - 0.18F * tolt;
        final float toltEngage = 2.2F * tolt;
        final float braceFront = -0.9F * settle;
        final float braceBack = 0.9F * settle;

        for (int i = 0; i < legs.length; i++) {
            ModelPart leg = legs[i];
            Rest rest = legRest[i];

            float rot;
            float reach;
            float lift;

            switch (i) {
                case 0 -> {
                    float s = Mth.sin(ls - Mth.sin(ls) / trotInner);
                    rot = Mth.sin(breath) / 60.0F
                        + (((FIVE_DEG * trot + s) / 1.8F * moveClamped)
                           + Mth.clamp(-Mth.cos(ls) / 2.0F * toltKneeGain, 0.0F, toltKneeCeil)
                             * (1.0F + trot / 2.0F + 0.2F * tolt) / 2.0F * move) * notRearing * walk
                        + ((-Mth.cos(ls) / 1.6F + Mth.clamp(-Mth.sin(ls) / 4.0F, 0.0F, NINTH_PI))
                           * speed * 1.2F * notRearing) * run
                        + (-Mth.cos(st) / 2.0F
                           + Mth.clamp(-Mth.sin(st) / 3.0F, 0.0F, NINTH_PI)) * notRearing * swim
                        + landFront
                        + (12.0F * Math.max(0.0F, stampSwing * 1.5F + 0.4F))
                          * Mth.DEG_TO_RAD * stampFL;
                    reach = ((1.0F / 6.5F * trot + s) * 6.5F * toltReach * moveClamped * notRearing) * walk
                          + (-Mth.cos(ls) * 8.0F * speed * notRearing) * run
                          + (-Mth.cos(st) * 6.0F * notRearing) * swim
                          + Math.max(0.0F, Mth.sin(Mth.PI / 4.0F + stampT) / 1.5F + 0.4F) * stampFL
                          + braceFront;
                    lift = Mth.clamp((-1.7F * trot + Mth.cos(ls) * 2.0F * toltLift * move * notRearing) * walk
                          + (-2.3F * speed * notRearing + Mth.sin(ls) * 2.0F * speed * notRearing)
                            * run * 1.2F
                          + (-2.0F + Mth.sin(st) * 1.5F * notRearing) * swim
                          + (-stampSwing * 2.0F - 0.8F) * stampFL, toltLiftFloor, 0.0F);
                }
                case 1 -> {
                    float s = -Mth.sin(ls + Mth.sin(ls) / trotInner);
                    float rl = Mth.PI / 6.0F + ls;
                    rot = Mth.sin(breath) / 60.0F
                        + (((FIVE_DEG * trot + s) / 1.8F * moveClamped)
                           + Mth.clamp(Mth.cos(ls) / 2.0F * toltKneeGain, 0.0F, toltKneeCeil)
                             * (1.0F + trot / 2.0F + 0.2F * tolt) / 2.0F * move) * notRearing * walk
                        + ((-Mth.sin(rl) / 1.6F + Mth.clamp(Mth.cos(rl) / 4.0F, 0.0F, NINTH_PI))
                           * speed * 1.2F * notRearing) * run
                        + (Mth.cos(st) / 2.0F
                           + Mth.clamp(Mth.sin(st) / 3.0F, 0.0F, NINTH_PI)) * notRearing * swim
                        + landFront;
                    reach = ((1.0F / 6.5F * trot + s) * 6.5F * toltReach * moveClamped * notRearing) * walk
                          + (-Mth.sin(rl) * 8.0F * speed * notRearing) * run
                          + (Mth.cos(st) * 6.0F * notRearing) * swim
                          + braceFront;
                    lift = Mth.clamp((-1.7F * trot - Mth.cos(ls) * 2.0F * toltLift * move * notRearing) * walk
                          + (-2.3F * speed * notRearing - Mth.cos(rl) * 2.0F * speed * notRearing)
                            * run * 1.2F
                          + (-2.0F - Mth.sin(st) * 1.5F * notRearing) * swim, toltLiftFloor, 0.0F);
                }
                case 2 -> {
                    float w = -Mth.sin(ls + Mth.sin(ls) / 4.5F) * trot
                            + Mth.cos(ls - Mth.cos(ls) / 2.5F) * (1.0F - trot);
                    rot = -Mth.sin(breath) / 60.0F
                        + (((w + FIVE_DEG * trot) / 1.8F * moveClamped)
                           + Mth.clamp(-Mth.cos(ls) / 4.0F * trot - Mth.sin(ls) / 4.0F * (1.0F - trot),
                                       -NINTH_PI, 0.0F)
                             * (1.0F + trot / 2.0F) / 2.0F * move) * walk
                        + ((Mth.sin(ls) / 1.6F + Mth.clamp(Mth.cos(ls) / 4.0F, -NINTH_PI, 0.0F))
                           * speed * 1.2F) * run
                        + (Mth.sin(st) / 2.0F
                           + Mth.clamp(Mth.cos(st) / 3.0F, -NINTH_PI, 0.0F)) * swim
                        + landBack;
                    reach = (((-Mth.sin(ls + Mth.sin(ls) / 4.5F) + 1.0F / 6.5F) * trot
                              + Mth.cos(ls - Mth.cos(ls) / 2.5F) * (1.0F - trot))
                             * 6.5F * moveClamped) * walk
                          + (Mth.sin(ls) * 8.0F * speed) * run
                          + (Mth.sin(st) * 6.0F) * swim
                          - toltEngage
                          + braceBack;
                    lift = Mth.clamp((-1.7F * trot
                            + (-Mth.cos(ls) * trot - Mth.sin(ls) * (1.0F - trot))
                              * 2.0F * move * notRearing) * walk
                          + (-2.3F * speed + Mth.cos(ls) * 2.0F * speed) * run * 1.2F
                          + (-2.0F + Mth.cos(st) * 1.5F) * swim, -4.0F, 0.0F);
                }
                default -> {
                    float w = Mth.sin(ls - Mth.sin(ls) / 4.5F) * trot
                            - Mth.cos(ls + Mth.cos(ls) / 2.5F) * (1.0F - trot);
                    float rl = Mth.PI / 6.0F + ls;
                    rot = -Mth.sin(breath) / 60.0F
                        + (((w + FIVE_DEG * trot) / 1.8F * moveClamped)
                           + Mth.clamp(Mth.cos(ls) / 4.0F * trot + Mth.sin(ls) / 4.0F * (1.0F - trot),
                                       -NINTH_PI, 0.0F)
                             * (1.0F + trot / 2.0F) / 2.0F * move) * walk
                        + ((-Mth.cos(rl) / 1.6F + Mth.clamp(Mth.sin(rl) / 4.0F, -NINTH_PI, 0.0F))
                           * speed * 1.2F) * run
                        + (-Mth.sin(st) / 2.0F
                           + Mth.clamp(-Mth.cos(st) / 3.0F, -NINTH_PI, 0.0F)) * swim
                        + landBack
                        + (-6.0F * stampRoll) * Mth.DEG_TO_RAD * stampBR;
                    reach = (((Mth.sin(ls - Mth.sin(ls) / 4.5F) + 1.0F / 6.5F) * trot
                              - Mth.cos(ls + Mth.cos(ls) / 2.5F) * (1.0F - trot))
                             * 6.5F * moveClamped) * walk
                          + (-Mth.cos(rl) * 8.0F * speed) * run
                          + (-Mth.sin(st) * 6.0F) * swim
                          - toltEngage
                          + (-stampRoll * 1.5F) * stampBR
                          + braceBack;
                    lift = Mth.clamp((-1.7F * trot
                            + (Mth.cos(ls) * trot + Mth.sin(ls) * (1.0F - trot))
                              * 2.0F * move * notRearing) * walk
                          + (-2.3F * speed + Mth.sin(rl) * 2.0F * speed) * run * 1.2F
                          + (-2.0F - Mth.cos(st) * 1.5F) * swim
                          + (stampSwing * 2.0F - 0.8F) * stampBR, -4.0F, 0.0F);
                }
            }

            final boolean front = i < 2;

            if (canter > 0.0F) {
                float p = (ls / Mth.TWO_PI
                        + (lead > 0.0F ? CANTER_RIGHT_LEAD : CANTER_LEFT_LEAD)[i]) % 1.0F;
                if (p < 0.0F) {
                    p += 1.0F;
                }
                final float sweep = canterSweep(p);
                final float swing = p < CANTER_DUTY
                        ? 0.0F
                        : Mth.sin(Mth.PI * (p - CANTER_DUTY) / (1.0F - CANTER_DUTY));
                final float on = canter * speed * (front ? notRearing : 1.0F);

                rot += sweep * (front ? CANTER_ROT_FRONT : CANTER_ROT_BACK) * on;
                reach += sweep * (front ? CANTER_REACH_FRONT : CANTER_REACH_BACK) * on;
                lift += Mth.clamp(
                        -swing * (front ? CANTER_LIFT_FRONT : CANTER_LIFT_BACK) * on,
                        -4.0F, 0.0F);
            }

            final float calm = 1.0F - Math.max(state.fallDamp, panic);
            rot *= calm;
            reach *= calm;
            lift *= calm;
            if (panic > 0.0F) {
                final float u = panicT + PANIC_LEG_PHASE[i];
                final float knee = Mth.clamp(-Mth.sin(u) / 3.0F, 0.0F, NINTH_PI);
                rot += (-Mth.cos(u) / 2.0F + (front ? knee : -knee)) * PANIC_STROKE * panic;
                reach += -Mth.cos(u) * 6.0F * PANIC_STROKE * panic;
                lift += Mth.clamp(-2.0F + Mth.sin(u) * 1.5F, -4.0F, 0.0F) * panic;
            }

            final float gait = gaitScale(front);
            if (gait != 1.0F) {
                rot *= gait;
                reach *= gait;
                lift *= gait;
            }

            final float hold = gaitShoulderHold(front);
            if (hold != 0.0F) {
                final float lv = legLever[i];
                reach += lv * Mth.sin(rot) * hold;
                lift += -lv * (1.0F - Mth.cos(rot)) * hold;
            }

            final float reachScale = gaitReachScale(front);
            if (reachScale != 1.0F) {
                reach *= reachScale;
            }

            float sore = 0.0F;
            if ((i == 0 && soreSign > 0.0F) || (i == 1 && soreSign < 0.0F)) {
                sore = limp;
            }
            rot *= 1.0F - 0.55F * sore;
            reach *= 1.0F - 0.60F * sore;
            lift *= 1.0F - 0.35F * sore;
            rot += (10.0F * Mth.DEG_TO_RAD) * sore;

            rot += front ? skidFront : skidBack;

            final float rearSide = (i == 0 || i == 2) ? 1.0F : -1.0F;
            float rearRot = 0.0F;
            float rearReach = 0.0F;
            float rearLift = 0.0F;
            float rearLateral = 0.0F;
            float rearRoll = 0.0F;
            float rearYaw = 0.0F;
            if (rear > 0.0F) {
                if (front) {
                    rearRot = (-0.4F - rearSide * Mth.sin(rearT) / 2.5F) * rear;
                    rearLift = (-15.0F - rearSide * Mth.cos(rearT) * 1.2F) * rear * frameScale;
                    rearReach = (-1.0F - rearSide * Mth.sin(rearT) * 2.0F) * rear * frameScale;
                    rearLateral = (rearSide * 0.3F + Mth.cos(rearT25) / 4.0F) * rear * frameScale;
                    rearRoll = rearSide * rear / 7.5F;
                } else {
                    rearRoll = (-2.0F * Mth.DEG_TO_RAD) * Mth.cos(rearT25) * rear;
                    if (i == 2) {
                        rearReach = 1.5F * rear * frameScale;
                        rearLateral = rear / 7.5F * frameScale;
                        rearYaw = -rear / 3.75F * idle;
                    } else {
                        rearRot = -0.4F * rear;
                        rearReach = -2.5F * rear * frameScale;
                    }
                }
            }

            float soreStand = 0.0F;
            if ((i == 0 && soreSign > 0.0F) || (i == 1 && soreSign < 0.0F)) {
                soreStand = limpStand;
            }
            final float soreFold = (14.0F * Mth.DEG_TO_RAD) * soreStand;

            final float grazeFold = front ? (-7.0F * Mth.DEG_TO_RAD) * graze : 0.0F;

            final float kickFold = front
                    ? (5.0F * kickCoil - 4.0F * kickSnap) * Mth.DEG_TO_RAD
                    : (-34.0F * kickCoil + 66.0F * kickSnap) * Mth.DEG_TO_RAD;

            final float stompFold = i == stompLeg
                    ? (-58.0F * stompLift + 22.0F * stompDrive) * Mth.DEG_TO_RAD
                    : 0.0F;

            final float lever = legLever[i];
            final float legSwing = soreFold + grazeFold + kickFold + stompFold;
            rot += legSwing;
            reach += lever * Mth.sin(legSwing);
            lift += -lever * (1.0F - Mth.cos(legSwing));

            final float legPivotPhase = pivotT + PIVOT_LEG_PHASE[i];
            final float swing = Mth.sin(legPivotPhase);
            final float lateral = swing * (front ? PIVOT_FRONT_DEG : PIVOT_BACK_DEG)
                    * Mth.DEG_TO_RAD * pivot * pivotDir;
            final float pivotLift = -Math.max(0.0F, Mth.sin(legPivotPhase))
                    * (front ? 2.2F : 1.4F) * pivot * frameScale;

            leg.xRot = rest.xRot() + rot + rearRot;
            leg.z = rest.z() + reach + rearReach;
            leg.y = rest.y() + lift + swimSink + pivotLift + rearLift;
            leg.yRot = rest.yRot() + rearYaw;
            leg.zRot = rest.zRot() + lateral + rearRoll;
            leg.x = rest.x() - legLever[i] * Mth.sin(lateral) + rearLateral;
        }

        float clipPitch = 0.0F;
        final float w = state.jumpWeight;
        if (w > 0.0F && BhJumpClips.ready()) {
            BhJumpClips.sample(state.jumpClip, state.jumpClipTime, clipPose);
            if (state.jumpFade < 1.0F) {
                BhJumpClips.sample(state.jumpFromClip, state.jumpFromTime, clipFrom);
                BhJumpClips.mix(clipPose, clipFrom, 1.0F - state.jumpFade);
            }
            jumpRig.solve(clipPose, state.jumpPitchScale);
            final float[] o = jumpRig.out;
            clipPitch = o[BhJumpClips.BODY * 6 + BhJumpRig.XROT] - bodyRest.xRot();
            blend(body, bodyRest, o, BhJumpClips.BODY, w);
            blend(neck, neckRest, o, BhJumpClips.NECK, w);
            blend(head, headRest, o, BhJumpClips.HEAD, w);
            blend(leftEar, leftEarRest, o, BhJumpClips.LEFT_EAR, w);
            blend(rightEar, rightEarRest, o, BhJumpClips.RIGHT_EAR, w);
            blend(tail, tailRest, o, BhJumpClips.TAIL, w);
            for (int i = 0; i < legs.length; i++) {
                blend(legs[i], legRest[i], o, BhJumpClips.FRONT_LEFT + i, state.jumpLegWeight);
            }
            neck.xRot += viewDuck * w;
            head.xRot -= viewDuck * w;
        }

        final float whip = state.jumpWhip;
        body.y += 3.4F * jHit * frameScale;
        body.xRot += (5.0F * Mth.DEG_TO_RAD) * jHit;
        neck.xRot += (17.0F * jHit - 12.0F * jHit2) * Mth.DEG_TO_RAD - 0.45F * whip;
        head.xRot += (-15.0F * Mth.DEG_TO_RAD) * jHit - 0.30F * whip;
        leftEar.xRot += (18.0F + 5.0F * lead) * Mth.DEG_TO_RAD * jHit;
        rightEar.xRot += (18.0F - 5.0F * lead) * Mth.DEG_TO_RAD * jHit;
        tail.xRot += (-20.0F * jHit + 15.0F * jHit2) * Mth.DEG_TO_RAD - 0.75F * whip;

        for (int i = 0; i < legs.length; i++) {
            final boolean front = i < 2;
            final float legShift =
                    -LEG_LAG_SECONDS[i] * 0.5F * (1.0F - lead * LEG_DIAGONAL[i]);
            final float hit = front
                    ? -12.0F * BhEquineGait.impactShifted(state, legShift)
                    : -15.0F * BhEquineGait.impactSecondShifted(state, legShift);
            final float flailT = phase + age * 1.4F + i * 1.7F;
            final float flail = (Mth.sin(flailT) * 0.65F + Mth.sin(flailT * 0.43F + i) * 0.35F)
                  * (front ? FLAIL_FRONT_DEG : FLAIL_BACK_DEG) * state.jumpFlail;
            swingLeg(legs[i], legLever[i], (hit + flail) * Mth.DEG_TO_RAD);
        }

        final float bodyPitchNow = body.xRot - bodyRest.xRot();
        publishRiderMotion(state, body.y - bodyRest.y(), bodyPitchNow,
                bodyPitchNow - clipPitch * w * (1.0F - RIDER_JUMP_FOLLOW), bankAngle);
        savePose(state);
    }

    private static final float SADDLE_BODY_Y = -5.0F;

    private static void blend(ModelPart part, Rest rest, float[] pose, int bone, float k) {
        final int at = bone * 6;
        part.x += (pose[at + BhJumpRig.X] - part.x) * k;
        part.y += (pose[at + BhJumpRig.Y] - part.y) * k;
        part.z += (pose[at + BhJumpRig.Z] - part.z) * k;
        part.xRot += (pose[at + BhJumpRig.XROT] - part.xRot) * k;
        part.yRot += (pose[at + BhJumpRig.YROT] - rest.yRot()) * k;
        part.zRot += (pose[at + BhJumpRig.ZROT] - rest.zRot()) * k;
    }

    private static void swingLeg(ModelPart leg, float lever, float a) {
        if (a == 0.0F) {
            return;
        }
        final float to = leg.xRot + a;
        leg.y += lever * (Mth.cos(to) - Mth.cos(leg.xRot));
        leg.z += lever * (Mth.sin(to) - Mth.sin(leg.xRot));
        leg.xRot = to;
    }

    private void publishRiderMotion(BhHorseRenderState state, float bodyY, float bodyPitch,
                                    float riderPitch, float bankAngle) {
        final float bodyRoll = body.zRot - bodyRest.zRot();

        float mx = -SADDLE_BODY_Y * Mth.sin(bodyRoll);
        float my = bodyY + SADDLE_BODY_Y * (Mth.cos(bodyPitch) - 1.0F)
                 + SADDLE_BODY_Y * (Mth.cos(bodyRoll) - 1.0F);
        float mz = (body.z - bodyRest.z()) + SADDLE_BODY_Y * Mth.sin(bodyPitch);

        final float saddleRootY = bodyRest.y() + SADDLE_BODY_Y;
        final float bankArm = saddleRootY - GROUND_Y;

        mx += -bankArm * Mth.sin(bankAngle);
        my += bankArm * (Mth.cos(bankAngle) - 1.0F);

        BhRiderMotion.publish(state.entityId, new BhRiderMotion(
                -mx / 16.0F,
                -my / 16.0F,
                -mz / 16.0F,
                riderPitch,
                bankAngle + bodyRoll));
    }
}
