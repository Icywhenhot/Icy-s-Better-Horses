package icy.betterhorses.net.client.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.client.render.BhStabilizerStateAccess;
import icy.betterhorses.net.client.render.HorseStabilizerLayer;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorseRenderer.class)
public abstract class AbstractHorseRendererMixin<
        T extends AbstractHorse,
        S extends EquineRenderState,
        M extends EntityModel<? super S>>
        extends AgeableMobRenderer<T, S, M> {

    protected AbstractHorseRendererMixin(EntityRendererProvider.Context context, M adultModel, M babyModel, float shadow) {
        super(context, adultModel, babyModel, shadow);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bh_addStabilizerLayer(EntityRendererProvider.Context context, M adultModel, M babyModel, CallbackInfo ci) {
        this.addLayer(new HorseStabilizerLayer<>(this));
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/horse/AbstractHorse;Lnet/minecraft/client/renderer/entity/state/EquineRenderState;F)V",
            at = @At("TAIL"))
    private void bh_captureStabilizerState(T horse, S state, float partialTick, CallbackInfo ci) {
        BhStabilizerStateAccess access = (BhStabilizerStateAccess) state;
        access.bh_setHorseId(horse.getId());
        if (horse instanceof IHorseData data) {
            access.bh_setHasStabilizer(data.bh_hasGear(GearSlot.STABILIZER));
            access.bh_setStabilizerState(data.bh_getStabilizerState());
        } else {
            access.bh_setHasStabilizer(false);
        }
    }
}
