package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.HorseStabilizerLayer;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the stabilizer wing layer to every {@link AbstractHorseRenderer} in 1.21.1.
 * 1.21.1 AbstractHorseRenderer ctor signature: (Context, HorseModel, float).
 */
@Mixin(AbstractHorseRenderer.class)
public abstract class AbstractHorseRendererMixin<T extends AbstractHorse, M extends HorseModel<T>>
        extends MobRenderer<T, M> {

    protected AbstractHorseRendererMixin(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bh_addStabilizerLayer(EntityRendererProvider.Context context, HorseModel<T> model, float shadowRadius, CallbackInfo ci) {
        this.addLayer(new HorseStabilizerLayer<>(this));
    }
}
