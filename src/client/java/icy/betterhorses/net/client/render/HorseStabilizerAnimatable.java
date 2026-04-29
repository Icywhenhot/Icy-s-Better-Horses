package icy.betterhorses.net.client.render;

import icy.betterhorses.net.HorseStabilizerState;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HorseStabilizerAnimatable implements GeoAnimatable {
    private static final RawAnimation DEPLOY_AND_GLIDE = RawAnimation.begin()
            .then("animation", Animation.LoopType.PLAY_ONCE)
            .thenLoop("wingflap");
    private static final RawAnimation GLIDE_LOOP = RawAnimation.begin().thenLoop("wingflap");
    private static final Map<Integer, HorseStabilizerAnimatable> INSTANCES = new ConcurrentHashMap<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final AnimationController<HorseStabilizerAnimatable> controller =
            new AnimationController<HorseStabilizerAnimatable>("stabilizer", 0, this::animationPredicate);

    private double tick;
    private HorseStabilizerState state = HorseStabilizerState.CLOSED;
    private boolean active;
    private boolean deploySequenceRequested;

    public static HorseStabilizerAnimatable get(int horseId) {
        return INSTANCES.computeIfAbsent(horseId, ignored -> new HorseStabilizerAnimatable());
    }

    public void syncFromState(HorseStabilizerState newState, double tick) {
        this.tick = tick;
        boolean nextActive = newState != HorseStabilizerState.CLOSED;
        if (nextActive && !this.active) {
            this.deploySequenceRequested = true;
            this.controller.forceAnimationReset();
        } else if (!nextActive && this.active) {
            this.deploySequenceRequested = false;
            this.controller.stop();
        }
        this.active = nextActive;
        this.state = newState;
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

    @Override
    public double getTick(Object relatedObject) {
        return this.tick;
    }

    private PlayState animationPredicate(AnimationTest<HorseStabilizerAnimatable> test) {
        if (!this.active) {
            return PlayState.STOP;
        }

        if (this.deploySequenceRequested) {
            this.deploySequenceRequested = false;
            test.setAnimation(DEPLOY_AND_GLIDE);
            return PlayState.CONTINUE;
        }

        if (!test.isCurrentAnimation(DEPLOY_AND_GLIDE) && !test.isCurrentAnimation(GLIDE_LOOP)) {
            test.setAnimation(GLIDE_LOOP);
        }
        return PlayState.CONTINUE;
    }
}
