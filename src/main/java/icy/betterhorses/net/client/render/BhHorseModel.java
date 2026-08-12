package icy.betterhorses.net.client.render;

import icy.betterhorses.net.mixin.HorseModelAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The shared breed-horse model and animator.
 *
 * <p>The motion follows Fresh Animations' approach rather than vanilla's, because vanilla's
 * horse rig is too coarse to say what we want: it lumps head, ears, mouth, neck and mane
 * into a single container it swivels as one piece, so an arching neck is not expressible.
 * This rig splits the neck into two segments and the head into head plus muzzle.
 *
 * <p>The organising idea is that gaits <em>cross-fade</em> rather than switch. Every bone
 * below is a weighted sum: a rest pose, plus one term per gait, each multiplied by that
 * gait's weight. There is no transition code anywhere because there is no transition -
 * only weights sliding, which {@link BhEquineGait} handles.
 *
 * <p>Poses are applied as <em>offsets from the rest pose captured at construction</em>, not
 * as absolute positions. Fresh Animations has to assign absolute values because that is how
 * CEM works, which is why retargeting it to a new model means re-measuring every pivot.
 * Working in offsets is what lets <em>one</em> animator drive every breed: a Friesian whose
 * neck joins 7px higher and whose barrel is 1.25x the Icelandic's simply works, with no
 * constant re-tuned. Keep it that way - never write an absolute position into the maths
 * below, and never fork this class per breed. The breed subclasses exist to name a type,
 * not to change behaviour.
 */
