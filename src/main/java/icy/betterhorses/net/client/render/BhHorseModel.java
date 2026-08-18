package icy.betterhorses.net.client.render;

import icy.betterhorses.net.mixin.HorseModelAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public abstract class BhHorseModel extends EntityModel<BhHorseRenderState>
        implements HorseModelAccessor {

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

    private record Rest(float x, float y, float z, float xRot, float yRot, float zRot) {
        static Rest of(ModelPart p) {
            return new Rest(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot);
        }
    }

    /** Distance from each leg's pivot up to its shoulder/hip, measured off the baked cubes. */
    private final float[] legLever;

    /** Body size relative to the medium frame, so pixel amounts scale with the breed. */
    private final float frameScale;

    /** Neck world angle and head-relative angle that land THIS breed's muzzle on the grass. */
    private final float grazeNeck;
    private final float grazeHeadRel;

    private static ModelPart childOrEmpty(ModelPart parent, String name) {
        return parent.hasChild(name)
                ? parent.getChild(name)
                : new ModelPart(java.util.List.of(), java.util.Map.of());
    }

    /**
     * How far the muzzle reaches forward of the head pivot, measured off the baked snout.
     *
     * <p>Skips the reins, which hang off the snout as children and would otherwise be measured as
     * part of the face.
     */
    private static float measureSnoutReach(ModelPart snout, float snoutZ, float fallback) {
        final float[] minZ = {Float.MAX_VALUE};
        snout.visit(new com.mojang.blaze3d.vertex.PoseStack(), (pose, path, index, cube) -> {
            if (path.contains("rein")) {
                return;
            }
            minZ[0] = Math.min(minZ[0], cube.minZ);
        });
        return minZ[0] == Float.MAX_VALUE ? fallback : -(snoutZ + minZ[0]);
    }

    /**
     * How far the top of a leg sits above its pivot.
     *
     * <p>The pivots sit at the hoof, not the shoulder, so a bare rotation swings the leg about
     * the wrong end and drags its top clean out of the barrel. Rotating about the top instead
     * means rotating about a point {@code lever} above the pivot, and that needs the real number
     * per breed: 10 on the Icelandic, 12 on the medium frame, 14 on the Friesian. Hardcoding one
     * of those into the shared animator is how a leg detaches on the other two.
     */
    private static float measureLegLever(ModelPart part, float fallback) {
        final float[] top = {Float.MAX_VALUE};
        part.visit(new com.mojang.blaze3d.vertex.PoseStack(),
                (pose, path, index, cube) -> top[0] = Math.min(top[0], cube.minY));
        return top[0] == Float.MAX_VALUE ? fallback : -top[0];
    }

    protected BhHorseModel(ModelPart root) {
        super(root);
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
        // 1.0 on the medium frame, ~0.83 on the Icelandic, ~1.17 on the Friesian. Compression
        // in pixels has to follow the animal, or the pony sinks half a leg on the same landing
        // the Friesian barely notices.
        this.frameScale = this.legLever[0] / 12.0F;

        // Graze angles are SOLVED per breed, not shared. The neck pivot sits at root y=8 on the
        // Icelandic against y=3 on the medium and y=1 on the Friesian - the pony's head starts
        // five pixels nearer the grass before anything animates. One shared angle therefore
        // buries the Icelandic's face and leaves the Friesian short: the three need 95, 114 and
        // 129 degrees respectively, a 33 degree spread that no single constant can cover.
        final float snoutReach = measureSnoutReach(this.snout, this.snoutRest.z(), 10.0F);
        final float grazeDrop = GRAZE_DROP_PIXELS * this.frameScale;
        final float neckBaseY = this.bodyRest.y() + this.neckRest.y()
                + grazeDrop + (-this.neckRest.z()) * Mth.sin(GRAZE_PITCH);

        // Rotating the head offset (0, hy, hz) about the neck by t moves it to
        // hy*cos(t) - hz*sin(t). Aim the muzzle straight down so the snout contributes its full
        // length, and that reduces to A*cos(t) + B*sin(t) = K, which solves in closed form.
        final float a = this.headRest.y();
        final float b = -this.headRest.z();
        final float r = (float) Math.sqrt(a * a + b * b);
        float neck = GRAZE_NECK_MAX;
        if (r > 1.0E-4F) {
            float k = Mth.clamp((GRAZE_MUZZLE_Y - neckBaseY - snoutReach) / r, -1.0F, 1.0F);
            neck = (float) (Math.atan2(b, a) - Math.acos(k));
        }
        this.grazeNeck = Mth.clamp(neck, GRAZE_NECK_MIN, GRAZE_NECK_MAX);
        // Head relative to the neck, so the muzzle finishes pointing at the ground.
        this.grazeHeadRel = Mth.PI / 2.0F - this.grazeNeck;
    }

    @Override
    public ModelPart bh_getBody() {
        return this.body;
    }

    // rest y, not live: subtracting the live value cancels the animation
    public float bhBodyRestY() {
        return bodyRest.y();
    }

    public static boolean DEBUG_REST_POSE = false;

    private static final float BANK_ROLL = 11.0F * Mth.DEG_TO_RAD;

    private static final float GROUND_Y = 24.0F;

    /**
     * Height in root space the jump arc pivots around - roughly the barrel centre, so the horse
     * rotates about itself rather than swinging off a point above its own back.
     */
    private static final float ARC_PIVOT_Y = 11.0F;

    /** How far the barrel sinks to graze, before the frame scale. */
    private static final float GRAZE_DROP_PIXELS = 3.5F;
    /**
     * Nose-down pitch to graze. Capped by the hips, not by looks: pitching raises the croup by
     * 10*sin(pitch) while the legs stay put, and only the drop above pays for that. At 12 the hip
     * still nets ~1.4px down on the medium frame.
     */
    private static final float GRAZE_PITCH = 12.0F * Mth.DEG_TO_RAD;
    /** Just above the ground plane, so the muzzle meets the grass instead of clipping through. */
    private static final float GRAZE_MUZZLE_Y = GROUND_Y - 0.5F;
    private static final float GRAZE_NECK_MIN = 70.0F * Mth.DEG_TO_RAD;
    private static final float GRAZE_NECK_MAX = 145.0F * Mth.DEG_TO_RAD;

    /**
     * How far <em>behind</em> the horse a leg runs the jump pulses when it is the trailing one of
     * its diagonal pair, in seconds. The leading pair runs on the horse's own clock.
     *
     * <p>Delay only, never advance. Advancing a leg starts its pulse partway up the curve, and
     * {@code smoothPulse}'s zero slope at 0 - the whole reason the takeoff does not pop - only
     * exists at the start of it. Measured in jump_sim, a 0.022s advance put the lead foreleg 46%
     * into the thrust on its first frame: a 26 degree single-frame jerk, worse than the staircase
     * this work set out to remove. The fronts split harder than the hinds because the lead foreleg
     * is the one you actually watch.
     */
    private static final float[] LEG_LAG_SECONDS = {0.020F, 0.020F, 0.013F, 0.013F};

    /**
     * Which diagonal each leg belongs to, matching {@link #legs}: front left pairs with back
     * right. A horse leading with the left fore leaves and lands on that diagonal first, so the
     * two legs of a pair share a clock and the other two share a later one.
     */
    private static final float[] LEG_DIAGONAL = {1.0F, -1.0F, -1.0F, 1.0F};

    /**
     * Per-leg scale on the held part of the flight tuck, before the lead sign. The clock offsets
     * above only move the thrust and impact <em>pulses</em>, and both of those are zero for the
     * whole airborne phase - so without this the two forelegs are pixel-identical for exactly the
     * stretch of the jump you spend the longest looking at. A few percent is enough; this is the
     * difference between two legs tucking, not between a good leg and a bad one.
     */
    private static final float[] LEG_TUCK_BIAS = {0.07F, -0.07F, -0.05F, 0.05F};

    /**
     * Step order for turning on the spot. Diagonals, quarter-cycle apart, so the horse always has
     * three feet down - putting the pairs half a cycle apart instead makes it hop.
     */
    private static final float[] PIVOT_LEG_PHASE =
            {0.0F, Mth.PI, Mth.PI * 0.5F, Mth.PI * 1.5F};

    /** How far the hoof swings sideways on a pivot step. Fronts cross over, hinds mostly hold. */
    private static final float PIVOT_FRONT_DEG = 11.0F;
    private static final float PIVOT_BACK_DEG = 5.0F;

    @Override
    public void setupAnim(BhHorseRenderState state) {
        super.setupAnim(state);
        if (DEBUG_REST_POSE) {
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
        final float idle = state.idleWeight;
        final float move = state.moveWeight;
        final float tolt = state.toltWeight;

        // rearWeight, not standAnimation: the jump owns the pose when the two overlap, and that
        // clamp lives in BhEquineGait so the land trigger sees the same number this does.
        final float rear = state.rearWeight;
        final float graze = state.eatAnimation;

        final float blown = state.exertion;
        final float breathDepth = 1.0F + 5.0F * blown;

        final float breath = state.breathPhase;
        final float swimT = phase + age / 20.0F * Mth.TWO_PI + stride / 3.0F;
        // FA's var.Rt, and the two slower readings of it the rear pose uses. Every wobble in the
        // rear runs off one of these three: var.id + var.Rt, var.id + var.Rt/2.5 (the lateral
        // sway) and var.id + var.Rt/1.5 (the tail). Note it is id + Rt/n, not (id + Rt)/n - the
        // per-entity phase is never divided, or a herd rearing together drifts into sync.
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

        final float jGather = state.jumpGather;
        final float jThrust = state.jumpThrust;
        final float jFlight = state.jumpFlight;
        final float jRise = state.jumpRise;
        // jumpFall is deliberately not read here - every descent pose hangs off jumpReach, which
        // is the smoothed version of it that survives touchdown. Using jumpFall directly is what
        // would make the landing pop.
        final float jReach = state.jumpReach;
        final float jHit = state.jumpImpact;
        final float jHit2 = state.jumpImpactSecond;
        final float jAny = state.jumpActive;
        // +1 or -1, fixed for this horse. Everything that should not be perfectly symmetric
        // through a jump - the legs, the ears - hangs off this one sign.
        final float lead = state.jumpLeadSign;

        // How much a leg is hanging in the air with nothing under it.
        //
        // NOT jFlight on its own: that hard-cuts to 0 on touchdown, which would rip several
        // degrees of wobble out of the pose in a single frame - the same trap jumpReach exists to
        // get around. jReach carries it across the handoff, and (1 - jHit) fades it out exactly as
        // the landing compression takes the weight, so the flail stops the moment the leg has
        // something to stand on.
        // (1 - jThrust) as well: during the push the leg is doing something very deliberate and
        // has the whole animal's weight on it. Without this the wobble eats into the extension
        // and the push-off peak drops from 52 degrees to 50 - the one moment that should be at
        // full amplitude, quietly shaved by the thing meant to add life everywhere else.
        final float airWobble = Math.max(jFlight, jReach)
              * Math.max(0.0F, 1.0F - Mth.clamp(jHit, 0.0F, 1.0F))
              * Math.max(0.0F, 1.0F - Mth.clamp(jThrust, 0.0F, 1.0F));

        // landWeight is the recovery from a *rear*, not from a jump. The two used to be the same
        // thing because vanilla reared on every jump; now that the rear is gone from jumps they
        // are separate events, and stacking them would double every landing term.
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
        final float restL = state.restLeftHind;
        final float restR = state.restRightHind;
        final float restAny = Math.max(restL, restR);
        final float feed = Mth.clamp(state.feedingAnimation, 0.0F, 1.0F);
        final float chewing = Math.max(graze, feed);
        final float flinch = state.hurt * state.hurt
                * Mth.clamp((1.0F - state.hurt) * 8.0F, 0.0F, 1.0F);

        final float notRearingEarly = 1.0F - rear;
        final float bank = state.bankWeight * (0.55F + 0.45F * run) * notRearingEarly;
        final float skid = state.skidWeight;
        // The moving limp used to be scaled by `move` alone, which is min(1, speed*6) - so at a
        // standstill it was exactly zero and the horse showed no sign of being hurt at all, and in
        // a slow gear it was scaled most of the way out. A floor keeps it readable at the crawl
        // first gear runs at, and limpStand carries the standing pose that `move` used to erase.
        final float limp = state.limpWeight * Math.max(0.45F, move) * notRearingEarly;
        final float limpStand = state.limpWeight * (1.0F - move) * notRearingEarly;

        final float pivot = state.pivotWeight;
        final float pivotDir = state.pivotDir;
        final float pivotT = state.pivotPhase * Mth.TWO_PI;
        final float back = state.backWeight;

        final float soreSign = state.random01 > 0.5F ? 1.0F : -1.0F;
        final float limpNod = Mth.cos(stride) * soreSign * limp;

        // +y is downward in model space, so a positive term lowers the horse
        float bodyDrop =
                Mth.sin(breath) / 6.0F * idle * breathDepth
              + ((-Mth.sin(stride * 2.0F) + 2.0F) / 3.0F * Math.max(0.3F, move) * (1.0F - trot)
                   * (1.0F - 0.88F * tolt)
                 + (0.1F + Mth.cos(Mth.PI / 4.0F + stride * 2.0F) / 2.0F) * trot) * walk
              // FA damps the run bob during a rear rather than deleting it - a horse that goes up
              // on its hind legs at a gallop is still breathing hard and still moving.
              + Mth.cos(-Mth.PI / 3.0F + stride) * speed * run
                * (1.0F - rear / (state.onGround ? 2.5F : 0.75F))
              + speed / 2.0F
              // The rear lifts the barrel 4.7px. This is the half that was missing when the rear
              // last lived on the body: the forelegs have to come up with it (see the leg block),
              // and without them the chest climbed off the tops of them. Both halves or neither.
              + (-4.7F - Mth.cos(rearT) / 6.0F) * rear * frameScale
              + 2.0F * land
              + 1.3F * settle
              + 0.35F * restAny
              + 0.5F * limpStand
              // Grazing has to lower the barrel, not just swing the neck. The neck pivot sits at
              // root y=3 and the neck is about 8px long, so rotation alone tops out with the head
              // pivot around y=11 against a ground plane at 24 - which is why the muzzle used to
              // stop a block short however far the neck was cranked. Paired with the nose-down
              // pitch below, this drops the shoulder ~5.5px and the hip only ~1.4px, so the
              // forehand dips and the croup stays put.
              + GRAZE_DROP_PIXELS * graze * frameScale
              + 1.0F * skid
              + 0.8F * Math.abs(limpNod)
              // The barrel only ever sinks on a jump. Legs hang off the root, not off the body,
              // so a body that rises leaves a hole at the shoulders; a body that sinks buries
              // the tops of the legs, which is exactly what leg compression looks like.
              + (3.3F * jGather + 3.4F * jHit) * frameScale;

        float bodyPitch =
                Mth.sin(-Mth.PI / 4.0F + breath) / 60.0F * idle
              - Mth.cos(stride * 2.0F) / 40.0F * Math.max(0.3F, move) * walk * (1.0F - 0.75F * tolt)
              + (Mth.sin(stride) / 20.0F - Mth.cos(stride) / 13.0F) * speed * 0.7F * run
                * (1.0F - rear / (state.onGround ? 1.2F : 3.0F))
              + Mth.sin(Mth.PI / 4.0F + swimT * 2.0F) / 20.0F * swim
              // The rear, in full, exactly as Fresh Animations authors it: 40 degrees nose-up on
              // the BARREL, about the barrel's own pivot. That is the whole pitch - there is no
              // root rotation any more.
              //
              // Pitching here rather than on the root is what makes a rear look like a rear. The
              // barrel turns about mid-body, so the chest rises ~11.8px while the croup SINKS
              // ~2.4px - hocks folding under the weight. Turning the root about the hind hooves
              // instead took the hind legs with it, and since they are rigid sticks pivoted at the
              // hoof they leaned 42 degrees off vertical: a horse balancing on angled stilts.
              + (-0.7F + Mth.sin(rearT) / 25.0F) * rear
              + (10.0F * Mth.cos(landT * 3.0F)) * Mth.DEG_TO_RAD * land
              - 0.22F * skid
              // Nose-down onto the forehand to reach the grass. Kept at 12 deg deliberately: the
              // croup rises 10*sin(pitch) while the legs stay put, and the 3.5px drop above has to
              // cover that or the barrel opens a gap at the hips. At 12 the hip still nets 1.4px
              // down. The neck's -bodyPitch counter-rotation cancels this out of the neck's world
              // angle automatically, so the graze term below is a true world angle.
              + GRAZE_PITCH * graze
              // Nose-DOWN on the gather. A horse loading for a jump sets down onto its forehand
              // and coils the hocks under it; nose-up plus a sink is a dog begging, which is what
              // this used to read as. Nose-down instead sinks the shoulder ~4.2px and the hip only
              // ~2.2px, so the weight visibly goes forward.
              //
              // The direction that needs watching is now the hip, not the shoulder: nose-down
              // raises the back of the barrel by 10*sin(pitch) while the legs stay put, and only
              // the drop above keeps that from opening a gap at the hips. jump_sim checks it.
              + (6.0F * Mth.DEG_TO_RAD) * jGather
              + (5.0F * Mth.DEG_TO_RAD) * jHit;

        // The arc. Positive is nose-down. Every term is weighted by something that decays
        // smoothly, and the one still live at touchdown is jReach - which is why the horse
        // levels out through the landing instead of snapping flat when the hoof lands.
        //
        // Authored in BhEquineGait.arcPitch, because the inertia lag has to chase the identical
        // curve and per-entity state cannot live here: one model instance serves every horse of
        // the breed.
        final float arcPitch = state.arcPitch;
        // How hard the body is rotating right now. Bones that should feel heavy subtract a share
        // of it and so arrive late; it returns to zero the moment the arc stops moving, so this
        // borrows nothing from the authored pose once the horse has settled.
        final float arcWhip = state.arcWhip;

        final float swimSink = (state.isRidden ? 4.0F : 8.0F) * swim;

        body.y = bodyRest.y() + bodyDrop + swimSink;
        // The barrel slides back over the hind legs as it comes up. Without it, pitching about
        // mid-body throws the chest forward instead of stacking it over the hocks.
        body.z = bodyRest.z() + (4.0F - Mth.sin(rearT) / 4.0F) * rear * frameScale;
        body.xRot = bodyRest.xRot() + bodyPitch;
        body.yRot = bodyRest.yRot() + (-1.0F * Mth.cos(stampT)) * Mth.DEG_TO_RAD * stampBR
              + Mth.sin(shakeT * 0.85F - Mth.PI / 3.0F) * 0.06F * wetShake
              + 0.09F * flinch;
        body.zRot = bodyRest.zRot()
              + (-1.5F * Mth.cos(Mth.PI / 4.0F + stampT)) * Mth.DEG_TO_RAD * (stampBR - stampFL)
              + Mth.sin(shakeT * 0.85F) * 0.11F * wetShake
              + (2.5F * Mth.DEG_TO_RAD) * (restR - restL)
              // leans into a pivot, the way anything turning on the spot shifts its weight
              + (5.0F * Mth.DEG_TO_RAD) * pivotDir * pivot
              + (4.0F * Mth.DEG_TO_RAD) * soreSign * limp
              // standing lame: weight rolls off the sore side onto the good one
              + (5.5F * Mth.DEG_TO_RAD) * soreSign * limpStand
              // balancing on two legs is never perfectly upright
              + (3.0F * Mth.DEG_TO_RAD) * Mth.cos(rearT25) * rear;

        // The arc rides on the root, not the body: root is the parent of the barrel *and* all
        // four legs, so the whole animal tips together and nothing can come apart no matter how
        // far it pitches. Each rotation carries the translation that keeps its own pivot point
        // still - GROUND_Y for the bank (a horse leans about the ground it stands on), the
        // barrel centre for the arc (an airborne horse rotates about its own mass).
        // Rearing is NOT on the root. It was, turning the whole animal about the hind hooves, and
        // that is what made it read as broken: the hind legs are children of the root and rigid
        // sticks pivoted at their own hooves, so they leaned 42 degrees off vertical along with
        // everything else. Fresh Animations pitches the barrel and lifts the forelegs instead,
        // leaving the hinds standing - which is what a rearing horse actually does. See bodyPitch.
        final float bankAngle = BANK_ROLL * bank;
        rootPart.zRot = rootRest.zRot() + bankAngle;
        rootPart.xRot = rootRest.xRot() + arcPitch;
        rootPart.x = rootRest.x() + GROUND_Y * Mth.sin(bankAngle);
        rootPart.y = rootRest.y() + GROUND_Y * (1.0F - Mth.cos(bankAngle))
              + ARC_PIVOT_Y * (1.0F - Mth.cos(arcPitch));
        rootPart.z = rootRest.z() - ARC_PIVOT_Y * Mth.sin(arcPitch);

        final float viewDuck = state.riddenHeadDrop * state.riddenWeight
              * (1.0F - graze) * (1.0F - rear);

        float neckPitch = -bodyPitch
              + viewDuck
              + (20.0F * speed * (1.0F - 0.4F * tolt) * Mth.DEG_TO_RAD)
              + Mth.cos(breath) / 80.0F
              + (14.0F * Mth.DEG_TO_RAD + Mth.sin(breath) * 0.055F)
                * blown * idle * (1.0F - graze)
              + Mth.sin(Mth.PI / 4.0F + stride * 2.0F) / 20.0F * Math.max(0.3F, move) * walk
              + Mth.cos(stride) / 6.0F * speed * (1.0F - land) * run
              + ((-20.0F * Mth.DEG_TO_RAD) - Mth.cos(rearT) / 25.0F) * rear
              // Solved per breed at construction, not a shared constant - see the constructor.
              // A world angle: the -bodyPitch at the top of neckPitch cancels the barrel's own
              // pitch out of it, so this is the angle the neck actually ends up at.
              + grazeNeck * graze
              + (20.0F * Mth.DEG_TO_RAD) * land
              + (-8.0F * Mth.DEG_TO_RAD) * tolt
              + (3.0F * Mth.sin(idleT * 1.5F + Mth.sin(-Mth.PI / 4.0F + idleT / 1.5F) * 2.0F)
                 * alive * (1.0F - graze)) * Mth.DEG_TO_RAD
              + (10.0F * Mth.DEG_TO_RAD) * shake
              // A horse reversing carries its head high and watches where it is going.
              - (16.0F * Mth.DEG_TO_RAD) * back
              + (22.0F * Mth.DEG_TO_RAD) * stay * (1.0F - graze)
              + (30.0F * Mth.DEG_TO_RAD) * feed
              - (14.0F * Mth.DEG_TO_RAD) * flinch
              - (16.0F * Mth.DEG_TO_RAD) * skid
              - (16.0F * Mth.DEG_TO_RAD) * limpNod
              // The head leads the jump: DOWN and forward to gather - a horse lowers its neck onto
              // the forehand to load, it does not lift its nose - then driven up and out over the
              // push, stretched down the far side, and absorbing on the landing before it
              // rebounds. Starting from +20 instead of -10 also means the thrust now whips the
              // neck through 44 degrees instead of 14, which is most of the explosiveness.
              + (20.0F * Mth.DEG_TO_RAD) * jGather
              + (-24.0F * Mth.DEG_TO_RAD) * jThrust
              + (-14.0F * Mth.DEG_TO_RAD) * jRise
              + (7.0F * Mth.DEG_TO_RAD) * jReach
              + (17.0F * Mth.DEG_TO_RAD) * jHit
              - (12.0F * Mth.DEG_TO_RAD) * jHit2
              // The barrel is pitching under it now, so give some of that back or the head
              // swings twice as far as it should.
              - 0.3F * arcPitch
              // ...and give back more of it the faster the barrel is moving. A neck is heavy: it
              // does not reach its pose on the frame the body decides to. Peaks near 6.5 degrees
              // on a charged takeoff, then hands every degree back as the arc settles.
              - 0.45F * arcWhip;

        float neckYaw = (7.0F * Mth.DEG_TO_RAD) * bank
              // looks where it is turning, and leads the body round
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
              // A rearing horse stops tracking the camera with its head - FA damps the pitch
              // follow to 30% for exactly as long as the rear lasts, and the gait with it.
              + Mth.clamp(state.xRot * Mth.DEG_TO_RAD, -0.7F, 0.7F) * (1.0F - graze)
                * (1.0F - 0.7F * rear)
              + ((-10.0F * Mth.DEG_TO_RAD)
                 - Mth.sin(Mth.PI / 6.0F + stride) / 7.0F * (1.0F - land))
                * speed * run * (1.0F - 0.7F * rear)
              // brings the muzzle to straight down, so the snout contributes its full length
              // instead of angling out over the grass. Paired with grazeNeck.
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
              // muzzle keeps roughly level while the neck swings around underneath it - the neck
              // now drops 20 on the gather, so the head has to come back up to keep the horse
              // looking at what it is about to jump rather than at its own feet
              + (-9.0F * Mth.DEG_TO_RAD) * jGather
              + (11.0F * Mth.DEG_TO_RAD) * jThrust
              + (8.0F * Mth.DEG_TO_RAD) * jRise
              - (4.0F * Mth.DEG_TO_RAD) * jReach
              - (15.0F * Mth.DEG_TO_RAD) * jHit
              // second-order lag: the head trails the neck, which is already trailing the body
              - 0.30F * arcWhip;

        float headYaw = Mth.clamp(state.yRot * Mth.DEG_TO_RAD, -0.6F, 0.6F) * 0.5F
              + (5.0F * Mth.DEG_TO_RAD)
                * Mth.cos(Mth.clamp(0.5F - Mth.sin(idleT) * 1.5F, 0.0F, 1.0F) * Mth.PI) * alive
              + (-Mth.sin(shakeT) / 3.0F + Mth.cos(shakeT) / 8.0F) * shake;

        head.yRot = headRest.yRot() + headYaw;
        head.zRot = headRest.zRot()
              - BANK_ROLL * 0.45F * bank
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

        // hiding rein2 hides rein3 too: render returns early on an invisible part
        leftRein.visible = state.isRidden;
        rightRein.visible = state.isRidden;

        final float earGait = (-Mth.sin(Mth.PI / 4.0F + breath) / 20.0F
              + Mth.sin(-Mth.PI / 3.0F + stride * 2.0F) / 4.0F * speed * walk * (1.0F - trot)
              + Mth.cos(Mth.PI / 4.0F + stride * 2.0F) / 9.0F * trot
              + Mth.sin(stride) / 3.0F * speed * run) * (1.0F - rear);
        final float earLand = (-40.0F * Mth.cos(landT * 8.0F)) * Mth.DEG_TO_RAD * land;
        final float earChew = (3.0F * Mth.sin(chewT)) * Mth.DEG_TO_RAD * graze;
        // ears lock forward on the approach and flatten on the landing
        final float earJump = (-14.0F * jGather - 22.0F * jThrust - 10.0F * jRise
              - 6.0F * jReach + 18.0F * jHit) * Mth.DEG_TO_RAD;
        final float earFixed = (-12.0F * Mth.DEG_TO_RAD) * skid + earJump;
        // A pair of ears is never a perfect mirror, and through the jump these were exactly one -
        // the same number fed to both sides. Small, on the same lead sign as the legs, so the
        // whole animal reads as favouring one side rather than the ears doing their own thing.
        final float earJumpSplit = (5.0F * Mth.DEG_TO_RAD) * lead * (jThrust + jHit);
        final float earYawSplit = (7.0F * Mth.DEG_TO_RAD) * lead * (jThrust + 0.6F * jRise);

        leftEar.xRot = leftEarRest.xRot()
              + (10.0F - 40.0F * Mth.cos(age * 1.5F) * flickL * (1.0F - shake) * walk)
                * Mth.DEG_TO_RAD
              + earGait
              - Mth.sin(rearT) / 6.0F * rear
              + earChew + earLand + earFixed + earJumpSplit
              + (14.0F * Mth.DEG_TO_RAD) * stay
              - (10.0F * Mth.DEG_TO_RAD) * feed;
        rightEar.xRot = rightEarRest.xRot()
              + (10.0F + 25.0F * Mth.sin(age * 1.5F) * flickR * (1.0F - shake) * walk)
                * Mth.DEG_TO_RAD
              + earGait
              - Mth.sin(Mth.PI / 6.0F + rearT) / 6.0F * rear
              + earChew + earLand + earFixed - earJumpSplit
              + (14.0F * Mth.DEG_TO_RAD) * stay
              - (10.0F * Mth.DEG_TO_RAD) * feed;

        leftEar.yRot = leftEarRest.yRot() + earYawSplit
              + (70.0F * Mth.cos(Mth.PI / 6.0F + age * 1.5F) * flickL * (1.0F - shake) * walk
                 - 50.0F * graze - 25.0F * shake) * Mth.DEG_TO_RAD;
        rightEar.yRot = rightEarRest.yRot() + earYawSplit
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

        // two unrelated periods so the mane never looks like it is on a metronome mid-flight
        final float maneFly = Mth.sin(phase + age * 1.7F) * 0.6F
              + Mth.sin(phase + age * 0.71F) * 0.4F;

        // The hair used to hang off jFlight alone, so it woke up only once the horse was already
        // airborne and died the instant the hoof landed - the push and the impact, the two moments
        // with the most force in them, had nothing at all. hairDrive carries the drift into both;
        // maneWhip is a separate fast transient, roughly 10 Hz, that only exists during them. A
        // whip is a transient, not a faster version of a drift, which is why it is its own term.
        final float hairDrive = Math.min(1.5F, jFlight + 0.8F * jThrust + 0.6F * jHit);
        final float maneWhip = Mth.sin(phase + age * 3.1F)
              * Math.min(1.4F, jThrust + 0.7F * jHit);

        // The mane's only gait term hung off `walk`, and walk is (1 - swim - run): at a gallop it
        // is exactly zero. The one gait where a mane actually flies was the one gait where it was
        // dead still, and the jump was the only thing that ever moved it.
        //
        // maneGait is the stride-locked swing, now carried by trot and run as well. maneWind is
        // the free flutter, driven by ground speed rather than by any single gait weight, so it
        // cannot drop out in the hand-off between walk, trot and run - which is the same failure
        // in a different place. Squared because a mane barely stirs at a walk and streams at a
        // gallop, and that is nowhere near linear.
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
              // tail flags up over the whole jump and whips down as the horse loads on landing
              + (12.0F * jGather + 34.0F * jThrust + 26.0F * jRise + 12.0F * jReach
                 - 20.0F * jHit + 15.0F * jHit2) * Mth.DEG_TO_RAD
              // The heaviest lag of the three. A tail is hair hanging off the end of the longest
              // lever on the animal - it has no reason to keep up with anything, and about
              // 11 degrees of trailing here is what stops the jump reading as one rigid piece.
              - 0.75F * arcWhip;
        // same walk-only bug as the mane: the tail stopped swinging at exactly the speed it
        // should be streaming
        tail.yRot = tailRest.yRot() + Mth.sin(stride - Mth.PI / 3.0F) / 8.0F * move * walk
              + Mth.sin(stride - Mth.PI / 3.0F) / 6.0F * speed * run
              + (5.0F * Mth.DEG_TO_RAD) * Mth.sin(-Mth.PI / 4.0F + rearT15) * rear
              + maneFly * (0.09F * hairDrive + 0.11F * maneWind) + maneWhip * 0.06F;
        tail.zRot = tailRest.zRot() + (40.0F * Mth.sin(age / 3.0F)) * Mth.DEG_TO_RAD * swish;
        tail.yScale = 1.0F - (0.4F - 1.3F * Mth.sin(Mth.PI / 4.0F + age / 3.0F)) / 20.0F * swish;

        final float ls = stride;
        final float st = swimT;
        final float moveClamped = Math.max(0.3F, move);
        final float notRearing = 1.0F - rear;
        final float trotInner = 2.5F + 2.0F * trot;
        final float FIVE_DEG = 5.0F * Mth.DEG_TO_RAD;
        // fronts clamp to (0, pi/6) and hinds to (-pi/9, 0): they fold opposite ways
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
                        + landBack
                        + (13.0F * Mth.DEG_TO_RAD) * restL;
                    reach = (((-Mth.sin(ls + Mth.sin(ls) / 4.5F) + 1.0F / 6.5F) * trot
                              + Mth.cos(ls - Mth.cos(ls) / 2.5F) * (1.0F - trot))
                             * 6.5F * moveClamped) * walk
                          + (Mth.sin(ls) * 8.0F * speed) * run
                          + (Mth.sin(st) * 6.0F) * swim
                          - toltEngage
                          + braceBack
                          - 1.1F * restL;
                    lift = Mth.clamp((-1.7F * trot
                            + (-Mth.cos(ls) * trot - Mth.sin(ls) * (1.0F - trot))
                              * 2.0F * move * notRearing) * walk
                          + (-2.3F * speed + Mth.cos(ls) * 2.0F * speed) * run * 1.2F
                          + (-2.0F + Mth.cos(st) * 1.5F) * swim, -4.0F, 0.0F)
                          - 1.3F * restL;
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
                        + (-6.0F * stampRoll) * Mth.DEG_TO_RAD * stampBR
                        + (13.0F * Mth.DEG_TO_RAD) * restR;
                    reach = (((Mth.sin(ls - Mth.sin(ls) / 4.5F) + 1.0F / 6.5F) * trot
                              - Mth.cos(ls + Mth.cos(ls) / 2.5F) * (1.0F - trot))
                             * 6.5F * moveClamped) * walk
                          + (-Mth.cos(rl) * 8.0F * speed) * run
                          + (-Mth.sin(st) * 6.0F) * swim
                          - toltEngage
                          + (-stampRoll * 1.5F) * stampBR
                          + braceBack
                          - 1.1F * restR;
                    lift = Mth.clamp((-1.7F * trot
                            + (Mth.cos(ls) * trot + Mth.sin(ls) * (1.0F - trot))
                              * 2.0F * move * notRearing) * walk
                          + (-2.3F * speed + Mth.sin(rl) * 2.0F * speed) * run * 1.2F
                          + (-2.0F - Mth.cos(st) * 1.5F) * swim
                          + (stampSwing * 2.0F - 0.8F) * stampBR, -4.0F, 0.0F)
                          - 1.3F * restR;
                }
            }

            final boolean front = i < 2;

            // NOTE: backing up is NOT handled here. Inverting reach on its own breaks the
            // reach/rot pairing that keeps the leg swinging about its shoulder, and the leg
            // visibly comes off. The reversal lives in BhEquineGait, on the stride phase.

            // Nothing gallops in mid-air. The gait keeps running because walkAnimationSpeed is
            // still high on a running jump, so damp it out here rather than fighting it above.
            //
            // Not all the way out, though. At 0.90 this stripped the breath along with the gait
            // and left a pose with literally nothing moving in it, which is half of why flight
            // read as frozen - the other half being the jumpRise staircase. A fifth of the gait
            // is not enough to look like galloping in mid-air but is enough to look alive.
            final float airborneDamp = 1.0F - 0.80F * jFlight;
            rot *= airborneDamp;
            reach *= airborneDamp;
            lift *= airborneDamp;

            float sore = 0.0F;
            if ((i == 0 && soreSign > 0.0F) || (i == 1 && soreSign < 0.0F)) {
                sore = limp;
            }
            rot *= 1.0F - 0.55F * sore;
            reach *= 1.0F - 0.60F * sore;
            lift *= 1.0F - 0.35F * sore;
            rot += (10.0F * Mth.DEG_TO_RAD) * sore;

            rot += front ? skidFront : skidBack;

            // The jump leg pose, as a single angle per leg. Negative swings the hoof forward.
            // Added last so the limp scaling above never touches it.
            //
            // Thrust and rise do not stack: the rise term is gated by (1 - jThrust) so the push
            // owns the first fifth of a second and the flight pose takes over from there. Every
            // other pair overlaps only where one of them is already fading, which is what keeps
            // takeoff, flight and landing reading as one move.
            // The trailing diagonal runs the two *event* pulses a couple of hundredths of a second
            // behind the leading one. Same curve, same amplitude, later instant - so the forelegs
            // stop leaving and hitting the ground in perfect unison, which is the single most
            // mechanical thing the old jump did. The arc weights (gather, rise, reach) stay
            // shared: those describe where the whole animal is, not what one leg is doing.
            // 0 for the leading diagonal, a full lag for the trailing one.
            final float legShift =
                    -LEG_LAG_SECONDS[i] * 0.5F * (1.0F - lead * LEG_DIAGONAL[i]);
            final float jThrustLeg = BhEquineGait.thrustShifted(state, legShift);
            final float jHitLeg = BhEquineGait.impactShifted(state, legShift);
            final float jHit2Leg = BhEquineGait.impactSecondShifted(state, legShift);

            // A leg dangling in the air is not still. Two unrelated periods (Part 9) at a
            // frequency you can actually see inside a 0.3-0.8s flight - about 4.5 Hz, where the
            // idle motions elsewhere in this file run near 0.35 Hz and would read as a constant
            // offset over a window this short. Offset per leg so no two agree, and by phase so a
            // herd does not flail in unison.
            //
            // Hinds get more than fronts: a jumping horse tucks its forelegs deliberately and
            // lets the hind end trail behind it.
            final float flailT = phase + age * 1.4F + i * 1.7F;
            final float flail = (Mth.sin(flailT) * 0.65F + Mth.sin(flailT * 0.43F + i) * 0.35F)
                  * (front ? 3.5F : 5.0F) * Mth.DEG_TO_RAD * airWobble;

            // ...and the held part of the tuck differs slightly too, so they are not identical
            // even on a frame where the wobble happens to line up.
            final float tuck = 1.0F + LEG_TUCK_BIAS[i] * lead;

            final float jumpAngle = front
                    // Forelegs stay planted almost straight under the shoulder on the gather -
                    // they are the prop the weight is coming forward onto, so they should not be
                    // reaching anywhere. The sinking barrel is what reads as them taking load.
                    ? (-2.0F * jGather
                     - 52.0F * jThrustLeg
                     - (44.0F * jRise * (1.0F - jThrustLeg) + 26.0F * jReach) * tuck
                     - 12.0F * jHitLeg) * Mth.DEG_TO_RAD + flail
                    // hocks step further under the barrel to coil - this is the spring, and it is
                    // correct that the hind end folds. What was wrong was the barrel leaning away
                    // from the forelegs at the same time.
                    : (-23.0F * jGather
                     + 46.0F * jThrustLeg
                     + (34.0F * jRise * (1.0F - jThrustLeg) + 6.0F * jReach) * tuck
                     - 15.0F * jHit2Leg) * Mth.DEG_TO_RAD + flail;

            // ---- rearing ----------------------------------------------------------------
            //
            // Ported channel for channel from Fresh Animations, and deliberately NOT folded into
            // legSwing below. Every other large rotation here derives its translation from the
            // shoulder lever; FA authors the rear as a rotation AND its own travel - the forelegs
            // rise 15px and reach 1px forward as they fold - so running it through the lever
            // compensation as well would apply that travel twice.
            //
            // The 15px lift is the piece that was missing when the rear last lived on the body.
            // The barrel comes up 4.7px and pitches 40 degrees, which raises the chest ~11.8px;
            // without the forelegs rising with it the chest simply left them behind, and the fix
            // at the time was to move the rotation to the root instead of restoring this.
            //
            // The hind legs get no lift at all, and that is correct: the croup SINKS 2.4px under
            // the pitch, so the hocks stay planted and take the weight. They only stagger - one
            // steps under, one steps back - and the right one tucks.
            final float rearSide = (i == 0 || i == 2) ? 1.0F : -1.0F;   // +1 left, -1 right
            float rearRot = 0.0F;
            float rearReach = 0.0F;
            float rearLift = 0.0F;
            float rearLateral = 0.0F;
            float rearRoll = 0.0F;
            float rearYaw = 0.0F;
            if (rear > 0.0F) {
                if (front) {
                    // -23 degrees of fold with a +-23 degree paw, the two forelegs exactly out of
                    // phase with each other - one strikes as the other draws back.
                    rearRot = (-0.4F - rearSide * Mth.sin(rearT) / 2.5F) * rear;
                    rearLift = (-15.0F - rearSide * Mth.cos(rearT) * 1.2F) * rear * frameScale;
                    rearReach = (-1.0F - rearSide * Mth.sin(rearT) * 2.0F) * rear * frameScale;
                    // splayed out, both swaying the same way - the pair balances together
                    rearLateral = (rearSide * 0.3F + Mth.cos(rearT25) / 4.0F) * rear * frameScale;
                    rearRoll = rearSide * rear / 7.5F;
                } else {
                    rearRoll = (-2.0F * Mth.DEG_TO_RAD) * Mth.cos(rearT25) * rear;
                    if (i == 2) {
                        // left hind steps under and turns out to brace; the yaw is idle-only, so a
                        // horse rearing out of a canter keeps its hind end square
                        rearReach = 1.5F * rear * frameScale;
                        rearLateral = rear / 7.5F * frameScale;
                        rearYaw = -rear / 3.75F * idle;
                    } else {
                        rearRot = -0.4F * rear;
                        rearReach = -2.5F * rear * frameScale;
                    }
                }
            }

            // A lame horse standing still rests the sore foreleg out in front with the toe barely
            // down. Gated on (1 - move) so it is the standing pose only; the nodding limp above
            // covers it once it walks.
            float soreStand = 0.0F;
            if ((i == 0 && soreSign > 0.0F) || (i == 1 && soreSign < 0.0F)) {
                soreStand = limpStand;
            }
            final float soreFold = (14.0F * Mth.DEG_TO_RAD) * soreStand;

            // Grazing drops the shoulder ~5.5px. Without the forelegs bracing forward under it
            // that reads as the barrel sinking onto the legs rather than the horse reaching down.
            // A rotation, so it goes through the lever compensation and the hoof is what travels.
            final float grazeFold = front ? (-7.0F * Mth.DEG_TO_RAD) * graze : 0.0F;

            // All three channels, or the leg comes off - the rule that has bitten twice already.
            // The pivot is at the hoof, so rotating by jumpAngle drags the shoulder
            // lever*sin(a) backwards and lever*(1-cos(a)) down. These two lines put it back,
            // which turns the rotation into a clean swing about the shoulder instead. At the
            // 52 degrees the push-off reaches, dropping the lift term alone would open a
            // five-pixel hole in the chest.
            // One compensation for every large leg rotation that does NOT bring its own travel -
            // the jump, the standing limp and the graze. The rear is the exception and is applied
            // after this, for the reason given above.
            final float lever = legLever[i];
            final float legSwing = jumpAngle + soreFold + grazeFold;
            rot += legSwing;
            reach += lever * Mth.sin(legSwing);
            lift += -lever * (1.0F - Mth.cos(legSwing));

            // Turning on the spot. The hoof has to travel sideways while the shoulder stays put,
            // so this needs the same treatment as everything else here: rotate about the hoof
            // pivot, then translate the leg back so its TOP is what stays still.
            //
            // Note the sign is the OPPOSITE way round to the fore/aft case. A Z rotation carries
            // the leg's top in +x, an X rotation carries it in -z, so one compensation subtracts
            // where the other adds. Getting this backwards doubles the drift instead of undoing
            // it, and it looks like the leg is skating.
            final float legPivotPhase = pivotT + PIVOT_LEG_PHASE[i];
            final float swing = Mth.sin(legPivotPhase);
            final float lateral = swing * (front ? PIVOT_FRONT_DEG : PIVOT_BACK_DEG)
                    * Mth.DEG_TO_RAD * pivot * pivotDir;
            // lifts only on its own half of the cycle - a leg bearing weight does not float
            final float pivotLift = -Math.max(0.0F, Mth.sin(legPivotPhase))
                    * (front ? 2.2F : 1.4F) * pivot * frameScale;

            // The rear's roll and lateral are FA's own pair and are deliberately not run through
            // the sin(lateral) compensation next to them: FA authors the sideways travel directly
            // rather than deriving it, and the forelegs are off the ground anyway.
            leg.xRot = rest.xRot() + rot + rearRot;
            leg.z = rest.z() + reach + rearReach;
            leg.y = rest.y() + lift + swimSink + pivotLift + rearLift;
            leg.yRot = rest.yRot() + rearYaw;
            leg.zRot = rest.zRot() + lateral + rearRoll;
            leg.x = rest.x() - legLever[i] * Mth.sin(lateral) + rearLateral;
        }

        // The rear is inside bodyDrop and bodyPitch now, so the rider tips back with the barrel
        // without this needing a term of its own.
        publishRiderMotion(state, bodyDrop + swimSink, bodyPitch, arcPitch, bankAngle);
    }

    /** Height of the saddle above the body's pivot, model px. Negative is up. */
    private static final float SADDLE_BODY_Y = -5.0F;

    /**
     * Works out where the animation has carried the saddle, so the rider can be drawn following
     * it. See {@link BhRiderMotion} for why this is render-only and does not touch the camera.
     *
     * <p>Both root rotations are taken about the same pivots the root block above compensates
     * for - the ground for the bank, the barrel centre for the arc - because those translations
     * exist precisely to make the rotation happen about that point. Re-deriving them here rather
     * than reading {@code rootPart.y} keeps the two from disagreeing if one is retuned.
     */
    private void publishRiderMotion(BhHorseRenderState state, float bodyY, float bodyPitch,
                                    float arcPitch, float bankAngle) {
        final float bodyRoll = body.zRot - bodyRest.zRot();

        // Saddle in body space, carried by the body's own translation and rotation.
        float mx = -SADDLE_BODY_Y * Mth.sin(bodyRoll);
        float my = bodyY + SADDLE_BODY_Y * (Mth.cos(bodyPitch) - 1.0F)
                 + SADDLE_BODY_Y * (Mth.cos(bodyRoll) - 1.0F);
        float mz = (body.z - bodyRest.z()) + SADDLE_BODY_Y * Mth.sin(bodyPitch);

        // Then the root rotations, about their own pivots. The saddle sits bodyRest.y +
        // SADDLE_BODY_Y up the root, so its height relative to each pivot is that minus the pivot.
        final float saddleRootY = bodyRest.y() + SADDLE_BODY_Y;
        final float arcArm = saddleRootY - ARC_PIVOT_Y;
        final float bankArm = saddleRootY - GROUND_Y;

        my += arcArm * (Mth.cos(arcPitch) - 1.0F);
        mz += arcArm * Mth.sin(arcPitch);
        mx += -bankArm * Mth.sin(bankAngle);
        my += bankArm * (Mth.cos(bankAngle) - 1.0F);

        // Model space is +x left, +y down, -z forward, 16 px to the block.
        BhRiderMotion.publish(state.entityId, new BhRiderMotion(
                -mx / 16.0F,
                -my / 16.0F,
                -mz / 16.0F,
                arcPitch + bodyPitch,
                bankAngle + bodyRoll));
    }
}
