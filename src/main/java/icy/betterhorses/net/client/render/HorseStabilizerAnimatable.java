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

public final class HorseStabilizerAnimatable implements GeoAnimatable {
    private static final RawAnimation DEPLOY_AND_GLIDE = RawAnimation.begin()
            .then("animation", LoopType.PLAY_ONCE)
            .thenLoop("wingflap");
    private static final RawAnimation GLIDE_LOOP = RawAnimation.begin().thenLoop("wingflap");
    private static final Map<AbstractHorse, HorseStabilizerAnimatable> INSTANCES = new WeakHashMap<>();

    /**
     * Render-time lookup, keyed by entity id because a render state carries the id and not the
     * entity.
     *
     * <p>Ids are recycled: tear a level down and rebuild it - which Flashback does on every
     * backward scrub - and a fresh horse can be handed the id of a dead one. Scanning
     * {@link #INSTANCES} for a matching id, as this used to, could then match a stale entry that
     * had not been collected yet and animate the new horse with a dead one's wing state. This map
     * is restamped from the live entity in {@link #syncFromHorse}, which the renderer calls for
     * that horse in the same frame as the lookup, so the newest binding always wins.
     */
    private static final Map<Integer, HorseStabilizerAnimatable> BY_ID = new java.util.HashMap<>();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final AnimationController<HorseStabilizerAnimatable> controller =
            new AnimationController<>("stabilizer", 0, this::animationPredicate);

    private @Nullable AbstractHorse horse;
    private HorseStabilizerState state = HorseStabilizerState.CLOSED;
    private boolean active;
    private boolean deploySequenceRequested;

    public static HorseStabilizerAnimatable get(AbstractHorse horse) {
        return INSTANCES.computeIfAbsent(horse, ignored -> new HorseStabilizerAnimatable());
    }

    public static @Nullable HorseStabilizerAnimatable getById(int entityId) {
        return BY_ID.get(entityId);
    }

    /** Drops every cached animatable. Called on disconnect so ids never carry across worlds. */
    public static void reset() {
        INSTANCES.clear();
        BY_ID.clear();
    }

    public void syncFromHorse(AbstractHorse horse, HorseStabilizerState state) {
        this.horse = horse;
        BY_ID.put(horse.getId(), this);

        boolean nextActive = state != HorseStabilizerState.CLOSED;
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
