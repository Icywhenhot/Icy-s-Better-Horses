package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.client.render.BhMountedHorseVisibility;
import icy.betterhorses.net.client.render.HorseStabilizerAnimatable;
import icy.betterhorses.net.client.render.HorseStabilizerLayer;
import icy.betterhorses.net.client.render.IBhEquineStabilizerState;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the stabilizer wing layer to every {@link AbstractHorseRenderer}, and forwards the
 * relevant entity state into {@link IBhEquineStabilizerState} so the layer can access it
 * during the new state-based submit phase.
 *
 * 1.21.5+ rebuild:
 *  - {@link AbstractHorseRenderer} now has three type parameters {@code <T, S, M>} where {@code S}
 *    is an {@link EquineRenderState} and {@code M} an {@link EntityModel} parameterised by it.
 *  - The constructor signature is {@code (Context, M, M)} (two models — adult/baby), no longer
 *    {@code (Context, M, float)}, so the {@code @Inject} parameter list must match.
 *  - It extends {@link AgeableMobRenderer} (which itself extends {@code MobRenderer}); we extend
 *    that here so the mixin can call inherited methods like {@code addLayer}.
 *  - Per-frame entity data has to be captured in {@code extractRenderState}: by the time
 *    {@code submit} runs the entity is no longer reachable, only the render state is.
 */
@Mixin(AbstractHorseRenderer.class)
public abstract class AbstractHorseRendererMixin<
        T extends AbstractHorse,
        S extends EquineRenderState,
        M extends EntityModel<? super S>>
        extends AgeableMobRenderer<T, S, M> {

    protected AbstractHorseRendererMixin(EntityRendererProvider.Context context, M adultModel, M babyModel, float shadowRadius) {
        super(context, adultModel, babyModel, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bh_addStabilizerLayer(EntityRendererProvider.Context context, M adultModel, M babyModel, CallbackInfo ci) {
        this.addLayer(new HorseStabilizerLayer<>(this));
    }

    /**
     * Capture the current stabilizer flag/state from the live horse onto the render state, and
     * keep the per-horse {@link HorseStabilizerAnimatable} ticking in sync (so its
     * {@code AnimationController} fires deploy / glide transitions at the right moments).
     */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void bh_captureStabilizerState(T entity, S state, float partialTick, CallbackInfo ci) {
        IBhEquineStabilizerState extState = (IBhEquineStabilizerState) state;
        Minecraft minecraft = Minecraft.getInstance();
        boolean riddenByPlayerInFirstPerson = minecraft.options.getCameraType() == CameraType.FIRST_PERSON
                && minecraft.player != null
                && entity.hasPassenger(minecraft.player);
        extState.bh_setMountedViewData(riddenByPlayerInFirstPerson, BhMountedHorseVisibility.getOpacity(entity));

        if (!(entity instanceof IHorseData data)) {
            extState.bh_setStabilizerData(false, HorseStabilizerState.CLOSED, entity.getId(), partialTick);
            return;
        }

        boolean hasStabilizer = data.bh_hasGear(GearSlot.STABILIZER);
        extState.bh_setStabilizerData(
                hasStabilizer, data.bh_getStabilizerState(), entity.getId(), partialTick);

        if (hasStabilizer) {
            HorseStabilizerAnimatable animatable = HorseStabilizerAnimatable.get(entity);
            animatable.syncFromHorse(entity, data.bh_getStabilizerState());
        }
    }
}
