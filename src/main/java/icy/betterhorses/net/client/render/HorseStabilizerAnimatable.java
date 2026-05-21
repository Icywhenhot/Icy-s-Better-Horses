package icy.betterhorses.net.client.render;

import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.IcysBetterHorsesClient;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.LoopType;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.animation.state.AnimationTest;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * GeckoLib-driven animation state holder for the stabilizer wings.
 *
 * Updated for GeckoLib 5: {@code Animation.LoopType} → {@link LoopType}, {@code AnimationState}
 * → {@link AnimationTest}, and {@code AnimatableManager} now lives under {@code animatable.manager}.
 * The actual rendering side (stabilizer model on the horse) is currently stubbed pending the
 * GeckoLib 5 GeoRenderState pipeline port — see {@link HorseStabilizerLayer}.
 */
public final class HorseStabilizerAnimatable implements GeoAnimatable {
    private static final RawAnimation DEPLOY_AND_GLIDE = RawAnimation.begin()
            .then("animation", LoopType.PLAY_ONCE)
            .thenLoop("wingflap");
    private static final RawAnimation GLIDE_LOOP = RawAnimation.begin().thenLoop("wingflap");
    private static final Map<AbstractHorse, HorseStabilizerAnimatable> INSTANCES = new WeakHashMap<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /**
     * GeckoLib 5 changed the {@link AnimationController} ctor: there's no leading {@code this}
     * animatable parameter, and the animatable is implicitly tied through registration. Signature
     * is now {@code (String name, int transitionTicks, AnimationStateHandler<T>)}.
     */
    private final AnimationController<HorseStabilizerAnimatable> controller =
            new AnimationController<>("stabilizer", 0, this::animationPredicate);

    private @Nullable AbstractHorse horse;
    private HorseStabilizerState state = HorseStabilizerState.CLOSED;
    private boolean active;
    private boolean deploySequenceRequested;
    private int predicateCallCount;
    private long lastPredicateLogTick = -1L;

    public static HorseStabilizerAnimatable get(AbstractHorse horse) {
        return INSTANCES.computeIfAbsent(horse, ignored -> new HorseStabilizerAnimatable());
    }

    /**
     * Look up the animatable for a horse by its entity id. Used by the render layer in 1.21.11,
     * which only has access to the {@code RenderState} (entity id captured at extract time)
     * during {@code submit}.
     */
    public static @Nullable HorseStabilizerAnimatable getById(int entityId) {
        for (Map.Entry<AbstractHorse, HorseStabilizerAnimatable> entry : INSTANCES.entrySet()) {
            if (entry.getKey().getId() == entityId) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void syncFromHorse(AbstractHorse horse, HorseStabilizerState state) {
        this.horse = horse;

        boolean nextActive = state != HorseStabilizerState.CLOSED;
        HorseStabilizerState prevState = this.state;
        boolean prevActive = this.active;

        // GeckoLib 5: forceAnimationReset() and stop() were unified into reset().
        if (nextActive && !this.active) {
            this.deploySequenceRequested = true;
            this.controller.reset();
            this.predicateCallCount = 0;
        } else if (!nextActive && this.active) {
            this.deploySequenceRequested = false;
            this.controller.reset();
        }

        this.active = nextActive;
        this.state = state;

        if (prevState != state || prevActive != nextActive) {
            IcysBetterHorsesClient.LOGGER.info(
                    "[STAB-DEBUG][ANIMATABLE] horse={} syncFromHorse state {}->{} active {}->{} deployRequested={}",
                    horse.getId(), prevState, state, prevActive, nextActive, this.deploySequenceRequested);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public HorseStabilizerState getState() {
        return this.state;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(this.controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private PlayState animationPredicate(AnimationTest<HorseStabilizerAnimatable> test) {
        this.predicateCallCount++;
        int horseId = this.horse == null ? -1 : this.horse.getId();
        long nowTick = (long) test.renderState().getAnimatableAge();

        if (!this.active) {
            if (nowTick != this.lastPredicateLogTick && this.predicateCallCount <= 3) {
                this.lastPredicateLogTick = nowTick;
                IcysBetterHorsesClient.LOGGER.info(
                        "[STAB-DEBUG][PREDICATE] horse={} predicate#{} active=false -> STOP (state={})",
                        horseId, this.predicateCallCount, this.state);
            }
            return PlayState.STOP;
        }

        if (this.deploySequenceRequested) {
            this.deploySequenceRequested = false;
            IcysBetterHorsesClient.LOGGER.info(
                    "[STAB-DEBUG][PREDICATE] horse={} predicate#{} -> setAndContinue(DEPLOY_AND_GLIDE)",
                    horseId, this.predicateCallCount);
            return test.setAndContinue(DEPLOY_AND_GLIDE);
        }

        if (!test.isCurrentAnimation(DEPLOY_AND_GLIDE) && !test.isCurrentAnimation(GLIDE_LOOP)) {
            IcysBetterHorsesClient.LOGGER.info(
                    "[STAB-DEBUG][PREDICATE] horse={} predicate#{} -> setAndContinue(GLIDE_LOOP) (no current animation matched)",
                    horseId, this.predicateCallCount);
            return test.setAndContinue(GLIDE_LOOP);
        }

        if (nowTick != this.lastPredicateLogTick && this.predicateCallCount % 20 == 0) {
            this.lastPredicateLogTick = nowTick;
            IcysBetterHorsesClient.LOGGER.info(
                    "[STAB-DEBUG][PREDICATE] horse={} predicate#{} CONTINUE (deployCurrent={} glideCurrent={})",
                    horseId, this.predicateCallCount,
                    test.isCurrentAnimation(DEPLOY_AND_GLIDE),
                    test.isCurrentAnimation(GLIDE_LOOP));
        }

        return PlayState.CONTINUE;
    }
}