public abstract class BhHorseModel extends EntityModel<BhHorseRenderState>
        implements HorseModelAccessor {

    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart snout;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart mane;
    private final ModelPart tail;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
    /** Only present on the saddle model; empty no-ops on the body and armour. */
    private final ModelPart leftRein;
    private final ModelPart rightRein;

    /** Rest pose, captured once so every frame can be expressed as an offset from it. */
    private final Rest bodyRest;
    private final Rest neckRest;
    private final Rest headRest;
    private final Rest tailRest;
    private final Rest[] legRest;
    private final ModelPart[] legs;

    private record Rest(float x, float y, float z, float xRot, float yRot, float zRot) {
        static Rest of(ModelPart p) {
            return new Rest(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot);
        }
    }

    /**
     * The tack models share this class so they share its animator exactly - that is what
     * keeps a saddle glued to the barrel through a gallop instead of drifting. But they do
     * not all carry every bone: the saddle has no legs, the armour has no hind legs. Rather
     * than fork the animator (and risk the two copies diverging, which is the whole failure
     * mode we are avoiding), missing bones resolve to a detached no-op part that is animated
     * harmlessly and never rendered.
     */
    private static ModelPart childOrEmpty(ModelPart parent, String name) {
        return parent.hasChild(name)
                ? parent.getChild(name)
                : new ModelPart(java.util.List.of(), java.util.Map.of());
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
        this.tail = childOrEmpty(this.body, "tail2");
        this.frontLeftLeg = childOrEmpty(root, "front_left_leg");
        this.frontRightLeg = childOrEmpty(root, "front_right_leg");
        this.backLeftLeg = childOrEmpty(root, "back_left_leg");
        this.backRightLeg = childOrEmpty(root, "back_right_leg");
        this.leftRein = childOrEmpty(this.snout, "left_rein2");
        this.rightRein = childOrEmpty(this.snout, "right_rein2");

        this.bodyRest = Rest.of(this.body);
        this.neckRest = Rest.of(this.neck);
        this.headRest = Rest.of(this.head);
        this.tailRest = Rest.of(this.tail);
        this.legs = new ModelPart[] {frontLeftLeg, frontRightLeg, backLeftLeg, backRightLeg};
        this.legRest = new Rest[legs.length];
        for (int i = 0; i < legs.length; i++) {
            this.legRest[i] = Rest.of(legs[i]);
        }
    }

    @Override
    public ModelPart bh_getBody() {
        return this.body;
    }

    /**
     * The body bone's y in its <em>rest</em> pose.
     *
     * <p>Anything anchored to the barrel needs this rather than the live {@code body.y}.
     * Subtracting the live value cancels the animation, which is why the stabilizer stayed
     * put while the horse dipped; subtracting the rest value gives the same placement at
     * rest and lets the animation carry through.
     */
    public float bhBodyRestY() {
        return bodyRest.y();
    }

    /**
     * Flip to {@code true} to freeze the model in its rest pose, bypassing all animation.
     * If the horse looks correct with this on, any problem is in the maths below; if it
     * still looks wrong, the problem is in the generated geometry. Faster than guessing.
     *
     * <p>Bound to <b>K</b> in game ({@code IcysBetterHorsesClient.REST_POSE_KEY}), so this
     * does not need a rebuild to try. It applies to every breed at once.
     */
    public static boolean DEBUG_REST_POSE = false;

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

        // vanilla hands us these directly; Fresh Animations has to infer them from an
        // invisible neck bone because a resource pack cannot see entity state
        final float rear = state.standAnimation;
        final float graze = state.eatAnimation;

        // independent timers, each offset per entity so a herd never moves in lockstep
        final float breath = phase + age / 70.0F * Mth.TWO_PI;
        final float swimT = phase + age / 20.0F * Mth.TWO_PI + stride / 3.0F;
        // rear/paw timer. The two front legs read it with opposite sign so they paw
        // alternately rather than in lockstep.
        final float rearT = phase + age / 2.0F + state.walkAnimationPos / 20.0F;

        // ---- body -----------------------------------------------------------------
        // +y is downward in model space, so a positive term lowers the horse
        float bodyDrop =
                Mth.sin(breath) / 6.0F * idle
              + ((-Mth.sin(stride * 2.0F) + 2.0F) / 3.0F * Math.max(0.3F, move) * (1.0F - trot)
                 + (0.1F + Mth.cos(Mth.PI / 4.0F + stride * 2.0F) / 2.0F) * trot) * walk
              + Mth.cos(-Mth.PI / 3.0F + stride) * speed * run
              + speed / 2.0F
              - 4.7F * rear;

        float bodyPitch =
                Mth.sin(-Mth.PI / 4.0F + breath) / 60.0F * idle
              - Mth.cos(stride * 2.0F) / 40.0F * Math.max(0.3F, move) * walk
              + (Mth.sin(stride) / 20.0F - Mth.cos(stride) / 13.0F) * speed * 0.7F * run
              + Mth.sin(Mth.PI / 4.0F + swimT * 2.0F) / 20.0F * swim
              - 0.7F * rear;

        body.y = bodyRest.y() + bodyDrop;
        body.z = bodyRest.z() + 4.0F * rear;
        body.xRot = bodyRest.xRot() + bodyPitch;

        // ---- neck and head --------------------------------------------------------
        // the neck subtracts the body's pitch so the head stays level while the barrel
        // rocks; without this the head bobs twice as hard as it should
        float neckPitch = -bodyPitch
              + (20.0F * speed * Mth.DEG_TO_RAD)
              + Mth.cos(breath) / 80.0F
              + Mth.sin(Mth.PI / 4.0F + stride * 2.0F) / 20.0F * Math.max(0.3F, move) * walk
              + Mth.cos(stride) / 6.0F * speed * run
              - (20.0F * Mth.DEG_TO_RAD) * rear
              + (75.0F * Mth.DEG_TO_RAD) * graze;

        neck.xRot = neckRest.xRot() + neckPitch;
        neck.yRot = neckRest.yRot() + Mth.clamp(state.yRot * Mth.DEG_TO_RAD, -0.6F, 0.6F) * 0.35F;

        head.xRot = headRest.xRot()
              + Mth.clamp(state.xRot * Mth.DEG_TO_RAD, -0.7F, 0.7F) * (1.0F - graze)
              + ((-10.0F * Mth.DEG_TO_RAD) - Mth.sin(Mth.PI / 6.0F + stride) / 7.0F) * speed * run
              - (20.0F * Mth.DEG_TO_RAD) * graze;
        head.yRot = headRest.yRot() + Mth.clamp(state.yRot * Mth.DEG_TO_RAD, -0.6F, 0.6F) * 0.5F;

        // chewing, only while actually grazing
        float chew = Mth.sin(age / 2.0F) * 0.06F * graze;
        snout.xRot = chew;

        // Reins only exist while someone is holding them, matching vanilla. Hiding rein2
        // hides rein3 too, because ModelPart.render returns early on an invisible part and
        // never walks its children. Harmless on the body and armour models, where these
        // resolve to detached no-op parts.
        leftRein.visible = state.isRidden;
        rightRein.visible = state.isRidden;

        // ears flick on an offset timer, and pin back when running hard
        float earBase = (10.0F * Mth.DEG_TO_RAD) - Mth.sin(Mth.PI / 4.0F + breath) / 20.0F;
        float earFlickL = Mth.sin(phase + age / 9.0F) * 0.35F;
        float earFlickR = Mth.sin(phase * 1.7F + age / 11.0F) * 0.35F;
        leftEar.xRot = earBase + Math.max(0.0F, earFlickL) * (1.0F - run);
        rightEar.xRot = earBase + Math.max(0.0F, earFlickR) * (1.0F - run);
        leftEar.zRot = -0.15F * run;
        rightEar.zRot = 0.15F * run;

        // mane trails the neck by a quarter cycle rather than being simulated
        mane.yRot = Mth.sin(stride * 2.0F - Mth.PI / 4.0F) / 12.0F * move * walk;

        // ---- tail -----------------------------------------------------------------
        tail.xRot = tailRest.xRot()
              + Mth.cos(breath) / 30.0F
              + (10.0F + 40.0F * speed * speed) * Mth.DEG_TO_RAD
              + Mth.sin(Mth.PI / 4.0F + stride * 2.0F) / 16.0F * Math.max(0.3F, move) * walk
              - Mth.sin(stride - Mth.PI / 4.0F) / 5.0F * speed * run
              + (-7.0F * Mth.DEG_TO_RAD) * rear;
        tail.yRot = tailRest.yRot() + Mth.sin(stride - Mth.PI / 3.0F) / 8.0F * move * walk;

        // ---- legs -----------------------------------------------------------------
        // Ported term-for-term from Fresh Animations rather than approximated with a
        // uniform phase offset, because the four legs are genuinely not symmetric:
        //   * fronts swing on +/-sin, hinds on +/-cos, giving the diagonal four-beat
        //   * fronts clamp their extra bend to (0, pi/6) and hinds to (-pi/9, 0) - knees
        //     fold backwards, hocks fold forwards. One shared clamp bends hinds the wrong way.
        //   * the fore/aft carry is large: +/-6.5 at a walk, +/-8*speed at a gallop. Getting
        //     this too small is what makes legs look like they are scrubbing rather than
        //     stepping, because rotation then dominates motion that should be translation.
        final float ls = stride;
        final float st = swimT;
        final float moveClamped = Math.max(0.3F, move);
        final float notRearing = 1.0F - rear;
        final float trotInner = 2.5F + 2.0F * trot;
        final float FIVE_DEG = 5.0F * Mth.DEG_TO_RAD;
        final float SIXTH_PI = Mth.PI / 6.0F;
        final float NINTH_PI = Mth.PI / 9.0F;

        for (int i = 0; i < legs.length; i++) {
            ModelPart leg = legs[i];
            Rest rest = legRest[i];

            float rot;
            float reach;
            float lift;

            switch (i) {
                case 0 -> {   // front left
                    float s = Mth.sin(ls - Mth.sin(ls) / trotInner);
                    rot = Mth.sin(breath) / 60.0F
                        + (((FIVE_DEG * trot + s) / 1.8F * moveClamped)
                           + Mth.clamp(-Mth.cos(ls) / 2.0F, 0.0F, SIXTH_PI)
                             * (1.0F + trot / 2.0F) / 2.0F * move) * notRearing * walk
                        + ((-Mth.cos(ls) / 1.6F + Mth.clamp(-Mth.sin(ls) / 4.0F, 0.0F, NINTH_PI))
                           * speed * 1.2F * notRearing) * run
                        + (-Mth.cos(st) / 2.0F
                           + Mth.clamp(-Mth.sin(st) / 3.0F, 0.0F, NINTH_PI)) * notRearing * swim
                        + (-0.4F - Mth.sin(rearT) / 2.5F) * rear;
                    reach = ((1.0F / 6.5F * trot + s) * 6.5F * moveClamped * notRearing) * walk
                          + (-Mth.cos(ls) * 8.0F * speed * notRearing) * run
                          + (-Mth.cos(st) * 6.0F * notRearing) * swim
                          // the paw: without this the leg only rotates, about a pivot near the
                          // hoof, which reads as swinging the wrong way
                          + (-1.0F - Mth.sin(rearT) * 2.0F) * rear;
                    lift = Mth.clamp((-1.7F * trot + Mth.cos(ls) * 2.0F * move * notRearing) * walk
                          + (-2.3F * speed * notRearing + Mth.sin(ls) * 2.0F * speed * notRearing)
                            * run * 1.2F
                          + (-2.0F + Mth.sin(st) * 1.5F * notRearing) * swim, -4.0F, 0.0F)
                          + (-13.0F + (-2.0F - Mth.cos(rearT) * 1.2F)) * rear;
                }
                case 1 -> {   // front right - mirrored inner sign, not just phase-shifted
                    float s = -Mth.sin(ls + Mth.sin(ls) / trotInner);
                    float rl = Mth.PI / 6.0F + ls;
                    rot = Mth.sin(breath) / 60.0F
                        + (((FIVE_DEG * trot + s) / 1.8F * moveClamped)
                           + Mth.clamp(Mth.cos(ls) / 2.0F, 0.0F, SIXTH_PI)
                             * (1.0F + trot / 2.0F) / 2.0F * move) * notRearing * walk
                        + ((-Mth.sin(rl) / 1.6F + Mth.clamp(Mth.cos(rl) / 4.0F, 0.0F, NINTH_PI))
                           * speed * 1.2F * notRearing) * run
                        + (Mth.cos(st) / 2.0F
                           + Mth.clamp(Mth.sin(st) / 3.0F, 0.0F, NINTH_PI)) * notRearing * swim
                        + (-0.4F + Mth.sin(rearT) / 2.5F) * rear;
                    reach = ((1.0F / 6.5F * trot + s) * 6.5F * moveClamped * notRearing) * walk
                          + (-Mth.sin(rl) * 8.0F * speed * notRearing) * run
                          + (Mth.cos(st) * 6.0F * notRearing) * swim
                          // mirrored against the left leg, so the pair paws alternately
                          + (-1.0F + Mth.sin(rearT) * 2.0F) * rear;
                    lift = Mth.clamp((-1.7F * trot - Mth.cos(ls) * 2.0F * move * notRearing) * walk
                          + (-2.3F * speed * notRearing - Mth.cos(rl) * 2.0F * speed * notRearing)
                            * run * 1.2F
                          + (-2.0F - Mth.sin(st) * 1.5F * notRearing) * swim, -4.0F, 0.0F)
                          + (-13.0F + (-2.0F + Mth.cos(rearT) * 1.2F)) * rear;
                }
                case 2 -> {   // back left - cos-based, and the clamp runs negative
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
                           + Mth.clamp(Mth.cos(st) / 3.0F, -NINTH_PI, 0.0F)) * swim;
                    reach = (((-Mth.sin(ls + Mth.sin(ls) / 4.5F) + 1.0F / 6.5F) * trot
                              + Mth.cos(ls - Mth.cos(ls) / 2.5F) * (1.0F - trot))
                             * 6.5F * moveClamped) * walk
                          + (Mth.sin(ls) * 8.0F * speed) * run
                          + (Mth.sin(st) * 6.0F) * swim
                          + 1.5F * rear;
                    lift = Mth.clamp((-1.7F * trot
                            + (-Mth.cos(ls) * trot - Mth.sin(ls) * (1.0F - trot))
                              * 2.0F * move * notRearing) * walk
                          + (-2.3F * speed + Mth.cos(ls) * 2.0F * speed) * run * 1.2F
                          + (-2.0F + Mth.cos(st) * 1.5F) * swim, -4.0F, 0.0F);
                }
                default -> {  // back right
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
                        - 0.4F * rear;
                    reach = (((Mth.sin(ls - Mth.sin(ls) / 4.5F) + 1.0F / 6.5F) * trot
                              - Mth.cos(ls + Mth.cos(ls) / 2.5F) * (1.0F - trot))
                             * 6.5F * moveClamped) * walk
                          + (-Mth.cos(rl) * 8.0F * speed) * run
                          + (-Mth.sin(st) * 6.0F) * swim
                          - 2.5F * rear;
                    lift = Mth.clamp((-1.7F * trot
                            + (Mth.cos(ls) * trot + Mth.sin(ls) * (1.0F - trot))
                              * 2.0F * move * notRearing) * walk
                          + (-2.3F * speed + Mth.sin(rl) * 2.0F * speed) * run * 1.2F
                          + (-2.0F - Mth.cos(st) * 1.5F) * swim, -4.0F, 0.0F);
                }
            }

            leg.xRot = rest.xRot() + rot;
            leg.z = rest.z() + reach;
            leg.y = rest.y() + lift;
            leg.zRot = rest.zRot();
        }
    }
}
