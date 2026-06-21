package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.IHorseData;
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

// Adds the stabilizer wing layer to every AbstractHorseRenderer and forwards entity state into IBhEquineStabilizerState for the submit phase. 1.21.5+ renderer is <T, S, M> with a (Context, M, M) ctor and extends AgeableMobRenderer; per-frame data must be captured in extractRenderState since the entity is gone by submit.
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

    // Capture the live horse's stabilizer flag/state onto the render state and keep the per-horse HorseStabilizerAnimatable ticking so its controller fires deploy/glide transitions on time.
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
