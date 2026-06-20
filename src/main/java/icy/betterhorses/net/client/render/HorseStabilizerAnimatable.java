package icy.betterhorses.net.client.render;

import icy.betterhorses.net.HorseStabilizerState;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.WeakHashMap;

// GeckoLib animation state holder for the stabilizer wings.
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
    private double tick;

    public static HorseStabilizerAnimatable get(AbstractHorse horse) {
        return INSTANCES.computeIfAbsent(horse, ignored -> new HorseStabilizerAnimatable());
    }

    public static @Nullable HorseStabilizerAnimatable getById(int entityId) {
        for (Map.Entry<AbstractHorse, HorseStabilizerAnimatable> entry : INSTANCES.entrySet()) {
            if (entry.getKey().getId() == entityId) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void syncFromHorse(AbstractHorse horse, HorseStabilizerState state, double tick) {
        this.horse = horse;
        this.tick = tick;

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
        return this.tick;
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
        state.setAnimation(GLIDE_LOOP);
        return PlayState.CONTINUE;
    }
}
