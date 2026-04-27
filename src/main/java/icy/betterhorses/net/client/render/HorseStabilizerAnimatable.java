package icy.betterhorses.net.client.render;

import icy.betterhorses.net.HorseStabilizerState;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.WeakHashMap;

public final class HorseStabilizerAnimatable implements GeoAnimatable {
    private static final RawAnimation DEPLOY_AND_GLIDE = RawAnimation.begin()
            .then("animation", Animation.LoopType.PLAY_ONCE)
            .thenLoop("wingflap");
    private static final RawAnimation GLIDE_LOOP = RawAnimation.begin().thenLoop("wingflap");
    private static final Map<AbstractHorse, HorseStabilizerAnimatable> INSTANCES = new WeakHashMap<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final AnimationController<HorseStabilizerAnimatable> controller =
            new AnimationController<>(this, "stabilizer", 0, this::animationPredicate);

    private @Nullable AbstractHorse horse;
    private HorseStabilizerState state = HorseStabilizerState.CLOSED;
    private boolean active;
    private boolean deploySequenceRequested;

    public static HorseStabilizerAnimatable get(AbstractHorse horse) {
        return INSTANCES.computeIfAbsent(horse, ignored -> new HorseStabilizerAnimatable());
    }

    public void syncFromHorse(AbstractHorse horse, HorseStabilizerState state) {
        this.horse = horse;

        boolean nextActive = state != HorseStabilizerState.CLOSED;
        if (nextActive && !this.active) {
            this.deploySequenceRequested = true;
            this.controller.forceAnimationReset();
        } else if (!nextActive && this.active) {
            this.deploySequenceRequested = false;
            this.controller.stop();
        }

        this.active = nextActive;
        this.state = state;
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
        return this.horse == null ? 0 : this.horse.tickCount;
    }

    private PlayState animationPredicate(AnimationState<HorseStabilizerAnimatable> state) {
        if (!this.active) {
            return PlayState.STOP;
        }

        if (this.deploySequenceRequested) {
            this.deploySequenceRequested = false;
            state.setAnimation(DEPLOY_AND_GLIDE);

            return PlayState.CONTINUE;
        }

        if (!state.isCurrentAnimation(DEPLOY_AND_GLIDE) && !state.isCurrentAnimation(GLIDE_LOOP)) {
            state.setAnimation(GLIDE_LOOP);
        }

        return PlayState.CONTINUE;
    }
}
