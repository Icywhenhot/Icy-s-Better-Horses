package icy.betterhorses.net.client.render;

import icy.betterhorses.net.HorseStabilizerState;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;

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
        // GeckoLib 5: forceAnimationReset() and stop() were unified into reset().
        if (nextActive && !this.active) {
            this.deploySequenceRequested = true;
            this.controller.reset();
        } else if (!nextActive && this.active) {
            this.deploySequenceRequested = false;
            this.controller.reset();
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

    private PlayState animationPredicate(AnimationTest<HorseStabilizerAnimatable> test) {
        if (!this.active) {
            return PlayState.STOP;
        }

        if (this.deploySequenceRequested) {
            this.deploySequenceRequested = false;
            return test.setAndContinue(DEPLOY_AND_GLIDE);
        }

        if (!test.isCurrentAnimation(DEPLOY_AND_GLIDE) && !test.isCurrentAnimation(GLIDE_LOOP)) {
            return test.setAndContinue(GLIDE_LOOP);
        }

        return PlayState.CONTINUE;
    }
}
